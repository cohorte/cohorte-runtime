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

# FIXME: It should set up by the configuration service
PSEM2M_MULTICAST_PORT = 42000
""" The PSEM2M multicast port """

SIGNAL_PREFIX = "/psem2m-multicast-agent"
SIGNAL_MATCH_ALL = "%s/*" % SIGNAL_PREFIX
SIGNAL_CONFIRM_BEAT = "%s/confirm" % SIGNAL_PREFIX

PACKET_REGISTER = 1
""" Packet type 'Register' : for isolate first registration """

@ComponentFactory("psem2m-multicast-agent-factory")
@Instantiate("psem2m-multicast-agent")
@Requires("_config", "org.psem2m.isolates.services.conf.ISvcConfig")
@Requires("_directory", "org.psem2m.signals.ISignalDirectory")
@Requires("_receiver", "org.psem2m.signals.ISignalReceiver")
@Requires("_sender", "org.psem2m.signals.ISignalBroadcaster")
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
        self._receiver = None
        self._sender = None

        self._target = None
        self._socket = None

        self._thread = None
        self._thread_running = False


    def handle_received_signal(self, name, data):
        """
        Handles a received signal
        
        :param name: Signal name
        :param data: Complete signal data (meta-data + content)
        :return: Data to send to the requester, or None
        """
        if name == SIGNAL_CONFIRM_BEAT:
            # Beat confirmation
            isolate_id = self._directory.get_isolate_id()

            # FIXME: setup groups from the configuration service
            # FIXME: the virtual group ALL should be computed by the directory
            groups = ["ALL"]

            if isolate_id.startswith("org.psem2m.internals.isolates.forker"):
                # Forkers can only be forkers
                groups.append("FORKERS")

            else:
                # A forker can't be an isolate and vice versa
                groups.append("ISOLATES")

            if isolate_id.startswith("org.psem2m.internals.isolates.monitor"):
                # A monitor is a special isolate
                groups.append("MONITORS")

            return {'id': isolate_id,
                    'node': self._directory.get_local_node(),
                    'groups': groups}


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        self._thread_running = False

        # Unregister to the signals
        self._receiver.unregister_listener(SIGNAL_MATCH_ALL, self)

        # Close the socket
        close_multicast_socket(self._socket, self._target[0])
        self._target = None
        self._socket = None


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
        self._thread = threading.Thread(target=self._run, name="MulticastAgent")
        self._thread.start()

        # Register to signals
        self._receiver.register_listener(SIGNAL_MATCH_ALL, self)

        # Send the registration packet
        self._send_beat()


    def _handle_registration(self, data, host):
        """
        Handles the registration of an isolate
        """
        try:
            # Little endian
            port = struct.unpack("<H", data[1:])[0]

            # Ask for a confirmation
            response = self._sender.send_to(SIGNAL_CONFIRM_BEAT, None,
                                            host, port)

            if response and response.get('results', None):
                # We got at least one result.
                # Register the isolate, considering the first result only
                results = response['results'][0]

                isolate_id = results['id']
                node = results['node']
                groups = results['groups']

                if self._directory is not None:
                    # Directory can be None in the end of the instance
                    self._directory.register_isolate(isolate_id, node,
                                                     host, port, groups)

            else:
                _logger.error("The isolate at [%s]:%d didn't confirmed "
                              "its existence", host, port)

        except:
            _logger.exception("Error confirming an agent beat "
                              "from [%s]:%d", host, port)

    def _run(self):
        """
        Thread listening to the multicast socket
        """
        while self._thread_running:
            try:
                _logger.debug("WAITING FOR DATA....")
                data, sender = self._socket.recvfrom(1500)
                _logger.debug("GOT DATA from '%s' - '%r'", sender, data)

                # Get the content
                if data[0] == PACKET_REGISTER:
                    # Send a signal to (sender[0], content["port"])
                    host = sender[0]

                    # Register in a new thread
                    threading.Thread(target=self._handle_registration,
                                     args=(data, host)).start()

            except:
                if self._thread_running:
                    # Only log if it's a "live" error
                    _logger.exception("Error reading multicast socket")


    def _send_beat(self):
        """
        Forges and sends the registration packet
        """
        port = self._receiver.get_access_info()[1]

        # Prepare the packet
        packet = struct.pack("<BH", PACKET_REGISTER, port)

        # Send over the network
        self._socket.sendto(packet, 0, self._target)
