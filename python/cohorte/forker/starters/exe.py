#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Starts basic executable file as isolates

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 1.1.0

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
import subprocess

# Pelix framework
from pelix.ipopo.decorators import ComponentFactory, Property, Provides, \
    Instantiate

# COHORTE modules
import cohorte.forker
import cohorte.forker.starters.common as common

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

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
    @staticmethod
    def _prepare_arguments(configuration):
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
        name = configuration.get('name', '<no-name>')

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
        self._watcher.watch(uid, name, process, io_watch)

        # Don't wait for the state to be updated
        return False
