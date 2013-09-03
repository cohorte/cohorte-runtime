#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Defines main thread looper utility classes and methods

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 0.1
:status: Alpha
"""

# Module version
__version_info__ = (0, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

# Standard library
import threading

# ------------------------------------------------------------------------------

class EventResult(object):
    """
    Method result waiter
    """
    def __init__(self):
        """
        Sets up members
        """
        self._event = threading.Event()
        self._result = None


    def get(self, timeout=None):
        """
        Waits for a result
        
        :param timeout: Maximum time to wait for a result
        :return: The method result
        :raise ValueError: Time out reached
        """
        if not self._event.wait(timeout):
            raise ValueError("Timeout reached")

        return self._result


    def set(self, result):
        """
        Sets the method result and the event flag
        """
        self._result = result
        self._event.set()
