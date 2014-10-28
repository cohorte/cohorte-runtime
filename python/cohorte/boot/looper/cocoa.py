#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Defines the main thread handler for Cocoa applications

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

# Module version
__version_info__ = (0, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

# Pelix utilities
import pelix.utilities as utils

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


try:
    # Cocoa
    import Cocoa
    from PyObjCTools import AppHelper
except ImportError:
    _logger.warning("PyObjCTools is not installed on this system. "
                    "GUI applications will likely freeze. (You've been warned)")

    from cohorte.boot.looper.default import get_looper
else:
    def get_looper(*args, **kwargs):
        """
        Constructs the CocoaLoader
        """
        return CocoaLoader()


class CocoaLoader(object):
    """
    A main thread handler for Cocoa
    """
    def __init__(self):
        """
        Sets up members
        """
        self._app = None
        self._argv = None

    def __ui_runner(self, event, method, args, kwargs):
        """
        Runs the given method and stores its result

        :param event: An EventResult object
        :param method: The method to call
        :param args: Method arguments
        :param kwargs: Method keyword arguments
        """
        result = None
        try:
            result = method(*args, **kwargs)
        except Exception as ex:
            _logger.exception("Error executing %s: %s", method.__name__, ex)
        finally:
            # Task executed
            event.set(result)

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

        # Call the runner
        AppHelper.callAfter(self.__ui_runner, event, method, args, kwargs)

        # Wait for it
        return event.wait()

    def setup(self, argv=None):
        """
        Sets up the Cocoa loop
        """
        # Create the application
        self._app = Cocoa.NSApplication.sharedApplication()
        self._argv = argv

    def loop(self):
        """
        Event loop
        """
        # Bring app to top
        Cocoa.NSApp.activateIgnoringOtherApps_(True)

        # Main loop
        AppHelper.runEventLoop(self._argv)

    def stop(self):
        """
        Stops the loop
        """
        AppHelper.stopEventLoop()
