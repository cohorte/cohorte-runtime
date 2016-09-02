# -- Content-Encoding: utf-8 --
"""
Utility methods implementations for POSIX

**TODO:**
* Complete review/refactoring

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
import errno
import os
import signal
import sys
import time

# Cohorte
import cohorte
import cohorte.utils as utils

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------


class OSUtils(utils.BaseOSUtils):
    """
    Utility class implementation for POSIX
    """
    def find_java_interpreter(self, java_home):
        """
        Finds the Java interpreter, in the given Java Home if possible

        :param java_home: The preferred Java home
        :return: The path to the first Java interpreter found, or None
        """
        # Case 1 : Try "preferred" JVM (embedded one)
        java = self._test_java_path(java_home)
        if java is not None:
            return java

        # Case 2 : Try with JAVA_HOME environment variable
        java_home = os.getenv(cohorte.ENV_JAVA_HOME)
        java = self._test_java_path(java_home)
        if java is not None:
            return java

        # Case 3 : Try with all with PATH
        return utils.find_in_path("java")

    def find_python2_interpreter(self):
        """
        Finds a Python 2 interpreter

        :return: The path to the first Python 2 interpreter found, or None
        """
        # Try with embedded interpreter first
        if self.home is not None:
            path = os.path.join(self.home, "bin", "python2")
            if os.path.exists(path):
                return os.path.abspath(path)

        # Try with current interpreter
        if sys.version_info[0] == 2 and sys.executable is not None:
            return sys.executable

        # Try with common names
        common_names = ("python", "python2", "python2.7")
        for name in common_names:
            found = utils.find_in_path(name)
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
        if self.home is not None:
            path = os.path.join(self.home, "bin", "python3")
            if os.path.exists(path):
                return os.path.abspath(path)

        # Try with current interpreter
        if sys.version_info[0] == 3 and sys.executable is not None:
            return sys.executable

        # Try with common names
        common_names = ("python3", "python3.3", "python3.4", "python3.5")
        for name in common_names:
            found = utils.find_in_path(name)
            if found is not None:
                return found

        # Not found
        return None

    @staticmethod
    def is_process_running(pid):
        """
        Tests if the given process is running

        :param pid: PID of the process to test
        :return: True if the process is running else False
        """
        if pid <= 0:
            # Invalid PID
            return False

        try:
            os.kill(pid, 0)
        except OSError as ex:
            return ex.errno == errno.EPERM
        else:
            # No exception
            return True

    def kill_pid(self, pid):
        """
        Kills the given PID, if possible

        :param pid: PID of the process to kill
        :raise ValueError: Invalid PID
        :raise OSError: Unauthorized operation
        """
        if pid is None or not self.is_process_running(pid):
            raise ValueError("Invalid PID: {0:d}".format(pid))

        os.kill(pid, signal.SIGKILL)

    def wait_pid(self, pid, timeout=None):
        """
        Waits for process with the given PID to terminate and return its
        exit status code as an integer.

        If PID is not a children of os.getpid() (current process) just
        waits until the process disappears and return None.

        If pid does not exist at all return None immediately.

        Code from the psutil Python library:
        Copyright (c) 2009, Jay Loden, Giampaolo Rodola'. All rights reserved.

        :param pid: The PID to wait for
        :param timeout: The maximum time to wait, in seconds.
                        None to wait forever
        :raise TimeoutExpired: when timeout expired.
        """
        def check_timeout(delay):
            """
            Tests if the time out has expired

            :param delay: Delay before the method returns
            :return: The next delay value (max: 4 ms)
            """
            if timeout is not None:
                if time.time() >= stop_at:
                    raise utils.TimeoutExpired(pid)
            time.sleep(delay)
            return min(delay * 2, 0.04)

        if timeout is not None:
            def wait_call():
                return os.waitpid(pid, os.WNOHANG)
            stop_at = time.time() + timeout
        else:
            def wait_call():
                return os.waitpid(pid, 0)

        delay = 0.0001
        while 1:
            try:
                retpid, status = wait_call()
            except OSError as err:
                if err.errno == errno.EINTR:
                    delay = check_timeout(delay)
                    continue
                elif err.errno == errno.ECHILD:
                    # This has two meanings:
                    # - pid is not a child of os.getpid() in which case
                    #   we keep polling until it's gone
                    # - pid never existed in the first place
                    # In both cases we'll eventually return None as we
                    # can't determine its exit status code.
                    while 1:
                        if self.is_process_running(pid):
                            delay = check_timeout(delay)
                        else:
                            return
                else:
                    raise
            else:
                if retpid == 0:
                    # WNOHANG was used, pid is still running
                    delay = check_timeout(delay)
                    continue
                # process exited due to a signal; return the integer of
                # that signal
                if os.WIFSIGNALED(status):
                    return os.WTERMSIG(status)
                # process exited using exit(2) system call; return the
                # integer exit(2) system call has been called with
                elif os.WIFEXITED(status):
                    return os.WEXITSTATUS(status)
                else:
                    # should never happen
                    raise RuntimeError("Unknown process exit status")

    @staticmethod
    def _test_java_path(java_home):
        """
        Tries to return the path to a Java interpreter

        :param java_home: The Java home to test
        :return: The Java interpreter path or None
        """
        if not java_home:
            return None

        java = os.path.join(java_home, "bin", "java")
        if utils.is_file(java):
            return java

        return None
