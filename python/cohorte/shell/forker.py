#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE forker shell commands

Provides commands to the Pelix shell to work with the forker

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 0.2

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

# Module version
__version_info__ = (0, 2, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# -----------------------------------------------------------------------------

# Cohorte
import cohorte
import herald

# Shell constants
from pelix.shell import SHELL_COMMAND_SPEC

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# ------------------------------------------------------------------------------


@ComponentFactory("cohorte-forker-shell-commands-factory")
@Requires("_directory", herald.SERVICE_DIRECTORY)
@Requires("_forker", cohorte.SERVICE_FORKER)
@Provides(SHELL_COMMAND_SPEC)
@Instantiate("cohorte-forker-shell-commands")
class ForkerCommands(object):
    """
    Forker shell commands
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._directory = None
        self._forker = None

    def get_namespace(self):
        """
        Retrieves the name space of this command handler
        """
        return "forker"

    def get_methods(self):
        """
        Retrieves the list of tuples (command, method) for this command handler
        """
        return [("stop", self.stop_isolate),
                ("ping", self.ping)]

    def get_methods_names(self):
        """
        Retrieves the list of tuples (command, method name) for this command
        handler.
        """
        return [(command, method.__name__)
                for command, method in self.get_methods()]

    def __ping_to_str(self, ping_result):
        """
        Converts a ping() result (integer) into a string

        :param ping_result: A ping result
        :return: A string
        """
        if ping_result == 0:
            return "ALIVE"
        elif ping_result == 1:
            return "DEAD"
        elif ping_result == 2:
            return "STUCK"
        else:
            return "<UNKNOWN:{0}>".format(ping_result)

    def ping(self, io_handler, isolate=None):
        """
        Checks if the given isolate (name or UID) is alive
        """
        try:
            for peer in self._directory.get_peers_for_name(isolate):
                uid = peer.uid
                io_handler.write_line_no_feed("{0}...", uid)
                result = self._forker.ping(uid)
                io_handler.write_line("\r{2} - {0} ({1})", uid, peer.name,
                                      self.__ping_to_str(result))
        except KeyError as ex:
            io_handler.write_line("Error: {0}", ex)

    def stop_isolate(self, io_handler, isolate):
        """
        Stops the given isolate (name or UID)
        """
        try:
            for uid in self._directory.get_uids_for_name(isolate):
                io_handler.write_line("Stopping {0}...", uid)
                self._forker.stop_isolate(uid)
        except KeyError as ex:
            io_handler.write_line("Error: {0}", ex)
