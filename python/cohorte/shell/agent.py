#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE shell agent commands

Provides commands to the Pelix shell to find other shells

:author: Thomas Calmant
:license: Apache Software License 2.0

..

    Copyright 2014 isandlaTech

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

# Standard library
import logging
import os

# Shell constants
from pelix.shell import SHELL_COMMAND_SPEC, SHELL_UTILS_SERVICE_SPEC, \
    REMOTE_SHELL_SPEC

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate, Property, Validate, Invalidate

# Pelix
from pelix.utilities import CountdownEvent
import pelix.http

# Herald
import herald
import herald.beans as beans

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# -----------------------------------------------------------------------------

_SUBJECT_PREFIX = "cohorte/shell/agent"
""" Common prefix to agent signals """

_SUBJECT_MATCH_ALL = "{0}/*".format(_SUBJECT_PREFIX)
""" Filter to match agent signals """

SUBJECT_GET_SHELLS = "{0}/get_shells".format(_SUBJECT_PREFIX)
""" Signal to request the ports to access remote shells """

SUBJECT_GET_PID = "{0}/get_pid".format(_SUBJECT_PREFIX)
""" Signal to request the isolate PID """

SUBJECT_GET_HTTP = "{0}/get_http".format(_SUBJECT_PREFIX)
""" Signal to request the ports to access HTTP services """


