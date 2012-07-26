#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Forker heart beat.

Sends heart beat signals to the monitors to signal the forker presence.

:author: Thomas Calmant
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Instantiate, \
    Requires, Validate, Invalidate

# ------------------------------------------------------------------------------

import logging
_logger = logging.getLogger(__name__)

import socket
import struct
import sys
import threading

# ------------------------------------------------------------------------------

# Heart beat packet type
PACKET_TYPE_HEARTBEAT = 1

# PSEM2M property: Isolate ID
PROPERTY_ISOLATE_ID = 'psem2m.isolate.id'

# PSEM2M property: Isolate Node
PROPERTY_ISOLATE_NODE = 'psem2m.isolate.node'

# ------------------------------------------------------------------------------

if sys.version_info[0] == 3:
    # Python 3
    def _to_bytes(data, encoding="UTF-8"):
        """
        Converts the given string to a bytes array
        """
        if type(data) is bytes:
            # Nothing to do
            return data

        return data.encode(encoding)

else:
    # Python 2
    def _to_bytes(data, encoding="UTF-8"):
        """
        Converts the given string to a bytes array
        """
        if type(data) is str:
            # Nothing to do
            return data

        return data.encode(encoding)

# ------------------------------------------------------------------------------

import os

if os.name == "nt":
    # Windows Specific code
    def pton(family, address):
        """
        Calls inet_pton
        
        :param family: Socket family
        :param address: A string address
        :return: The binary form of the given address
        """
        if family == socket.AF_INET:
            return socket.inet_aton(address)

        elif family == socket.AF_INET6:
            # Do it using WinSocks
            import ctypes
            winsock = ctypes.windll.ws2_32

            # Prepare structure
            class sockaddr_in6(ctypes.Structure):
                _fields_ = [("sin6_family", ctypes.c_short),
                            ("sin6_port", ctypes.c_ushort),
                            ("sin6_flowinfo", ctypes.c_ulong),
                            ("sin6_addr", ctypes.c_ubyte * 16),
                            ("sin6_scope_id", ctypes.c_ulong)
                            ]

            # Prepare pointers
            addr_ptr = ctypes.c_char_p(_to_bytes(address))

            out_address = sockaddr_in6()
            size = len(sockaddr_in6)
            size_ptr = ctypes.pointer(size)

            # Second call
            winsock.WSAStringToAddressA(addr_ptr, family, 0,
                                        out_address, size_ptr)

            # Convert the array...
            bin_addr = 0
            for part in out_address.sin6_addr:
                bin_addr = bin_addr * 16 + part

            return bin_addr

        else:
            raise ValueError("Unhandled socket family: %s", family)

else:
    # Other systems
    def pton(family, address):
        """
        Calls inet_pton
        
        :param family: Socket family
        :param address: A string address
        :return: The binary form of the given address
        """
        return socket.inet_pton(family, address)


def make_mreq(family, address):
    """
    Makes a mreq structure object for the given address and socket family.
    
    :param family: A socket family (AF_INET or AF_INET6)
    :param address: A multicast address
    :raise ValueError: Invalid family or address
    """
    if not address:
        raise ValueError("Empty address")

    # Convert the address to a binary form
    group_bin = pton(family, address)

    if family == socket.AF_INET:
        # IPv4
        # struct ip_mreq
        # {
        #     struct in_addr imr_multiaddr; /* IP multicast address of group */
        #     struct in_addr imr_interface; /* local IP address of interface */
        # };
        # "=I" : Native order, standard size unsigned int
        return group_bin + struct.pack("=I", socket.INADDR_ANY)

    elif family == socket.AF_INET6:
        # IPv6
        # struct ipv6_mreq {
        #    struct in6_addr ipv6mr_multiaddr;
        #    unsigned int    ipv6mr_interface;
        # };
        # "@I" : Native order, native size unsigned int
        return group_bin + struct.pack("@I", 0)

    raise ValueError("Unknown family %s", family)


def create_multicast_socket(address, port):
    """
    Creates a multicast socket according to the given address and port.
    Handles both IPv4 and IPv6 addresses.
    
    :raise ValueError: Invalid address or port
    """
    # Get the information about a datagram (UDP) socket, of any family
    try:
        addr_info = socket.getaddrinfo(address, port, socket.AF_UNSPEC,
                                       socket.SOCK_DGRAM)
    except socket.gaierror:
        raise ValueError("Error retrieving address informations (%s, %s)" \
                         % (address, port))

    if len(addr_info) > 1:
        _logger.debug("More than one address information found. "
                      "Using the first one.")

    # Get the first entry : (family, socktype, proto, canonname, sockaddr)
    addr_info = addr_info[0]

    # Only accept IPv4/v6 addresses
    if addr_info[0] not in (socket.AF_INET, socket.AF_INET6):
        # Unhandled address family
        raise ValueError("Unhandled socket family : %d" % (addr_info[0]))

    # Prepare the socket
    sock = socket.socket(addr_info[0], socket.SOCK_DGRAM)

    # Reuse address
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    if hasattr(socket, 'SO_REUSEPORT'):
        # Special for MacOS
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEPORT, 1)

    # Bind the socket
    if sock.family == socket.AF_INET:
        # IPv4 binding
        sock.bind(('0.0.0.0', port))

    else:
        # IPv6 Binding
        sock.bind(('::', port))

    # Prepare the mreq structure to join the group
    # addrinfo[4] = (addr,port)
    mreq = make_mreq(sock.family, addr_info[4][0])

    # Join the group
    if sock.family == socket.AF_INET:
        # IPv4
        sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

        # Allow multicast packets to get back on this host
        sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_LOOP, 1)

    else:
        # IPv6
        sock.setsockopt(socket.IPPROTO_IPV6, socket.IPV6_JOIN_GROUP, mreq)

        # Allow multicast packets to get back on this host
        sock.setsockopt(socket.IPPROTO_IPV6, socket.IPV6_MULTICAST_LOOP, 1)

    return (sock, addr_info[4][0])


