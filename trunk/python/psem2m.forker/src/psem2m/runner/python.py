#-- Content-Encoding: UTF-8 --
"""
Python 2, Python 3 and Pelix runners

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

import os

# ------------------------------------------------------------------------------

ISOLATE_HTTP_PORT = "HTTP_PORT"
""" Python isolate HTTP Port environment variable """

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
        super(PythonRunner, self).__init__()
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


    def _make_args(self, isolate_descr, working_dir):
        """
        Prepares the Python interpreter arguments
        
        :param isolate_descr: A dictionary describing the isolate
        :param working_dir: The isolate working directory
        
        :return: The parameters to give to the interpreter (array)
        """
        # Get the list of bundles in the configuration
        bundles = isolate_descr["bundles"]
        if len(bundles) != 1:
            raise ValueError("There must be exactly one module in the bundles"
                             " configuration list")

        # Get the name of the module to run
        module = bundles[0]["symbolicName"]
        if not module:
            raise ValueError("Empty module name")

        # Prepare arguments
        args = []

        # Interpreter arguments from user (-m is forbidden)
        interpreter_args = isolate_descr.get("vmArgs", None)
        if hasattr(interpreter_args, "__iter__") \
        and "-m" not in interpreter_args:
            args.extend(interpreter_args)

        # The module to load
        args.append("-m")
        args.append(module)

        # Module argument
        extra_args = isolate_descr.get("appArgs", None)
        if hasattr(extra_args, "__iter__"):
            # Got an iterable object
            args.extend(extra_args)

        return args


    def _make_env(self, isolate_descr, base_env, working_dir):
        """
        Retrieves the process environment variables to be set.
        
        :param isolate_descr: Isolate description
        :param base_env: Current environment variables
        :param working_dir: The isolate working directory
        
        :return: The isolate environment variables
        """
        # Copy the existing Python path
        python_path = base_env.get("PYTHONPATH", None)

        # Call the parent method (will override environment values)
        env = super(PythonRunner, self)._make_env(isolate_descr, base_env,
                                                  working_dir)
        if env is None:
            # Parent did nothing
            env = {}

        # Set up Python specific values
        env[ISOLATE_HTTP_PORT] = str(isolate_descr["httpPort"])

        # Update the Python path
        if python_path:
            current_path = env.get("PYTHONPATH", None)
            if not current_path:
                # Python path erased, reset it
                env["PYTHONPATH"] = python_path

            else:
                # Merge elements
                path = set(current_path.split(os.pathsep))
                path |= set(python_path.split(os.pathsep))

                env["PYTHONPATH"] = os.pathsep.join(path)

        return env


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        # Python 2
        self._python2_path = self._utils.find_python2_interpreter()
        if not self._python2_path:
            _logger.warning("No Python 2 interpreter found.")
            self._python2_path = None

        else:
            _logger.debug("Python 2 interpreter: %s", self._python2_path)

        # Python 3
        self._python3_path = self._utils.find_python3_interpreter()
        if not self._python3_path:
            _logger.warning("No Python 3 interpreter found.")
            self._python3_path = None

        else:
            _logger.debug("Python 3 interpreter: %s", self._python3_path)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        self._python2_path = None
        self._python3_path = None

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-runner-pelix")
@Instantiate("PelixRunner")
@Provides("org.psem2m.isolates.forker.IIsolateRunner")
class PelixRunner(PythonRunner):
    """
    A runner for Python/Pelix isolates based on ``psem2m.forker.boot``
    """

    PELIX_LOADER_MODULE = "psem2m.forker.boot"
    """ The Pelix isolate loader module """

    def __init__(self):
        """
        Constructor
        """
        super(PelixRunner, self).__init__()
        self._context = None


    def can_handle(self, kind):
        """
        Tests if this runner can start an isolate of the given kind
        
        :param kind: Kind of the isolate to run
        :return: True if the isolate can be started by this runner
        """
        return kind in ("pelix", "pelix_py3")


    def _get_executable(self, isolate_descr):
        """
        Retrieves the path to the executable to run for that isolate
        
        :param isolate_descr: The configuration of the isolate to run
        :return: The path to the executable, or None
        """
        kind = isolate_descr["kind"]

        if kind == "pelix":
            return self._python2_path

        elif kind == "pelix_py3":
            return self._python3_path

        return None


    def _make_args(self, isolate_descr, working_dir):
        """
        Prepares the Python interpreter arguments
        
        :param isolate_descr: A dictionary describing the isolate
        :param working_dir: The isolate working directory
        :return: The parameters to give to the interpreter (array)
        """
        # Arguments list
        args = []

        # Interpreter arguments from user (-m is forbidden)
        interpreter_args = isolate_descr.get("vmArgs", None)
        if interpreter_args is not None and "-m" not in interpreter_args:
            args.extend(interpreter_args)

        # Isolate loader module
        args.append("-m")
        args.append(PelixRunner.PELIX_LOADER_MODULE)

        # Loader arguments
        args.append("--start-isolate")
        args.append(isolate_descr["id"])

        # Configuration Broker URL
        broker_url = isolate_descr.get("psem2m.configuration.broker", None)
        if broker_url is not None:
            args.append("--configuration-url")
            args.append(broker_url)

        # Directory dumper signals port
        dumper_port = isolate_descr.get("psem2m.directory.dumper.port", None)
        if dumper_port is not None:
            args.append("--directory-dumper-port")
            args.append(str(dumper_port))

        # Log file
        args.append("--logfile")
        args.append(os.path.join(working_dir, "isolate.log"))

        # Debug mode, if needed
        if self._context.get_property('psem2m.debug'):
            args.append("--debug-mode")

        if self._context.get_property('psem2m.verbose'):
            args.append("--verbose-mode")

        return args


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        super(PelixRunner, self).validate(context)
        self._context = context


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        self._context = None
        super(PelixRunner, self).invalidate(context)
