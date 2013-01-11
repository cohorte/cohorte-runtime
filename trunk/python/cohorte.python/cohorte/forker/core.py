#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Core of the COHORTE Forker

**TODO:**
* Review all the code !

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE modules
import cohorte
import cohorte.utils as utils

# Pelix framework
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate
import pelix.framework

# Python standard library
import json
import logging
import os
import subprocess
import sys
import threading
import uuid

if sys.version_info[0] == 3:
    # Python 3
    from urllib.parse import quote as url_quote

else:
    # Python 2
    from urllib import quote as url_quote

# ------------------------------------------------------------------------------

# Ugly code from Recipe 576780 in ActivateState, by
# http://code.activestate.com/recipes/576780-timeout-for-nearly-any-callable
#
# Uses Thread private/protected methods to kill a thread
if sys.version_info[0] == 3:
    # Python 3
    _Thread_stop = getattr(threading.Thread, '_stop', None)

else:
    # Python 2
    _Thread_stop = getattr(threading.Thread, '_Thread__stop', None)

# ------------------------------------------------------------------------------

MONITOR_KIND = 'osgi'
""" Kind of isolate for the monitor """

MONITOR_NAME = 'cohorte.internals.monitor'
""" All monitors have the same name """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

ISOLATE_STATUS_CLASS = "org.psem2m.isolates.base.isolates.boot.IsolateStatus"

ISOLATE_LOST_SIGNAL = "/psem2m/isolate/lost"
ISOLATE_STATUS_SIGNAL = "/psem2m/isolate/status"

MONITOR_PREFIX = "org.psem2m.internals.isolates.monitor"
PROPERTY_START_MONITOR = "psem2m.forker.start_monitor"

# ------------------------------------------------------------------------------

# The order ID request key
CMD_ID = "requestToken"

# The order result key
RESULT_CODE = "result"

# The signals prefix
SIGNAL_PREFIX = "/psem2m/internals/forkers/"

# The ping isolate signal
SIGNAL_PING_ISOLATE = SIGNAL_PREFIX + "ping"

# The signal match string
SIGNAL_PREFIX_MATCH_ALL = SIGNAL_PREFIX + "*"

# The response signal
SIGNAL_RESPONSE = SIGNAL_PREFIX + "response"

# The start isolate signal
SIGNAL_START_ISOLATE = SIGNAL_PREFIX + "start"

# The stop isolate signal
SIGNAL_STOP_ISOLATE = SIGNAL_PREFIX + "stop"

# The platform is stopping
SIGNAL_PLATFORM_STOPPING = SIGNAL_PREFIX + "platform-stopping"

# TODO: from psem2m
SIGNAL_ISOLATE_STOP = "STOP"

# ------------------------------------------------------------------------------

