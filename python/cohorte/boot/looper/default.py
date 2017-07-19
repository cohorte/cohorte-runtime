#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Defines the default main thread handler

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

# Standard library
import logging
import threading

try:
    # Python 3
    import queue
except ImportError:
    # Python 2
    import Queue as queue

# Pelix utilities
import pelix.utilities as utils

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


def get_looper():
    """
    Constructs the DefaultLooper
    """
    return DefaultLooper()


class DefaultLooper(object):
    """
    Default main thread handler
    """
    def __init__(self):
        """
        Sets up members
        """
        # The loop control event
        self._stop_event = threading.Event()
        self._stop_event.set()

        # The task queue
        self._queue = queue.Queue(0)

        # Small timeout to avoid to blocked in the main loop forever
        self._timeout = 1

    def setup(self, argv=None):
        """
        Configures the looper (does nothing)
        """
        self._stop_event.clear()

    def run(self, method, *args, **kwargs):
        """
        Runs the given method in the main thread

        :param method: The method to call
        :param args: Method arguments
        :param kwargs: Method keyword arguments
        :return: The result of the method
        """
        # Make an event object
        event = utils.EventData()

        # Add the method to the waiting list
        self._queue.put((method, args, kwargs, event))

        # Wait for it...
        event.wait()
        return event.data

    def loop(self):
        """
        Main loop
        """
        while not self._stop_event.is_set():
            if self._queue is None:
                # Nothing to see here...
                return

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
                method, args, kwargs, event = task
                result = None

                try:
                    # Call the method
                    result = method(*args, **kwargs)
                except Exception as ex:
                    _logger.exception("Error executing %s: %s",
                                      method.__name__, ex)
                finally:
                    # Task executed
                    event.set(result)
                    if self._queue is not None:
                        self._queue.task_done()

    def stop(self):
        """
        Stops the loop
        """
        # Stop the loop
        self._queue.put(self._stop_event)

        # Exit all pending methods
        while not self._queue.empty():
            waiting = self._queue.get_nowait()
            # Set the event (to release wait_on_ui)
            if waiting is not self._stop_event:
                waiting[-1].set()
                self._queue.task_done()

        # Clean up
        self._queue = None
