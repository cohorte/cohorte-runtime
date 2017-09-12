#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Local discovery based for isolates of the same node.
It is based on the interaction between the newly created isolates 
and their forker. Then created, the new isolate contacts the forker,
and when it is registered it demands from it the list of other isolates
belonging to the same node and contacts them respectively.

:author: Bassem Debbabi
:copyright: Copyright 2016, Cohorte Technologies (ex. isandlaTech)
:license: Apache License 2.0
:version: 0.0.5
:status: Alpha

..

    Copyright 2016 Cohorte Technologies (ex. isandlaTech)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
"""

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

# Herald
from herald.transports.http import ACCESS_ID, SERVICE_HTTP_TRANSPORT, SERVICE_HTTP_RECEIVER, \
    FACTORY_DISCOVERY_MULTICAST, PROP_MULTICAST_GROUP, PROP_MULTICAST_PORT
import herald
import herald.beans as beans
import herald.utils as utils
import herald.transports.peer_contact as peer_contact
from . import FACTORY_DISCOVERY_LOCAL

# Pelix/iPOPO
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, Provides, \
    Invalidate, Property, RequiresBest, Instantiate

# Cohorte
import cohorte
import cohorte.forker
import cohorte.monitor

# Standard library
import logging
import threading

try:
    # Python 3
    import urllib.parse as urlparse

except ImportError:
    # Python 2
    import urlparse

# ------------------------------------------------------------------------------

_SUBJECT_PREFIX = "cohorte/local/discovery"
""" Common prefix to Local Discovery  """

_SUBJECT_MATCH_ALL = "{0}/*".format(_SUBJECT_PREFIX)
""" Filter to match agent signals """

SUBJECT_GET_NEIGHBORS_LIST = "{0}/get_neighbors_list".format(_SUBJECT_PREFIX)
""" Gets local peers of the same node """

_logger = logging.getLogger(__name__)


# ------------------------------------------------------------------------------


@ComponentFactory(FACTORY_DISCOVERY_LOCAL)
@Provides(herald.SERVICE_DIRECTORY_LISTENER)
@Provides(herald.SERVICE_LISTENER)
@RequiresBest('_probe', herald.SERVICE_PROBE)
@Requires('_directory', herald.SERVICE_DIRECTORY)
@Requires('_receiver', SERVICE_HTTP_RECEIVER)
@Requires('_transport', SERVICE_HTTP_TRANSPORT)
@Requires('_herald', herald.SERVICE_HERALD)
@Property('_filters', herald.PROP_FILTERS, [SUBJECT_GET_NEIGHBORS_LIST])
@Instantiate("cohorte-local-discovery")
class LocalDiscovery(object):
    """
    Discovery of Herald peers based on multicast
    """

    def __init__(self):
        """
        Sets up the component
        """
        # Injected services
        self._herald = None
        self._directory = None
        self._receiver = None
        self._transport = None
        self._probe = None
        self._herald = None
        # Local peer bean
        self._local_peer = None
        # Discovered forker 
        self._forker = None
        # Threads
        self._discover_forker_thread = None
        self._discover_neighbors_thread = None
        # self._lst_lock = threading.Lock()
        # Bundle context
        self._context = None
        # forker access information (herald http servlet)         
        self._forker_host = "127.0.0.1"
        # default port value (real value will be retrieved from broker url)
        self._forker_port = 8080
        # default path value (real value will be retrieved from Http Receiver component) 
        self._forker_path = "/herald"

    def __extract_forker_http_port(self, broker_url):
        """
        Extract forker accesses
        """
        if not broker_url:
            # No data
            raise ValueError("Empty Broker URL")
        # Only HTTP is handled
        url = urlparse.urlparse(broker_url)
        if url.scheme != "http":
            raise ValueError("Unknown forker access protocol: {0}"
                             .format(url.scheme))

            # The access port (default: 8080)
        if url.port:
            port = url.port
        else:
            # Default port
            port = 8080
        # update forker port
        return port

    def __discover_peer(self, host, port, path):
        """
        Discover a local Peer by sending to him the contact message
        """
        if path.startswith('/'):
            # Remove the starting /, as it is added while forging the URL
            path = path[1:]

        # Normalize the address of the sender
        host = utils.normalize_ip(host)

        # Prepare the "extra" information, like for a reply
        extra = {'host': host, 'port': port, 'path': path}
        local_dump = self._directory.get_local_peer().dump()
        try:
            self._transport.fire(
                None,
                beans.Message(peer_contact.SUBJECT_DISCOVERY_STEP_1,
                              local_dump), extra)
        except Exception as ex:
            _logger.exception("Error contacting peer: %s", ex)

    def __discover_forker(self):
        """
        Discover the forker of this local Peer by sending to him 
        the contact message
        """
        host = self._forker_host
        port = self._forker_port
        path = self._forker_path

        if path.startswith('/'):
            # Remove the starting /, as it is added while forging the URL
            path = path[1:]

        # Normalize the address of the sender
        host = utils.normalize_ip(host)

        # Prepare the "extra" information, like for a reply
        extra = {'host': host, 'port': port, 'path': path}
        local_dump = self._directory.get_local_peer().dump()
        try:
            self._transport.fire(
                None,
                beans.Message(peer_contact.SUBJECT_DISCOVERY_STEP_1,
                              local_dump), extra)
        except Exception as ex:
            _logger.exception("Error contacting peer: %s", ex)

    def __discover_neighbors(self):
        """
        Discover local neighbor Peers.
        The list of the neighbor peers is retrieved from the forker.
        We should ensure that the forker if properly added to the local directory
        before sending him this contact message.
        """
        try:
            reply = self._herald.send(
                self._forker.uid,
                beans.Message(SUBJECT_GET_NEIGHBORS_LIST, self._local_peer.uid))
            if reply is not None:
                for peer_uid in reply.content:
                    self.__discover_neighbor(reply.content[peer_uid])
        except Exception as ex:
            _logger.exception("Error contacting forker peer to retrieve local neighbor peers: %s", ex)

    def __discover_neighbor(self, peer_dump):
        """
        Discover local neighbor Peer
        """
        host = peer_dump['accesses']['http'][0]
        port = peer_dump['accesses']['http'][1]
        path = peer_dump['accesses']['http'][2]

        if path.startswith('/'):
            # Remove the starting /, as it is added while forging the URL
            path = path[1:]

        # Normalize the address of the sender
        host = utils.normalize_ip(host)

        # Prepare the "extra" information, like for a reply
        extra = {'host': host, 'port': port, 'path': path}
        local_dump = self._directory.get_local_peer().dump()
        try:
            self._transport.fire(
                None,
                beans.Message(peer_contact.SUBJECT_DISCOVERY_STEP_1,
                              local_dump), extra)
        except Exception as ex:
            _logger.exception("Error contacting peer: %s", ex)

    """
    Herald callbacks ---------------------------------------------------
    """

    def herald_message(self, herald_svc, message):
        """
        Handles herald message

        :param herald_svc: The Herald service
        :param message: The received message bean
        """
        subject = message.subject
        reply = None
        if subject == cohorte.monitor.SIGNAL_ISOLATE_LOST:
            # local neighbor Peer is going away                                
            try:
                peer = self._directory.get_peer(message.content)
                if peer.node_uid == self._local_peer.node_uid:
                    peer.unset_access(ACCESS_ID)
            except KeyError:
                # Unknown peer
                pass
        elif subject == SUBJECT_GET_NEIGHBORS_LIST:
            # get all peers of the local node (except the peer requested the list)
            neighbors = self._directory.get_peers_for_node(self._local_peer.node_uid)
            reply = {peer.uid: peer.dump() for peer in neighbors if
                     peer.uid not in (message.content, self._local_peer.uid)}
            herald_svc.reply(message, reply)

    """
    Directory Listener callbacks ---------------------------------------
    """

    def peer_registered(self, peer):
        if peer is not None:
            if self._local_peer.name != cohorte.FORKER_NAME:
                # monitor only local peers (same node)
                if peer.node_uid == self._local_peer.node_uid:
                    # if the forker peer is registered
                    if peer.name == cohorte.FORKER_NAME:
                        # forker registred                        
                        self._forker = peer
                        # discover other local peers (of the same node)
                        self._discover_neighbors_thread = threading.Thread(target=self.__discover_neighbors,
                                                                           name="Local-Discovery-DiscoverNeighbors")
                        self._discover_neighbors_thread.start()

    def peer_updated(self, peer, access_id, data, previous):
        pass

    def peer_unregistered(self, peer):
        pass

    """
    Lifecycle --------------------------------------------------------
    """

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # store framework objects
        self._context = context
        # init local Peer bean        
        self._local_peer = self._directory.get_local_peer()
        # update forker path (name of the http receiver servlet)        
        host, port, path = self._receiver.get_access_info()
        self._forker_path = path
        # update forker's' access information               
        forker_port = context.get_property(cohorte.PROP_FORKER_HTTP_PORT)
        if forker_port:
            self._forker_port = forker_port
        else:
            self._forker_port = None
        # if this Peer is not the forker, we should send a contact message 
        # to its forker
        broker_url = context.get_property(cohorte.PROP_CONFIG_BROKER)
        if broker_url:
            # update forker port if None
            if not self._forker_port:
                self._forker_port = self.__extract_forker_http_port(broker_url)
            # discover the forker           
            self._discover_forker_thread = threading.Thread(target=self.__discover_forker,
                                                            name="Local-Discovery-DiscoverForker")
            self._discover_forker_thread.start()

    @Invalidate
    def invalidate(self, _):
        """
        Component invalidated
        """
        # Wait for the threads to stop        
        if self._discover_forker_thread is not None:
            self._discover_forker_thread.join(.5)
        self._discover_forker_thread = None

        if self._discover_neighbors_thread is not None:
            self._discover_neighbors_thread.join(.5)
        self._discover_neighbors_thread = None