@ComponentFactory('cohorte-forker-factory')
@Requires('_config', cohorte.SERVICE_CONFIGURATION_READER)
@Requires('_config_broker', cohorte.SERVICE_CONFIGURATION_BROKER)
@Requires('_receiver', cohorte.SERVICE_SIGNALS_RECEIVER)
@Requires('_sender', cohorte.SERVICE_SIGNALS_SENDER)
@Requires('_state_dir', 'cohorte.forker.state')
@Requires('_state_updater', 'cohorte.forker.state.updater')
class Forker(object):
    """
    The forker core component
    """
    def __init__(self):
        """
        Constructor
        """
        # Injected services
        self._config = None
        self._config_broker = None
        self._receiver = None
        self._sender = None
        self._state_dir = None
        self._state_updater = None

        # Bundle context
        self._context = None

        # Get OS specific methods
        self._utils = utils.get_os_utils()

        # Platform is not yet stopped
        self._platform_stopping = False

        # Monitor UID
        self._monitor_uid = None

        # Isolate ID -> process object
        self._isolates = {}

        # List of watching threads Isolate ID -> Thread
        self._threads = {}

        # Loop control of thread watching isolates
        self._watchers_running = False


    def _start_monitor_in_thread(self):
        """
        Starts a new thread to handle the launch of the monitor
        """
        def start_monitor_checker():
            """
            Calls _start_monitor and stops the framework if it fails 
            """
            try:
                if self._start_monitor() not in (0, 1):
                    # Monitor has not been started or is not yet running,
                    # it is a fatal error.
                    _logger.error("Forker couldn't start a monitor. Abandon.")
                    if self._context:
                        # Stop the framework
                        self._context.get_bundle(0).stop()

                else:
                    # All right
                    _logger.debug('Monitor started')

            except Exception as ex:
                # Something went wrong
                _logger.exception("Error starting the monitor: %s", ex)
                if self._context:
                    # Stop the framework
                    self._context.get_bundle(0).stop()

        # Start the thread
        thread = threading.Thread(target=start_monitor_checker,
                                  name="monitor-starter")
        thread.start()


    def _start_monitor(self):
        """
        Starts the monitor isolate, calling the boot module in a new interpreter
        instance.
        
        :return: The start_isolate() result code 
        """
        # Test if the monitor is running
        if self._monitor_uid is not None \
        and self._state_dir.knows(self._monitor_uid):
            # Isolate is already running
            return 1

        # TODO: test a bit more (what if the UID has been reset ?)

        # Store the monitor UID
        self._monitor_uid = str(uuid.uuid4())
        node = self._context.get_property(cohorte.PROP_NODE)

        # Make the configuration
        try:
            configuration = self._config.prepare_isolate(self._monitor_uid,
                                                         MONITOR_NAME,
                                                         node, MONITOR_KIND)

        except IOError as ex:
            _logger.error("Can't read a configuration for the monitor: %s", ex)
            return -1

        except KeyError as ex:
            _logger.error("Missing a parameter in the configuration of the "
                          "monitor: %s", ex)
            return -1

        except ValueError as ex:
            _logger.error("Error reading the the configuration of the "
                          "monitor: %s", ex)
            return -1

        # Start the isolate
        return self.start_isolate(configuration)


    def _prepare_working_directory(self, uid, name):
        """
        Prepare a working directory for the given isolate
        
        :param uid: Isolate UID
        :param name: Isolate name
        :return: The path to the working directory
        """
        # Get the base directory
        base = self._context.get_property(cohorte.PROP_BASE)

        # Escape the name
        name = url_quote(name)

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
        path = os.path.join(path,
                            '{index:03d}-{uid}'.format(index=index, uid=uid))

        # Ensure the whole path is created
        if not os.path.exists(path):
            os.makedirs(path)

        return path


    def _run_boot_script(self, working_directory, configuration,
                         config_broker_url, state_updater_url):
        """
        Runs the boot script in a new process
        
        :param working_directory: Isolate working directory (must exist)
        :param configuration: Isolate configuration dictionary
        :param config_broker_url: URL to the configuration in the broker
        :param state_updater_url: URL to the isolate state updater
        :return: A POpen object
        """
        # Increase readability
        get_property = self._context.get_property

        # Process environment
        environment = os.environ.copy()

        # Use configuration environment variables
        config_env = configuration.get('environment')
        if config_env:
            environment.update(config_env)

        # Internal values
        environment[cohorte.ENV_HOME] = get_property(cohorte.PROP_HOME)
        environment[cohorte.ENV_BASE] = get_property(cohorte.PROP_BASE)
        environment[cohorte.ENV_NODE] = get_property(cohorte.PROP_NODE)

        # Python interpreter to use
        args = [sys.executable]

        # Interpreter arguments
        interpreter_args = configuration.get('boot_args')
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

        # Log file
        logname = 'log_{0}_{1}.log'.format(configuration['name'],
                                           configuration['uid'])
        args.append('--logfile={0}'.format(os.path.join(working_directory,
                                                        logname)))

        # Debug arguments
        if self._context.get_property(cohorte.PROP_DEBUG):
            args.append('--debug')

        if self._context.get_property(cohorte.PROP_VERBOSE):
            args.append('--verbose')

        # Run the process and return its reference
        return subprocess.Popen(args, executable=args[0],
                                env=environment,
                                cwd=working_directory,
                                stdin=subprocess.PIPE,
                                stdout=subprocess.PIPE,
                                stderr=subprocess.STDOUT)


    def start_isolate(self, isolate_config):
        """
        Starts the given isolate description.
        
        The result can be one of :
        
        * 0: SUCCESS, the isolate has been successfully started
        * 1: ALREADY_RUNNING, the isolate is already running
        * 2: RUNNER_EXCEPTION, an error occurred starting the isolate process
        * 3: INVALID_PARAMETER, a parameter is missing or invalid
        
        :param isolate_config: The isolate configuration dictionary
        :return: The result code
        """
        # Name is mandatory
        name = isolate_config.get('name')
        if not name:
            _logger.error("Isolate doesn't have a name")
            return 3

        uid = isolate_config.get('uid')
        if not uid:
            # UID is missing, generate one
            uid = isolate_config['uid'] = str(uuid.uuid4())

        elif self.ping(uid) == 0:
            # Isolate is already running
            _logger.error("UID '%s' is already running", isolate_config['uid'])
            return 1

        # Prepare the working directory
        working_dir = self._prepare_working_directory(uid, name)

        # Tell the state directory to prepare an entry
        self._state_dir.prepare_isolate(uid)

        # Store the configuration in the broker
        config_url = self._config_broker.store_configuration(uid,
                                                             isolate_config)

        # Start the boot script
        try:
            # Start the process
            process = self._run_boot_script(working_dir, isolate_config,
                                            config_url,
                                            self._state_updater.get_url())

        except ValueError as ex:
            # Invalid argument given
            _logger.error("Invalid argument given to the boot script for "
                          "isolate '%s' (%s): %s",
                          isolate_config['name'], uid, ex)

            # Clear the configuration in the broker
            self._config_broker.delete_configuration(uid)

            # Clear the state
            self._state_dir.clear_isolate(uid)

            # Invalid parameter
            return 3

        except OSError as ex:
            # Error starting the process
            _logger.error("Error starting the boot script for "
                          "isolate '%s' (%s): %s",
                          isolate_config['name'], uid, ex)

            # Clear the configuration in the broker
            self._config_broker.delete_configuration(uid)

            # Clear the state
            self._state_dir.clear_isolate(uid)

            # Runner exception
            return 2

        except Exception as ex:
            _logger.exception('WTF ?? %s', ex)
            return -1

        else:
            # Store the isolate process information
            self._isolates[uid] = process

            # Start the waiting thread
            thread_name = '{0}-waiter'.format(uid)
            thread = self._threads[thread_name] = threading.Thread(
                                            name=thread_name,
                                            target=self.__process_wait_watcher,
                                            args=(uid, process, 1))
            thread.daemon = True
            thread.start()

            # Start the IO watching thread
            thread_name = '{0}-reader'.format(uid)
            thread = self._threads[thread_name] = threading.Thread(
                                            name=thread_name,
                                            target=self.__process_io_watcher,
                                            args=(uid, process, 1))
            thread.daemon = True
            thread.start()

            try:
                # Wait for it to be loaded (30 seconds max)
                _logger.debug('Waiting for %s - %s', uid, process.pid)
                self._state_dir.wait_for(uid, 30)

            except ValueError as ex:
                # Timeout reached or isolate lost
                _logger.error("Error waiting for the isolate to be loaded")

                # Kill the isolate
                try:
                    if process.poll() is not None:
                        process.terminate()

                except OSError:
                    # Ignore errors
                    pass

                return 2

        # Success
        return 0


    def ping(self, uid):
        """
        Tests the given isolate state.
        
        The result can be one of :
        
        * 0: ALIVE, the isolate is running
        * 1: DEAD, the isolate is not running (or unknown)
        * 2: STUCK, the isolate is running but doesn't answer to the forker
          ping request. (*not yet implemented*)
        
        :param uid: The UID of the isolate to test
        :return: The isolate process state
        """
        process = self._isolates.get(uid, None)

        if process is None:
            # No PID for this ID
            return 1

        # Poll the process and wait for an answer
        if not self._utils.is_process_running(process.pid):
            # FIXME: current implementation can't test if a process is stuck
            return 1

        return 0


    def stop_isolate(self, uid, timeout=3):
        """
        Kills the process with the given isolate ID. Does nothing the if the
        isolate ID is unknown.
        
        Implementation of ``org.psem2m.isolates.services.forker.IForker``
        
        :param uid: The UID of the isolate to stop
        :param timeout: Maximum time to wait before killing the isolate process
                        (in seconds)
        """
        # Get the isolate PID
        process = self._isolates.get(uid, None)
        if process is None:
            # Unknown isolate
            _logger.warn("Can't stop an unknown isolate: %s", uid)
            return

        # Send the stop signal (stop softly)
        # TODO: use a new API in Signals (UID/Names)
        self._sender.send(SIGNAL_ISOLATE_STOP, None, isolate=uid)

        try:
            # Wait a little
            self._utils.wait_pid(process.pid, timeout)
            _logger.warn("Isolate stopped: %s", uid)

        except utils.TimeoutExpired:
            # The isolate didn't stop -> kill the process
            _logger.warn("Isolate timed out: %s. Trying to kill it", uid)
            process.kill()

        # Remove references to the isolate
        del self._isolates[uid]
        self._state_dir.clear_isolate(uid)

