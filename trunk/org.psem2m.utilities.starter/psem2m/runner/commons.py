#-- Content-Encoding: utf-8 --
"""
Utility class default implementation

:author: Thomas Calmant
"""

from psem2m import PSEM2MException
import psem2m.runner

import os
import psutil
import subprocess

# ------------------------------------------------------------------------------

def find_file_recursive(file_name, root_directory):
    """
    Finds the given file in the given root directory and its children
    """

    for root_dirs_files in os.walk(root_directory, followlinks=True):
        if file_name in root_dirs_files[2]:
            return os.path.join(root_dirs_files[0], file_name)

    # File not found
    return None


def find_in_path(file_name):
    """
    Searches for file in the directories of the PATH environment variable
    
    :param file_name: The file to look for
    :return The first found file, or None
    """
    if not file_name:
        # Nothing to do
        return None

    if file_name[0] == '/':
        # Remove the starting separator
        file_name = file_name[1:]

    paths = os.getenv("PATH", "").split(os.pathsep)
    for path in paths:
        file_path = os.path.join(path, file_name)
        if is_file(file_path):
            return file_path


def get_os_utils(psem2m_utils):
    """
    Retrieves an instance of CommonsUtils implementation according to the host
    operating system
    """

    module_name = "psem2m.runner.%s" % os.name
    try:
        module = __import__(module_name, globals(), {}, [os.name])

    except ImportError as ex:
        raise PSEM2MException("Can't import Utilities for OS : %s (%s)" \
                              % (os.name, module_name), ex)

    if not hasattr(module, "Utils"):
        raise PSEM2MException("No Utils implementation found for %s (%s)" \
                              % (os.name, module_name))

    utils = module.Utils(psem2m_utils)
    assert isinstance(utils, OSSpecificUtils)
    return utils


def get_working_directory(isolate_id):
    """
    Retrieves the path to the isolate working directory.
    
    Doesn't create nor clean up the directory.
    
    :param isolate_id: The ID of an isolate
    :return: The isolate working directory
    """
    base = os.getenv(psem2m.PSEM2M_BASE,
                     os.getenv(psem2m.PSEM2M_HOME, os.getcwd()))

    # Replace path separators with underscores
    escaped_id = isolate_id.replace(os.sep, "_")

    return os.path.abspath(os.path.join(base, "var", "work", escaped_id))


def is_dir(path):
    """
    Tests if the given path points to a directory or to a link to a directory
    """
    return os.path.isdir(os.path.realpath(path))


def is_file(path):
    """
    Tests if the given path points to a file or to a link to a file
    """
    return os.path.isfile(os.path.realpath(path))


def remove_quotes(path):
    """
    Removes the quotes surrounding the given string, if any
    """
    if not path:
        return path

    for quote in ['"', "'"]:
        if path[0] == quote and path[-1] == quote:
            return path[1:-1]

    return path


def read_framework_file(file_name):
    """
    Reads the first non-commented and non-empty line of a framework file.
    Framework files contains the file name of an OSGi framework JAR main file;
    
    :param file_name: Name of the framework file to read
    :return: The framework file content, None if not found
    """
    with open(file_name) as fp:
        for line in fp:
            line = line.strip()

            if len(line) != 0 and not line.startswith("#"):
                # Not a comment not an empty line
                return line

    # No valid line found
    return None

# ------------------------------------------------------------------------------

class OSSpecificUtils(object):
    """
    Abstract OS-specific utility class
    """

    __slots__ = ("_utils")

    def __init__(self, psem2m_utils):
        """
        Constructor
        """
        self._utils = psem2m_utils


    def find_java_interpreter(self, java_home):
        """
        Finds the Java interpreter, in the given Java Home if possible
        
        :param java_home: The preferred Java home
        :return: The path to the first Java interpreter found, or None
        """
        raise NotImplementedError("This method must implemented by child class")


    def find_python2_interpreter(self):
        """
        Finds a Python 2 interpreter
        
        :return: The path to the first Python 2 interpreter found, or None
        """
        raise NotImplementedError("This method must implemented by child class")


    def find_python3_interpreter(self):
        """
        Finds a Python 3 interpreter
        
        :return: The path to the first Python 3 interpreter found, or None
        """
        raise NotImplementedError("This method must implemented by child class")

# ------------------------------------------------------------------------------

