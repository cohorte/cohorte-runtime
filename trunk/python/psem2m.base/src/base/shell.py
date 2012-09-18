#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Created on 18 sept. 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import pelix.framework as pelix


import logging
import shlex
import sys

# ------------------------------------------------------------------------------

SHELL_SERVICE_SPEC = "pelix.shell"
DEFAULT_NAMESPACE = "default"

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

def _find_assignment(arg_token):
    """
    Find the first non-escaped assignment in the given argument token.
    Returns -1 if no assignment was found.
    
    :param arg_token: The argument token
    :return: The index of the first assignment, or -1
    """
    idx = arg_token.find('=')
    while idx != -1:
        if idx != 0:
            if arg_token[idx - 1] != '\\':
                # No escape character
                return idx

        idx = arg_token.find('=', idx + 1)

    # No assignment found
    return -1


def _make_args(args_list):
    """
    Converts the given list of arguments into a list (args) and a
    dictionary (kwargs).
    All arguments with an assignment are put into kwargs, others in args.
    
    :param args_list: The list of arguments to be treated
    :return: The (arg_token, kwargs) tuple.
    """
    args = []
    kwargs = {}

    for arg_token in args_list:
        idx = _find_assignment(arg_token)
        if idx != -1:
            # Assignment
            key = arg_token[:idx]
            value = arg_token[idx + 1:]
            kwargs[key] = value

        else:
            # Direct argument
            args.append(arg_token)

    return args, kwargs


def _split_ns_command(cmd_token):
    """
    Extracts the name space and the command name of the given command token
    
    :param cmd_token: The command token
    :return: The extracted (name space, command) tuple
    """
    namespace = None
    cmd_split = cmd_token.split(':', 1)
    if len(cmd_split) == 1:
        # No name space given
        command = cmd_split[0]

    else:
        # Got a name space and a command
        namespace = cmd_split[0]
        command = cmd_split[1]

    if not namespace:
        # No name space given or empty one
        namespace = DEFAULT_NAMESPACE

    # Use lower case values only
    return namespace.lower(), command.lower()

# ------------------------------------------------------------------------------

def bundlestate_to_str(state):
    """
    Converts a bundle state integer to a string
    """
    states = {
              pelix.Bundle.INSTALLED: "INSTALLED",
              pelix.Bundle.ACTIVE:"ACTIVE",
              pelix.Bundle.RESOLVED:"RESOLVED",
              pelix.Bundle.STARTING:"STARTING",
              pelix.Bundle.STOPPING:"STOPPING",
              pelix.Bundle.UNINSTALLED:"UNINSTALLED"
    }

    if state in states:
        return states[state]

    return "UNKNOWN STATE (%d)".format(state)

# ------------------------------------------------------------------------------

def make_table(headers, lines):
    """
    Generates an ASCII table according to the given headers and lines
    """
    if lines and len(headers) != len(lines[0]):
        raise ValueError("Different sizes for header and lines")

    # Maximum lengths
    lengths = [len(title) for title in headers]

    # Lines
    for line in lines:
        # Recompute lengths
        i = 0
        for entry in line:
            if len(entry) > lengths[i]:
                lengths[i] = len(entry)
            i += 1

    # Prepare the head (centered text)
    format_str = "|"
    i = 0
    for length in lengths:
        format_str += " {%d:^%d} |" % (i, length)
        i += 1

    head_str = format_str.format(*headers)

    # Prepare the separator, according the length of the headers string
    separator = '-' * len(head_str)
    idx = head_str.find('|')
    while idx != -1:
        separator = '+'.join((separator[:idx], separator[idx + 1:]))
        idx = head_str.find('|', idx + 1)

    # Prepare the output
    output = []
    output.append(separator)
    output.append(head_str)
    output.append(separator)

    # Compute the lines
    format_str = format_str.replace('^', '<')
    for line in lines:
        output.append(format_str.format(*line))
        output.append(separator)

    # Join'em
    return '\n'.join(output)

# ------------------------------------------------------------------------------

