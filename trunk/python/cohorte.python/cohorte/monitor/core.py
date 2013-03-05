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
import cohorte.java.repository as repository

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Requires

# Standard library
import logging
import threading
import uuid

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-monitor-core-factory")
@Provides(cohorte.monitor.SERVICE_MONITOR)
@Provides(cohorte.forker.SERVICE_FORKER_LISTENER)
@Requires('_config', cohorte.SERVICE_CONFIGURATION_READER)
@Requires('_finder', cohorte.SERVICE_FILE_FINDER)
@Requires('_forkers', cohorte.forker.SERVICE_AGGREGATOR)
@Requires('_receiver', cohorte.SERVICE_SIGNALS_RECEIVER)
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
        self._sender = None
        self._status = None

        # Bundle context
        self._context = None

        # Java repository
        self._repository = None

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
        if name == cohorte.monitor.SIGNAL_STOP_PLATFORM:
            # Platform must stop
            self._stop_platform()

        elif name == cohorte.monitor.SIGNAL_ISOLATE_READY:
            # Isolate ready
            self._status.isolate_ready(data['signalSender'])

        elif name == cohorte.monitor.SIGNAL_ISOLATE_STOPPING:
            # Isolate ready
            self._status.isolate_stopping(data['signalSender'])

        elif name == cohorte.monitor.SIGNAL_ISOLATE_LOST:
            # Isolate signaled as lost
            self._handle_lost(data['signalContent'])


    def ping(self, uid):
        """
        Tells a forker to ping an isolate
        
        :param uid: UID of the isolate to ping
        :return: True if the forker knows and pings the isolate
        """
        return self._forkers.ping(uid)


    def start_isolate(self, name, node, kind, level, sublevel,
                      bundles=None, factories=None, composition=None):
        """
        Starts an isolate according to the given elements, or stacks the order
        until a forker appears on the given node
        
        :param name: Isolate name
        :param node: Node hosting the isolate
        :param kind: The kind of isolate to boot (pelix, osgi, ...)
        :param level: The level of configuration (boot, java, python, ...)
        :param sublevel: Category of configuration (monitor, isolate, ...)
        :param bundles: Extra bundles to install (Bundle beans)
        :param factories: Factories needed to start the isolate (extend bundles)
        :param composition: Extra components to instantiate (dictionary)
        :raise IOError: Unknown/unaccessible kind of isolate
        :raise KeyError: A parameter is missing in the configuration files
        :raise ValueError: Error reading the configuration
        """
        # Compute bundles according to the factories
        if bundles is None:
            bundles = []

        # Use the repository class
        if kind == 'osgi':
            # OSGi/iPOJO factories
            resolved, missing = self._repository.find_ipojo_factories(factories)

        elif kind == 'pelix':
            # TODO: Find iPOPO factories
            resolved = {}
            missing = []

        else:
            raise ValueError("Unknown kind: {0}".format(kind))

        if missing:
            # Some factories can't be found -> error
            raise ValueError("Some factories are missing: {0}".format(missing))

        # Compute the bundles
        for providers in resolved.values():
            for provider in providers:
                if provider in bundles:
                    # Already known provider
                    break

            else:
                # No known provider: use the first one found
                bundles.append(providers[0])

        # TODO: Resolve the dependencies
        # self._repository.resolve_installation(bundles)

        # Generate a UID
        uid = str(uuid.uuid4())

        # Run on local node if none is given
        if not node:
            node = self._context.get_property(cohorte.PROP_NODE)

        # Prepare a configuration
        config = self._config.prepare_isolate(uid, name, node, kind, level,
                                              sublevel, bundles, composition)

        # Talk to the forker aggregator
        result = self._forkers.start_isolate(uid, node, kind, config)
        if result == cohorte.forker.REQUEST_NO_MATCHING_FORKER:
            # Stack the request
            self._waiting.setdefault(node, {})[uid] = (kind, config)
            _logger.warning("No forker for node %s yet - %s waiting.",
                            node, name)
            return False

        return result in cohorte.forker.REQUEST_SUCCESSES


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
        isolates = self._waiting[node]

        # Start all isolates waiting on that node
        for iso_uid, (kind, config) in isolates.items():
            # Call the forker
            result = self._forkers.start_isolate(iso_uid, node, kind, config)
            if result in cohorte.forker.REQUEST_SUCCESSES:
                # Isolate started
                del isolates[iso_uid]


    def _load_repository(self):
        """
        Loads all the JARs in the bundle repositories
        """
        self._repository = repository.Repository()

        # Load all JAR files in the "repo" directory
        for jar_file in self._finder.find_gen("*.jar", "repo", True):
            self._repository.add_file(jar_file)


    def _handle_lost(self, uid):
        """
        Handles a lost isolate
        
        :param uid: UID of the lost isolate
        """
        # Get previous state
        state = self._status.get_state(uid)

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

        # Set the monitor in stopping state
        self._platform_stopping.set()

        # Set the forkers in stopping state
        self._forkers.set_platform_stopping()

        # Tell the forkers to stop the the running isolates
        for uid in self._status.get_running():
            self._forkers.stop_isolate(uid)

        # Stop the forkers
        self._forkers.stop_forkers()

        # Stop this isolate
        self._sender.fire(cohorte.monitor.SIGNAL_STOP_ISOLATE, None,
                          dir_group="CURRENT")


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context

        # Load the Java repository
        self._load_repository()

        # Register to signals
        self._receiver.register_listener(cohorte.monitor.SIGNAL_STOP_PLATFORM,
                                         self)
        self._receiver.register_listener(\
                                 cohorte.monitor.SIGNALS_ISOLATE_PATTERN, self)

        _logger.info("Monitor core validated")


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister to signals
        self._receiver.unregister_listener(cohorte.monitor.SIGNAL_STOP_PLATFORM,
                                         self)
        self._receiver.unregister_listener(\
                                 cohorte.monitor.SIGNALS_ISOLATE_PATTERN, self)

        # Clear the repository
        self._repository.clear()
        self._repository = None

        # Clear the waiting list
        self._waiting.clear()

        self._context = None
        _logger.info("Monitor core invalidated")
