#!/usr/bin/python
#-- Content-Encoding: utf-8 --
"""
Core of the PSEM2M Forker

:author: Thomas Calmant
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Provides, Requires, \
    Instantiate, Validate, Invalidate, Bind

# ------------------------------------------------------------------------------

import psutil
import socket

import logging
import psem2m
import threading
import json
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

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-forker-factory")
@Instantiate("Forker")
@Provides("org.psem2m.isolates.services.forker.IForker")
@Requires("_config", "org.psem2m.isolates.services.conf.ISvcConfig")
@Requires("_sender", "org.psem2m.signals.ISignalBroadcaster")
@Requires("_receiver", "org.psem2m.signals.ISignalReceiver")
@Requires("_runners", "org.psem2m.isolates.forker.IIsolateRunner",
          aggregate=True)
@Requires("_config_broker", "org.psem2m.forker.configuration.store")
class Forker(object):
    """
    The forker component
    """
    def __init__(self):
        """
        Constructor
        """
        self._config = None
        self._runners = None
        self._sender = None
        self._receiver = None
        self._config_broker = None

        # Platform is not yet stopped
        self._platform_stopping = False

        # The forker may have to start the monitor
        self._start_monitor = False

        # Isolate ID -> process object
        self._isolates = {}

        # List of watching threads Isolate ID -> Thread
        self._threads = {}

        # Loop control of thread watching isolates
        self._watchers_running = False


    def getHostName(self):
        """
        Retrieves the name of the host machine of this forker.
        
        Implementation of ``org.psem2m.isolates.services.forker.IForker``
        
        Uses socket.gethostname().
        
        :return: The host name
        """
        return socket.gethostname()


    def ping(self, isolate_id):
        """
        Tests the given isolate state.
        
        Implementation of ``org.psem2m.isolates.services.forker.IForker``
        
        The result can be one of :
        
        * 0: ALIVE, the isolate is running
        * 1: DEAD, the isolate is not running (or unknown)
        * 2: STUCK, the isolate is running but doesn't answer to the forker
          ping request. (*not yet implemented*)
        
        :param isolate_id: The ID of the isolate to test
        :return: The isolate process state
        """
        process = self._isolates.get(isolate_id, None)

        if process is None:
            # No PID for this ID
            return 1

        # Poll the process and wait for an answer
        if not process.is_running():
            # FIXME: current implementation can't test if a process is stuck
            return 1

        return 0


    def start_isolate_id(self, isolate_id):
        """
        Starts the isolate with the given ID
        
        The method calls :epydoc:startIsolate, so it returns the same error
        code, plus :
        
        * 6: No configuration service available
        * 7: Unknown isolate
        
        :param isolate_id: An isolate ID
        :return: The start error code (see :epydoc:startIsolate)
        """
        if self._config is None:
            # Configuration not injected, consider a runner exception
            return 6

        descr = self._config.get_application().get_isolate(isolate_id)
        if descr is None or descr.get_raw() is None:
            return 7

        # Call the main start method
        return self.startIsolate(descr.get_raw())


    def startIsolate(self, isolate_descr):
        """
        Starts the given isolate description.
        
        Implementation of ``org.psem2m.isolates.services.forker.IForker``
        
        The result can be one of :
        
        * 0: SUCCESS, the isolate has been successfully started
        * 1: ALREADY_RUNNING, the isolate is already running
        * 2: NO_PROCESS_REF, the process couldn't be started (no reference to
          the process)
        * 3: NO_WATCHER, the process watcher couldn't be started
        * 4: RUNNER_EXCEPTION, an error occurred starting the isolate process
        * 5: UNKNOWN_KIND, unknown kind of isolate
        
        :param isolate_descr: The isolate description
        :return: The start error code
        """
        isolate_id = isolate_descr.get("id", None)
        if not isolate_id:
            # Consider the lack of ID as a runner exception
            _logger.debug("Invalid ID")
            return 3

        if self.ping(isolate_id) == 0:
            # Isolate is already running
            _logger.debug("'%s' is already running", isolate_id)
            return 1

        # Find compatible runners
        kind = isolate_descr.get("kind", None)
        runners = [runner for runner in self._runners
                   if runner.can_handle(kind)]

        if not runners:
            # Un-handled kind
            _logger.debug("'%s': unknown kind '%s'", isolate_id, kind)
            return 5

        # A runner can handle this kind of isolate : store the configuration
        # in the broker
        self._config_broker.store_configuration(isolate_id,
                                                json.dumps(isolate_descr))

        # Store the access URL to the broker
        isolate_descr["psem2m.configuration.broker"] = \
                                            self._config_broker.get_access_url()

        # Store the access port to the forker signals
        isolate_descr["psem2m.directory.dumper.port"] = \
                                            self._receiver.get_access_info()[1]

        # Stop at the first runner that succeed to start the isolate
        for runner in runners:
            try:
                process = runner.run_isolate(isolate_descr)
                if process is not None:
                    # Success !
                    self._isolates[isolate_id] = process
                    _logger.debug("'%s': isolate started", isolate_id)
                    break

            except:
                # Something bad occurred
                _logger.exception("Error trying to start '%s' with '%s'",
                                  isolate_id, type(runner).__name__)

        else:
            # No runner succeeded
            _logger.debug("'%s': all runners failed to start isolate",
                          isolate_id)

            # Forget the configuration
            self._config_broker.delete_configuration(isolate_id)
            return 3

        # Start the watching thread
        thread = self._threads[isolate_id] = threading.Thread(
                                name="watcher-%s-%d"
                                % (isolate_id, process.pid),
                                target=self.__process_wait_watcher,
                                args=(isolate_id, process, 1))

        thread.daemon = True
        thread.start()

        thread = self._threads[isolate_id + "-reader"] = threading.Thread(
                                name="reader-%s-%d"
                                % (isolate_id, process.pid),
                                target=self.__process_io_watcher,
                                args=(isolate_id, process, 1))

        thread.daemon = True
        thread.start()

        # Success
        return 0


    def stopIsolate(self, isolate_id, timeout=3):
        """
        Kills the process with the given isolate ID. Does nothing the if the
        isolate ID is unknown.
        
        Implementation of ``org.psem2m.isolates.services.forker.IForker``
        
        :param isolate_id: The ID of the isolate to stop
        :param timeout: Maximum time to wait before killing the isolate process
                        (in seconds)
        """
        # Get the isolate PID
        process = self._isolates.get(isolate_id, None)
        if process is None:
            # Unknown isolate
            return

        # Send the stop signal (stop softly)
        self._sender.send(psem2m.SIGNAL_ISOLATE_STOP, None, isolate=isolate_id)

        try:
            # Wait a little (psutil API)
            process.wait(timeout)

        except psutil.TimeoutExpired:
            # The isolate didn't stop -> kill the process
            process.kill()

        # Remove references to the isolate
        del self._isolates[isolate_id]


    def __process_io_watcher(self, isolate_id, process, timeout=0):
        """
        Thread redirecting isolate I/O to monitors
        
        :param isolate_id: ID of the watched isolate
        :param process: A psutil.Process object
        :param timeout: Wait time out (in seconds)
        """
        if timeout <= 0:
            timeout = 1

        logger = logging.getLogger(isolate_id)

        for line in iter(process.stdout.readline, b''):

            # In debug mode, print the raw output
            _logger.debug("FROM %s : %r", isolate_id, line)

            parts = line.decode("UTF-8").split("::")
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
                                  groups=["MONITORS"])

            except:
                logger.exception("Error reading isolate status line :\n%s\n",
                                 parts[1])


    def __process_wait_watcher(self, isolate_id, process, timeout):
        """
        Thread monitoring a psutil.Process, waiting for its death
        
        :param isolate_id: ID of the watched isolate
        :param process: A psutil.Process object
        :param timeout: Wait time out (in seconds)
        """
        if timeout <= 0:
            timeout = 1

        while self._watchers_running:
            try:
                process.wait(timeout)

                # Being here means that the process ended
                self.__handle_lost_isolate(isolate_id)
                break

            except psutil.TimeoutExpired:
                # Time out expired : process is still there, continue the loop
                pass



    def __handle_lost_isolate(self, isolate_id):
        """
        Handle the loss of an isolate.
        If the isolate is a monitor, it must be restarted immediately.
        If not, a lost isolate signal is sent to monitors.
        
        :param isolate_id: The ID of the lost isolate
        """
        if not self._platform_stopping \
        and isolate_id.startswith("org.psem2m.internals."):
            # Internal isolate : restart it immediately
            self.start_isolate_id(isolate_id)
            return

        # Send a signal to monitors
        self._sender.send(ISOLATE_LOST_SIGNAL, isolate_id, dir_group="MONITORS")


    def start_monitor(self):
        """
        Starts a monitor, if none is already running
        
        :return: The result of startIsolate(), or -1 if no monitor was found in
                 the configuration.
        """
        app = self._config.get_application()
        for isolate_id in app.get_isolate_ids():
            if isolate_id.startswith(MONITOR_PREFIX):
                # TODO: test if the monitor is running

                # Start the first found monitor
                isolate_descr = app.get_isolate(isolate_id).get_raw()
                if isolate_descr is not None:
                    _logger.debug("Starting monitor : %s", isolate_id)
                    return self.startIsolate(isolate_descr)

        else:
            # No monitor found
            _logger.warning("No monitor configuration found")
            return -1


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


    @Bind
    def bind(self, service, reference):
        """
        Called by iPOPO when a service is bound to the component
        
        :param service: The injected service
        :param reference: The corresponding ServiceReference object
        """
        specifications = reference.get_property("objectClass")

        if "org.psem2m.isolates.forker.IIsolateRunner" in specifications:
            # Runner bound
            if self._start_monitor:
                # Monitor must be started
                if self.start_monitor() in (0, 1):
                    # Success !
                    self._start_monitor = False


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        self._watchers_running = True

        self._receiver.register_listener(SIGNAL_PREFIX_MATCH_ALL, self)

        if context.get_property(PROPERTY_START_MONITOR) is True:
            # A monitor must be started
            if self.start_monitor() not in (0, 1):
                # Monitor has not been started or is not yet running,
                # try with each bound runner
                self._start_monitor = True

            else:
                # Monitor started
                self._start_monitor = False


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        self._watchers_running = False
        self._start_monitor = False

        self._receiver.unregister_listener(SIGNAL_PREFIX_MATCH_ALL, self)

        # Isolates to be removed from thread dictionary
        to_remove = []

        for isolate_id, thread in self._threads.items():
            thread.join(2)
            if thread.is_alive():
                _logger.warning("Thread watching %s is still running...",
                                isolate_id)

            else:
                # Remove the entry if the thread was gone
                to_remove.append(isolate_id)

        # Remove entries
        for isolate_id in to_remove:
            del self._threads[isolate_id]

        del to_remove[:]
