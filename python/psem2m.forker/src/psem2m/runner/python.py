#-- Content-Encoding: UTF-8 --
"""
Python 2, Python 3 and Java runners

@author: Thomas Calmant
"""

from psem2m.component.decorators import ComponentFactory, Invalidate, Validate, \
    Provides, Instantiate

# ------------------------------------------------------------------------------

import psem2m.runner.commons as runner

# ------------------------------------------------------------------------------

import logging
_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

# Python isolate HTTP Port
ISOLATE_HTTP_PORT = "HTTP_PORT"

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-runner-python")
@Instantiate("PythonRunner")
@Provides("org.psem2m.isolates.forker.IIsolateRunner")
class PythonRunner(runner.Runner):
    """
    Python 2 and Python 3 isolate runner
    """
    def __init__(self):
        """
        Constructor
        """
        runner.Runner.__init__(self)
        self._python2_path = None
        self._python3_path = None


    def can_handle(self, kind):
        """
        Tests if this runner can start an isolate of the given kind
        
        :param kind: Kind of the isolate to run
        :return: True if the isolate can be started by this runner
        """
        return kind in ("python", "python3")


    def _get_executable(self, isolate_descr):
        """
        Retrieves the path to the executable to run for that isolate
        
        :param isolate_descr: The configuration of the isolate to run
        :return: The path to the executable, or None
        """
        kind = isolate_descr["kind"]

        if kind == "python":
            return self._python2_path

        elif kind == "python3":
            return self._python3_path

        return None


    def _make_args(self, isolate_descr):
        """
        Prepares the Python interpreter arguments
        
        :param isolate_descr: A dictionary describing the isolate
        :return: The parameters to give to the interpreter (array)
        """
        # Get the list of bundles in the configuration
        bundles = isolate_descr["bundles"]
        if len(bundles) != 1:
            raise ValueError("There must be exactly one module in the bundles"
                             " configuration list")

        # Get the name of the module to run
        module = bundles[0]
        if not module:
            raise ValueError("Empty module name")

        # Return the interpreter parameter
        return ["-m", module]


    def _make_env(self, isolate_descr):
        """
        Retrieves the process environment variables to be set.
        
        :return: The isolate environment variables
        """
        # Call the parent method
        env = super(PythonRunner, self)._make_env(isolate_descr)
        if env is None:
            # Parent did nothing
            env = {}

        # Set up Python specific values
        env[ISOLATE_HTTP_PORT] = isolate_descr["httpPort"]


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        self._python2_path = self._utils.find_python2_interpreter()
        self._python3_path = self._utils.find_python3_interpreter()


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        self._python2_path = None
        self._python3_path = None
