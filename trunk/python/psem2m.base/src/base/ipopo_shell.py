#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
iPOPO shell commands

:author: Thomas Calmant
:copyright: Copyright 2012, isandlaTech
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

# ------------------------------------------------------------------------------

from pelix.ipopo.constants import IPOPO_SERVICE_SPECIFICATION
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

import logging

# ------------------------------------------------------------------------------

SHELL_COMMAND_SPEC = "ipopo.shell.command"
SHELL_UTILS_SERVICE_SPEC = "pelix.shell.utilities"

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

def ipopo_state_to_str(state):
    """
    Converts the state of a component instance to its string representation
    
    :param state: The state of an iPOPO component
    :return: A string representation of the state
    """
    ipopo_states = {0: "INVALID",
                    1:"VALID",
                    2:"KILLED",
                    3:"VALIDATING"
    }

    return ipopo_states.get(state, "Unknown state (%d)".format(state))

# ------------------------------------------------------------------------------

@ComponentFactory("ipopo-shell-commands-factory")
@Requires("_ipopo", IPOPO_SERVICE_SPECIFICATION)
@Requires("_utils", SHELL_UTILS_SERVICE_SPEC)
@Provides(SHELL_COMMAND_SPEC)
@Instantiate("ipopo-shell-commands")
class IPopoCommands(object):
    """
    iPOPO shell commands
    """
    def __init__(self):
        """
        Sets up the object
        """
        self._ipopo = None
        self._utils = None


    def get_namespace(self):
        """
        Retrieves the name space of this command handler
        """
        return "ipopo"


    def get_methods(self):
        """
        Retrieves the list of tuples (command, method) for this command handler
        """
        return [("factories", self.list_factories),
                ("instances", self.list_instances)]


    def get_methods_names(self):
        """
        Retrieves the list of tuples (command, method name) for this command
        handler.
        """
        result = []
        for command, method in self.get_methods():
            result.append((command, method.__name__))

        return result


    def list_factories(self, stdin, stdout):
        """
        Lists the available iPOPO component factories
        """
        header = ('Factory', 'Bundle')
        lines = [(name, self._ipopo.get_factory_bundle(name))
                 for name in self._ipopo.get_factories()]
        stdout.write(self._utils.make_table(header, lines))


    def list_instances(self, stdin, stdout):
        """
        Lists the active iPOPO component instances
        """
        headers = ('Name', 'Factory', 'State')
        lines = [(name, factory, ipopo_state_to_str(state))
                 for name, factory, state in self._ipopo.get_instances()]

        stdout.write(self._utils.make_table(headers, lines))


