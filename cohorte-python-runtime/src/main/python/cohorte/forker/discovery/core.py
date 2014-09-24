#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Forker discovery

Uses probes to find other forkers (i.e. other nodes) and their disappearing.

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE
import cohorte.forker
import cohorte.signals

# Herald
import herald

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, BindField, Provides, Property

# Pelix utilities
import pelix.http
import pelix.threadpool

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-forker-discovery-factory")
@Provides(cohorte.forker.SERVICE_DISCOVERY)
@Requires('_directory', cohorte.SERVICE_SIGNALS_DIRECTORY)
@Requires('_listeners', cohorte.forker.SERVICE_FORKER_LISTENER, True, True)
@Requires('_http', pelix.http.HTTP_SERVICE)
@Requires('_sender', cohorte.SERVICE_SIGNALS_SENDER)
@Property('_app_id', 'cohorte.application', '<unknown-app>')
class ForkerDiscovery(object):
    """
    Forker/Node discoverer
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._directory = None
        self._listeners = None
        self._http = None
        self._sender = None

        # Local Forker UID
        self._local_uid = None

        # Application ID
        self._application_id = None

        # Forker UID -> Node UID
        self._forkers = {}

        # Notification queue
        self._events_thread = None


    @Validate
    def _validate(self, context):
        """
        Component validated
        """
        # Store the local UID
        self._local_uid = context.get_property(cohorte.PROP_UID)

        # Start the event pool
        self._events_thread = pelix.threadpool.ThreadPool(
            1, logname="forker-aggregator")
        self._events_thread.start()


    @Invalidate
    def _invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister all forkers
        for forker in tuple(self._forkers.keys()):
            self.forker_lost(forker)

        # Stop the thread pool
        self._events_thread.stop()
        self._events_thread = None

        # Clean up
        self._forkers.clear()
        self._local_uid = None


    @BindField('_listeners')
    def _bind_listener(self, field, service, svc_ref):
        """
        A new listener has been bound
        """
        try:
            # Get the 'forker_ready' callback method
            forker_ready = getattr(service, "forker_ready")
        except AttributeError:
            # Not implemented, ignore
            pass
        else:
            # Notify the listener of all known forkers
            for uid, node_uid in self._forkers.items():
                forker_ready(uid, node_uid)


    def get_appid(self):
        """
        Returns the local application ID
        """
        return self._application_id or "<unknown-app>"


    def get_uid(self):
        """
        Returns the local forker UID
        """
        return self._local_uid


    def register_forker(self, uid, node_uid, node_name, host, port):
        """
        Registers a forker in the directory.

        :param uid: Forker UID
        :param node_uid: UID of the node hosting the forker
        :param node_name: Name of the node hosting the forker
        :param address: Node address
        :param port: Forker access port
        """
        _logger.debug("Registering forker: %s from %s/%s",
                      uid, node_uid, node_name)

        # Update the node host
        self._directory.set_node_address(node_uid, host)

        # Update the node name
        self._directory.set_node_name(node_uid, node_name)

        # Register the forker
        if self._directory.register_isolate(uid, cohorte.forker.FORKER_NAME,
                                            node_uid, port):
            # New isolate: send it a SYN-ACK
            self._sender.fire(cohorte.signals.SIGNAL_REGISTER_SYNACK, None, uid)

            # Fresh forker: send a contact signal
            self._send_contact(host, port)

            # Store information
            self._forkers[uid] = node_uid

            _logger.info("Newly registered forker ID=%s Node=%s/%s Port=%d",
                         uid, node_uid, node_name, port)

        # FIXME: do it only once ?
        # Notify listeners
        self._notify_listeners(uid, node_uid, True)


    def forker_lost(self, uid, reason="Unknown reason"):
        """
        A discovery service lost a forker

        :param uid: UID of the forker
        :param reason: Optional message about the loss
        """
        # Compute the forker node and its isolates
        node_uid = self._directory.get_isolate_node(uid)
        isolates = self._directory.get_isolates_on_node(node_uid)

        _logger.debug("Lost forker %s on node %s (%s isolates): %s",
                      uid, node_uid, len(isolates), reason)

        # Compute the signal exclusion list
        excluded = set(isolates)
        excluded.add(uid)

        # Prepare the signal content
        content = {'uid': uid, 'node': node_uid, 'isolates': isolates}

        # Send the signal to all but the lost forker and its isolates
        self._sender.post(cohorte.forker.SIGNAL_FORKER_LOST,
                          content, dir_group=cohorte.signals.GROUP_ALL,
                          excluded=excluded)


    def _notify_listeners(self, uid, node, registered):
        """
        Notifies listeners of a forker event

        :param uid: UID of a forker
        :param node: Node hosting the forker
        :param registered: If True, the forker has been registered, else lost
        """
        if not self._listeners:
            # Nothing to do
            return

        # Compute the method name
        if registered:
            method_name = "forker_ready"
        else:
            method_name = "forker_lost"

        # Enqueue the notification call
        self._events_thread.enqueue(self.__notification, self._listeners[:],
                                    method_name, uid, node)


    def __notification(self, listeners, method_name, uid, node):
        """
        Listeners notification loop

        :param listeners: List of listeners to call
        :param method: Name of the method to call in listeners
        :param uid: UID of a forker
        :param node: Node hosting the forker
        """
        for listener in listeners:
            # Get the listener method
            try:
                method = getattr(listener, method_name)
            except AttributeError:
                # Method not implemented
                pass
            else:
                try:
                    # Call it
                    method(uid, node)
                except Exception as ex:
                    _logger.exception("A forker event listener failed: %s", ex)


    def _send_contact(self, host, port):
        """
        Sends a CONTACT signal to the given access point.

        :param host: A host address
        :param port: A signal access port
        """
        try:
            # Get access info
            local_port = self._http.get_access()[1]

            # Send the contact signal
            result = self._sender.send_to(cohorte.signals.SIGNAL_CONTACT,
                                          {"port": local_port}, host, port)
            if not result:
                _logger.warning("No response from forker at host=%s port=%d",
                                host, port)
        except Exception as ex:
            # Just log the exception
            _logger.error("Error sending contact signal: %s", ex)
