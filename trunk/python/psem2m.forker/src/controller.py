#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
PSEM2M Forker control script (could be used as an init.d script)

:author: Thomas Calmant
"""

import logging
import os
import sys

# ------------------------------------------------------------------------------

# Computation of the path of this file, to be able to make relative paths

CONTROLLER_PATH = os.path.dirname(__file__)
if not CONTROLLER_PATH:
    # Directory name not found
    CONTROLLER_PATH = os.getcwd()

# ------------------------------------------------------------------------------
# 
# You should modify those constants. None value means environment value.
#

PSEM2M_GIT = os.path.abspath(os.path.join(CONTROLLER_PATH,
                                          # Controller is in :
                                          # $GIT/trunk/python/psem2m.forker/src
                                          4 * ('..' + os.path.sep)))

DEFAULT_PSEM2M_HOME = "%s/platforms/psem2m.home" % PSEM2M_GIT
DEFAULT_PSEM2M_BASE = "%s/platforms/base-demo-july2012" % PSEM2M_GIT

# ------------------------------------------------------------------------------

import inspect
import json
import socket
import subprocess
import time

if sys.version_info >= (3, 0):
    # Python 3
    import http.client as httplib

else:
    # Python 2
    import httplib

# ------------------------------------------------------------------------------

PSEM2M_HOME = os.environ.get("PSEM2M_HOME", DEFAULT_PSEM2M_HOME)
PSEM2M_BASE = os.environ.get("PSEM2M_BASE", DEFAULT_PSEM2M_BASE)

# Set up the environment variables
if PSEM2M_HOME is not None:
    # Override the home directory
    os.environ["PSEM2M_HOME"] = os.path.abspath(PSEM2M_HOME)

if PSEM2M_BASE is not None:
    # Override the base directory
    os.environ["PSEM2M_BASE"] = os.path.abspath(PSEM2M_BASE)

else:
    # Use HOME as BASE by default
    os.environ["PSEM2M_BASE"] = os.getenv("PSEM2M_HOME")

# ------------------------------------------------------------------------------

# Setup the logger
_logger = logging.getLogger("PSEM2M Controller")


# Get OS utilities
import psem2m.utils
_utils = psem2m.utils.get_os_utils()

# ------------------------------------------------------------------------------

def read_file_line(base, filename):
    """
    Reads the first non-commented line of the file $base/var/$filename, or
    returns None
    
    :param base: The PSEM2M instance base directory
    :param filename: The file to read
    :return: The first line of the file, or None
    """
    if not base:
        print("Invalid base directory")
        return None

    if not filename:
        print("Invalid file name")
        return None

    # Get the file path
    data_file = os.path.join(base, "var", filename)
    if not os.path.isfile(data_file):
        # File not found
        return None

    # Read it
    with open(data_file) as data_fp:
        while True:
            # Read the line
            line = data_fp.readline()
            if not line:
                # Empty line : end of file
                return None

            line = line.strip()
            if not line.startswith("#"):
                # Ignore comments
                return line

    return None


def get_forker_process(base):
    """
    Retrieves the psutil Process object of the forker from the PSEM2M base
    directory information. Returns None if the process is not running.
    
    :param base: The PSEM2M instance base directory
    :return: The forker Process or None
    """
    pid_line = read_file_line(base, "forker.pid")
    if not pid_line:
        # No valid line found
        return None

    try:
        pid = int(pid_line)

    except ValueError as ex:
        print("Error reading the forker PID file : %s" % ex)
        return None

    if _utils.is_process_running(pid):
        # PID is valid
        return pid

    return None


# def has_forker_cmd_line(executable, argv):
#    """
#    Tests if the given executable and arguments can correspond to the forker
#    
#    :param executable: The name of an executable
#    :param argv: A list of arguments
#    :return: True if given parameters corresponds to a forker
#    """
#    if "python" not in executable:
#        # Not a Python process, ignore it
#        return False
#
#    if not argv:
#        return False
#
#    for arg in argv:
#        if "psem2m.forker" in arg:
#            # The "forker" string was found in parameters
#            return True
#
#    else:
#        # Not a forker
#        return False

# ------------------------------------------------------------------------------

SERVLET_PATH = "/psem2m-signal-receiver"
SIGNAL_NAME = "/psem2m-forker-control-command"

