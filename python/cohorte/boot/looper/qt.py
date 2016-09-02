#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Defines the main thread handler for Qt applications

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
import sys

try:
    # Python 3
    import queue
except ImportError:
    # Python 2
    import Queue as queue

# Pelix utilities
import pelix.utilities as utils

# PyQt 4
import PyQt4.QtCore as QtCore
import PyQt4.QtGui as QtGui

# ------------------------------------------------------------------------------

# Module version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


def get_looper():
    """
    Constructs the QtLoader
    """
    return QtLoader()


class QtLoader(QtCore.QObject):
    """
    Qt application loader.
    Provides a run_on_ui() method to allow multi-threaded application
    construction.

    setup(), loop() and stop() should be called in the same thread, which
    should be the process main thread.
    """
    __ui_queued = QtCore.pyqtSignal()
    """ Signals a queued method to call in the UI thread """

    def __init__(self):
        """
        Sets up members
        """
        QtCore.QObject.__init__(self)
        self.__app = None
        self.__waiting_calls = None

    def __ui_runner(self):
        """
        Method called by Qt in the UI thread
        """
        # Get the next method to call
        method, args, kwargs, event = self.__waiting_calls.get()
        result = None

        try:
            # Call the method
            result = method(*args, **kwargs)
        except Exception as ex:
            _logger.exception("%s: %s", type(ex).__name__, ex)
        finally:
            # Task executed
            event.set(result)
            if self.__waiting_calls is not None:
                self.__waiting_calls.task_done()

    def get_application(self):
        """
        Get the Qt application object
        """
        return self.__app

    def run(self, method, *args, **kwargs):
        """
        Runs the given method in the main thread

        :param method: The method to call
        :param args: Method arguments
        :param kwargs: Method keyword arguments
        :return: The result of the method
        :raise ValueError: Qt is not started yet
        """
        if self.__waiting_calls is None:
            raise ValueError("UI not yet set up")

        # Make an event object
        event = utils.EventData()

        # Add the method to the waiting list
        self.__waiting_calls.put((method, args, kwargs, event))

        # Emit to signal, to let Qt execute __ui_runner
        self.__ui_queued.emit()

        # Force Qt to handle the event
        self.__app.processEvents()

        # Wait for the event before returning
        return event.wait()

    def setup(self, argv=None):
        """
        Sets up the QtApplication
        """
        if self.__app is None:
            # Create the UI runner queue
            self.__waiting_calls = queue.Queue()

            # Create the QApplication object
            self.__app = QtGui.QApplication(argv or sys.argv)

            # Connect the UI runner signal
            self.__ui_queued.connect(self.__ui_runner)

        return self.__app

    def loop(self):
        """
        Blocking event loop.

        This method will return when stop() is called or when the quit()
        signal is emitted.
        """
        self.__app.exec_()

    def stop(self):
        """
        Destroys the application
        """
        if self.__app:
            # Close windows (will clean up a bit)
            self.__app.closeAllWindows()

            # No need for the "run" now
            self.__ui_queued.disconnect()

            # Exit all pending methods
            while not self.__waiting_calls.empty():
                waiting = self.__waiting_calls.get_nowait()
                # Set the event (to release wait_on_ui)
                waiting[-1].set()
                self.__waiting_calls.task_done()

            # Stop the UI loop
            self.__app.quit()

            # Clean up
            self.__app = None
            self.__waiting_calls = None
