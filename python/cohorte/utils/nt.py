# -- Content-Encoding: utf-8 --
"""
Utility methods implementations for Win32

**TODO:**
* Complete review/refactoring
* Tests

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
import logging
import os
import sys

# Cohorte
import cohorte
import cohorte.utils as utils

# Windows specific modules
# pylint: disable=F0401
import pywintypes
import win32api
import win32event
import win32process

try:
    # Python 3
    import winreg
except ImportError:
    # Python 2
    import _winreg as winreg

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# From http://msdn.microsoft.com/en-us/library/ms681382%28v=VS.85%29.aspx
ERROR_INVALID_PARAMETER = 0x57

# From http://msdn.microsoft.com/en-us/library/ms684880%28v=VS.85%29.aspx
SYNCHRONIZE = 0x00100000
PROCESS_TERMINATE = 1
PROCESS_QUERY_INFORMATION = 0x0400

# From http://msdn.microsoft.com/en-us/library/ms683189%28v=VS.85%29.aspx
STILL_ACTIVE = 259

# From windows.h
INFINITE = 0xFFFFFFFF

# From win32.h
WAIT_TIMEOUT = 0x00000102
WAIT_FAILED = 0xFFFFFFFF

# ------------------------------------------------------------------------------


def get_registry_java_home():
    """
    Retrieves the value of the JavaHome registry key
    """
    jre_keys = (r"SOFTWARE\JavaSoft\Java Runtime Environment",
                r"SOFTWARE\Wow6432Node\JavaSoft\Java Runtime Environment")
    for jre_key_name in jre_keys:
        try:
            jre_key = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, jre_key_name)

            # Compute current version key name
            value = winreg.QueryValueEx(jre_key, "CurrentVersion")
            if not value:
                _logger.warning("No 'current' JVM in registry.")
                return None

            # Close the key
            winreg.CloseKey(jre_key)

            # Get its JavaHome
            current_jre_key_name = jre_key_name + "\\" + value[0]
            jre_key = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE,
                                     current_jre_key_name)

            value = winreg.QueryValueEx(jre_key, "JavaHome")
            if not value:
                _logger.warning("No current JavaHome in registry.")
                return None

            # Value found
            return utils.remove_quotes(value[0])
        except WindowsError as ex:
            _logger.warning("Java path lookup error in the registry: %s (%s)",
                            ex, jre_key_name)

    _logger.error("Java Runtime not found in registry")
    return None


class OSUtils(utils.BaseOSUtils):
    """
    Utility class implementation for Win32
    """
    def find_java_interpreter(self, java_home):
        """
        Finds the Java interpreter, in the given Java Home if possible

        :param java_home: The preferred Java home
        """
        # Case 1 : Try "preferred" JVM (embedded one)
        java_home = utils.remove_quotes(java_home)
        java = self._test_java_path(java_home)
        if java is not None:
            return java

        # Case 2 : Use registry
        java_home = get_registry_java_home()
        java = self._test_java_path(java_home)
        if java is not None:
            return java

        # Case 3 : Try with JAVA_HOME environment variable
        java_home = utils.remove_quotes(os.getenv(cohorte.ENV_JAVA_HOME))
        java = self._test_java_path(java_home)
        if java is not None:
            return java

        # Case 4 : Try with all with PATH
        return utils.find_in_path("java.exe")

    def find_python2_interpreter(self):
        """
        Finds a Python 2 interpreter

        :return: The path to the first Python 2 interpreter found, or None
        """
        # Try with embedded interpreter first
        if self.home is not None:
            path = os.path.join(self.home, "bin", "python2", "bin",
                                "python.exe")
            if os.path.exists(path):
                return os.path.abspath(path)

        # Try with current interpreter
        if sys.version_info[0] == 2 and sys.executable is not None:
            return sys.executable

        # TODO: try with the registry

        # Try in the path
        return utils.find_in_path("python3.exe")

    def find_python3_interpreter(self):
        """
        Finds a Python 3 interpreter

        :return: The path to the first Python 3 interpreter found, or None
        """
        # Try with embedded interpreter first
        if self.home is not None:
            path = os.path.join(self.home, "bin", "python3", "bin",
                                "python3.exe")
            if os.path.exists(path):
                return os.path.abspath(path)

        # Try with current interpreter
        if sys.version_info[0] == 3 and sys.executable is not None:
            return sys.executable

        # TODO: try with the registry

        # Try in the path
        return utils.find_in_path("python.exe")

    @staticmethod
    def is_process_running(pid):
        """
        Tests if the given process is running

        :param pid: PID of the process to test
        :return: True if the process is running else False
        """
        if pid < 0:
            # Invalid PID
            return False

        try:
            # Windows loves handles
            handle = win32api.OpenProcess(PROCESS_QUERY_INFORMATION,
                                          False, pid)
        except pywintypes.error as ex:
            # PID not in the system anymore
            if ex.winerror == ERROR_INVALID_PARAMETER:
                return False

            # Other kind of exception
            raise ex

        if not handle:
            # OpenProcess failed
            return False

        # Look at the process state
        exit_code = win32process.GetExitCodeProcess(handle)

        # Clean the place before leaving
        win32api.CloseHandle(handle)

        # Return real state
        return exit_code == STILL_ACTIVE

    def kill_pid(self, pid):
        """
        Kills the given PID, if possible

        :param pid: PID of the process to kill
        :raise ValueError: Invalid PID
        :raise OSError: Unauthorized operation
        """
        if pid is None or not self.is_process_running(pid):
            raise ValueError("Invalid PID: {0:d}".format(pid))

        handle = None
        try:
            handle = win32api.OpenProcess(PROCESS_TERMINATE, False, pid)
            win32api.TerminateProcess(handle, -1)
        except pywintypes.error as ex:
            # PID not in the system anymore
            if ex.winerror == ERROR_INVALID_PARAMETER:
                raise ValueError("Invalid PID: {0:d}".format(pid))

            # Other kind of exception
            raise ex
        finally:
            if handle is not None:
                win32api.CloseHandle(handle)

    @staticmethod
    def wait_pid(pid, timeout=None):
        """
        Waits for process with the given PID to terminate and return its
        exit status code as an integer.

        If PID is not a children of os.getpid() (current process) just
        waits until the process disappears and return None.

        If pid does not exist at all return None immediately.

        Raise TimeoutExpired on timeout expired.

        Code converted from C from the psutil Python library:
        Copyright (c) 2009, Jay Loden, Giampaolo Rodola'. All rights reserved.

        :param pid: The PID to wait for
        :param timeout: The maximum time to wait, in seconds.
                        None to wait forever
        :raise TimeoutExpired: when timeout expired.
        """
        if pid == 0:
            return None

        try:
            # Windows loves handles
            handle = win32api.OpenProcess(
                SYNCHRONIZE | PROCESS_QUERY_INFORMATION, False, pid)
        except pywintypes.error as ex:
            # PID not in the system anymore
            if ex.winerror == ERROR_INVALID_PARAMETER:
                return None

            # Other kind of exception
            raise ex

        if not handle:
            # OpenProcess failed
            return None

        if timeout is None:
            # There is no "None" on Windows
            timeout = INFINITE
        else:
            # Convert to an integer in milliseconds
            timeout = int(timeout * 1000)

        try:
            ret_val = win32event.WaitForSingleObject(handle, timeout)
            if ret_val == WAIT_TIMEOUT:
                # Time out raised
                raise utils.TimeoutExpired(pid)

            return win32process.GetExitCodeProcess(handle)
        finally:
            # Always clean up
            win32api.CloseHandle(handle)

    @staticmethod
    def _test_java_path(java_home):
        """
        Tries to return the path to a Java interpreter

        :param java_home: The Java home to test
        :return: The Java interpreter path or None
        """
        if not java_home:
            return None

        java = os.path.join(java_home, "bin", "java.exe")
        if utils.is_file(java):
            return java
