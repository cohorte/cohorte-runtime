#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Utility package

Provides utility modules

**TODO:** Avoid COHORTE specific stuff here -> move recursive file search
COHORTE directory into the FileFinder service

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
import importlib
import os

# Cohorte
import cohorte

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------


def get_os_utils():
    """
    Retrieves an a utility class instance, according to the host operating
    system

    :return: System utility instance
    """
    module_name = "{0}.{1}".format(__name__, os.name)
    try:
        # Do not use the __import__ statement, as it would return the package,
        # not the module
        module = importlib.import_module(module_name)
    except ImportError as ex:
        raise ImportError(
            "Can't import Utilities for OS : {0} ({1}) - {2}"
            .format(os.name, module_name, str(ex)))

    utils = getattr(module, "OSUtils", None)
    if not utils:
        raise ImportError("No OS utilities implementation found for {0} ({1})"
                          .format(os.name, module_name))

    return utils()

# ------------------------------------------------------------------------------


def find_file_recursive(file_name, root_directory):
    """
    Finds the given file in the given root directory and its children

    :param file_name: Name of the file to find
    :param root_directory: Root folder of recursion
    :return: The path to the found file, or None
    """
    for root_dirs_files in os.walk(root_directory, followlinks=True):
        if file_name in root_dirs_files[2]:
            return os.path.abspath(os.path.join(root_dirs_files[0], file_name))

    # File not found
    return None


def find_in_path(file_name):
    """
    Searches for file in the directories of the PATH environment variable

    :param file_name: The file to look for
    :return: The first found file, or None
    """
    if not file_name:
        # Nothing to do
        return None

    if file_name[0] == os.sep:
        # Remove the leading path separator
        file_name = file_name[len(os.sep):]

    paths = os.getenv("PATH", "").split(os.pathsep)
    for path in paths:
        file_path = os.path.join(path, file_name)
        if is_file(file_path):
            return file_path


def is_dir(path):
    """
    Tests if the given path points to a directory or to a link to a directory

    :param path: A file system path
    :return: True if the path points to a directory or to a link to a directory
    """
    return os.path.isdir(os.path.realpath(path))


def is_file(path):
    """
    Tests if the given path points to a file or to a link to a file

    :param path: A file system path
    :return: True if the path points to a file or to a link to a file
    """
    return os.path.isfile(os.path.realpath(path))

# ------------------------------------------------------------------------------


def remove_quotes(path):
    """
    Removes the quotes surrounding the given string, if any

    :param path: A string
    :return: The string without surrounding quotes
    """
    if not path:
        return path

    for quote in ('"', "'"):
        if path[0] == path[-1] == quote:
            return path[1:-1]

    return path

# ------------------------------------------------------------------------------


class TimeoutExpired(Exception):
    """
    Exception thrown when a time out expired
    """
    def __init__(self, pid):
        """
        Constructor

        :param pid: PID of the process that failed to respond
        """
        Exception.__init__(
            self, "Timeout expired waiting for PID: {0:d}".format(pid))

# ------------------------------------------------------------------------------


class BaseOSUtils(object):
    """
    Abstract OS-specific utility class
    """
    def __init__(self):
        """
        Constructor
        """
        # Get Cohorte home and base
        self.home = os.getenv(cohorte.ENV_HOME, os.getcwd())
        self.base = os.getenv(cohorte.ENV_BASE, self.home)

    def find_bundle_file(self, bundle_name):
        """
        Search for the given file in Cohorte local repositories

        :param bundle_name: A bundle file name
        :return: The first found file in a repo directory
        """
        return self.find_file(bundle_name, "repo")

    def find_conf_file(self, file_name):
        """
        Search for the given file in Cohorte configuration directories

        :param file_name: A file name
        :return: The first found file in a conf directory
        """
        return self.find_file(file_name, "conf")

    def find_file(self, file_name, *sub_dirs):
        """
        Finds the given file name in the given Cohorte sub-directory

        :param file_name: A file name
        :param sub_dirs: Possible sub-directories (variable arguments)
        :return: The first found file, or None
        """
        if not sub_dirs:
            # Search at least in the current directory
            sub_dirs = ["."]

        for prefix in (self.base, self.home, os.getcwd()):
            # Compute the absolute file path
            for sub_dir in sub_dirs:
                found_file = find_file_recursive(
                    file_name, os.path.join(prefix, sub_dir))
                if found_file is not None:
                    # Found
                    return found_file

        return None

    def find_java_interpreter(self, java_home):
        """
        Finds the Java interpreter, in the given Java Home if possible

        :param java_home: The preferred Java home
        :return: The path to the first Java interpreter found, or None
        """
        raise NotImplementedError(
            "This method must implemented by child class")

    def find_python2_interpreter(self):
        """
        Finds a Python 2 interpreter

        :return: The path to the first Python 2 interpreter found, or None
        """
        raise NotImplementedError(
            "This method must implemented by child class")

    def find_python3_interpreter(self):
        """
        Finds a Python 3 interpreter

        :return: The path to the first Python 3 interpreter found, or None
        """
        raise NotImplementedError(
            "This method must implemented by child class")

    @staticmethod
    def read_framework_file(file_name):
        """
        Reads the first non-commented and non-empty line of a framework file.
        Framework files contains the file name of an OSGi framework JAR file

        :param file_name: Name of the framework file to read
        :return: The framework file content, None if not found
        """
        with open(file_name) as framework_fp:
            for line in framework_fp:
                line = line.strip()

                if len(line) > 0 and not line.startswith("#"):
                    # Not a comment and not an empty line
                    return line

        # No valid line found
        return None
