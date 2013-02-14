#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Utilities: Task pool

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version__ = '1.0.0'

# ------------------------------------------------------------------------------

# Standard library
import logging
import threading
import sys

if sys.version_info[0] == 3:
    # Python 3
    import queue

else:
    # Python 2
    import Queue as queue

# ------------------------------------------------------------------------------

class TaskPool(object):
    """
    Executes the tasks stored in a FIFO in a thread pool
    """
    def __init__(self, nb_threads, queue_size=0, timeout=5, logname=None):
        """
        Sets up the task executor
        
        :param nb_threads: Size of the thread pool
        :param queue_size: Size of the task queue (0 for infinite)
        :param timeout: Queue timeout (in seconds)
        :param logname: Name of the logger
        :raise ValueError: Invalid number of threads
        """
        # Validate parameters
        if type(nb_threads) is not int or nb_threads < 1:
            raise ValueError("Invalid pool size: {0}".format(nb_threads))

        # The logger
        self._logger = logging.getLogger(logname or __name__)

        # The loop control event
        self._stop_event = threading.Event()
        self._stop_event.set()

        # The task queue
        self._queue = queue.Queue(queue_size)
        self._queue_size = queue_size
        self._timeout = timeout

        # The thread pool
        self._nb_threads = nb_threads
        self._threads = []


    def start(self):
        """
        Starts the thread pool. Does nothing if the pool is already started.
        """
        if not self._stop_event.is_set():
            # Stop event not set: we're running
            return

        # Clear the stop event
        self._stop_event.clear()

        # Create the threads
        i = 0
        while i < self._nb_threads:
            i += 1
            thread = threading.Thread(target=self.__run)
            self._threads.append(thread)

        # Start'em
        for thread in self._threads:
            thread.start()


    def stop(self):
        """
        Stops the thread pool. Does nothing if the pool is already stopped.
        """
        if self._stop_event.is_set():
            # Stop event set: we're stopped
            return

        # Set the stop event
        self._stop_event.set()

        # Add something in the queue (to unlock the join())
        try:
            for _ in self._threads:
                self._queue.put(self._stop_event, True, self._timeout)

        except queue.Full:
            # There is already something in the queue
            pass

        # Join threads
        for thread in self._threads:
            thread.join()

        # Clear storage
        del self._threads[:]
        self.reset_queue()


    def enqueue(self, method, *args, **kwargs):
        """
        Enqueues a task in the pool
        
        :param method: Method to call
        :raise ValueError: Invalid method
        :raise Full: The task queue is full
        """
        if not hasattr(method, '__call__'):
            raise ValueError("{0} has no __call__ member." \
                             .format(method.__name__))

        # Add the task to the queue
        self._queue.put((method, args, kwargs), True, self._timeout)


    def reset_queue(self):
        """
        Creates a new queue (deletes references to the old one)
        """
        self._queue = queue.Queue(self._queue_size)


    def __run(self):
        """
        The main loop
        """
        while not self._stop_event.is_set():
            try:
                # Wait for an action (blocking)
                task = self._queue.get(True, self._timeout)
                if task is self._stop_event:
                    # Stop event in the queue: get out
                    return

            except queue.Empty:
                # Nothing to do
                pass

            else:
                # Extract elements
                method, args, kwargs = task
                try:
                    # Call the method
                    method(*args, **kwargs)

                except Exception as ex:
                    self._logger.exception("Error executing %s: %s",
                                           method.__name__, ex)

                finally:
                    # Mark the action as executed
                    self._queue.task_done()
