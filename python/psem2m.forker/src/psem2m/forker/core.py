#!/usr/bin/python
#-- Content-Encoding: utf-8 --
"""
Core of the PSEM2M Forker

:author: Thomas Calmant
"""

from psem2m.component.decorators import ComponentFactory, Provides, Requires, \
    Property, Instantiate

# ------------------------------------------------------------------------------

import psutil
import socket

import logging
import psem2m
_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-forker-factory")
@Instantiate("Forker")
@Provides("org.psem2m.isolates.services.forker.IForker")
@Property("__export_itf", "service.exported.interfaces", "*")
@Property("__export_conf", "service.exported.configs", "*")
@Requires("_config", "org.psem2m.isolates.services.conf.ISvcConfig",
          optional=True)
@Requires("_sender", "org.psem2m.SignalSender")
@Requires("_receiver", "org.psem2m.SignalReceiver")
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
        self._receiver = None
        self._runners = None
        self._sender = None

        # Isolate ID -> process object
        self._isolates = {}


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
        process = self._isolates_processes.get(isolate_id, None)

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
        if descr is None:
            return 7

        # Call the main start method
        return self.startIsolate(descr)


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
                process = self._runners.run_isolate(self, isolate_descr)
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
        process = self._isolates_processes.get(isolate_id, None)
        if process is None:
            # Unknown isolate
            return

        # Send the stop signal (stop softly)
        self.sender.send_data(isolate_id, psem2m.SIGNAL_ISOLATE_STOP, None)

        try:
            # Wait a little (psutil API)
            process.wait(timeout)

        except psutil.TimeoutExpired:
            # The isolate didn't stop -> kill the process
            process.kill()

        # Remove references to the isolate
        del self._isolates_processes[isolate_id]
