#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE signals shell commands

Provides commands to the Pelix shell to work with the signals service

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 0.2
:status: Alpha
"""

# Module version
__version_info__ = (0, 2, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# -----------------------------------------------------------------------------

# Cohorte
import cohorte.signals

# Shell constants
from pelix.shell import SHELL_COMMAND_SPEC, SHELL_UTILS_SERVICE_SPEC

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-signals-shell-commands-factory")
@Requires("_directory", cohorte.SERVICE_SIGNALS_DIRECTORY)
@Requires("_utils", SHELL_UTILS_SERVICE_SPEC)
@Provides(SHELL_COMMAND_SPEC)
@Instantiate("cohorte-signals-shell-commands")
class SignalsCommands(object):
    """
    Signals shell commands
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._directory = None
        self._utils = None


    def get_namespace(self):
        """
        Retrieves the name space of this command handler
        """
        return "signals"


    def get_methods(self):
        """
        Retrieves the list of tuples (command, method) for this command handler
        """
        return [("dir", self.dir),
                ("group", self.group),
                ("groups", self.groups),
                ("host", self.host),
                ("local", self.local),
                ("named", self.named)]


    def get_methods_names(self):
        """
        Retrieves the list of tuples (command, method name) for this command
        handler.
        """
        return [(command, method.__name__)
                for command, method in self.get_methods()]


    def dir(self, io_handler, prefix=None):
        """
        Lists the known isolates, filtered by the given name prefix or UID
        """
        headers = ('Name', 'UID', 'Node Name', 'Node UID', 'Host', 'Port')
        content = []

        # Grab data
        for isolate_uid in self._directory.get_all_isolates(prefix, True):
            isolate_name = self._directory.get_isolate_name(isolate_uid)
            node_uid = self._directory.get_isolate_node(isolate_uid)
            node_name = self._directory.get_node_name(node_uid)
            host, port = self._directory.get_isolate_access(isolate_uid)

            content.append((isolate_name, isolate_uid, node_name, node_uid,
                            host, port))

        if not content:
            # No match found
            io_handler.write_line("No matching isolate.")
            return

        # Sort the list
        content.sort()

        # Print the table
        io_handler.write_line(self._utils.make_table(headers, content))


    def group(self, io_handler, group):
        """
        Lists the isolates from the given group
        """
        headers = ('Name', 'UID', 'Node Name', 'Node UID', 'Host', 'Port')
        content = []

        # Grab data
        accesses = self._directory.get_computed_group_accesses(group)
        if not accesses:
            # No match found
            io_handler.write_line("No isolate found in group {0}", group)
            return

        for isolate_uid, access in accesses.items():
            isolate_name = self._directory.get_isolate_name(isolate_uid)
            node_uid = self._directory.get_isolate_node(isolate_uid)
            node_name = self._directory.get_node_name(node_uid)
            host, port = access

            content.append((isolate_name, isolate_uid, node_name, node_uid,
                            host, port))

        # Sort the list
        content.sort()

        # Print the table
        io_handler.write_line(self._utils.make_table(headers, content))


    def groups(self, io_handler, prefix=None):
        """
        Lists the available isolate groups
        """
        if prefix is not None:
            prefix = prefix.upper()

        for member in dir(cohorte.signals):
            if member.startswith("GROUP_"):
                group = getattr(cohorte.signals, member)
                if prefix is None or group.startswith(prefix):
                    io_handler.write_line(group)


    def host(self, io_handler, prefix=None):
        """
        Prints the known host address/name of known nodes
        """
        headers = ('Node Name', 'Node UID', 'Host')
        content = []

        # Get data
        node_uids = self._directory.get_all_nodes()
        if not node_uids:
            # Nothing to show
            io_handler.write_line("No known node.")
            return

        for node_uid in node_uids:
            node_name = self._directory.get_node_name(node_uid)
            if not prefix or node_name.startswith(prefix) or node_uid == prefix:
                content.append((node_name, node_uid,
                                self._directory.get_host_for_node(node_uid)))

        if not content:
            io_handler.write_line("No matching node")
            return

        # Sort the list
        content.sort()

        # Print the table
        io_handler.write_line(self._utils.make_table(headers, content))


    def local(self, io_handler):
        """
        Prints information about this isolate
        """
        uid = self._directory.get_isolate_uid()

        # Isolate
        io_handler.write_line("Isolate UID.: {0}", uid)
        io_handler.write_line("Isolate Name: {0}",
                              self._directory.get_isolate_name(uid))

        # Node
        node_uid = self._directory.get_local_node()
        io_handler.write_line("Node UID....: {0}", node_uid)
        io_handler.write_line("Node Name...: {0}",
                              self._directory.get_node_name(node_uid))

        # Signals Access
        host, port = self._directory.get_isolate_access(uid)
        io_handler.write_line("HTTP Access.: {0} : {1}", host, port)


    def named(self, io_handler, name=None):
        """
        Lists the isolates having the exact given name, or the same name as this
        isolate.
        """
        headers = ('Name', 'UID', 'Node Name', 'Node UID', 'Host', 'Port')
        content = []

        if name is None:
            name = self._directory.get_isolate_name()

        # Grab data
        for isolate_uid in self._directory.get_name_uids(name):
            isolate_name = self._directory.get_isolate_name(isolate_uid)
            node_uid = self._directory.get_isolate_node(isolate_uid)
            node_name = self._directory.get_node_name(node_uid)
            host, port = self._directory.get_isolate_access(isolate_uid)

            content.append((isolate_name, isolate_uid, node_name, node_uid,
                            host, port))

        if not content:
            # No match found
            io_handler.write_line("No matching isolate.")
            return

        # Sort the list
        content.sort()

        # Print the table
        io_handler.write_line(self._utils.make_table(headers, content))
