#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: Action queue


:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# Composer core
import cohorte.composer.core

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Property, Requires

# Standard library
import logging
import sys
import threading

if sys.version_info[0] == 3:
    # Python 3
    import queue

else:
    # Python 2
    import Queue as queue

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-core-queue-factory")
@Provides(cohorte.composer.core.SERVICE_QUEUE)
@Requires('_executor', cohorte.composer.core.SERVICE_EXECUTOR)
@Property('_timeout', "queue.timeout", 1)
class ActionQueue(object):
    """
    Composer core action executor
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Action executor
        self._executor = None

        # Queue time out
        self._timeout = 1

        # Action queue
        self._queue = queue.Queue()

        # Queue thread
        self._thread = None
        self._stop = threading.Event()


    def enqueue(self, action):
        """
        Enqueues the given action
        
        :param action: The representation of an action
        """
        if action is not None:
            self._queue.append(action)


    def join(self):
        """
        Waits until all elements of the queue are treated or until the queue
        has been cleared.
        """
        if not self._queue.empty():
            self._queue.join()


    def stop(self):
        """
        Stops the executor loop immediately
        """
        self._stop.set()
        # Add something into the queue to kill the timeout
        self._queue.put_nowait(self._stop)


    def run(self):
        """
        Loops on the queue items and calls the executor
        """
        while not self._stop.is_set():
            try:
                # Wait for an action (blocking)
                action = self._queue.get(True, self._timeout)
                if action is self._stop:
                    # Stop event in the queue: get out
                    return

            except queue.Empty:
                # Nothing to do
                pass

            else:
                # Call the executor
                try:
                    self._executor.execute(action)

                except Exception as ex:
                    _logger.exception("Error executing %s: %s", action, ex)

                finally:
                    # Mark the action as executed
                    self._queue.task_done()


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # Start the thread
        self._stop.clear()
        self._thread = threading.Thread(target=self.run, "action-queue")
        self._thread.start()

        _logger.info("Action queue validated")


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Stop the thread
        self._stop.set()
        self._thread.join()
        self._thread = None

        # Clear storage
        if self._queue.count() != 0:
            _logger.warning("Stopping the action but some actions were waiting")
        self._queue.clear()

        _logger.info("Action queue invalidated")
