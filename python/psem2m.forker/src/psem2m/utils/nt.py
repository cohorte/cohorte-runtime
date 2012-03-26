#-- Content-Encoding: utf-8 --
"""
Utility methods implementations for Win32

:author: Thomas Calmant
"""

import psem2m
import psem2m.utils as utils

import logging
import os
import sys

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

# From http://msdn.microsoft.com/en-us/library/ms681382%28v=VS.85%29.aspx
ERROR_INVALID_PARAMETER = 0x57

# From http://msdn.microsoft.com/en-us/library/ms684880%28v=VS.85%29.aspx
PROCESS_QUERY_INFORMATION = 0x0400

# From http://msdn.microsoft.com/en-us/library/ms683189%28v=VS.85%29.aspx
STILL_ACTIVE = 259

# ------------------------------------------------------------------------------

def get_registry_java_home():
    """
    Retrieves the value of the JavaHome registry key
    """
    try:
        jre_key_name = r"SOFTWARE\JavaSoft\Java Runtime Environment"
        jre_key = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, jre_key_name)

        # Compute current version key name
        value = winreg.QueryValueEx(jre_key, "CurrentVersion")
        if not value:
            print("Warning: No current JVM in registry.")
            return None

        # Close the key
        winreg.CloseKey(jre_key)

        # Get its JavaHome
        current_jre_key_name = jre_key_name + "\\" + value[0]
        jre_key = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, \
                                 current_jre_key_name)

        value = winreg.QueryValueEx(jre_key, "JavaHome")
        if not value:
            print("Warning: No current JavaHome in registry.")
            return None

        # Value found
        return utils.remove_quotes(value[0])

    except WindowsError as ex:
        _logger.exception("Error looking for the Java path in the registry")
        return None


class OSUtils(utils._BaseOSUtils):
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
        if java != None:
            return java

        # Case 2 : Use registry
        java_home = get_registry_java_home()
        java = self._test_java_path(java_home)
        if java != None:
            return java

        # Case 3 : Try with JAVA_HOME environment variable
        java_home = utils.remove_quotes(os.getenv(psem2m.JAVA_HOME))
        java = self._test_java_path(java_home)
        if java != None:
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


    def is_process_running(self, pid):
        """
        Tests if the given process is running
        
        :param pid: PID of the process to test
        """
        if pid < 0:
            # Invalid PID
            return False

        try:
            # Windows loves handles
            handle = win32api.OpenProcess(PROCESS_QUERY_INFORMATION, False, pid)

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


    def _test_java_path(self, java_home):
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

        return None
