#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Process watchers package

I/0 threads and process waiter component

:author: Thomas Calmant
:license: Apache Software License 2.0

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

# Python standard library
import logging
import threading

# Pelix framework
from pelix.ipopo.decorators import ComponentFactory, Validate, Invalidate, \
    Provides, Requires, Instantiate
import pelix.threadpool

# Cohorte
import cohorte.forker
import cohorte.utils

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.forker.SERVICE_WATCHER)
@Requires('_listeners', cohorte.forker.SERVICE_WATCHER_LISTENER,
          aggregate=True, optional=True)
@Instantiate('cohorte-forker-watcher')
class IsolateWatcher(object):
    """
    Isolate watcher service
    """
    def __init__(self):
        """
        Sets up members
        """
        # Lost isolates listeners
        self._listeners = []

        # Utility methods
        self._utils = None

        # Isolate UID -> Process
        self._isolates = {}

        # Isolate UID -> Name
        self._names = {}

        # Loop control
        self._stop_event = threading.Event()

        # Waiting threads
        self._wait_thread = None

        # UID -> (thread, event)
        self._io_threads = {}

        # Pause between loops (in seconds)
        self._pause = .5

        # Notification thread
        self._pool = pelix.threadpool.ThreadPool(1, logname="IOWatcher-Notify")

        # Some locking
        self.__lock = threading.Lock()

    @Validate
    def _validate(self, context):
        """
        Component validated
        """
        # Clear state
        self._stop_event.clear()

        # Get utility methods
        self._utils = cohorte.utils.get_os_utils()

        # Start the notification thread
        self._pool.start()

        # Start the waiting thread
        self._wait_thread = threading.Thread(target=self.__wait_thread,
                                             name="Isolate-Waiter")
        self._wait_thread.daemon = True
        self._wait_thread.start()

    @Invalidate
    def _invalidate(self, context):
        """
        Component invalidated
        """
        # Set states
        self._stop_event.set()
        for _, event in list(self._io_threads.values()):
            event.set()

        # Wait for the waiter thread to stop
        self._wait_thread.join()

        # FIXME: Do not wait for I/O threads as they might be stuck

        # Stop the notification thread
        self._pool.stop()

        # Clean up
        self._names.clear()
        self._isolates.clear()
        self._wait_thread = None
        self._io_threads.clear()

    @staticmethod
    def _notify_listeners(listeners, uid, name):
        """
        Notifies listeners of the loss of an isolate

        :param listeners: Listeners to notify
        :param uid: UID of the lost isolate
        :param name: Name of the lost isolate
        """
        for listener in listeners:
            try:
                listener.handle_lost_isolate(uid, name)
            except Exception as ex:
                _logger.exception("Error notifying listener: %s", ex)

    def __wait_thread(self):
        """
        Thread that waits for children processes to stop
        """
        while not self._stop_event.wait(self._pause) \
                and not self._stop_event.is_set():
            with self.__lock:
                # Copy the isolates information
                isolates = list(self._isolates.items())

            for uid, process in isolates:
                try:
                    # Check PID presence
                    self._utils.wait_pid(process.pid, .1)
                except cohorte.utils.TimeoutExpired:
                    # Time out expired : process is still there,
                    # continue the loop
                    pass
                else:
                    # Being here means that the process ended,
                    # ... clean up
                    self.unwatch(uid)
                    name = self._names.pop(uid)

                    # ... notify listeners
                    if self._listeners:
                        listeners = self._listeners[:]
                        self._pool.enqueue(self._notify_listeners,
                                           listeners, uid, name)

    @staticmethod
    def __io_thread(uid, process, event):
        """
        Thread that looks for the I/O of the given process

        :param uid: Isolate UID
        :param process: Isolate process
        :param event: Loop control event
        """
        # Setup the logger for this isolate
        logger = logging.getLogger("io_watcher.{0}".format(uid))

        while not event.is_set():
            line = process.stdout.readline()
            if not line:
                break

            try:
                # Try to decode the line
                line = line.decode('UTF-8').rstrip()
            except (AttributeError, UnicodeDecodeError):
                # Not a string line
                pass

            # In debug mode, print the raw output
            logger.debug(line)

    def watch(self, uid, name, process, watch_io=True):
        """
        Watch for the given isolate

        :param uid: Isolate UID
        :param process: Isolate process (from subprocess module)
        :param watch_io: If True, start an I/O printer thread
        :raise KeyError: Already known UID
        """
        with self.__lock:
            if uid in self._isolates:
                raise KeyError("Already known UID: {0}".format(uid))

            # Store the process
            self._isolates[uid] = process
            self._names[uid] = name

            if watch_io:
                # Start the I/O thread
                event = threading.Event()
                thread = threading.Thread(target=self.__io_thread,
                                          args=(uid, process, event),
                                          name="IOWatcher-{0}".format(uid))
                thread.daemon = True
                thread.start()

                # Store it
                self._io_threads[uid] = (thread, event)

    def unwatch(self, uid):
        """
        Stop watching an isolate

        :param uid: An isolate UID
        :raise KeyError: Unknown UID
        """
        with self.__lock:
            # Remove from waited isolates
            del self._isolates[uid]

            # Remove from the list of I/O threads
            try:
                _, event = self._io_threads.pop(uid)

                # Do not wait for the I/O thread as it might be stuck
                event.set()
            except KeyError:
                # I/O watcher wasn't requested
                return