def close_multicast_socket(sock, address):
    """
    Cleans up the given multicast socket.
    Unregisters it of the multicast group.
    
    Parameters should be the result of create_multicast_socket
    
    :param sock: A multicast socket
    :param address: The multicast address used by the socket
    """
    if not sock:
        return

    if address:
        # Prepare the mreq structure to join the group
        mreq = make_mreq(sock.family, address)

        # Quit group
        if sock.family == socket.AF_INET:
            # IPv4
            sock.setsockopt(socket.IPPROTO_IP, socket.IP_DROP_MEMBERSHIP, mreq)

        elif sock.family == socket.AF_INET6:
            # IPv6
            sock.setsockopt(socket.IPPROTO_IPV6, socket.IPV6_LEAVE_GROUP, mreq)

    # Close the socket
    sock.close()

# ------------------------------------------------------------------------------

def make_heartbeat(port, application_id, isolate_id, node_id):
    """
    Prepares the heart beat UDP packet
    
    Format : Little endian
    * Packet type (1 byte)
    * Signals port (2 bytes)
    * Application ID length (2 bytes)
    * application_id (variable, UTF-8)
    * Isolate ID length (2 bytes)
    * Isolate ID (variable, UTF-8)
    * Node ID length (2 bytes)
    * Node ID (variable, UTF-8)
    
    :param port: The Signals access port
    :param application_id: The ID of the current application
    :param isolate_id: The ID of this isolate
    :param node_id: The host node ID
    :return: The heart beat packet content (byte array)
    """
    # Type and port...
    packet = struct.pack("<BH", PACKET_TYPE_HEARTBEAT, port)

    for string in (application_id, isolate_id, node_id):
        # Strings...
        string_bytes = _to_bytes(string)
        packet += struct.pack("<H", len(string_bytes))
        packet += string_bytes

    return packet

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-forker-heart-factory")
@Instantiate("psem2m-forker-heart")
@Requires("_config", "org.psem2m.isolates.services.conf.ISvcConfig")
# Just to have the same life cycle than the forker...
@Requires("_forker", "org.psem2m.isolates.services.forker.IForker")
# To retrieve access information
@Requires("_http", "HttpService")
@Requires("_receiver", "org.psem2m.signals.ISignalReceiver")
@Requires("_sender", "org.psem2m.signals.ISignalBroadcaster")
class Heart(object):
    """
    The heart beat sender
    """
    def __init__(self):
        """
        Constructor
        """
        self._config = None
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
        self._event = None
        self._thread = None


    def _run(self):
        """
        Heart beat sender
        """
        # Prepare the packet
        app = self._config.get_application()
        if app:
            appId = app.get_application_id()

        else:
            appId = "<unknown-app>"

        beat = make_heartbeat(self._http.get_port(),
                              appId,
                              self._context.get_property(PROPERTY_ISOLATE_ID),
                              self._context.get_property(PROPERTY_ISOLATE_NODE))

        while not self._event.is_set():
            # Send the heart beat using the multicast socket
            self._socket.sendto(beat, 0, self._target)

            # Wait 3 seconds before next loop
            self._event.wait(3)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Get out of the waiting condition
        self._event.set()

        # Unregister to the signals
        # self._receiver.unregister_listener(SIGNAL_MATCH_ALL, self)

        # Wait for the thread to stop
        self._thread.join(.5)

        # Close the socket
        close_multicast_socket(self._socket, self._target[0])

        # Clean up
        self._context = None
        self._event = None
        self._socket = None
        self._target = None
        self._thread = None


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context

        # Get the multicast configuration
        group = self._config.get_application().get_multicast_group()
        port = self._config.get_application().get_multicast_port()

        # Create the socket
        self._socket, address = create_multicast_socket(group, port)

        # Store group information
        self._target = (address, port)

        # Prepare the thread controls
        self._event = threading.Event()

        # Register to signals
        # self._receiver.register_listener(SIGNAL_MATCH_ALL, self)

        # Start the heart
        self._thread = threading.Thread(target=self._run, name="HeartBeart")
        self._thread.start()