class Shell(object):
    """
    A simple shell, based on shlex.
    
    Allows to use name spaces.
    """
    def __init__(self, context):
        """
        Sets up the shell
        
        :param context: The bundle context
        """
        self._commands = {}
        self._context = context

        # Register basic commands
        self.register_command(None, "lb", self.list_bundles)
        self.register_command(None, "ls", self.list_services)
        self.register_command(None, "start", self.start)
        self.register_command(None, "stop", self.stop)
        self.register_command(None, "update", self.update)
        self.register_command(None, "install", self.install)

        self.register_command(None, "help", self.print_help)
        self.register_command(None, "?", self.print_help)

        self.register_command(None, "quit", self.quit)
        self.register_command(None, "close", self.quit)
        self.register_command(None, "exit", self.quit)


    def register_command(self, namespace, command, method):
        """
        Registers the given command to the shell.
        
        The namespace can be None, empty or "default" 
        
        :param namespace: The command name space.
        :param command: The shell name of the command
        :param method: The method to call
        :return: True if the method has been registered, False if it was already
                 known or invalid
        """
        if not namespace:
            namespace = DEFAULT_NAMESPACE

        if not command:
            _logger.error("No command name given")
            return False

        if method is None:
            _logger.error("No method given for %s:%s", namespace, command)
            return False

        # Store everything in lower case
        namespace = namespace.lower()
        command = command.lower()

        if namespace not in self._commands:
            space = self._commands[namespace] = {}
        else:
            space = self._commands[namespace]

        if command in space:
            _logger.error("Command already registered: %s:%s", namespace,
                          command)
            return False

        space[command] = method
        return True


    def unregister(self, namespace, command=None):
        """
        Unregisters the given command. If command is None, the whole name space
        is unregistered.
        
        :param namespace: The command name space.
        :param command: The shell name of the command, or None
        :return: True if the command was known, else False
        """
        if not namespace:
            namespace = DEFAULT_NAMESPACE

        namespace = namespace.lower()

        if namespace not in self._commands:
            _logger.warning("Unknown name space: %s", namespace)
            return False

        if command is not None:
            # Remove the command
            if command not in self._commands[namespace]:
                _logger.warning("Unknown command: %s:%s", namespace, command)
                return False

            del self._commands[namespace][command]

            # Remove the name space if necessary
            if not self._commands[namespace]:
                del self._commands[namespace]

        else:
            # Remove the whole name space
            del self._commands[namespace]

        return True


    def execute(self, cmdline, stdin=sys.stdin, stdout=sys.stdout):
        """
        Executes the command corresponding to the given line
        """
        # Split the command line
        if not cmdline:
            return False

        line_split = shlex.split(cmdline, True, True)
        if not line_split:
            return False

        namespace, command = _split_ns_command(line_split[0])

        # Get the space
        space = self._commands.get(namespace, None)
        if not space:
            _logger.warning("Unknown name space: %s", namespace)
            stdout.write("Unknown name space %s" % namespace)
            stdout.flush()
            return False

        # Get the method
        method = space.get(command, None)
        if method is None:
            _logger.warning("Unknown command: %s:%s", namespace, command)
            stdout.write("Unknown command: %s:%s" % (namespace, command))
            stdout.flush()
            return False

        # Make arguments and keyword arguments
        args, kwargs = _make_args(line_split[1:])

        # Execute it
        try:
            return method(stdin, stdout, *args, **kwargs)

        except TypeError as ex:
            # Invalid arguments...
            _logger.exception("Invalid method call: %s", ex)
            stdout.write("Invalid method call: %s\n" % (ex))
            stdout.flush()
            return False


    def get_banner(self):
        """
        Returns the Shell banner
        """
        return "** Pelix Shell prompt **"


    def get_ps1(self):
        """
        Returns the PS1, the basic shell prompt
        """
        return "$ "


    def list_bundles(self, stdin, stdout):
        """
        Lists the bundles in the framework and their state
        """
        # Head of the table
        headers = ('ID', 'Name', 'State', 'Version')

        # Get the bundles
        bundles = self._context.get_bundles()

        # The framework is not in the result of get_bundles()
        bundles.insert(0, self._context.get_bundle(0))

        # Make the entries
        lines = []
        for bundle in bundles:
            # Make the line
            line = [str(entry) for entry in
                    (bundle.get_bundle_id()), bundle.get_symbolic_name(),
                    bundlestate_to_str(bundle.get_state()),
                    bundle.get_version()]

            lines.append(line)

        # Print'em all
        stdout.write(make_table(headers, lines) + "\n")
        stdout.flush()


    def list_services(self, stdin, stdout):
        """
        Lists the services in the framework
        """
        # Head of the table
        headers = ('ID', 'Specifications', 'Bundle', 'Ranking')

        # Lines
        references = self._context.get_all_service_references(None, None)

        # Use the reverse order (ascending service IDs instead of descending)
        references.reverse()

        lines = []
        for ref in references:
            # Make the line
            line = [str(entry) for entry in
                    (ref.get_property(pelix.SERVICE_ID),
                     ref.get_property(pelix.OBJECTCLASS),
                     ref.get_bundle(),
                     ref.get_property(pelix.SERVICE_RANKING))]

            lines.append(line)

        # Print'em all
        stdout.write(make_table(headers, lines) + "\n")
        stdout.flush()


    def print_help(self, stdin, stdout):
        """
        Prints the available methods and there documentation
        """
        namespaces = [ns for ns in self._commands.keys()]
        namespaces.remove(DEFAULT_NAMESPACE)
        namespaces.sort()
        namespaces.insert(0, DEFAULT_NAMESPACE)

        for ns in namespaces:
            stdout.write("* Namespace '%s':\n" % ns)
            names = [command for command in self._commands[ns]]
            names.sort()

            for name in names:
                stdout.write("- %s\n" % name)
                doc = getattr(self._commands[ns][name], '__doc__',
                              "(Documentation missing)")
                stdout.write("\t\t%s\n" % ' '.join(doc.split()))

        stdout.flush()


    def quit(self, stdin, stdout):
        """
        Stops the current shell session (raises a KeyboardInterrupt exception)
        """
        raise KeyboardInterrupt()


    def start(self, stdin, stdout, bundle_id):
        """
        Starts the given bundle ID
        """
        bundle_id = int(bundle_id)
        bundle = self._context.get_bundle(bundle_id)
        if bundle is None:
            stdout.write("Unknown bundle: %d", bundle_id)

        bundle.start()


    def stop(self, stdin, stdout, bundle_id):
        """
        Stops the given bundle ID
        """
        bundle_id = int(bundle_id)
        bundle = self._context.get_bundle(bundle_id)
        if bundle is None:
            stdout.write("Unknown bundle: %d\n" % bundle_id)

        bundle.stop()


    def update(self, stdin, stdout, bundle_id):
        """
        Updates the given bundle ID
        """
        bundle_id = int(bundle_id)
        bundle = self._context.get_bundle(bundle_id)
        if bundle is None:
            stdout.write("Unknown bundle: %d\n" % bundle_id)

        bundle.update()


    def install(self, stdin, stdout, location):
        """
        Installs the given bundle
        """
        bundle_id = self._context.install_bundle(location)
        stdout.write("Bundle ID: %d\n" % bundle_id)


# ------------------------------------------------------------------------------

class PelixActivator(object):
    """
    Activator class for Pelix
    """
    def __init__(self):
        """
        Sets up the activator
        """
        self._svc_reg = None


    def start(self, context):
        """
        Bundle starting
        
        :param context: The bundle context
        """
        try:
            self._svc_reg = context.register_service(SHELL_SERVICE_SPEC,
                                                     Shell(context), {})
            _logger.info("Shell service registered")

        except pelix.BundleException as ex:
            _logger.exception("Error registering the shell service: %s", ex)


    def stop(self, context):
        """
        Bundle stopping
        
        :param context: The bundle context
        """
        if self._svc_reg is not None:
            self._svc_reg.unregister()
            _logger.info("Shell service unregistered")
            self._svc_reg = None


# Activator for Pelix
activator = PelixActivator()
