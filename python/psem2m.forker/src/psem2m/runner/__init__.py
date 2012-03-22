#!/usr/bin/python
#-- Content-Encoding: utf-8 --
"""
Runners package

:author: Thomas Calmant
"""

import os
import psem2m
import psutil
import shutil

# ------------------------------------------------------------------------------

class Runner(object):
    """
    Abstract isolate runner class.
    
    Implementers must have the following methods :
    
    * ``can_handle(kind)`` : tests if the given isolate kind can be handled
      by the implementer.
    * ``_get_executable(kind)`` : returns the (full) path to the executable to
      run. Kind is the kind of isolate to run. Returns None if no executable can
      be found, or raises a ValueError.
    * ``_make_args(descr)`` : returns a list of arguments to be given to the
      process.
    
    
    Implementers should have the following method(s) :
    
    * ``_make_env(descr)`` : returns the process environment to be used. None is
      ignored.
    
    """
    def __init__(self):
        """
        Constructor
        """
        # TODO: instantiate a runner.OSSpecificUtils or let iPOPO inject one 
        self._utils = None


    def can_handle(self, kind):
        """
        Tests if this runner can start an isolate of the given kind
        
        :param kind: Kind of the isolate to run
        :return: True if the isolate can be started by this runner
        """
        return False


    def run_isolate(self, isolate_descr):
        """
        Prepares and starts the isolate described by the given dictionary.
        
        The description of the isolate must contain at least the following
        entries :
        
        * id : The ID of the isolate
        * kind : The kind of isolate
        * environment : The process environment
        
        :param isolate_descr: An isolate description dictionary
        :return: A psutil.Popen object
        :raise KeyError: A key is missing in the isolate description
        :raise ValueError: Invalid configuration value
        :raise OSError: Error during an OS level task (process start, working
                        directory creation, ...)
        """
        isolate_id = isolate_descr["id"]

        # Get the executable
        executable = self._get_executable(isolate_descr)
        if not executable:
            raise ValueError("Can't find the executable for isolate '%s'"
                             % isolate_id)

        # Get the working directory
        working_dir = self._make_working_directory(isolate_id)

        # Prepare the environment...
        env = os.environ.copy()

        # ... use configuration values
        iso_env = self._make_env(isolate_descr)
        if iso_env is not None:
            env.update(iso_env)

        # ... set up constants values
        home = os.getenv(psem2m.PSEM2M_HOME, os.getcwd())
        env[psem2m.PSEM2M_HOME] = home
        env[psem2m.PSEM2M_BASE] = os.getenv(psem2m.PSEM2M_BASE, home)
        env[psem2m.PSEM2M_ISOLATE_ID] = isolate_id

        # Prepare the interpreter arguments
        args = self._make_args(isolate_descr)

        # Run the process and return its reference
        return psutil.Popen(args, executable=executable, env=env,
                            cwd=working_dir)


    def _make_env(self, isolate_descr):
        """
        Retrieves the process environment variables to be set.
        
        The result must only contain strings keys and string values.
        
        Default implementation: returns the normalized content of
        isolate_descr["environment"].
        
        :return: The isolate environment variables
        """
        env = isolate_descr.get("environment", None)
        if not env or type(env) is not dict:
            # Nothing to do or to understand...
            return None

        # Normalize the dictionary (only strings are allowed)
        return {((str(key), str(value)) for (key, value) in env.items())}


    def _make_working_directory(self, isolate_id):
        """
        Sets up the isolate working directory, given its ID, and returns its
        path.
        
        If needed, the directory is cleaned up or created.
        
        :param isolate_id: The ID of the isolate to run
        :return: The path to the isolate working directory
        :raise OSError: Error preparing the working directory
        """
        # Get the base directory
        base = os.getenv(psem2m.PSEM2M_BASE,
                         os.getenv(psem2m.PSEM2M_HOME), os.getcwd())

        # Replace path separators by '_' in the isolate ID
        escaped_id = isolate_id.replace(os.sep, "_")

        # Get the absolute path to the working directory
        working_dir = os.path.abspath(os.path.join(base, "var", "work",
                                                   escaped_id))

        if os.path.isdir(working_dir):
            # Remove the existing directory
            shutil.rmtree(working_dir, True)

        elif os.path.exists(working_dir):
            # A file or a link already exists with this name
            os.remove(working_dir)

        # (Re-)Make the directory
        os.makedirs(working_dir)
