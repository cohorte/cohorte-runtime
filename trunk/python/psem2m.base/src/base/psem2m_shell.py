#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
PSEM2M shell commands for Pelix/iPOPO shell

:author: Thomas Calmant
"""

__version__ = (0, 1, 0)

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

import logging
import pprint

# ------------------------------------------------------------------------------

SHELL_COMMAND_SPEC = "ipopo.shell.command"
SHELL_UTILS_SERVICE_SPEC = "pelix.shell.utilities"

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-directory-shell-commands-factory")
@Requires("_directory", "org.psem2m.signals.ISignalDirectory")
@Requires("_utils", SHELL_UTILS_SERVICE_SPEC)
@Provides(SHELL_COMMAND_SPEC)
@Instantiate("psem2m-directory-shell-commands")
class DirectoryCommands(object):
    """
    PSEM2M signals directory shell commands
    """
    def __init__(self):
        """
        Sets up the object
        """
        self._directory = None
        self._utils = None


    def get_namespace(self):
        """
        Retrieves the name space of this command handler
        """
        return "dir"


    def get_methods(self):
        """
        Retrieves the list of tuples (command, method) for this command handler
        """
        return [("list", self.list_entries),
                ("dump", self.dump)]


    def get_methods_names(self):
        """
        Retrieves the list of tuples (command, method name) for this command
        handler.
        """
        result = []
        for command, method in self.get_methods():
            result.append((command, method.__name__))

        return result


    def list_entries(self, stdin, stdout):
        """
        Lists the isolates stored in the directory
        """
        headers = ('Isolate', 'Node', 'Host', 'Port')
        lines = []
        for isolate in self._directory.get_all_isolates(None, True):
            node = self._directory.get_isolate_node(isolate)
            access = self._directory.get_isolate_access(isolate)
            if access is None:
                host = port = "<unknown>"
            else:
                host, port = access

            lines.append((isolate, node, host, port))

        stdout.write(self._utils.make_table(headers, lines))


    def dump(self, stdin, stdout):
        """
        Prints out the result of a directory dump
        """
        raw_result = self._directory.dump()

        stdout.write(pprint.pformat(raw_result))
        stdout.write('\n')


# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-composer-shell-commands-factory")
@Requires("_composer", "org.psem2m.composer.Agent")
@Requires("_utils", SHELL_UTILS_SERVICE_SPEC)
@Provides(SHELL_COMMAND_SPEC)
@Instantiate("psem2m-composer-shell-commands")
class ComposerCommands(object):
    """
    PSEM2M Composer Agent shell commands
    """
    def __init__(self):
        """
        Sets up the object
        """
        self._composer = None
        self._utils = None


    def get_namespace(self):
        """
        Retrieves the name space of this command handler
        """
        return "composer"


    def get_methods(self):
        """
        Retrieves the list of tuples (command, method) for this command handler
        """
        return [("instances", self.list_instances)]


    def get_methods_names(self):
        """
        Retrieves the list of tuples (command, method name) for this command
        handler.
        """
        result = []
        for command, method in self.get_methods():
            result.append((command, method.__name__))

        return result


    def list_instances(self, stdin, stdout):
        """
        Lists the components instantiated by this agent
        """
        self._utils.make_table(('Instance',),
                               [(instance,)
                                for instance in self._composer.instances])
