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

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Requires

# Standard library
import logging
import threading

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-monitor-core-factory")
@Provides(cohorte.monitor.SERVICE_MONITOR)
@Requires('_config', cohorte.SERVICE_CONFIGURATION_READER)
@Requires('_forkers', cohorte.forker.SERVICE_AGGREGATOR)
@Requires('_receiver', cohorte.SERVICE_SIGNALS_RECEIVER)
@Requires('_sender', cohorte.SERVICE_SIGNALS_SENDER)
@Requires('_status', cohorte.monitor.SERVICE_STATUS)
class MonitorCore(object):
    """
    Monitor core component
    
    TODO:
    * start isolate
    * start isolate on node apparition
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Injected services
        self._config = None
        self._forkers = None
        self._receiver = None
        self._sender = None
        self._status = None

        # Platform stopping event
        self._platform_stopping = threading.Event()

        # Isolates waiting on nodes (Node -> [isolates configs])
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


    def start_isolate(self):
        """
        TODO:
        """
        pass


    def stop_isolate(self, uid):
        """
        Stops the isolate with the given UID
        
        :param uid: UID of an isolate
        :return: True if the forker associated to the isolate has been reached
        """
        return self._forkers.stop_isolate(uid)


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

        _logger.info("Monitor core invalidated")
