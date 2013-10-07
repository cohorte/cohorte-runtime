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
@Requires('_parser', cohorte.composer.SERVICE_PARSER, optional=True)
@Requires('_distributor', cohorte.composer.SERVICE_DISTRIBUTOR_NODE, optional=True)
@Requires('_utils', SHELL_UTILS_SERVICE_SPEC)
@Provides(SHELL_COMMAND_SPEC)
@Instantiate('cohorte-composer-parser-shell')
class ParserCommands(object):
    """
    Signals shell commands
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._parser = None
        self._utils = None

        self.logger = logging.getLogger('composer-shell')

    def get_namespace(self):
        """
        Retrieves the name space of this command handler
        """
        return "parser"


    def get_methods(self):
        """
        Retrieves the list of tuples (command, method) for this command handler
        """
        return [("load", self.load_composition),
                ("read", self.read_file)]


    def read_file(self, io_handler, filename, base="conf"):
        """
        Reads a file
        """
        # Read the file content (dictionary)
        data = self._reader.load_file(filename, base)

        # Pretty print
        io_handler.write_line("{0}", json.dumps(data, sort_keys=True,
                                                indent='  ',
                                                separators=(',', ': ')))


    def load_composition(self, io_handler, filename, base="conf"):
        """
        Parses a composition
        """
        # Load the composition
        composition = self._parser.load(filename, base)

        # Distribute it
        distribution = self._distributor.distribute(composition)

        # Pretty print
        io_handler.write_line('{0}', pformat(distribution, indent=2))
