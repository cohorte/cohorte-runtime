#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Monitor: Monitor core

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# Cohorte modules
import cohorte.forker
import cohorte.monitor
import cohorte.monitor.fsm as fsm
import cohorte.repositories
import cohorte.repositories.beans as beans
import cohorte.signals

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Requires

# Standard library
import logging
import sys
import threading
import uuid

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

HANDLED_SIGNALS = (cohorte.monitor.SIGNALS_ISOLATE_PATTERN,
                   cohorte.monitor.SIGNALS_PLATFORM_PATTERN,
                   cohorte.forker.SIGNAL_FORKER_LOST)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-monitor-core-factory")
@Provides(cohorte.monitor.SERVICE_MONITOR)
@Provides(cohorte.forker.SERVICE_FORKER_LISTENER)
@Requires('_config', cohorte.SERVICE_CONFIGURATION_READER)
@Requires('_finder', cohorte.SERVICE_FILE_FINDER)
@Requires('_forkers', cohorte.forker.SERVICE_AGGREGATOR)
@Requires('_receiver', cohorte.SERVICE_SIGNALS_RECEIVER)
@Requires('_repositories', cohorte.repositories.SERVICE_REPOSITORY_ARTIFACTS,
          aggregate=True)
@Requires('_sender', cohorte.SERVICE_SIGNALS_SENDER)
@Requires('_status', cohorte.monitor.SERVICE_STATUS)
class MonitorCore(object):
    """
    Monitor core component: interface to the forker aggregator
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Injected services
        self._config = None
        self._finder = None
        self._forkers = None
        self._receiver = None
        self._repositories = []
        self._sender = None
        self._status = None

        # Bundle context
        self._context = None

        # Platform stopping event
        self._platform_stopping = threading.Event()

        # Isolates waiting on nodes (Node -> [uid -> (kind, config)])
        self._waiting = {}


    def handle_received_signal(self, name, data):
        """
        Handles a signal
        
        :param name: Signal name
        :param data: Signal data dictionary
        """
        if name == cohorte.monitor.SIGNAL_ISOLATE_READY:
            # Isolate ready
            self._status.isolate_ready(data['senderUID'])

        elif name == cohorte.monitor.SIGNAL_ISOLATE_STOPPING:
            # Isolate stopping
            self._status.isolate_stopping(data['senderUID'])

        elif name == cohorte.monitor.SIGNAL_ISOLATE_LOST:
            # Isolate signaled as lost
            self._handle_lost(data['signalContent'])

        elif name == cohorte.forker.SIGNAL_FORKER_LOST:
            # Forker lost: tell of its isolates to stop
            self._handle_forker_lost(data['signalContent']['uid'],
                                     data['signalContent']['isolates'])


        elif name == cohorte.monitor.SIGNAL_STOP_PLATFORM:
            # Platform must stop (new thread)
            threading.Thread(name="platform-stop",
                             target=self._stop_platform).start()

        elif name == cohorte.monitor.SIGNAL_PLATFORM_STOPPING:
            # Platform goes into stopping mode (let the sender do the job)
            self._platform_stopping.set()


    def ping(self, uid):
        """
        Tells a forker to ping an isolate
        
        :param uid: UID of the isolate to ping
        :return: True if the forker knows and pings the isolate
        """
        return self._forkers.ping(uid)


    def _get_isolate_artifacts(self, kind, level, sublevel):
        """
        Retrieves the list of bundles configured for this isolate
        """
        # Load the isolate model file
        boot_dict = self._config.load_conf_raw(level, sublevel)

        # Parse it
        configuration = self._config.load_boot_dict(boot_dict)

        # Convert configuration bundles to repository artifacts
        return [beans.Artifact(level, bundle.name, bundle.version,
                               bundle.filename)
                for bundle in configuration.bundles]


    def start_isolate(self, name, node, kind, level, sublevel, bundles=None,
                      uid=None):
        """
        Starts an isolate according to the given elements, or stacks the order
        until a forker appears on the given node
        
        :param name: Isolate name
        :param node: Node hosting the isolate
        :param kind: The kind of isolate to boot (pelix, osgi, ...)
        :param level: The level of configuration (boot, java, python, ...)
        :param sublevel: Category of configuration (monitor, isolate, ...)
        :param bundles: Extra bundles to install (Bundle beans)
        :param uid: A user-defined isolate UID
        :raise IOError: Unknown/unaccessible kind of isolate
        :raise KeyError: A parameter is missing in the configuration files
        :raise ValueError: Error reading the configuration
        """
        if self._platform_stopping.is_set():
            # Avoid to start new isolates while we are stopping
            return False

        # Compute bundles according to the factories
        if bundles is None:
            bundles = []

        # Find the repository associated to the language
        for repository in self._repositories:
            if repository.get_language() == level:
                break

        else:
            _logger.error("No repository found for language: %s", level)
            return False

        # Get the configured isolate artifacts
        isolate_artifacts = self._get_isolate_artifacts(kind, level, sublevel)

        # Resolve the custom bundles installation
        resolution = repository.resolve_installation(bundles, isolate_artifacts)
        if resolution[2]:
            # Some artifacts are missing
            _logger.error("Missing artifacts: %s", resolution[2])
            return False

        elif resolution[3]:
            # Some extra dependencies are missing
            _logger.warning("Missing extra elements: %s", resolution[3])

        # Clean up resolution
        custom_artifacts = [artifact for artifact in resolution[0]
                            if artifact not in isolate_artifacts]

        # Generate a UID, if necessary
        if not uid:
            uid = uuid.uuid4()

        # Convert the UID into a string
        uid = str(uid)

        # Run on local node if none is given
        default_node = self._context.get_property(cohorte.PROP_NODE)

        # Prepare a configuration
        config = self._config.prepare_isolate(uid, name, node, kind, level,
                                              sublevel, custom_artifacts,
                                              default_node=default_node)

        # Update the target node
        node = config['node']

        # FIXME: make that prettier
        # Compute the looper, if needed
        if sys.platform == "darwin" and kind in ("java", "osgi"):
            config['looper'] = 'cocoa'

        # Store the isolate in the status
        self._status.add_isolate(uid)

        # Talk to the forker aggregator
        self._status.isolate_requested(uid)
        result = self._forkers.start_isolate(uid, node, kind, config)
        if result == cohorte.forker.REQUEST_NO_MATCHING_FORKER:
            # Return in waiting mode
            self._status.isolate_gone(uid)

            # Stack the request
            self._waiting.setdefault(node, {})[uid] = (kind, config)
            _logger.warning("No forker for node %s yet - %s waiting.",
                            node, name)
            return False

        elif result in cohorte.forker.REQUEST_SUCCESSES:
            self._status.isolate_starting(uid)
            return True

        # Do not remove the failed isolate from the status:
        # an "isolate lost" signal will be received soon

        return False


    def stop_isolate(self, uid):
        """
        Stops the isolate with the given UID
        
        :param uid: UID of an isolate
        :return: True if the forker associated to the isolate has been reached
        """
        return self._forkers.stop_isolate(uid)


    def forker_ready(self, uid, node):
        """
        A forker has been detected
        
        :param uid: Forker uid
        :param node: Forker node
        """
        if node not in self._waiting:
            # Nothing to do
            return

        # Get the isolates waiting for that forker
        original = self._waiting[node]
        waiting = original.copy()

        # Start all isolates waiting on that node
        for iso_uid, (kind, config) in waiting.items():
            # Call the forker
            self._status.isolate_requested(iso_uid)
            result = self._forkers.start_isolate(iso_uid, node, kind, config)
            if result in cohorte.forker.REQUEST_SUCCESSES:
                # Isolate started
                self._status.isolate_starting(iso_uid)
                del original[iso_uid]


    def _handle_forker_lost(self, uid, isolates):
        """
        Handles a lost forker
        """
        if isolates:
            for isolate in isolates:
                self._sender.fire(cohorte.monitor.SIGNAL_STOP_ISOLATE, None,
                                  isolate=isolate)


    def _handle_lost(self, uid):
        """
        Handles a lost isolate
        
        :param uid: UID of the lost isolate
        """
        try:
            # Get previous state
            state = self._status.get_state(uid)

        except KeyError:
            # Unknown / Already lost isolate -> do nothing
            _logger.info("Unknown isolate %s lost (or already handled)", uid)
            return

        # Change state
        self._status.isolate_gone(uid)

        if state == fsm.ISOLATE_STATE_STOPPING:
            # TODO: The isolate was stopping, so it's not an error
            _logger.info("Isolate %s stopped nicely.", uid)

        else:
            # TODO: Isolate lost
            _logger.error("Isolate %s lost", uid)


    def _stop_platform(self):
        """
        Stops the whole platform
        """
        if self._platform_stopping.is_set():
            # Already stopping
            return

        _logger.critical(">>> PLATFORM STOPPING <<<")

        # Set this monitor in stopping state
        self._platform_stopping.set()

        # Send the platform stopping signal to other monitors
        self._sender.send(cohorte.monitor.SIGNAL_PLATFORM_STOPPING, None,
                          dir_group=cohorte.signals.GROUP_MONITORS,
                          excluded=self._context.get_property(cohorte.PROP_UID))

        # Set the forkers in stopping state
        self._forkers.set_platform_stopping()

        # Tell the forkers to stop the running isolates
        for uid in self._status.get_running():
            self._forkers.stop_isolate(uid)

        # Stop the forkers
        self._forkers.stop_forkers()

        # Stop this isolate
        self._context.get_bundle(0).stop()


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context

        # Register to signals
        for signal in HANDLED_SIGNALS:
            self._receiver.register_listener(signal, self)

        _logger.info("Monitor core validated")


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister to signals
        for signal in HANDLED_SIGNALS:
            self._receiver.unregister_listener(signal, self)

        # Clear the waiting list
        self._waiting.clear()

        self._context = None
        _logger.info("Monitor core invalidated")
