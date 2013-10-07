#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Shell commands to debug the composer v3

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 3.0.0

..

    This file is part of Cohorte.

    Cohorte is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cohorte is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cohorte. If not, see <http://www.gnu.org/licenses/>.
"""

# Module version
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

# Composer
import cohorte.composer

# Shell constants
from pelix.shell import SHELL_COMMAND_SPEC, SHELL_UTILS_SERVICE_SPEC

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# Standard library
from pprint import pformat
import logging
import json

# ------------------------------------------------------------------------------

@ComponentFactory()
@Requires('_reader', cohorte.SERVICE_FILE_READER)
@Requires('_parser', cohorte.composer.SERVICE_PARSER,
          optional=True)
@Requires('_distributor', cohorte.composer.SERVICE_DISTRIBUTOR_NODE,
          optional=True)
@Requires('_composer', cohorte.composer.SERVICE_COMPOSER_TOP,
          optional=True)
@Requires('_status', cohorte.composer.SERVICE_STATUS_TOP)
@Requires('_utils', SHELL_UTILS_SERVICE_SPEC)
@Provides(SHELL_COMMAND_SPEC)
@Instantiate('cohorte-composer-shell-debug')
class ParserCommands(object):
    """
    Signals shell commands
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._reader = None
        self._utils = None
        self._parser = None
        self._composer = None
        self._distributor = None
        self._status = None

        self.logger = logging.getLogger('composer-shell')


    def get_namespace(self):
        """
        Retrieves the name space of this command handler
        """
        return "composer"


    def get_methods(self):
        """
        Retrieves the list of tuples (command, method) for this command handler
        """
        return [("distribute", self.distribute_composition),
                ("read", self.read_file),
                ('dump', self.dump_status),
                ('inst', self.instantiate_composition)]


    def read_file(self, io_handler, filename="autorun_conf.js", base="conf"):
        """
        Reads a file
        """
        if self._reader is None:
            io_handler.write_line("No file reader.")
            return

        # Read the file content (dictionary)
        data = self._reader.load_file(filename, base)

        # Pretty print
        io_handler.write_line("{0}", json.dumps(data, sort_keys=True,
                                                indent='  ',
                                                separators=(',', ': ')))


    def distribute_composition(self, io_handler,
                               filename="autorun_conf.js", base="conf"):
        """
        Parses a composition and computes its node distribution
        """
        if self._parser is None:
            io_handler.write_line("No parser found.")
            return

        # Load the composition
        composition = self._parser.load(filename, base)

        if self._distributor is None:
            io_handler.write_line("No distributor found.")
            io_handler.write_line("Composition: {0}", str(composition))
            return

        # Distribute it
        distribution = self._distributor.distribute(composition)

        # Pretty print
        io_handler.write_line('{0}', pformat(distribution, indent=2))


    def instantiate_composition(self, io_handler,
                                filename="autorun_conf.js", base="conf"):
        """
        Instantiates the given composition
        """
        if self._parser is None:
            io_handler.write_line("No parser found.")
            return

        # Load the composition
        composition = self._parser.load(filename, base)

        # Tell the top composer to work
        if self._composer is None:
            io_handler.write_line("No composer found.")
            return

        uid = self._composer.start(composition)
        io_handler.write_line("Started composition: {0} -> {1}",
                              composition.name, uid)


    def dump_status(self, io_handler, node=None):
        """
        Dumps the content of status
        """
        if self._status is None:
            io_handler.write_line("No status found.")
            return

        if not node:
            io_handler.write_line("{0}", self._status.dump())

        else:
            io_handler.write_line("{0}",
                          pformat(self._status.get_components_for_node(node)))
