#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Python implementation of the PSEM2M Directory updater

Created on 18 juin 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Instantiate

# ------------------------------------------------------------------------------

import logging
import threading

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

SIGNAL_PREFIX = "/psem2m-directory-updater"
""" Prefix of all directory updater signals """

SIGNAL_PREFIX_MATCH_ALL = "%s/*" % SIGNAL_PREFIX
""" Pattern to match all directory updater signals """

SIGNAL_DUMP = "%s/dump" % SIGNAL_PREFIX
""" Directory dump request """

SIGNAL_REGISTER = "%s/register" % SIGNAL_PREFIX
""" Isolate registration notification """

SIGNAL_CONTACT = "%s/contact" % SIGNAL_PREFIX
""" Special case for early starting forkers: a monitor signals its dump port """

SIGNAL_ISOLATE_LOST = "/psem2m/isolate/lost"
""" Isolate disappeared """

REGISTERED = 0
""" Isolate presence event: Isolate registered """

UNREGISTERED = 1
""" Isolate presence event: Isolate unregistered or lost """

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-signals-directory-updater-factory")
@Instantiate("psem2m-signals-directory-updater")
@Requires("_directory", "org.psem2m.signals.ISignalDirectory")
@Requires("_listeners",
          "org.psem2m.isolates.services.monitoring.IIsolatePresenceListener",
          aggregate=True, optional=True)
