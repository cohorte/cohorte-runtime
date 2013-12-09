#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE shell agent commands

Provides commands to the Pelix shell to find other shells

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
from pelix.shell import SHELL_COMMAND_SPEC, SHELL_UTILS_SERVICE_SPEC, \
    REMOTE_SHELL_SPEC

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate, Validate, Invalidate

# Standard library
import os

# ------------------------------------------------------------------------------

_SIGNALS_PREFIX = "/cohorte/shell/agent"
""" Common prefix to agent signals """

_SIGNALS_MATCH_ALL = "{0}/*".format(_SIGNALS_PREFIX)
""" Filter to match agent signals """

SIGNAL_GET_SHELLS = "{0}/get_shells".format(_SIGNALS_PREFIX)
""" Signal to request the ports to access remote shellS """

SIGNAL_GET_PID = "{0}/get_pid".format(_SIGNALS_PREFIX)
""" Signal to request the isolate PID """

REMOTE_SHELL_FACTORY = "ipopo-remote-shell-factory"
""" Name of the iPOPO remote shell factory """

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-shell-agent-commands-factory")
@Requires('_directory', cohorte.SERVICE_SIGNALS_DIRECTORY)
@Requires('_receiver', cohorte.SERVICE_SIGNALS_RECEIVER)
@Requires('_sender', cohorte.SERVICE_SIGNALS_SENDER)
@Requires('_remote_shell', REMOTE_SHELL_SPEC)
@Requires('_utils', SHELL_UTILS_SERVICE_SPEC)
@Provides(SHELL_COMMAND_SPEC)
@Instantiate("cohorte-shell-agent-commands")
class ShellAgentCommands(object):
    """
    Signals shell commands
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._directory = None
        self._receiver = None
        self._sender = None
        self._remote_shell = None
        self._utils = None


    def get_namespace(self):
        """
        Retrieves the name space of this command handler
        """
        return "shell"


    def get_methods(self):
        """
        Retrieves the list of tuples (command, method) for this command handler
        """
        return [("pids", self.pids),
                ("shells", self.shells)]


    def get_methods_names(self):
        """
        Retrieves the list of tuples (command, method name) for this command
        handler.
        """
        return [(command, method.__name__)
                for command, method in self.get_methods()]


    def handle_received_signal(self, name, signal_data):
        """
        Called when a remote services signal is received

        :param name: Signal name
        :param signal_data: Signal content
        """
        if name == SIGNAL_GET_SHELLS:
            # Shell information requested
            return {"pelix": self._remote_shell.get_access()[1]}

        elif name == SIGNAL_GET_PID:
            # Get the isolate PID
            return {"pid": os.getpid()}


    def pids(self, io_handler, isolate=None):
        """
        Prints the Process ID of the isolate(s)
        """
        # Prepare the signal targets
        params = {}
        if not isolate:
            # Get all shells
            params['dir_group'] = cohorte.signals.GROUP_ALL

        else:
            # Get shells of the given isolate(s)
            try:
                params['isolates'] = self._directory.get_uids(isolate)

            except KeyError as ex:
                io_handler.write_line("Error: {0}", ex)
                return False

        # Send the signal
        succeeded, failed = self._sender.send(SIGNAL_GET_PID, None, **params)

        # Setup the headers
        headers = ('Name', 'UID', 'Node Name', 'Node UID', 'PID')

        # Compute the table content
        table = []
        for uid, response in succeeded.items():
            # Get the first valid result
            if response is None:
                failed.append(uid)

            else:
                for result in response['results']:
                    try:
                        pid = result['pid']
                        break

                    except KeyError:
                        pass

                else:
                    pid = "<unknown>"

            # Add the line to the table
            node_uid = self._directory.get_isolate_node(uid)
            line = (self._directory.get_isolate_name(uid), uid,
                    self._directory.get_node_name(node_uid), node_uid, pid)
            table.append(line)

        if table:
            # Sort values
            table.sort()

            # Print the table
            io_handler.write_line(self._utils.make_table(headers, table))

        if failed:
            io_handler.write_line("These isolates didn't respond:")
            for uid in failed:
                io_handler.write_line("{0} - {1}",
                                      self._directory.get_isolate_name(uid),
                                      uid)


    def shells(self, io_handler, isolate=None, kind=None):
        """
        Prints the port(s) to access the isolate remote shell(s)
        """
        # Prepare the signal targets
        params = {}
        if not isolate:
            # Get all shells
            params['dir_group'] = cohorte.signals.GROUP_ALL

        else:
            # Get shells of the given isolate(s)
            try:
                params['isolates'] = self._directory.get_uids(isolate)

            except KeyError as ex:
                io_handler.write_line("Error: {0}", ex)

        # Send the signal
        succeeded, failed = self._sender.send(SIGNAL_GET_SHELLS, None, **params)

        # Compute the shell names
        shell_names = set()
        for uid, isolate_response in succeeded.items():
            if isolate_response is None:
                # Unreadable answer
                failed.append(uid)
            else:
                shell_names.update(shell_name
                                   for result in isolate_response['results']
                                   for shell_name in result)

        # Sort shell names, using a list
        shell_names = list(shell_names)
        shell_names.sort()

        if kind:
            # Filter on shell names given
            if kind in shell_names:
                shell_names = [kind]

            else:
                io_handler.write_line("No isolate with shell: {0}", kind)
                return

        # Compute the table content
        table = []
        for uid, response in succeeded.items():
            # First columns: Name, UID, Node Name, Node UID
            node_uid = self._directory.get_isolate_node(uid)
            line = [self._directory.get_isolate_name(uid), uid,
                    self._directory.get_node_name(node_uid), node_uid]

            shell_ports = {}
            for result in response['results']:
                # Find the shell ports
                for shell_name in shell_names:
                    try:
                        shell_ports[shell_name] = result[shell_name]
                    except KeyError:
                        pass

            # Make the lines
            for shell_name in shell_names:
                line.append(shell_ports.get(shell_name, 'n/a'))

            # Add the line to the table
            table.append(line)

        if table:
            # Setup the headers
            headers = ['Name', 'UID', 'Node Name', 'Node UID'] + shell_names

            # Sort values
            table.sort()

            # Print the table
            io_handler.write_line(self._utils.make_table(headers, table))

        if failed:
            io_handler.write_line("These isolates didn't respond:")
            for uid in failed:
                io_handler.write_line("{0} - {1}",
                                      self._directory.get_isolate_name(uid),
                                      uid)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._receiver.register_listener(_SIGNALS_MATCH_ALL, self)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._receiver.unregister_listener(_SIGNALS_MATCH_ALL, self)
