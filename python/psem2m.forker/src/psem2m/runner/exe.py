#-- Content-Encoding: UTF-8 --
"""
Runner for any executable files

:author: Thomas Calmant
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Invalidate, \
    Validate, Provides, Instantiate

# ------------------------------------------------------------------------------

import psem2m.runner.commons as runner

# ------------------------------------------------------------------------------

import logging
_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

ISOLATE_HTTP_PORT = "HTTP_PORT"
""" Python isolate HTTP Port environment variable """

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-runner-exe")
@Instantiate("ExeRunner")
@Provides("org.psem2m.isolates.forker.IIsolateRunner")
class ExecutableRunner(runner.Runner):
    """
    Executable file runner
    """
    def can_handle(self, kind):
        """
        Tests if this runner can start an isolate of the given kind
        
        :param kind: Kind of the isolate to run
        :return: True if the isolate can be started by this runner
        """
        return kind == "exec"


    def _get_executable(self, isolate_descr):
        """
        Retrieves the path to the executable to run for that isolate
        
        :param isolate_descr: The configuration of the isolate to run
        :return: The path to the executable, or None
        """
        # Get the list of bundles in the configuration
        bundles = isolate_descr["bundles"]
        if len(bundles) != 1:
            raise ValueError("There must be exactly one executable file name "
                             "in the bundles configuration list")

        # Get the name of the module to run
        executable = bundles[0]["symbolicName"]
        if not executable:
            raise ValueError("Empty executable name")

        return executable


    def _make_args(self, isolate_descr):
        """
        Prepares the executable arguments
        
        :param isolate_descr: A dictionary describing the isolate
        :return: The parameters to give to the interpreter (array)
        """
        # Prepare arguments
        args = []

        # VM arguments (if any)
        vm_args = isolate_descr.get("vmArgs", None)
        if hasattr(vm_args, "__iter__"):
            args.extend(vm_args)

        # Program argument (if any, after VMArgs)
        app_args = isolate_descr.get("appArgs", None)
        if hasattr(app_args, "__iter__"):
            # Got an iterable object
            args.extend(app_args)

        return args


    def _make_env(self, isolate_descr):
        """
        Retrieves the process environment variables to be set.
        
        :return: The isolate environment variables
        """
        # Call the parent method
        env = super(ExecutableRunner, self)._make_env(isolate_descr)
        if env is None:
            # Parent did nothing
            env = {}

        # Set up the PSEM2M Signals port variable, just in case
        env[ISOLATE_HTTP_PORT] = isolate_descr["httpPort"]
