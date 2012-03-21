#-- Content-Encoding: utf-8 --
"""
Utility methods implementations for POSIX

@author: Thomas Calmant
"""

import os
import sys
import psem2m
import psem2m.runner.commons as commons


class Utils(commons.OSSpecificUtils):
    """
    Utility class implementation for POSIX
    """
    def __init__(self, psem2m_utils):
        """
        Sets up the utility methods for POSIX
        
        :param psem2m_utils: PSEM2M Utilities instance
        """
        commons.OSSpecificUtils.__init__(self, psem2m_utils)


    def find_java_interpreter(self, java_home):
        """
        Finds the Java interpreter, in the given Java Home if possible
        
        :param java_home: The preferred Java home
        :return: The path to the first Java interpreter found, or None
        """
        # Case 1 : Try "preferred" JVM (embedded one)
        java = self._test_java_path(java_home)
        if java != None:
            return java

        # Case 2 : Try with JAVA_HOME environment variable
        java_home = os.getenv(psem2m.JAVA_HOME)
        java = self._test_java_path(java_home)
        if java != None:
            return java

        # Case 3 : Try with all with PATH
        return commons.find_in_path("java")


    def find_python2_interpreter(self):
        """
        Finds a Python 2 interpreter
        
        :return: The path to the first Python 2 interpreter found, or None
        """
        # Try with embedded interpreter first
        psem2m_home = os.getenv(psem2m.PSEM2M_HOME)
        if psem2m_home is not None:
            path = os.path.join(psem2m_home, "bin", "python2")
            if os.path.exists(path):
                return os.path.abspath(path)

        # Try with current interpreter
        if sys.version_info[0] == 2 and sys.executable is not None:
            return sys.executable

        # Try with common names
        common_names = ("python", "python2", "python2.6", "python2.7")
        for name in common_names:
            found = commons.find_in_path(name)
            if found is not None:
                return found

        # Not found
        return None


    def find_python3_interpreter(self):
        """
        Finds a Python 3 interpreter
        
        :return: The path to the first Python 3 interpreter found, or None
        """
        # Try with embedded interpreter first
        psem2m_home = os.getenv(psem2m.PSEM2M_HOME)
        if psem2m_home is not None:
            path = os.path.join(psem2m_home, "bin", "python3")
            if os.path.exists(path):
                return os.path.abspath(path)

        # Try with current interpreter
        if sys.version_info[0] == 3 and sys.executable is not None:
            return sys.executable

        # Try with common names
        common_names = ("python3", "python3.1", "python3.2")
        for name in common_names:
            found = commons.find_in_path(name)
            if found is not None:
                return found

        # Not found
        return None


    def is_process_running(self, pid):
        """
        Tests if the given process is running
        
        :param pid: PID of the process to test
        """
        if pid < 0:
            # Invalid PID
            return False

        try:
            os.kill(pid, 0)

        except OSError as ex:
            import errno
            return ex.errno == errno.EPERM

        else:
            # No exception
            return True


    def _test_java_path(self, java_home):
        """
        Tries to return the path to a Java interpreter
        
        :param java_home: The Java home to test
        :return: The Java interpreter path or None
        """
        if not java_home:
            return None

        java = os.path.join(java_home, "bin", "java")
        if commons.is_file(java):
            return java

        return None
