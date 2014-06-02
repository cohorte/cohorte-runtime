#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Defines the main thread handler for Cocoa applications

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
"""

# Module version
__version_info__ = (0, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

# Cocoa
import Cocoa
from PyObjCTools import AppHelper

# Looper utilities
import cohorte.boot.looper.utils as utils

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

def get_looper(*args, **kwargs):
    """
    Constructs the CocoaLoader
    """
    return CocoaLoader()

# ------------------------------------------------------------------------------

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
        event = utils.EventResult()

        # Call the runner
        AppHelper.callAfter(self.__ui_runner, event, method, args, kwargs)

        # Wait for it
        return event.get()


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
