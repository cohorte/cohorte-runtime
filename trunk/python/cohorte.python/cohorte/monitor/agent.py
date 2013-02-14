#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Monitor: Isolate Agent

To instantiate in every Python isolate

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# Cohorte modules
import cohorte.monitor

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-isolate-agent-factory")
@Requires('_receiver', cohorte.SERVICE_SIGNALS_RECEIVER)
@Requires('_sender', cohorte.SERVICE_SIGNALS_SENDER)
class MonitorCore(object):
    """
    Isolate agent component
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Injected services
        self._receiver = None
        self._sender = None

        # Bundle context
        self._context = None


    def handle_received_signal(self, name, data):
        """
        Handles a signal
        
        :param name: Signal name
        :param data: Signal data dictionary
        """
        if name == cohorte.monitor.SIGNAL_STOP_ISOLATE:
            # Isolate must stop
            self.stop()


    def stop(self):
        """
        Stops the whole isolate
        """
        self._context.get_bundle(0).stop()


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # Register to signals
        self._receiver.register_listener(cohorte.monitor.SIGNAL_STOP_ISOLATE,
                                         self)

        # FIXME: the directory might not be filled up at this time
        # Send the "ready" signal
        self._sender.fire(cohorte.monitor.SIGNAL_ISOLATE_READY, None,
                          dir_group="MONITORS")

        _logger.info("Isolate agent validated")


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister to signals
        self._receiver.unregister_listener(cohorte.monitor.SIGNAL_STOP_ISOLATE,
                                           self)

        # Send the stopping signal
        self._sender.fire(cohorte.monitor.SIGNAL_ISOLATE_STOPPING, None,
                          dir_group="MONITORS")

        _logger.info("Isolate agent invalidated")
