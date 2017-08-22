#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Common code for isolate starters

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
import cohorte
import cohorte.forker
import cohorte.utils
import os
from pelix.ipopo.decorators import Requires, Validate, Invalidate


try:
    # Python 3
    # pylint: disable=F0401,E0611
    from urllib.parse import quote
except ImportError:
    # Python 2
    # pylint: disable=F0401,E0611
    from urllib import quote

# Pelix framework

# COHORTE modules

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

@Requires('_environment', cohorte.forker.SERVICE_ENV_STARTER)
@Requires('_watcher', cohorte.forker.SERVICE_WATCHER)
class CommonStarter(object):
    """
    Common code for starters
    """
    def __init__(self):
        """
        Sets up members
        """
        # isolate environment 
        self._environment = None
        
        # Isolate watcher
        self._watcher = None

        # Bundle context
        self._context = None

        # Utility methods
        self._utils = None

        # Isolate UID -> process information
        self._isolates = {}

    @Validate
    def _validate(self, context):
        """
        Component validated
        """
        # Store the bundle context
        self._context = context

        # Get OS utility methods
        self._utils = cohorte.utils.get_os_utils()

    @Invalidate
    def _invalidate(self, context):
        """
        Component invalidated
        """
        self._context = None
        self._utils = None

    def uids(self):
        """
        Returns the list of UID of the isolates started by this component

        :return: A list of isolate UIDs
        """
        return list(self._isolates.keys())

    def ping(self, uid):
        """
        Pings the isolate with the given UID

        :param uid: The UID if an isolate
        """
        return self._utils.is_process_running(self._isolates[uid].pid)

    def kill(self, uid):
        """
        Kills the given isolate

        :param uid: The UID if an isolate
        :raise KeyError: Unknown UID
        :raise OSError: Error killing the process
        """
        process = self._isolates.pop(uid)
        if process.poll() is None:
            process.terminate()

    def stop(self, uid):
        """
        Stops the given isolate

        :param uid: The UID if an isolate
        """
        self.terminate(uid)

    def terminate(self, uid):
        """
        Softly terminates the given isolate

        :param uid: The UID of an isolate
        :raise KeyError: Unknown UID
        """
        try:
            self.kill(uid)
        except OSError:
            # Ignore errors
            pass

    @staticmethod
    def normalize_environment(environment):
        """
        Ensures that the environment dictionary only contains strings.

        :param environment: The environment dictionary (modified in-place)
        :return: The environment dictionary
        """
        for key in environment:
            value = environment[key]
            if value is None:
                environment[key] = ''
            elif not isinstance(value, str):
                environment[key] = str(value)

        return environment

    def setup_environment(self, configuration):
        """
        Sets up an environment dictionary. Uses the 'environment' entry from
        the configuration dictionary.

        :param configuration: An isolate configuration
        :return: A new environment dictionary
        """
        # Process environment
        env = os.environ.copy()

        # add environment variable 
        env_starter = self._environment.get_envs()
        if env_starter != None:
            env.update(env_starter)

        # Use configuration environment variables
        config_env = configuration.get('environment')
        if config_env:
            env.update(config_env)

        # Add Cohorte variables
        # ... directories
        env[cohorte.ENV_HOME] = self._context.get_property(cohorte.PROP_HOME)
        env[cohorte.ENV_BASE] = self._context.get_property(cohorte.PROP_BASE)

        # ... isolate
        env[cohorte.ENV_UID] = configuration['uid']
        env[cohorte.ENV_NAME] = configuration['name']

        # ... node
        env[cohorte.ENV_NODE_UID] = configuration['node_uid']
        env[cohorte.ENV_NODE_NAME] = configuration['node_name']

        # Normalize environment
        self.normalize_environment(env)
        return env

    def prepare_working_directory(self, configuration):
        """
        Prepares the working directory for the given isolate configuration.
        Uses the 'working_directory' configuration entry, if present, or
        creates a new folder in the base directory.

        :param configuration: An isolate configuration
        :return: A valid configuration directory
        """
        # The working directory can be specified in the configuration
        working_dir = configuration.get('working_directory')
        if working_dir:
            # Ensure the whole path is created
            if not os.path.exists(working_dir):
                os.makedirs(working_dir, exist_ok=True)

            # Prepare folders
            return working_dir
        else:
            # Prepare a specific working directory
            uid = configuration['uid']
            name = configuration['name']

            # Get the base directory
            base = self._context.get_property(cohorte.PROP_BASE)

            # Escape the name
            name = quote(name)

            # Compute the path (1st step)
            path = os.path.join(base, 'var', name)

            # Compute the instance index
            index = 0
            if os.path.exists(path):
                # The path already exists, get the maximum folder index value
                max_index = 0
                for entry in os.listdir(path):
                    if os.path.isdir(os.path.join(path, entry)):
                        try:
                            dir_index = int(entry[:entry.index('-')])
                            if dir_index > max_index:
                                max_index = dir_index

                        except ValueError:
                            # No '-' in the name or not an integer
                            pass

                index = max_index + 1

            # Set the folder name (2nd step)
            path = os.path.join(path, '{index:03d}-{uid}'
                                .format(index=index, uid=uid))

            # Ensure the whole path is created
            if not os.path.exists(path):
                os.makedirs(path)

            return path
