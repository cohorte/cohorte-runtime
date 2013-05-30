#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE signals shell commands

Provides commands to the Pelix shell to work with the signals service

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 0.1
:status: Alpha
"""

# Module version
__version_info__ = (0, 1, 0)
__version__ = ".".join(map(str, __version_info__))

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
        dir [<prefix>] - Lists the known isolates
        """
        headers = ('Name', 'UID', 'Node', 'Host', 'Port')
        content = []

        # Grab data
        for isolate_uid in self._directory.get_all_isolates(prefix, True):
            name = self._directory.get_isolate_name(isolate_uid)
            node = self._directory.get_isolate_node(isolate_uid)
            host, port = self._directory.get_isolate_access(isolate_uid)

            content.append((name, isolate_uid, node, host, port))

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
        group <group> - Lists the isolates from the given group
        """
        headers = ('Name', 'UID', 'Node', 'Host', 'Port')
        content = []

        # Grab data
        accesses = self._directory.get_computed_group_accesses(group)
        if not accesses:
            # No match found
            io_handler.write_line("No isolate found in group {0}", group)
            return

        for isolate_uid, access in accesses.items():
            name = self._directory.get_isolate_name(isolate_uid)
            node = self._directory.get_isolate_node(isolate_uid)
            host, port = access

            content.append((name, isolate_uid, node, host, port))

        # Sort the list
        content.sort()

        # Print the table
        io_handler.write_line(self._utils.make_table(headers, content))


    def groups(self, io_handler, prefix=None):
        """
        groups [<prefix>] - Lists the available isolate groups
        """
        if prefix is not None:
            prefix = prefix.upper()

        for member in dir(cohorte.signals):
            if member.startswith("GROUP_"):
                group = getattr(cohorte.signals, member)
                if prefix is None or group.startswith(prefix):
                    io_handler.write_line(group)


    def local(self, io_handler):
        """
        local - Prints information about this isolate
        """
        uid = self._directory.get_isolate_uid()

        io_handler.write_line("UID:\t{0}", uid)
        io_handler.write_line("Name:\t{0}",
                              self._directory.get_isolate_name(uid))
        io_handler.write_line("Node:\t{0}",
                              self._directory.get_isolate_node(uid))

        host, port = self._directory.get_isolate_access(uid)
        io_handler.write_line("Access:\t{0} - {1}", host, port)


    def named(self, io_handler, name=None):
        """
        named <name> - Lists the isolates having the exact given name
        """
        headers = ('Name', 'UID', 'Node', 'Host', 'Port')
        content = []

        # Grab data
        for isolate_uid in self._directory.get_name_uids(name):
            name = self._directory.get_isolate_name(isolate_uid)
            node = self._directory.get_isolate_node(isolate_uid)
            host, port = self._directory.get_isolate_access(isolate_uid)

            content.append((name, isolate_uid, node, host, port))

        if not content:
            # No match found
            io_handler.write_line("No matching isolate.")
            return

        # Sort the list
        content.sort()

        # Print the table
        io_handler.write_line(self._utils.make_table(headers, content))
