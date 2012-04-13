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
    Property, Instantiate, Validate, Invalidate, Bind

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

@ComponentFactory("psem2m-forker-factory")
@Instantiate("Forker")
@Provides("org.psem2m.isolates.services.forker.IForker")
@Property("__export_itf", "service.exported.interfaces", "*")
@Property("__export_conf", "service.exported.configs", "*")
@Requires("_config", "org.psem2m.isolates.services.conf.ISvcConfig")
@Requires("_sender", "org.psem2m.SignalSender")
@Requires("_runners", "org.psem2m.isolates.forker.IIsolateRunner",
          aggregate=True)
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
            return 3

        if self.ping(isolate_id) == 0:
            # Isolate is already running
            return 1

        # Find compatible runners
        kind = isolate_descr.get("kind", None)
        runners = [runner for runner in self._runners
                   if runner.can_handle(kind)]

        if not runners:
            # Un-handled kind
            return 5

        # Stop at the first runner that succeed to start the isolate
        for runner in runners:
            try:
                process = runner.run_isolate(isolate_descr)
                if process is not None:
                    # Success !
                    self._isolates[isolate_id] = process
                    break

            except:
                # Something bad occurred
                _logger.exception("Error trying to start '%s' with '%s'",
                                  isolate_id, type(runner).__name__)

        else:
            # No runner succeeded
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
        self._sender.send_data(isolate_id, psem2m.SIGNAL_ISOLATE_STOP, None)

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
                self._sender.send_data("MONITORS", ISOLATE_STATUS_SIGNAL,
                                       status_bean)

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
        if isolate_id.startswith("org.psem2m.internals."):
            # Internal isolate : restart it immediately
            self.start_isolate_id(isolate_id)

        else:
            # Send a signal to monitors
            self._sender.send_data("MONITORS", ISOLATE_LOST_SIGNAL, isolate_id)


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


    @Bind
    def bind(self, service, reference):
        """
        Called by iPOPO when a service is bound to the component
        
        :param service: The injected service
        :param reference: The corresponding ServiceReference object
        """
        if "org.psem2m.isolates.forker.IIsolateRunner" in \
           reference.get_property("objectClass"):
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