# ------------------------------------------------------------------------------

    def __process_io_watcher(self, isolate_id, process, timeout=0):
        """
        Thread redirecting isolate I/O to monitors
        
        :param isolate_id: ID of the watched isolate
        :param process: A subprocess.Process object
        :param timeout: Wait time out (in seconds)
        """
        if timeout <= 0:
            timeout = 1

        logger = logging.getLogger(isolate_id)

        for line in iter(process.stdout.readline, b''):

            try:
                line = line.decode('UTF-8').rstrip()
            except:
                pass

            # In debug mode, print the raw output
            w_isolate_id = isolate_id[:25]
            _logger.debug("FROM %25s:\n> %s", w_isolate_id, line)

            # parts = line.decode("UTF-8").split("::")
            parts = line.split("::")
            if len(parts) != 2:
                # Unknown format, ignore line
                continue

            if "Bootstrap.MessageSender.sendStatus" not in parts[0]:
                # Not a status, ignore
                continue

            # Get the status content (JSON)
            try:
                status_json = json.loads(parts[1])
                if status_json.get("type", None) != "IsolateStatus":
                    # Not a known status
                    continue

                status_bean = {
                               "javaClass": ISOLATE_STATUS_CLASS,
                               "isolateId": isolate_id,
                               "progress": float(status_json["progress"]),
                               "state": status_json["state"],
                               "statusUID": status_json["UID"],
                               "timestamp": status_json["timestamp"]
                               }

                # Re-transmit the isolate status
                self._sender.send(ISOLATE_STATUS_SIGNAL, status_bean,
                                  dir_group="MONITORS")

            except:
                logger.exception("Error reading isolate status line :\n%s\n",
                                 parts[1])


    def __process_wait_watcher(self, isolate_id, process, timeout):
        """
        Thread monitoring a subprocess.Process, waiting for its death
        
        :param isolate_id: ID of the watched isolate
        :param process: A subprocess.Process object
        :param timeout: Wait time out (in seconds)
        """
        if timeout <= 0:
            timeout = 1

        while self._watchers_running:
            try:
                self._utils.wait_pid(process.pid, timeout)

                # Being here means that the process ended
                self.__handle_lost_isolate(isolate_id)
                break

            except utils.TimeoutExpired:
                # Time out expired : process is still there, continue the loop
                pass



    def __handle_lost_isolate(self, uid):
        """
        Handle the loss of an isolate.
        If the isolate is a monitor, it must be restarted immediately.
        If not, a lost isolate signal is sent to monitors.
        
        :param uid: The ID of the lost isolate
        """
        # Clear isolate status
        self._state_dir.clear_isolate(uid)

        if not self._platform_stopping:
            # Send a signal to all isolates, except the lost one
            # -> avoids a time out
            self._sender.send(ISOLATE_LOST_SIGNAL, uid, dir_group="ALL",
                              excluded=uid)

            if uid == self._monitor_uid:
                # Internal isolate : restart it immediately
                # self._start_monitor()
                pass


    def handle_received_signal(self, name, signal_data):
        """
        Called when a remote services signal is received
        
        :param name: Signal name
        :param signal_data: Signal content
        """
        signal_content = signal_data["signalContent"]

        try:
            if name == SIGNAL_PING_ISOLATE:
                # Ping the isolate with the given ID
                return self.ping(signal_content["isolateId"])

            elif name == SIGNAL_START_ISOLATE:
                # Start an isolate with the given description
                return self.startIsolate(signal_content["isolateDescr"])

            elif name == SIGNAL_STOP_ISOLATE:
                # Stop the isolate with the given ID
                return self.stopIsolate(signal_content["isolateId"])

            elif name == SIGNAL_PLATFORM_STOPPING:
                # Platform is stopping: do not start new isolates
                self._platform_stopping = True

                # Nothing to send back
                return

            else:
                # Unhandled message
                _logger.warning("Received unknown signal: %s", name)
                return

        except:
            # Error
            _logger.exception("Error treating signal %s\n%s", name, signal_data)
            return


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        # Store the bundle context
        self._context = context

        # Activate watchers
        self._watchers_running = True

        # Register to signals
        self._receiver.register_listener(SIGNAL_PREFIX_MATCH_ALL, self)

        if context.get_property(cohorte.PROP_START_MONITOR):
            # A monitor must be started
            _logger.debug('Starting monitor (via a thread)')
            self._start_monitor_in_thread()


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        # De-activate watchers
        self._watchers_running = False

        # Unregister from signals
        self._receiver.unregister_listener(SIGNAL_PREFIX_MATCH_ALL, self)

        # Isolates to be removed from thread dictionary
        to_kill = {}

        for uid, thread in self._threads.items():
            thread.join(2)
            if thread.is_alive():
                # A thread is still there
                _logger.warning("Thread watching %s is still running...", uid)
                to_kill[uid] = thread

        if _Thread_stop:
            # Terminate threads
            for thread in to_kill.values():
                if thread.is_alive():
                    # Kill it
                    _Thread_stop(thread)

        # Clean up
        self._threads.clear()
        self._context = None