_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory("cohorte-shell-agent-commands-factory")
@Requires('_directory', herald.SERVICE_DIRECTORY)
@Requires('_herald', herald.SERVICE_HERALD)
@Requires('_remote_shell', REMOTE_SHELL_SPEC)
@Requires('_utils', SHELL_UTILS_SERVICE_SPEC)
@Provides((SHELL_COMMAND_SPEC, herald.SERVICE_LISTENER))
@Property('_filters', herald.PROP_FILTERS, _SUBJECT_MATCH_ALL)
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
        self._herald = None
        self._remote_shell = None
        self._utils = None

        # Bundle context
        self._context = None

        # Herald filter property
        self._filters = None

    @Validate
    def _validate(self, context):
        """
        Component validated
        """
        self._context = context

    @Invalidate
    def _invalidate(self, context):
        """
        Component invalidated
        """
        self._context = None

    @staticmethod
    def get_namespace():
        """
        Retrieves the name space of this command handler
        """
        return "shell"

    def get_methods(self):
        """
        Retrieves the list of tuples (command, method) for this command handler
        """
        return [("pids", self.pids),
                ("shells", self.shells),
                ("http", self.http)]

    def get_methods_names(self):
        """
        Retrieves the list of tuples (command, method name) for this command
        handler.
        """
        return [(command, method.__name__)
                for command, method in self.get_methods()]

    def herald_message(self, herald_svc, message):
        """
        Called by Herald when a message is received
        """
        subject = message.subject
        reply = None

        if subject == SUBJECT_GET_SHELLS:
            # Shell information requested
            reply = {"pelix": self._remote_shell.get_access()[1]}
        elif subject == SUBJECT_GET_PID:
            # Get the isolate PID
            reply = {"pid": os.getpid()}
        elif subject == SUBJECT_GET_HTTP:
            # Get the isolate HTTP port
            svc_ref = self._context.get_service_reference(
                pelix.http.HTTP_SERVICE)
            if svc_ref is not None:
                port = svc_ref.get_property(pelix.http.HTTP_SERVICE_PORT)
            else:
                port = -1

            reply = {"http.port": port}

        if reply is not None:
            herald_svc.reply(message, reply)

    def get_peers_uids(self, uid_or_name):
        """
        Returns the list of Peer UIDs matching the given UID or name.

        :param uid_or_name: The UID or Name of a peer
        :return: A list of UIDs matching the UID or name
        :raise KeyError: Unknown isolate (could be a group)
        """
        try:
            # Try with UID or name
            return self._directory.get_peers_for_name(uid_or_name)
        except KeyError:
            # Unknown isolate
            raise KeyError("No isolate matching name or UID {0}"
                           .format(uid_or_name))

    def __compute_targets(self, isolate):
        """
        Computes the group or the list of peers to send a message to, according
        to what has been given as shell argument

        :param isolate: The name given by the user
        :return: A (group, peers) tuple, one of which is None
        :raise KeyError: No matching peer found
        """
        # Prepare the targets
        group = None
        peers = None

        if not isolate:
            # Get all shells
            group = 'all'
        else:
            # Get shells of the given isolate(s)
            try:
                peers = self.get_peers_uids(isolate)
            except KeyError:
                # Not a Peer UID nor Name, try with a group
                try:
                    self._directory.get_peers_for_group(isolate)
                except KeyError:
                    # Not a group either
                    raise KeyError("Unknown isolate: {0}".format(isolate))
                else:
                    # Matches a group name
                    group = isolate

        return group, peers

    @staticmethod
    def __on_error(herald_svc, exception, failed_set, countdown):
        """

        :param herald_svc:
        :param exception:
        :param failed_set:
        :param countdown:
        :return:
        """
        target = exception.target
        if target is not None:
            try:
                if target.group is None:
                    if target.uid not in failed_set:
                        failed_set.add(target.uid)
                        countdown.step()
                else:
                    for target_uid in target.uids:
                        if target_uid not in failed_set:
                            failed_set.add(target_uid)
                            countdown.step()
            except ValueError:
                # We've gone too far
                _logger.debug("Got too many answers for a shell request")

    def pids(self, io_handler, isolate=None):
        """
        Prints the Process ID of the isolate(s)
        """
        try:
            # Prepare the targets
            group, peers = self.__compute_targets(isolate)
        except KeyError as ex:
            io_handler.write_line("{0}", ex)
            return

        # Prepare a count down event
        if group is not None:
            nb_peers = len(self._directory.get_peers_for_group(group))
            if nb_peers == 0:
                io_handler.write_line("No peer found in group '{0}'", group)
                return
            event = CountdownEvent(nb_peers)
        else:
            event = CountdownEvent(len(peers))

        # Prepare callback and errback
        succeeded = {}
        failed = set()

        def on_error(herald_svc, exception):
            """
            Failed to send a message
            """
            self.__on_error(herald_svc, exception, failed, event)

        def on_success(herald_svc, reply):
            """
            Got a reply for a message
            """
            # Reply content is a dictionary, extract PID
            step_up = reply.sender not in succeeded
            succeeded[reply.sender] = reply.content['pid']
            if step_up:
                event.step()

        # Send the message
        message = beans.Message(SUBJECT_GET_PID)
        if group is not None:
            self._herald.post_group(group, message, on_success, on_error)
        else:
            for uid in peers:
                self._herald.post(uid, message, on_success, on_error)

        # Wait for results (5 seconds max)
        event.wait(5)

        # Forget about the message
        self._herald.forget(message.uid)

        # Setup the headers
        headers = ('Name', 'UID', 'Node Name', 'Node UID', 'PID')

        # Compute the table content
        table = []
        for uid, pid in succeeded.items():
            # Add the line to the table
            try:
                peer = self._directory.get_peer(uid)
            except KeyError:
                # Unknown peer
                line = ("<unknown>", uid, "<unknown>", "<unknown>", pid)
            else:
                # Print known information
                line = (peer.name, uid, peer.node_name, peer.node_uid, pid)

            table.append(line)

        if table:
            # Sort values
            table.sort()

            # Print the table
            io_handler.write_line(self._utils.make_table(headers, table))

        if failed:
            io_handler.write_line("These isolates didn't respond:")
            for uid in failed:
                try:
                    name = self._directory.get_peer(uid).name
                except KeyError:
                    name = "<unknown>"

                io_handler.write_line("{0} - {1}", name, uid)

    def shells(self, io_handler, isolate=None, kind=None):
        """
        Prints the port(s) to access the isolate remote shell(s)
        """
        try:
            # Prepare the targets
            group, peers = self.__compute_targets(isolate)
        except KeyError as ex:
            io_handler.write_line("{0}", ex)
            return

        # Prepare a count down event
        if group is not None:
            nb_peers = len(self._directory.get_peers_for_group(group))
            if nb_peers == 0:
                io_handler.write_line("No peer found in group '{0}'", group)
                return
            event = CountdownEvent(nb_peers)
        else:
            event = CountdownEvent(len(peers))

        # Prepare callback and errback
        succeeded = {}
        failed = set()

        def on_error(herald_svc, exception):
            """
            Failed to send a message
            """
            self.__on_error(herald_svc, exception, failed, event)

        def on_success(herald_svc, reply):
            """
            Got a reply for a message
            """
            try:
                # Already known peer (multiple answers)
                succeeded[reply.sender].update(reply.content)
            except KeyError:
                # A new peer answered
                succeeded[reply.sender] = reply.content.copy()
                event.step()

        # Send the message
        message = beans.Message(SUBJECT_GET_SHELLS)
        if group is not None:
            self._herald.post_group(group, message, on_success, on_error)
        else:
            for uid in peers:
                self._herald.post(uid, message, on_success, on_error)

        # Wait for results (5 seconds max)
        event.wait(5)

        # Forget about the message
        self._herald.forget(message.uid)

        # Compute the shell names
        shell_names = set()
        for uid, shells in succeeded.items():
            shell_names.update(shell_name for shell_name in shells)

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
        for uid, shells in succeeded.items():
            # First columns: Name, UID, Node Name, Node UID
            try:
                # Known peer
                peer = self._directory.get_peer(uid)
                line = [peer.name, uid, peer.node_name, peer.node_uid]
            except KeyError:
                # Unknown peer
                line = ["<unknown>", uid, "<unknown>", "<unknown>"]

            shell_ports = {}
            # Find the shell ports
            for shell_name in shell_names:
                try:
                    shell_ports[shell_name] = shells[shell_name]
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
                                      self._directory.get_peer(uid).name,
                                      uid)

    def http(self, io_handler, isolate=None):
        """
        Prints the port(s) to access the isolates HTTP service
        """
        try:
            # Prepare the targets
            group, peers = self.__compute_targets(isolate)
        except KeyError as ex:
            io_handler.write_line("{0}", ex)
            return

        # Prepare a count down event
        if group is not None:
            nb_peers = len(self._directory.get_peers_for_group(group))
            if nb_peers == 0:
                io_handler.write_line("No peer found in group '{0}'", group)
                return
            event = CountdownEvent(nb_peers)
        else:
            event = CountdownEvent(len(peers))

        # Prepare callback and errback
        succeeded = {}
        failed = set()

        def on_error(herald_svc, exception):
            """
            Failed to send a message
            """
            self.__on_error(herald_svc, exception, failed, event)

        def on_success(herald_svc, reply):
            """
            Got a reply for a message
            """
            # Reply content is a dictionary, extract PID
            step_up = reply.sender not in succeeded
            succeeded[reply.sender] = reply.content['http.port']
            if step_up:
                event.step()

        # Send the message
        message = beans.Message(SUBJECT_GET_HTTP)
        if group is not None:
            self._herald.post_group(group, message, on_success, on_error)
        else:
            for uid in peers:
                self._herald.post(uid, message, on_success, on_error)

        # Wait for results (5 seconds max)
        event.wait(5)

        # Forget about the message
        self._herald.forget(message.uid)

        # Setup the headers
        headers = ('Name', 'UID', 'Node Name', 'Node UID', 'HTTP')

        # Compute the table content
        table = []
        for uid, http_port in succeeded.items():
            # Add the line to the table
            try:
                peer = self._directory.get_peer(uid)
            except KeyError:
                # Unknown peer
                line = ("<unknown>", uid, "<unknown>", "<unknown>", http_port)
            else:
                # Print known information
                line = (peer.name, uid, peer.node_name, peer.node_uid,
                        http_port)

            table.append(line)

        if table:
            # Sort values
            table.sort()

            # Print the table
            io_handler.write_line(self._utils.make_table(headers, table))

        if failed:
            io_handler.write_line("These isolates didn't respond:")
            for uid in failed:
                try:
                    name = self._directory.get_peer(uid).name
                except KeyError:
                    name = "<unknown>"

                io_handler.write_line("{0} - {1}", name, uid)