class PSEM2MUtils(object):
    """
    Utility class to work with PSEM2M platform files
    """

    def __init__(self, psem2m_home, psem2m_base):
        """
        Sets up the utility class
        
        :param psem2m_home: PSEM2M home directory
        :param psem2m_base: PSEM2M base directory
        """
        self.home = psem2m_home
        self.base = psem2m_base
        self._os_utils = get_os_utils(self)


    def find_bundle_file(self, bundle_name):
        """
        Search for the given file in PSEM2M local repositories
        
        :param bundle_name: A bundle file name
        :return: The first found file in a repo directory
        """
        return self.find_file(bundle_name, ["repo"])


    def find_conf_file(self, file_name):
        """
        Search for the given file in PSEM2M configuration directories
        
        :param file_name: A file name
        :return: The first found file in a conf directory
        """
        return self.find_file(file_name, ["conf"])


    def find_file(self, file_name, sub_dirs=["."]):
        """
        Finds the given file name in the given PSEM2M sub-directory
        
        :param file_name: A file name
        :param sub_dirs: Possible sub-directories
        :return: The first found file, or None
        """
        for prefix in [self.base, self.home, os.getcwd()]:
            # Compute the absolute file path
            for sub_dir in sub_dirs:

                found_file = find_file_recursive(file_name, \
                                                 os.path.join(prefix, sub_dir))

                if found_file != None:
                    # Found
                    return found_file

        return None


    def find_java_exe(self):
        """
        Tries to get the path of the Java interpreter
        
        :return: The Java interpreter executable
        :raise PSEM2MException: The Java interpreter could not be found
        """
        java_home = self.get_embedded_jvm()
        if not java_home:
            print("Warning: Using the system Java Virtual Machine.")

        java_file = self._os_utils.find_java_interpreter(java_home)
        if not is_file(java_file):
            raise PSEM2MException("Can't find the Java interpreter file")

        return java_file


    def get_embedded_jvm(self):
        """
        Retrieves the path to the Java Virtual Machine embedded with PSEM2M,
        None if not available
        
        :return: The path to the PSEM2M JVM, or None
        """
        jvm_dir = os.path.join(self.home, "java")
        if is_dir(jvm_dir):
            return jvm_dir

        return None


    def is_monitor_running(self):
        """
        Tests if the monitor process is running
        
        :return: True if the monitor is running
        """
        pid = self.read_monitor_pid()
        if not pid:
            # No PID file : not running
            return False

        return self._os_utils.is_process_running(pid)


    def read_monitor_pid(self):
        """
        Reads the monitor PID file content, returns None if absent
        
        :return: The monitor PID, or None
        """
        pid_file_name = os.path.join(self.base, psem2m.runner.MONITOR_PID_FILE)

        # Test if file exists
        if not os.path.exists(pid_file_name):
            return None

        try:
            with open(pid_file_name) as fp:
                pid = int(fp.readline().strip())

        except IOError:
            # Something went wrong
            return None

        except ValueError:
            # Invalid content ?
            return None

        return pid


    def run_java(self, java, main_class, classpath=[], arguments=[],
                 java_system_props={}, jvm_args=[]):
        """
        Runs the Java interpreter with the given class path
        
        :param java: Path to the Java interpreter executable
        :param main_class: Java main class to execute
        :param classpath: Java class path (strings array)
        :param arguments: Main class arguments (strings array)
        :param java_system_props: Java system properties (dictionary)
        :param jvm_args: JVM level arguments
        :return: A psutil.Popen object
        :raise PSEM2MException: Error preparing the run
        """
        if not java:
            raise PSEM2MException("No Java interpreter given")

        if not classpath:
            raise PSEM2MException("Can't run PSEM2M without a class path")

        if not main_class:
            raise PSEM2MException("No Main class given")

        # Prepare the command
        # ... interpreter
        cmd_line = [java]

        # ... JVM arguments
        if jvm_args != None:
            cmd_line.extend(jvm_args)

        # ... Java system properties
        if isinstance(java_system_props, dict):
            for prop in java_system_props:
                cmd_line.append("-D%s=%s" \
                                % (str(prop).strip(), \
                                   str(java_system_props[prop]).strip()))

        # ... class path
        cmd_line.append("-cp")
        cmd_line.append(os.pathsep.join(classpath))

        # ... main class
        cmd_line.append(main_class)

        # ... arguments, if any
        if arguments != None:
            cmd_line.extend(arguments)


        # Run the Java interpreter
        process = psutil.Popen(cmd_line, stdout=subprocess.PIPE)

        # Return the process object
        return process


    def write_monitor_pid(self, pid):
        """
        Writes the PID of the monitor in a file
        
        :param pid: PID of the monitor
        """
        pid_file_name = os.path.join(self.base, psem2m.runner.MONITOR_PID_FILE)

        # Make parent directories, if needed
        pid_folder = os.path.dirname(pid_file_name)
        if not os.path.exists(pid_folder):
            os.makedirs(pid_folder)

        # Write the file
        with open(pid_file_name, "w") as fp:
            fp.write(str(pid))
