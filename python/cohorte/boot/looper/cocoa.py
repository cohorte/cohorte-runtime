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

# Standard library
import logging

# Pelix utilities
import pelix.utilities as utils

# Cocoa
import cohorte.boot.looper.AppHelper as AppHelper
from cohorte.cocoapy import ObjCClass


# ------------------------------------------------------------------------------
# Module version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

NSApplication = ObjCClass('NSApplication')

# ------------------------------------------------------------------------------


def get_looper():
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

    @staticmethod
    def __ui_runner(event, method, args, kwargs):
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
        self._app = NSApplication.sharedApplication()
        self._argv = argv

    def loop(self):
        """
        Event loop
        """
        # Bring app to top
        self._app.finishLaunching()
        self._app.activateIgnoringOtherApps_(True)

        # Main loop
        AppHelper.runEventLoop(self._argv)

    @staticmethod
    def stop():
        """
        Stops the loop
        """
        AppHelper.stopEventLoop()
