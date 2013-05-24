#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
iPOPO shell commands

Provides commands to the Pelix shell to work with the EventAdmin service

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 0.1
:status: Alpha

..

    This file is part of iPOPO.

    iPOPO is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    iPOPO is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with iPOPO. If not, see <http://www.gnu.org/licenses/>.
"""

# Module version
__version_info__ = (0, 1, 0)
__version__ = ".".join(map(str, __version_info__))

# Documentation strings format
__docformat__ = "restructuredtext en"

# -----------------------------------------------------------------------------

# Cohorte
import cohorte

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
        return [("dir", self.dir)]


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

        # Sort the list
        content.sort()

        # Print the table
        io_handler.write_line(self._utils.make_table(headers, content))
