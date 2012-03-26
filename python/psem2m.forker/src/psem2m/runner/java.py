#-- Content-Encoding: UTF-8 --
"""
Python 2, Python 3 and Java runners

@author: Thomas Calmant
"""

from psem2m.component.decorators import ComponentFactory, Invalidate, \
    Provides, Instantiate, Validate

# ------------------------------------------------------------------------------

import psem2m
import psem2m.runner.commons as runner

# ------------------------------------------------------------------------------

import logging
import os

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

# PSEM2M Base directory Java property
PROP_PLATFORM_BASE = "org.psem2m.platform.base"

# PSEM2M Home directory Java property
PROP_PLATFORM_HOME = "org.psem2m.platform.home"

# PSEM2M Isolate ID Java property
PROP_PLATFORM_ISOLATE_ID = "org.psem2m.platform.isolate.id"

# PSEM2M debug port Java property
PROP_PLATFORM_DEBUG_PORT = "org.psem2m.debug.port"

# ------------------------------------------------------------------------------

# OSGi framework definition file
PLATFORM_FRAMEWORK_FILENAME = "platform.framework"

# Bootstrap JAR file name
BOOTSTRAP_FILENAME = "org.psem2m.utilities.bootstrap.jar"

# Bootstrap main class name
BOOTSTRAP_MAIN_CLASS = "org.psem2m.utilities.bootstrap.Main"

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-runner-java")
@Instantiate("JavaRunner")
@Provides("org.psem2m.forker.IsolateRunner")
class JavaRunner(runner.Runner):
    """
    Java isolate runner
    """
    def __init__(self):
        """
        Constructor
        """
        runner.Runner.__init__(self)
        self._path = None


    def can_handle(self, kind):
        """
        Tests if this runner can start an isolate of the given kind
        
        :param kind: Kind of the isolate to run
        :return: True if the isolate can be started by this runner
        """
        return kind == "java" and self._path is not None


    def _get_executable(self, isolate_descr):
        """
        Retrieves the path to the executable to run for that isolate
        
        :param isolate_descr: A dictionary describing the isolate
        :return: The path to the executable, or None
        """
        return self._path


    def _make_args(self, isolate_descr):
        """
        Prepares the Java interpreter arguments.
        
        The description dictionary may contain the following entries :
        
        * ``classpath``: A list of Java class path entries
        * ``app_args``: A list of arguments following the JVM specific
          arguments. Typically, it may contain the name of a class to execute
          and its paramaters.
        
        :param isolate_descr: A dictionary describing the isolate
        :return: The parameters to give to the interpreter (array)
        :raise OSError: File not found
        :raise ValueError: Error preparing the arguments
        """
        # Arguments list
        args = []

        # JVM arguments
        jvm_args = isolate_descr.get("vmArgs", None)
        if jvm_args is not None:
            args.extend(jvm_args)

        # JVM properties
        home = os.getenv(psem2m.PSEM2M_HOME, os.getcwd())
        java_props = {
                      PROP_PLATFORM_HOME: home,
                      PROP_PLATFORM_BASE: os.getenv(psem2m.PSEM2M_BASE, home),
                      PROP_PLATFORM_ISOLATE_ID: isolate_descr["id"]
                      }

        for key, value in java_props.items():
            args.append("-D{key}={value}".format(key=str(key).strip(),
                                                 value=str(value).strip()))

        # Classpath
        classpath = isolate_descr.get("classpath", None)
        if hasattr(classpath, "__iter__"):
            # Got an iterable object
            args.append("-cp")
            args.append(os.pathsep.join(classpath))

        # Application argument
        extra_args = isolate_descr.get("app_args", None)
        if hasattr(classpath, "__iter__"):
            # Got an iterable object
            args.extend(extra_args)

        return args


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        self._path = self._utils.find_java_interpreter(None)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        self._path = None

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-runner-osgi")
@Instantiate("OsgiRunner")
@Provides("org.psem2m.forker.IsolateRunner")
class OsgiRunner(JavaRunner):
    """
    OSGi isolate runner
    """
    def __init__(self):
        """
        Constructor
        """
        JavaRunner.__init__(self)
        self._kind_frameworks = {
                            # TODO: remove version numbers
                            "felix": "org.apache.felix.main-3.2.2.jar",
                            "equinox":"org.eclipse.osgi_3.7.0.v20110613.jar"
                        }


    def can_handle(self, kind):
        """
        Tests if this runner can start an isolate of the given kind
        
        :param kind: Kind of the isolate to run
        :return: True if the isolate can be started by this runner
        """
        return kind in ("felix", "equinox") and self._path is not None


    def _get_framework_file_name(self, isolate_descr):
        """
        Retrieves the path to the OSGi framework JAR file according to the
        isolate description or the framework configuration file.
        
        Tries to find the framework file name corresponding to the given
        description, or uses the framework described in the platform.framework
        file.
        
        :param isolate_descr: The description of an isolate
        :return: The path to the framework JAR file
        :raise OSError: Framework file not found
        """
        kind = isolate_descr["kind"]

        # Set up the list of possible names
        config_names = [# Explicit OSGi framework file
                        isolate_descr.get("osgiFramework", None),

                        # Default name for this kind
                        self._kind_frameworks.get(kind, None)
                        ]


        # Try to find those files
        for framework_name in config_names:
            if framework_name is not None:
                bundle = self._utils.find_bundle_file(framework_name)
                if bundle is not None:
                    # File found, use it
                    return bundle

        # No configuration hint, use the framework configuration file
        framework_conf = self._utils.find_conf_file(PLATFORM_FRAMEWORK_FILENAME)
        if not framework_conf:
            raise OSError("Framework file can't be found (%s)" \
                          % PLATFORM_FRAMEWORK_FILENAME)

        # Framework configuration file content
        framework_name = self._utils.read_framework_file(framework_conf)
        if not framework_name:
            raise OSError("Error reading framework file '%s'" \
                          % PLATFORM_FRAMEWORK_FILENAME)

        # Framework JAR file
        framework_file = self._utils.find_bundle_file(framework_name)
        if not framework_file:
            raise OSError("Framework file not found '%s'" % framework_name)

        return framework_file


    def _make_args(self, isolate_descr):
        """
        Prepares the Python interpreter arguments
        
        :param isolate_descr: A dictionary describing the isolate
        :return: The parameters to give to the interpreter (array)
        :raise OSError: File not found
        :raise ValueError: Error preparing the arguments
        """
        # Find the bootstrap file
        bootstrap = self.utils.find_bundle_file(BOOTSTRAP_FILENAME)
        if not bootstrap:
            raise OSError("Can't find the bootstrap file %s"
                          % BOOTSTRAP_FILENAME)

        # Find the OSGi framework file
        framework = self._get_framework_file_name()

        # Set up a new description, with OSGi specific entries
        new_descr = isolate_descr.copy()
        # ... class path
        new_descr["classpath"] = (bootstrap, framework)

        # ... Java arguments : the main class and its arguments
        new_descr["app_args"] = (BOOTSTRAP_MAIN_CLASS, "--human")

        # Call the parent class
        return super(OsgiRunner, self)._make_args(self, new_descr)