def send_cmd_signal(base, cmd):
    """
    Sends the given signal to the forker
    
    :param base: The PSEM2M instance base directory
    :param cmd: The command to send to the forker
    :return: True if the signal was successfully sent
    """
    # Get the forker access info
    access_info = read_file_line(base, "forker.access")
    if not access_info:
        _logger.warning("No 'forker.access' file found")
        return False

    # Read the access info line
    access_info = json.loads(access_info)

    host = access_info["host"]
    port = access_info["port"]

    # Set up the signal content
    signal = {
            "javaClass": "org.psem2m.signals.SignalData",
            "senderId": "<Controller>",
            "senderNode": "<Controller-Node>",
            "signalContent": {"javaClass": "java.util.HashMap",
                              "map":{"cmd": cmd, "args": None}},
            "timestamp": int(time.time() * 1000)
            }
    json_signal = json.dumps(signal)

    # Prepare request content
    headers = {"Content-Type": "application/json"}
    try:
        # 1 second timeout, to avoid useless waits
        conn = httplib.HTTPConnection(host, port, timeout=1)

        # Setup the signal URI
        signal_url = "%s%s" % (SERVLET_PATH, SIGNAL_NAME)

        conn.request("POST", signal_url, json_signal, headers)
        response = conn.getresponse()
        if response.status != 200:
            _logger.warn("Incorrect response for %s : %s %s",
                         signal_url, response.status,
                         response.reason)

        else:
            return True

    except socket.error as ex:
        # Socket error
        _logger.error("Error sending command to %s : %s", signal_url, str(ex))

    except:
        # Other error...
        _logger.exception("Error sending command to %s", signal_url)

    return False

# ------------------------------------------------------------------------------

def append_unique(appendable, value):
    """
    Appends the given value to the given list if it not yet in there
    """
    if not hasattr(appendable, "__in__") and not hasattr(appendable, "append"):
        # Unusable list
        raise TypeError("{0} is not handled".format(type(appendable).__name__))

    if value in appendable:
        return False

    appendable.append(value)
    return True

# ------------------------------------------------------------------------------

