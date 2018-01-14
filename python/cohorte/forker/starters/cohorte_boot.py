#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Starts Cohorte isolates using the boot script

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
import os
import subprocess
import sys

import cohorte
import cohorte.forker
import cohorte.forker.starters.common as common
import cohorte.monitor
import cohorte.utils
import cohorte.version
from herald import beans
import herald
from herald.exceptions import HeraldException
import pelix.http as http
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Property, Instantiate


# Pelix framework
# COHORTE modules
# Herald
# ------------------------------------------------------------------------------
# Bundle version
__version__ = cohorte.version.__version__

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.forker.SERVICE_STARTER)
@Property('_kinds', cohorte.forker.PROP_STARTER_KINDS,
          ('pelix', 'pelix-py3', 'osgi', 'cohorte-compat'))
@Requires('_broker', cohorte.SERVICE_CONFIGURATION_BROKER)
@Requires('_sender', herald.SERVICE_HERALD)
@Requires('_http', http.HTTP_SERVICE)
@Instantiate('cohorte-starter-boot')
class CohorteBoot(common.CommonStarter):
    """
    Isolate starter using the Cohorte boot script
    """
    def __init__(self):
        """
        Sets up members
        """
        super(CohorteBoot, self).__init__()

        # Pelix Http Service
        self._http = None

        # Configuration broker
        self._broker = None

        # Signal sender
        self._sender = None

        # Clean stop timeout (5 seconds)
        self._timeout = 5.

    def _run_boot_script(self, working_directory, configuration,
                         config_broker_url, state_updater_url,
                         looper_name=None, forker_http_port=None):
        """
        Runs the boot script in a new process

        :param working_directory: Isolate working directory (must exist)
        :param configuration: Isolate configuration dictionary
        :param config_broker_url: URL to the configuration in the broker
        :param state_updater_url: URL to the isolate state updater
        :param looper_name: Name of the main thread loop handler, if any
        :param forker_http_port: Http port of the forker
        :return: A POpen object
        """
        # Process environment
        environment = self.setup_environment(configuration)

        # Python interpreter to use
        args = [sys.executable]
        # Interpreter arguments
        interpreter_args = configuration.get('boot', {}).get('boot_args')
        if interpreter_args:
            if type(interpreter_args) is list:
                args.extend(interpreter_args)
            else:
                args.append(str(interpreter_args))

        # Boot script
        args.append('--')
        args.append(os.path.abspath(sys.modules['__main__'].__file__))

        # UID
        args.append('--uid={0}'.format(configuration['uid']))

        # Broker URL
        if config_broker_url:
            args.append('--configuration-broker={0}'.format(config_broker_url))

        # State updater URL
        if state_updater_url:
            args.append('--state-updater={0}'.format(state_updater_url))    

        # Main thread loop handler
        if looper_name:
            args.append('--looper={0}'.format(looper_name))

        # Forker Http port
        if forker_http_port:
            args.append('--forker-http-port={0}'.format(forker_http_port))

        # Log file
        logname = 'log_{0}.log'.format(configuration['name'])
        args.append('--logfile={0}'.format(os.path.join(working_directory,
                                                        logname)))

        # Debug arguments
        if self._context.get_property(cohorte.PROP_DEBUG):
            args.append('--debug')

        if self._context.get_property(cohorte.PROP_VERBOSE):
            args.append('--verbose')

        if self._context.get_property(cohorte.PROP_COLORED):
            args.append('--color')

        # Run the process and return its reference
        return subprocess.Popen(args, executable=args[0],
                                env=environment,
                                cwd=working_directory,
                                stdin=subprocess.PIPE,
                                stdout=subprocess.PIPE,
                                stderr=subprocess.STDOUT)

    def start(self, configuration, state_updater_url):
        """
        Starts an isolate with the given configuration and its monitoring
        threads

        :param configuration: An isolate configuration
        :param state_updater_url: URL to the isolate state updater
        :return: True in case of success, as it uses the state updater
        :raise ValueError: Invalid configuration
        :raise OSError: Error starting the isolate
        """
        uid = configuration['uid']
        name = configuration.get('name', '<no-name>')

        # FIXME: make that prettier
        # Compute the looper, if needed
        if sys.platform == "darwin"\
                and configuration['kind'] in ('java', 'osgi'):
            looper = 'cocoa'
        elif "looper" in configuration.keys() :
            looper = configuration["looper"]
        else:
            looper = None

        # Prepare the working directory
        working_dir = self.prepare_working_directory(configuration)

        # Store the configuration in the broker
        config_url = self._broker.store_configuration(uid, configuration)

        # forker Http port
        host, port = self._http.get_access()
        forker_http_port = port        

        try:
            # Start the boot script
            process = self._run_boot_script(
                working_dir, configuration, config_url, state_updater_url,
                looper, forker_http_port)
        except:
            # Delete the configuration in case of error
            self._broker.delete_configuration(uid)
            raise

        # Store the isolate process information
        self._isolates[uid] = process

        # Start watching after the isolate
        self._watcher.watch(uid, name, process)

        # Success, and uses the state updater
        return True

    def stop(self, uid):
        """
        Stops the given isolate

        :param uid: The UID if an isolate
        :raise KeyError: Unknown UID
        :raise OSError: Error killing the isolate
        """
        # Keep the reference to the process (just in case)
        process = self._isolates[uid]

        if process.poll() is None:
            # Fire the stop message
            try:
                self._sender.fire(
                    uid, beans.Message(cohorte.monitor.SIGNAL_STOP_ISOLATE))
            except HeraldException:
                # Error sending message
                _logger.warn("Isolate %s (PID: %d) didn't received the 'stop' "
                             "signal: Kill it!", uid, process.pid)
                try:
                    process.kill()
                except OSError as ex:
                    # Error stopping the process
                    _logger.warning("Can't kill the process: %s", ex)
            else:
                # Message sent
                try:
                    # Wait a little
                    _logger.info("Waiting for isolate %s (PID: %d) to stop...",
                                 uid, process.pid)
                    self._utils.wait_pid(process.pid, self._timeout)
                    _logger.info("Isolate stopped: %s (%d)", uid, process.pid)
                except cohorte.utils.TimeoutExpired:
                    # The isolate didn't stop -> kill the process
                    _logger.warn("Isolate timed out: %s (%d). "
                                 "Trying to kill it", uid, process.pid)
                    try:
                        process.kill()
                    except OSError as ex:
                        # Error stopping the process
                        _logger.warning("Can't kill the process: %s", ex)

        # Process stopped/killed without error
        try:
            del self._isolates[uid]
        except KeyError:
            # Isolate might already be removed from this component by the
            # forker
            pass
