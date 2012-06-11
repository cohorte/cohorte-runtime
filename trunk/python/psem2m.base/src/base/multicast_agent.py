#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Created on 11 juin 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import logging
import socket
import struct
import threading

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Instantiate

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

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
    group_bin = socket.inet_pton(family, address)

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

PSEM2M_MULTICAST_PORT = 42000

@ComponentFactory("psem2m-multicast-agent-factory")
@Instantiate("MulticastAgent")
@Requires("_config", "org.psem2m.isolates.services.conf.ISvcConfig")
@Requires("_directory", "org.psem2m.IsolateDirectory")
class MulticastAgent(object):
    """
    An UDP v4/v6 multicast agent for PSEM2M
    """
    def __init__(self):
        """
        Constructor
        """
        self._config = None
        self._directory = None

        self._target = None
        self._socket = None

        self._thread = None
        self._thread_running = False


    def run(self):
        """
        Thread listening to the multicast socket
        """
        while self._thread_running:
            data, sender = self._socket.recvfrom(1500)
            _logger.info("Received %r from %s", data, sender)
            # TODO: send a signal to (sender[0], content["port"])


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        # Get the application multicast group
        multicast = self._config.get_application().get_multicast()

        # Create the corresponding socket
        self._socket, address = create_multicast_socket(multicast,
                                                        PSEM2M_MULTICAST_PORT)
        self._target = (address, PSEM2M_MULTICAST_PORT)

        # Start the reading thread
        self._thread_running = True
        self._thread = threading.Thread(target=self.run, name="MulticastAgent")
        self._thread.start()

        # Send the registration packet
        # TODO: send isolate information (port)
        self._socket.sendto("Hello, World !\n".encode(), 0, self._target)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        self._thread_running = False

        # Close the socket
        close_multicast_socket(self._socket, self._target[0])
        self._target = None
        self._socket = None
