#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Starts Cohorte isolates using the boot script

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
import cohorte.monitor
import cohorte.utils

# Pelix framework
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Property, Instantiate

# Standard library
import logging
import os
import subprocess
import sys

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.forker.SERVICE_STARTER)
@Property('_kinds', cohorte.forker.PROP_STARTER_KINDS,
          ('pelix', 'pelix-py3', 'osgi', 'cohorte-compat'))
@Requires('_broker', cohorte.SERVICE_CONFIGURATION_BROKER)
@Requires('_sender', cohorte.SERVICE_SIGNALS_SENDER)
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

        # Configuration broker
        self._broker = None

        # Signal sender
        self._sender = None

        # Stop timeout (3 seconds)
        self._timeout = 3.


    def _run_boot_script(self, working_directory, configuration,
                         config_broker_url, state_updater_url,
                         looper_name=None):
        """
        Runs the boot script in a new process

        :param working_directory: Isolate working directory (must exist)
        :param configuration: Isolate configuration dictionary
        :param config_broker_url: URL to the configuration in the broker
        :param state_updater_url: URL to the isolate state updater
        :param looper_name: Name of the main thread loop handler, if any
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

        # Log file
        logname = 'log_{0}.log'.format(configuration['name'],
                                       configuration['uid'])
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
        :return: True in case of success, as it uses the state udpater
        :raise ValueError: Invalid configuration
        :raise OSError: Error starting the isolate
        """
        uid = configuration['uid']

        # FIXME: make that prettier
        # Compute the looper, if needed
        if sys.platform == "darwin" and configuration['kind'] == 'java':
            looper = 'cocoa'
        else:
            looper = None

        # Prepare the working directory
        working_dir = self.prepare_working_directory(configuration)

        # Store the configuration in the broker
        config_url = self._broker.store_configuration(uid, configuration)

        try:
            # Start the boot script
            process = self._run_boot_script(working_dir, configuration,
                                            config_url, state_updater_url,
                                            looper)

        except:
            # Delete the configuration in case of error
            self._broker.delete_configuration(uid)
            raise

        # Store the isolate process information
        self._isolates[uid] = process

        # Start watching after the isolate
        self._watcher.watch(uid, process)

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

        # Send the stop signal
        reached = self._sender.fire(cohorte.monitor.SIGNAL_STOP_ISOLATE,
                                    None, isolate=uid)
        if reached is None or uid not in reached:
            # Signal not handled
            _logger.warn("Isolate %s (PID: %d) didn't received the 'stop' "
                         "signal: Kill it!", uid, process.pid)
            process.kill()

        else:
            # Signal handled
            try:
                # Wait a little
                _logger.info("Waiting for isolate %s (PID: %d) to stop...",
                             uid, process.pid)
                self._utils.wait_pid(process.pid, self._timeout)
                _logger.info("Isolate stopped: %s (%d)", uid, process.pid)

            except cohorte.utils.TimeoutExpired:
                # The isolate didn't stop -> kill the process
                _logger.warn("Isolate timed out: %s (%d). Trying to kill it",
                             uid, process.pid)
                process.kill()

        # Process stopped/killed without error
        try:
            del self._isolates[uid]

        except KeyError:
            # Isolate might already be removed from this component by the
            # forker
            pass
