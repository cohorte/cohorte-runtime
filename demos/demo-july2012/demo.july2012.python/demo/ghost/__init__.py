#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Package providing components that only aim to mess with their isolate

:author: Thomas Calmant
"""

# Pelix
from pelix.ipopo.decorators import ComponentFactory, Provides, Property
from pelix.utilities import to_str
import pelix.services as services

# Standard library
import logging

#-------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

SERVICE_GHOST = "demo.ghost"
""" Represents a ghost service """

#-------------------------------------------------------------------------------

@Provides([services.SERVICE_MQTT_LISTENER, SERVICE_GHOST])
@Property('_id', 'ghost.id')
@Property('_topics', services.PROP_MQTT_TOPICS, 'cohorte/ghost/#')
class Ghost(object):
    """
    Parent class for Ghosts, handling the MQTT part
    """
    def __init__(self):
        """
        Sets up members
        """
        self._id = None
        self._topics = None


    def crash(self):
        """
        Simple way to crash: free(-1)
        """
        _logger.critical("CRASHING...")
        import ctypes
        ctypes.CDLL("libc.so.6").free(-1)
        _logger.critical("!!! AFTERMATH !!!")


    def handle_order(self, event):
        """
        Handles an order received through MQTT

        :param event: The event ID
        """
        _logger.warning("Not implemented: handle_order() - %s", self)


    def handle_mqtt_message(self, topic, payload, qos):
        """
        An MQTT message has been received
        """
        try:
            ghost_id = topic.split('/')[2]
            if ghost_id != self._id:
                # Not for us
                _logger.debug("Invalid ID: %s vs %s", ghost_id, self._id)
                return

        except IndexError:
            # No ID given: event for everyone
            ghost_id = ""

        if not payload:
            # Use ghost ID as payload if necessary
            data = ghost_id
        else:
            data = to_str(payload)

        if data:
            # Only handle "valid" events
            try:
                self.handle_order(data)

            except Exception as ex:
                _logger.exception("Error handling %s ; %s: %s", topic, payload,
                                  ex)


    def __str__(self):
        """
        String representation
        """
        return "{0}({1})".format(type(self).__name__, self._id)
