#-- Content-Encoding: utf-8 --
"""
Utility methods implementations for POSIX

@author: Thomas Calmant
"""

from psem2m.runner.commons import OSSpecificUtils
import os
import psem2m
import psem2m.runner.commons as commons


class Utils(OSSpecificUtils):
    """
    Utility class implementation for POSIX
    """

    # Java interpreter name
    JAVA_INTERPRETER = "java"

    # Java interpreter path under the Java home path
    JAVA_SUBPATH = "bin" + os.sep + JAVA_INTERPRETER

    def __init__(self, psem2m_utils):
        """
        Sets up the utility methods for POSIX
        
        @param psem2m_utils: PSEM2M Utilities instance
        """
        OSSpecificUtils.__init__(self, psem2m_utils)


    def find_java_interpreter(self, java_home):
        """
        Finds the Java interpreter, in the given Java Home if possible
        
        @param java_home: The preferred Java home
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
        paths = os.getenv("PATH").split(os.pathsep)
        for path in paths:
            java = os.path.join(path, Utils.JAVA_INTERPRETER)
            if commons.is_file(java):
                return java

        return None


    def _test_java_path(self, java_home):
        """
        Tries to return the path to a Java interpreter
        
        @param java_home: The Java home to test
        @return: The Java interpreter path or None
        """
        if not java_home:
            return None

        java = os.path.join(java_home, Utils.JAVA_SUBPATH)
        if commons.is_file(java):
            return java

        return None
