#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Forker heart beat.

Sends heart beat signals to the monitors to signal the forker presence.

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE constants
import cohorte

# Multicast utility module
import cohorte.utils.multicast as multicast

# Pelix/iPOPO
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Property
from pelix.utilities import to_bytes

import pelix.http

# ------------------------------------------------------------------------------

# Standard library
import logging
import struct
import threading

# ------------------------------------------------------------------------------

# Heart beat packet type
PACKET_TYPE_HEARTBEAT = 1

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

def make_heartbeat(port, application_id, isolate_id, node_id, node_name):
    """
    Prepares the heart beat UDP packet

    Format : Little endian
    * Packet type (1 byte)
    * Signals port (2 bytes)
    * Application ID length (2 bytes)
    * application_id (variable, UTF-8)
    * Isolate ID length (2 bytes)
    * Isolate ID (variable, UTF-8)
    * Node UID length (2 bytes)
    * Node UID (variable, UTF-8)
    * Node name length length (2 bytes)
    * Node name (variable, UTF-8)

    :param port: The Signals access port
    :param application_id: The ID of the current application
    :param isolate_id: The ID of this isolate
    :param node_id: The host node ID
    :param node_name: The host node name
    :return: The heart beat packet content (byte array)
    """
    # Type and port...
    packet = struct.pack("<BH", PACKET_TYPE_HEARTBEAT, port)

    for string in (application_id, isolate_id, node_id, node_name):
        # Strings...
        string_bytes = to_bytes(string)
        packet += struct.pack("<H", len(string_bytes))
        packet += string_bytes

    return packet

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-forker-heart-factory")
@Property('_group', 'multicast.group', "239.0.0.1")
@Property('_port', 'multicast.port', 42000)
@Property('_app_id', 'cohorte.application', "<unknown-app>")
# Just to have the same life cycle than the forker...
@Requires("_forker", cohorte.SERVICE_FORKER)
@Requires("_http", pelix.http.HTTP_SERVICE)
@Requires("_receiver", cohorte.SERVICE_SIGNALS_RECEIVER)
@Requires("_sender", cohorte.SERVICE_SIGNALS_SENDER)
class Heart(object):
    """
    The heart beat sender
    """
    def __init__(self):
        """
        Constructor
        """
        # Injected properties
        self._group = ""
        self._port = 0
        self._app_id = ""

        # Injected services
        self._forker = None
        self._http = None
        self._receiver = None
        self._sender = None

        # Bundle context
        self._context = None

        # Socket
        self._socket = None
        self._target = None

        # Thread control
        self._stop_event = None
        self._thread = None


    def _run(self):
        """
        Heart beat sender
        """
        # Prepare the packet
        beat = make_heartbeat(self._http.get_access()[1],
                              self._app_id,
                              self._context.get_property(cohorte.PROP_UID),
                              self._context.get_property(cohorte.PROP_NODE_UID),
                              self._context.get_property(cohorte.PROP_NODE_NAME)
                             )

        while not self._stop_event.is_set() and self._forker.is_alive():
            # Send the heart beat using the multicast socket
            self._socket.sendto(beat, 0, self._target)

            # Wait 3 seconds before next loop
            self._stop_event.wait(3)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Get out of the waiting condition
        self._stop_event.set()

        # Unregister to the signals
        # self._receiver.unregister_listener(SIGNAL_MATCH_ALL, self)

        # Wait for the thread to stop
        self._thread.join(.5)

        # Close the socket
        self._socket.close()

        _logger.debug("Heart multicast socket closed")

        # Clean up
        self._context = None
        self._stop_event = None
        self._socket = None
        self._target = None
        self._thread = None


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context
        self._port = int(self._port)

        _logger.info("Heart validated: multicast group=%s port=%d",
                     self._group, self._port)

        # Create the socket
        self._socket, address = multicast.create_multicast_socket(self._group,
                                                                  self._port,
                                                                  False)

        # Store group information
        self._target = (address, self._port)

        # Prepare the thread controls
        self._stop_event = threading.Event()

        # Register to signals
        # self._receiver.register_listener(SIGNAL_MATCH_ALL, self)

        # Start the heart
        self._thread = threading.Thread(target=self._run,
                                        name="Forker-HeartBeat")
        self._thread.start()
