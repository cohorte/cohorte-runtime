#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE forker shell commands

Provides commands to the Pelix shell to work with the forker

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 0.1
:status: Alpha
"""

# Module version
__version_info__ = (0, 1, 0)
__version__ = ".".join(map(str, __version_info__))

# Documentation strings format
__docformat__ = "restructuredtext en"

# -----------------------------------------------------------------------------

# Cohorte
import cohorte

# Shell constants
from pelix.shell import SHELL_COMMAND_SPEC

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-forker-shell-commands-factory")
@Requires("_directory", cohorte.SERVICE_SIGNALS_DIRECTORY)
@Requires("_forker", cohorte.SERVICE_FORKER)
@Provides(SHELL_COMMAND_SPEC)
@Instantiate("cohorte-forker-shell-commands")
class SignalsCommands(object):
    """
    Signals shell commands
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._directory = None
        self._forker = None


    def get_namespace(self):
        """
        Retrieves the name space of this command handler
        """
        return "forker"


    def get_methods(self):
        """
        Retrieves the list of tuples (command, method) for this command handler
        """
        return [("stop", self.stop_isolate),
                ("ping", self.ping)]


    def get_methods_names(self):
        """
        Retrieves the list of tuples (command, method name) for this command
        handler.
        """
        return [(command, method.__name__)
                for command, method in self.get_methods()]


    def __get_uids(self, isolate_name_or_uid):
        """
        Retrieves a list of isolate UIDs
        
        :param isolate_name_or_uid: A name or UID of isolate (or None)
        :return: A list of UIDs
        :raise KeyError: Unknown isolate
        """
        if isolate_name_or_uid is None:
            # Return every thing (except us)
            uids = list(self._directory.get_all_isolates(None, False))

        elif self._directory.is_registered(isolate_name_or_uid):
            # Consider the argument as a UID
            uids = [isolate_name_or_uid]

        else:
            # Consider the given argument as a name, i.e. multiple UIDs
            uids = list(self._directory.get_name_uids(isolate_name_or_uid))

        if not uids:
            # No matching UID
            raise KeyError("Unknown UID/Name: {0}".format(isolate_name_or_uid))

        # Sort names, to always have the same result
        uids.sort()
        return uids


    def __ping_to_str(self, ping_result):
        """
        Converts a ping() result (integer) into a string
        
        :param ping_result: A ping result
        :return: A string
        """
        if ping_result == 0:
            return "ALIVE"

        elif ping_result == 1:
            return "DEAD"

        elif ping_result == 2:
            return "STUCK"

        else:
            return "<UNKNOWN>"


    def ping(self, io_handler, isolate=None):
        """
        ping [<isolate>] - Checks if the given isolate (name or UID) is alive
        """
        try:
            for uid in self.__get_uids(isolate):
                io_handler.write_line_no_feed("{0}...", uid)
                result = self._forker.ping(uid)
                io_handler.write_line("\r{2} - {0} ({1})", uid,
                                      self._directory.get_isolate_name(uid),
                                      self.__ping_to_str(result))

        except KeyError as ex:
            io_handler.write_line("Error: {0}", ex)


    def stop_isolate(self, io_handler, isolate):
        """
        stop <isolate> - Stops the given isolate (name or UID)
        """
        try:
            for uid in self.__get_uids(isolate):
                io_handler.write_line("Stopping {0}...", uid)
                self._forker.stop_isolate(uid)

        except KeyError as ex:
            io_handler.write_line("Error: {0}", ex)
