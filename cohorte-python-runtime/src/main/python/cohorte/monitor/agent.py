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

# Cohorte & Herald
import cohorte.monitor
import herald.exceptions
import herald.beans as beans

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Provides, Property

# Standard library
import logging
import threading

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory("cohorte-isolate-agent-factory")
@Requires('_sender', herald.SERVICE_HERALD)
@Provides((herald.SERVICE_LISTENER, herald.SERVICE_DIRECTORY_GROUP_LISTENER))
@Property('_filters', herald.PROP_FILTERS, cohorte.monitor.SIGNAL_STOP_ISOLATE)
class IsolateAgent(object):
    """
    Isolate agent component
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Injected services
        self._sender = None

        # Bundle context
        self._context = None

    def herald_message(self, herald_svc, message):
        """
        Handles a signal

        :param herald_svc: The Herald service
        :param message: The received message bean
        """
        if message.subject == cohorte.monitor.SIGNAL_STOP_ISOLATE:
            # Isolate must stop
            # Let the method return first,
            # in order to return to the caller immediately
            threading.Thread(name="monitor-agent-stop",
                             target=self.stop).start()

    def stop(self):
        """
        Stops the whole isolate
        """
        # Send the "isolate stopping" signal
        _logger.warning(">>> Isolate will stop <<<")
        message = beans.Message(cohorte.monitor.SIGNAL_ISOLATE_STOPPING,
                                self._context.get_property(cohorte.PROP_UID))
        self._sender.fire_group('all', message)

        _logger.warning(">>> STOPPING isolate <<<")
        self._context.get_bundle(0).stop()

    def group_set(self, group):
        """
        A new Herald directory group has been set.

        Sends the "Ready" message to monitors, as soon as one of their peers
        has been detected
        """
        if group == 'monitors':
            # Send the "ready" signal
            try:
                self._sender.fire_group(
                    'monitors',
                    beans.Message(cohorte.monitor.SIGNAL_ISOLATE_READY))
            except herald.exceptions.NoTransport as ex:
                _logger.error("No transport to notify monitors: %s", ex)
            else:
                _logger.info("Monitors notified of isolate readiness")

    def group_unset(self, group):
        """
        An Herald directory group has been deleted
        """
        if group == 'monitors':
            _logger.warning("Group '%s' has disappeared...", group)

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # Store the context
        self._context = context
        _logger.info("Isolate agent validated")

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Send the stopping signal
        message = beans.Message(cohorte.monitor.SIGNAL_ISOLATE_STOPPING)
        self._sender.fire_group('monitors', message)

        # Clear the context
        self._context = None
        _logger.info("Isolate agent invalidated")
