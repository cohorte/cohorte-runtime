#-- Content-Encoding: utf-8 --
'''
Created on 12 déc. 2011

@author: Thomas Calmant
'''

from psem2m import PSEM2MException
from psem2m.runner.commons import PSEM2MUtils
import json
import os
import psem2m
import psem2m.runner as runner
import psem2m.runner.commons as commons
import shutil
import sys
import time

if sys.version_info >= (3, 0):
    # Python 3
    import urllib.request as urlrequest

else:
    # Python 2
    import urllib as urlrequest


def decode_bootstrap_line(line):
    """
    Decodes a bootstrap "human" (JSON) isolate status line
    
    @return: The IsolateStatus JSON dictionary, None if the line can be ignored
    """
    if "::" not in line:
        # Not a valid bootstrap line, ignore it
        return

    line_parts = line.split("::")
    if "Bootstrap.MessageSender.sendStatus" not in line_parts[0]:
        # Not an isolate status line, ignore it
        return

    # Return the parsed status
    return json.loads(line_parts[1])


class PSEM2MRunner(object):
    """
    PSEM2M instance life cycle class
    """

    # Slots, small hint for Python interpreter
    __slots__ = ("_base", "_home", "_isolate_id", "_utils", "_java")

    def __init__(self, psem2m_home=None, psem2m_base=None, \
                 isolate_id=runner.DEFAULT_ISOLATE_ID):
        """
        Sets up the PSEM2M Runner
                
        @param psem2m_home: PSEM2M home directory
        @param psem2m_base: PSEM2M base directory
        
        @raise PSEM2MException: Error while normalizing the Home directory
        """
        # Normalize PSEM2M home
        if not psem2m_home:
            psem2m_home = os.getenv(psem2m.PSEM2M_HOME)

            if not psem2m_home:
                raise PSEM2MException("Can't find the PSEM2M home directory")

        # Normalize PSEM2M base
        if not psem2m_base:
            psem2m_base = os.getenv(psem2m.PSEM2M_BASE, psem2m_home)

        # Prepare members
        self._base = psem2m_base
        self._home = psem2m_home
        self._isolate_id = isolate_id
        self._utils = PSEM2MUtils(self._home, self._base)

        # Raises a PSEM2MException if the interpreter is not found
        self._java = self._utils.find_java_exe()

        # Print out what we've found
        self.print_base_configuration()


    def clear_cache(self):
        """
        Clears the previous run cache
        """
        shutil.rmtree(os.path.join(self._home, runner.WORKING_DIRECTORY), True)
        shutil.rmtree(os.path.join(self._base, runner.WORKING_DIRECTORY), True)


    def _get_framework_file_name(self):
        """
        Retrieves the content of the framework configuration file, raises an
        exception on error
        """
        # Framework configuration file
        framework_conf = self._utils.find_conf_file(\
                                            runner.PLATFORM_FRAMEWORK_FILENAME)
        if not framework_conf:
            raise PSEM2MException("Framework file can't be found (%s)" \
                                  % runner.PLATFORM_FRAMEWORK_FILENAME)

        # Framework configuration file content
        framework_name = commons.read_framework_file(framework_conf)
        if not framework_name:
            raise PSEM2MException("Error reading framework file '%s'" \
                                  % runner.PLATFORM_FRAMEWORK_FILENAME)

        # Framework JAR file
        framework_file = self._utils.find_bundle_file(framework_name)
        if not framework_file:
            raise PSEM2MException("Framework file not found '%s'" \
                                  % framework_name)

        return framework_file


    def is_running(self):
        """
        Tests if the platform is running
        """
        return self._utils.is_running()


    def print_base_configuration(self):
        """
        Prints the current base configuration on the standard output
        """
        print("""Home      : %s
Base      : %s
Java      : %s
===========================
        """ % (self._home, self._base, self._java))


    def set_isolate_id(self, isolate_id):
        """
        Changes the ID of the isolate to start
        
        @param isolate_id: ID of the isolate to start
        """
        if not isolate_id:
            self._isolate_id = runner.DEFAULT_ISOLATE_ID

        else:
            self._isolate_id = isolate_id


    def start(self, jvm_args=[]):
        """
        Starts the monitor isolate
        """

        # Find the bootstrap
        bootstrap = self._utils.find_bundle_file(runner.BOOTSTRAP_FILENAME)
        if not bootstrap:
            raise PSEM2MException("Bootstrap file can't be found (%s)" \
                                  % runner.BOOTSTRAP_FILENAME)

        # Find the framework
        framework = self._get_framework_file_name()

        # Prepare system properties
        java_props = {}
        java_props[runner.PROP_PLATFORM_HOME] = self._home
        java_props[runner.PROP_PLATFORM_BASE] = self._base
        java_props[runner.PROP_PLATFORM_ISOLATE_ID] = self._isolate_id

        # Clear the cache first
        self.clear_cache()

        # Change the working directory -> Base
        current_dir = os.getcwd()
        os.chdir(self._base)

        # Wake up the beast
        pid, output = self._utils.run_java(self._java, \
                                           runner.BOOTSTRAP_MAIN_CLASS, \
                                           [bootstrap, framework], \
                                           # Human readable output
                                           ["--human"], \
                                           java_props, jvm_args)

        # Write the PID in a file
        self._utils.write_monitor_pid(pid)

        # Get back to the previous working directory
        os.chdir(current_dir)

        # Wait for the monitor to be fully launched
        return self.wait_monitor_start(output)


    def stop(self):
        """
        Sends the stop signal to the monitor
        """

        if not self._utils.is_running():
            # Nothing to do
            return 0

        access_file_name = os.path.join(self._base, runner.MONITOR_ACCESS_FILE)

        # Read the access file
        try:
            with open(access_file_name) as fp:
                access_url = fp.readline()

        except IOError as ex:
            print("Error reading monitor access file '%s'" % access_file_name, \
                  ex, file=sys.stderr)
            return 1

        if not access_url:
            print("No access URL found in '%s'" % access_file_name, \
                  file=sys.stderr)
            return 1

        # Send a signal
        try:
            urlrequest.urlopen(access_url + runner.SIGNAL_STOP, b"")

            # Wait for monitor to really stop
            wait_time = 0
            max_wait = 5
            poll_delay = .2

            while wait_time < max_wait:

                if not self._utils.is_running():
                    # Monitor stopped, we're done
                    return 0

                # Wait for max_wait seconds max
                time.sleep(poll_delay)
                wait_time += poll_delay

            else:
                # If still there, do it the hard way
                print("Error : Monitor is still here")
                return 1

        except IOError as ex:
            print("Error stopping platform :", ex, file=sys.stderr)

        # Something went wrong...
        return 1


    def wait_monitor_start(self, output):
        """
        Reads the given output with javaobj, to decode IsolateStatus
        
        @param output: Bootstrap output stream
        """

        for line in output:
            status = decode_bootstrap_line(str(line, encoding="utf-8"))

            if not status:
                # Ignore line
                continue

            print("[Status] :", status)
            state = int(status["state"])

            if state == -1:
                print("Isolate failed to start", file=sys.stderr)
                return 1

            elif state == 10:
                print("Monitor started")
                return 0

            elif state > 90:
                print("Monitor stopped directly")
                return 2
