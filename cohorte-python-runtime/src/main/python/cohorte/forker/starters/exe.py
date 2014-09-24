#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Starts basic executables as isolates

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 1.0.0

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

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE modules
import cohorte.forker
import cohorte.forker.starters.common as common

# Pelix framework
from pelix.ipopo.decorators import ComponentFactory, Property, Provides, \
    Instantiate

# Standard library
import logging
import subprocess

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.forker.SERVICE_STARTER)
@Property('_kinds', cohorte.forker.PROP_STARTER_KINDS, 'exe')
@Instantiate('cohorte-starter-exe')
class ExeStarter(common.CommonStarter):
    """
    Isolate starter for simple executables
    """
    def _prepare_arguments(self, configuration):
        """
        Prepares arguments to run an executable

        :param configuration: An isolate configuration
        :return: A list of arguments, the first one being the name of the
                 executable
        """
        executable = configuration.get('executable')
        if not executable:
            raise ValueError("No 'executable' given in configuration of %s",
                             configuration['name'])

        # The executable is the first argument
        arguments = [executable]

        # Boot arguments (at boot level)
        for level in (configuration, configuration.get('boot', {})):
            boot_args = level.get('boot_args')
            if boot_args:
                arguments.extend(boot_args)

        # Executable arguments (at root level)
        app_args = configuration.get('arguments')
        if app_args:
            arguments.extend(app_args)

        return arguments


    def start(self, configuration, state_udpater_url):
        """
        Starts an isolate with the given configuration and its monitoring
        threads

        :param configuration: An isolate configuration
        :return: False in case of success, as it doesn't use the state updater
        :raise KeyError: A mandatory configuration option is missing
        :raise OSError: Error starting the isolate
        """
        uid = configuration['uid']

        # Prepare environment variables
        environment = self.setup_environment(configuration)

        # Prepare arguments
        arguments = self._prepare_arguments(configuration)

        # Prepare working directory
        working_directory = self.prepare_working_directory(configuration)

        # Check I/O watch thread flag
        io_watch = configuration.get('io_watch', True)

        # Start the process
        process = subprocess.Popen(arguments, executable=arguments[0],
                                   env=environment,
                                   cwd=working_directory,
                                   stdin=subprocess.PIPE,
                                   stdout=subprocess.PIPE,
                                   stderr=subprocess.STDOUT)

        # Store the isolate process information
        self._isolates[uid] = process

        # Start watching after the isolate
        self._watcher.watch(uid, process, io_watch)

        # Don't wait for the state to be updated
        return False
