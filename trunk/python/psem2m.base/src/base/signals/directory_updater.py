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

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-signals-directory-updater-factory")
@Instantiate("psem2m-signals-directory-updater")
@Requires("_directory", "org.psem2m.signals.ISignalDirectory")
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
        self._receiver = None
        self._sender = None


    def _grab_directory(self, port):
        """
        Sends a directory dump signal to the given port on local host.
        
        :param port: The signal receiver port
        """
        sig_results = self._sender.send_to(SIGNAL_DUMP, None, "localhost", port)
        if not sig_results or not sig_results["results"]:
            _logger.warning("Nothing returned by the directory dumper")
            return

        # Get the first result only
        results = sig_results["results"]
        if len(results) > 1:
            _logger.warning("More than one result found. Ignoring others")

        result = results[0]

        # 1. Setup nodes hosts
        for node, host in result["nodes_host"].items():
            self._directory.set_node_address(node, host)

        # 2. Prepare isolates information
        new_isolates = {}

        for isolate_id, access in result["accesses"].items():
            # Access URL
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
            self._directory.register_isolate(isolate_id, info["node"],
                                             info["port"], info["groups"])

        # Now, we can send our registration signal
        self._send_registration()


    def _register_isolate(self, signal_data):
        """
        Registers an isolate according to the given map 
        """
        content = signal_data["signalContent"]

        # 1. Update the node host
        self._directory.set_node_address(content["node"],
                                         signal_data["senderAddress"])

        # 2. Register the isolate
        self._directory.register_isolate(content["id"], content["node"],
                                         content["port"], content["groups"])


    def _send_registration(self):
        """
        Sends the registration signal to all known isolates
        """
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

        content = {"id": isolate_id,
                   "node": self._directory.get_local_node(),
                   "port": self._receiver.get_access_info()[1],
                   "groups": groups}

        self._sender.fire(SIGNAL_REGISTER, content, groups=["ALL"])


    def handle_received_signal(self, name, signal_data):
        """
        Called when a remote services signal is received
        
        :param name: Signal name
        :param signal_data: Signal content
        """
        if name == SIGNAL_DUMP:
            # Dump the directory
            return self._directory.dump()

        elif name == SIGNAL_REGISTER:
            # Isolate registration
            self._register_isolate(signal_data)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister to isolate registration signals
        self._receiver.unregister_listener(SIGNAL_PREFIX_MATCH_ALL, self)


    @Validate
    def validate(self, context):
        """
        Component validate
        """
        # Get the local dumper port
        dump_port = context.get_property("psem2m.directory.dumper.port")
        if not dump_port:
            _logger.warning("No local dumper port found.")

        else:
            self._grab_directory(int(dump_port))

        # Register to isolate registration signals
        self._receiver.register_listener(SIGNAL_PREFIX_MATCH_ALL, self)