@Requires("_receiver", "org.psem2m.signals.ISignalReceiver")
@Requires("_sender", "org.psem2m.signals.ISignalBroadcaster")
class DirectoryUpdater(object):
    """
    Directory update component
    """
    def __init__(self):
        """
        Constructor
        """
        self._directory = None
        self._listeners = []
        self._receiver = None
        self._sender = None

        # Lock to have a clean directory during signals handling
        self._lock = threading.Lock()


    def _grab_directory(self, host, port, ignored_node=None):
        """
        Sends a directory dump signal to the dumper on local host.
        
        If ignored_host is not None, the address corresponding to it in the
        dumped directory won't be stored.
        
        :param host: Directory dumper host address
        :param port: Directory signal listener port
        :param ignored_port: The address for this node must be ignored
        """
        # Prepare pre-registration content, without propagation
        content = self._prepare_registration_content(False)

        # Send the dump signal, with a registration content
        sig_results = self._sender.send_to(SIGNAL_DUMP, content, host, port)
        if not sig_results or not sig_results["results"]:
            _logger.warning("Nothing returned by the directory dumper")
            return

        # Local information
        local_id = self._directory.get_isolate_id()
        local_node = self._directory.get_local_node()

        # Get the first result only
        results = sig_results["results"]
        if len(results) > 1:
            _logger.warning("More than one result found. Ignoring others")

        result = results[0]

        # 1. Setup nodes hosts
        for node, dumped_host in result["nodes_host"].items():
            if node != ignored_node and node != local_node:
                self._directory.set_node_address(node, dumped_host)

        # 2. Prepare isolates information
        new_isolates = {}

        for isolate_id, access in result["accesses"].items():
            # Access URL
            if isolate_id == local_id:
                # Ignore current isolate
                continue

            new_isolates[isolate_id] = {}
            new_isolates[isolate_id]["node"] = access["node"]
            new_isolates[isolate_id]["port"] = access["port"]

        for group, isolates in result["groups"].items():
            # Groups
            for isolate_id in isolates:
                if isolate_id in new_isolates:
                    if "groups" in new_isolates[isolate_id]:
                        new_groups = new_isolates[isolate_id]["groups"]
                    else:
                        new_groups = new_isolates[isolate_id]["groups"] = []

                    new_groups.append(group)

        # 3. Register all new isolates
        for isolate_id, info in new_isolates.items():
            try:
                self._directory.register_isolate(isolate_id,
                                                 info["node"], info["port"],
                                                 info.get("groups", None))
            except KeyError:
                _logger.warning("Missing information to register '%s': %s",
                                isolate_id, info)


        # Now, we can send our registration signal
        self._send_registration(host, port, True)


    def _grab_remote_directory(self, signal_data):
        """
        Retrieves the directory of a remote isolate.
        
        This method is called after a CONTACT signal has been received from a
        monitor.
        
        :param signal_data: The received contact signal
        """
        # Only monitors can send us contacts
        remote_id = signal_data["senderId"]
        if not remote_id.startswith("org.psem2m.internals.isolates.monitor"):
            _logger.warning("Contacts must be made by a monitor, not %s",
                            remote_id)
            return

        # Get information on the sender
        remote_address = signal_data["senderAddress"]
        remote_node = signal_data["senderNode"]

        _logger.debug("Grab directory from %s (%s)", remote_id, remote_node)

        # Get the dumper port
        content = signal_data["signalContent"]
        remote_port = content["port"]
        if not remote_port:
            _logger.warning("No port given")
            return

        # Store the remote node
        self._directory.set_node_address(remote_node, remote_address)

        # Grab the directory
        self._grab_directory(remote_address, remote_port, remote_node)


    def _notify_listeners(self, isolate_id, isolate_node, event):
        """
        Notifies listeners of an isolate presence event
        
        :param isolate_id: ID of the isolate
        :param isolate_node: Node of the isolate
        :param event: Kind of event
        """
        if not self._listeners:
            # No listeners
            return

        def notification_loop(self):
            # Use a copy of the listeners
            for listener in self._listeners[:]:
                try:
                    listener.handle_isolate_presence(isolate_id, isolate_node,
                                                     event)
                except:
                    # Just log...
                    _logger.exception("Error notifying a presence listener")

        # Notify in another thread
        threading.Thread(target=notification_loop, args=[self]).start()


    def _prepare_registration_content(self, propagate):
        """
        Prepares the registration signal content.
        
        :param propagate: If true, the receivers of this signal will re-emit it
        :return: The content for a registration signal
        """
        # Beat confirmation
        isolate_id = self._directory.get_isolate_id()

        # FIXME: setup groups from the configuration service
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

        return {"id": isolate_id,
                "address": None, # <- No address when sending
                "node": self._directory.get_local_node(),
                "port": self._receiver.get_access_info()[1],
                "groups": groups,
                "propagate": propagate}


    def _register_isolate(self, signal_data):
        """
        Registers an isolate according to the given map
        
        :param signal_data: The received signal 
        """
        content = signal_data["signalContent"]

        isolate_id = content["id"]
        if isolate_id == self._directory.get_isolate_id():
            # Ignore self-registration
            return

        node = content["node"]
        if node == signal_data["senderNode"]:
            # If both the registered and the registrar are on the same node,
            # use the sender address to update the node access
            address = signal_data["senderAddress"]

        else:
            # Else: use the address indicated in the signal, or use the sender
            # address
            address = content.get("address", None)
            if not address:
                # Address could be empty, so don't use the dict.get() parameter
                address = signal_data["senderAddress"]

        # 1. Update the node host
        self._directory.set_node_address(node, address)

        # 2. Register the isolate
        self._directory.register_isolate(isolate_id, node, content["port"],
                                         content["groups"])

        # 3. Propagate the registration, if needed
        if content["propagate"]:
            # Propagate only once...
            content["propagate"] = False

            # Indicate the address we used for the registration
            content["address"] = address

            self._sender.fire(SIGNAL_REGISTER, content, dir_group="OTHERS",
                              excluded=[isolate_id])


    def _send_registration(self, host, port, propagate):
        """
        Sends the registration signal to the listener at the given (host, port)
        
        If host is None, the signal will be sent to the directory group OTHERS,
        therefore the directory must contain some data.
        If host is not None, the signal will be sent to the given (host, port)
        couple.
        
        :param host: Registration listener host, or None
        :param port: Registration listener port
        :param propagate: If true, the receivers of this signal will re-emit it
        """
        content = self._prepare_registration_content(propagate)

        # Send the registration to the given address
        try:
            sig_results = self._sender.send_to(SIGNAL_REGISTER, content,
                                               host, port)

            if not sig_results:
                _logger.warning("Nothing returned during registration")
                return

        except Exception as ex:
            _logger.exception("Error sending registration signal to=%s port=%d:"
                              " %s. Sending to all known isolates.",
                              host, port, ex)

            self._sender.send(SIGNAL_REGISTER, content, dir_group="OTHERS")


    def handle_received_signal(self, name, signal_data):
        """
        Called when a remote services signal is received
        
        :param name: Signal name
        :param signal_data: Signal content
        """
        if name == SIGNAL_DUMP:
            with self._lock:
                # Register the incoming isolate
                self._register_isolate(signal_data)

                # Dump the directory
                return self._directory.dump()

        elif name == SIGNAL_REGISTER:
            with self._lock:
                # Isolate registration
                self._register_isolate(signal_data)

                # Notify listeners
                self._notify_listeners(signal_data["senderId"],
                                       signal_data["senderNode"], REGISTERED)

        elif name == SIGNAL_ISOLATE_LOST:
            with self._lock:
                # Unregister the isolate
                lost_isolate = signal_data["signalContent"]
                if lost_isolate:
                    # Get the node of the lost isolate
                    lost_node = self._directory.get_isolate_node(lost_isolate)

                    # Unregister it
                    self._directory.unregister_isolate(lost_isolate)

                    # Notify listeners
                    self._notify_listeners(lost_isolate, lost_node,
                                           UNREGISTERED)

        elif name == SIGNAL_CONTACT:
            # A contact has been signal, ask for a remote directory dump
            # -> Forker only
            self._grab_remote_directory(signal_data)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister to isolate registration signals
        self._receiver.unregister_listener(SIGNAL_PREFIX_MATCH_ALL, self)
        self._receiver.unregister_listener(SIGNAL_ISOLATE_LOST, self)


    @Validate
    def validate(self, context):
        """
        Component validate
        """
        # Get the local dumper port
        dump_port = context.get_property("psem2m.directory.dumper.port")
        if not dump_port:
            _logger.warning("No local dumper port found.")
            # Can't grab...

        else:
            # Grab from local host
            self._grab_directory("localhost", int(dump_port))

        # Register to isolate registration signals
        self._receiver.register_listener(SIGNAL_PREFIX_MATCH_ALL, self)
        self._receiver.register_listener(SIGNAL_ISOLATE_LOST, self)
