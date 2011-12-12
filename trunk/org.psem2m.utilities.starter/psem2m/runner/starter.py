'''
Created on 12 déc. 2011

@author: Thomas Calmant
'''

from psem2m import PSEM2MException
from psem2m.runner.commons import PSEM2MUtils
from urllib.error import URLError
import os
import psem2m
import psem2m.runner as runner
import psem2m.runner.commons as commons
import shutil
import sys
import urllib.request

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


    def print_base_configuration(self):
        """
        Prints the current base configuration on the standard output
        """
        print("""Home      : %s
Base      : %s
Java      : %s
===========================
        """ % (self._home, self._base, self._java))


    def start(self):
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
        pid = self._utils.run_java(self._java, \
                                   runner.BOOTSTRAP_MAIN_CLASS, \
                                   [bootstrap, framework], \
                                   # Human readable output
                                   ["--human"], \
                                   java_props)

        # Write the PID in a file
        self._utils.write_monitor_pid(pid)

        # Get back to the previous working directory
        os.chdir(current_dir)
        return 0


    def stop(self):
        """
        Sends the stop signal to the monitor
        """
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
            urllib.request.urlopen(access_url + runner.SIGNAL_STOP, b"")
            return 0

        except URLError as ex:
            print("Error stopping platform :", ex, file=sys.stderr)
            return 1

# ------------------------------------------------------------------------------

def main():
    """
    Entry point
    """
    runner = PSEM2MRunner()
    runner.start()

    import time
    time.sleep(10)

    sys.exit(runner.stop())

if __name__ == "__main__":
    main()