class Main(object):
    """
    Entry point class
    """
    def __init__(self):
        """
        Constructor
        
        :raise ValueError: Invalid environment variables
        """
        self.home = os.getenv("PSEM2M_HOME")
        if not self.home:
            raise ValueError("Invalid PSEM2M_HOME")

        self.base = os.getenv("PSEM2M_BASE", self.home)
        if not self.base:
            raise ValueError("Invalid PSEM2M_BASE")

        print("HOME = %s\nBASE = %s\n" % (self.home, self.base))


    def start(self, extra_args=None):
        """
        Starts the platform
        """
        if self._is_running():
            # Use the PID directly
            pid = get_forker_process(self.base)
            print("Forker is already running, PID: %d" % pid)
            return 1

        # Forker and monitor need to be started
        args = [sys.executable, "-m", "psem2m.forker.boot",
                "--start-forker"]

        # Activate debug mode
        if '-d' in extra_args or '--debug' in extra_args:
            args.append('--debug')

        # Start the monitor except if told otherwise
        if '-s' not in extra_args and '--single' not in extra_args:
            args.append("--with-monitor")

        # Set up environment (home and base are already there)
        env = os.environ.copy()

        # Setup the Python path
        python_path = []

        # Working directory
        append_unique(python_path, os.getcwd())

        # PSEM2M Home/base binaries and Python repository
        for root in (PSEM2M_BASE, PSEM2M_HOME):
            for path in ("bin", "python"):
                append_unique(python_path, os.path.abspath(os.path.join(root,
                                                                        path)))

        # Controller directory
        append_unique(python_path, os.path.abspath(os.path.dirname(__file__)))

        # Development libraries directory
        # FIXME: PSEM2M_GIT shouldn't be used
        append_unique(python_path, os.path.abspath(os.path.join(
                                                PSEM2M_GIT, "trunk", "python",
                                                "psem2m.base", "src")))

        existing_path = os.environ.get("PYTHONPATH")
        if existing_path:
            # Keep current path
            python_path.append(existing_path)

        env["PYTHONPATH"] = os.pathsep.join(python_path)

        # Run !
        print("Starting forker...")
        try:
            # TODO: change user before Popen
            subprocess.Popen(args, executable=args[0], env=env, close_fds=True)

        except:
            _logger.exception("Error starting the forker")
            return 1

        return 0


    def stop(self):
        """
        Stops the platform
        """
        pid = get_forker_process(self.base)
        if pid is not None:
            # FIXME: validate that it is not a refreshed PID
            # if has_forker_cmd_line(process.exe, process.cmdline):

            # Forker is running
            if send_cmd_signal(self.base, "stop"):
                # Signal sent
                print("Stop command sent")

                try:
                    # Wait 5 seconds max
                    return _utils.wait_pid(pid, 5)

                except psem2m.utils.TimeoutExpired:
                    print("Forker took too long time to stop... (PID: %d)" \
                          % pid)
                    return 3

            else:
                # Error
                print("Error sending stop command")
                return 2

        print("Forker is not running...")
        return 1


    def restart(self):
        """
        Restarts the platform
        """
        self.stop()
        return self.start()


    def _is_running(self):
        """
        Tests if the forker is running
        
        :return: True if the forker is running
        """
        pid = get_forker_process(self.base)

        if pid is not None:
            # A process with the same PID is running
            # FIXME: is it a refreshed PID ?
            # if has_forker_cmd_line(process.exe, process.cmdline):

            # Forker is running
            return True

        return False


    def try_restart(self):
        """
        The platform must be restarted only if it is actually running
        """
        if self._is_running():
            print("Platform is started...")
            return self.restart()

        else:
            print("Platform is not running")

        return 1


    def force_reload(self):
        """
        Forces the application to reload its configuration or to restart
        """
        return self.restart()


    def reload(self):
        """
        The application must reload its configuration (or restart)
        """
        return self.restart()


    def status(self):
        """
        Prints any relevant status info, and return a status code, an integer:
    
        0          program is running or service is OK
        1          program is dead and /var/run pid file exists
        2          program is dead and /var/lock lock file exists
        3          program is not running
        4          program or service status is unknown
        5-99      reserved for future LSB use
        100-149      reserved for distribution use
        150-199      reserved for application use
        200-254      reserved
    
        @see: http://dev.linux-foundation.org/betaspecs/booksets/LSB-Core-generic/LSB-Core-generic/iniscrptact.html
        """
        if self._is_running():
            pid = get_forker_process(self.base)
            print("Platform is running (forker PID: %d)" % pid)
            return 0

        else:
            print("Platform seems to be stopped")
            return 3


    def force_stop(self):
        """
        Forces the forker to stop (kills it)
        """
        if not self._is_running():
            print("Platform seems to be stopped")
            return 3

        else:
            pid = get_forker_process(self.base)
            if pid is None:
                print("Forker PID not found. Abandon.")
                return 3

            _utils.kill_pid(pid)
            print("SIGKILL signal sent to the forker")
            return 0

# ------------------------------------------------------------------------------

def print_usage():
    """
    Prints out the script usage
    """
    print("""%s (start|stop|status|restart|try-restart|reload|force-reload)

    start        : Starts the PSEM2M platform
    stop         : Stops the PSEM2M platform
    status       : Prints the platform execution status
    restart      : (Re)starts the PSEM2M platform
    try-restart  : Restarts the PSEM2M platform if it is already running
    reload       : (Re)starts the PSEM2M platform
    force-reload : (Re)starts the PSEM2M platform
""" % sys.argv[0])


def main(argv):
    """
    Entry point
    """
    if len(argv) < 2:
        print_usage()
        sys.exit(1)

    # Compute action name
    action = argv[1].strip().lower().replace("-", "_")

    try:
        app = Main()

    except ValueError as ex:
        print("Error starting controller : %s" % str(ex))
        sys.exit(1)

    if not hasattr(app, action):
        # Unknown action
        print_usage()
        sys.exit(1)

    # Get the implementation
    action_impl = getattr(app, action)
    nb_action_args = len(inspect.getargspec(action_impl).args)

    # Parse action arguments, if any
    if nb_action_args == 2:
        # Accepts an array of arguments
        # 2 arguments : self + args
        args = argv[2:]
        sys.exit(action_impl(args))

    elif nb_action_args == 1:
        # No extra arguments or expects default values
        # 1 argument : self
        sys.exit(action_impl())


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    main(sys.argv)

