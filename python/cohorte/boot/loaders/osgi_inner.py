#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Java isolate loader, based on jPype

**TODO:**
* Review constants names & values

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


Modifications:
    MOD_BD_20150916 Inherits PROP_NODE_DATA_DIR from pelix.

"""

# Python standard library
import logging
import os
import sys
import time
import threading

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Property, Requires
import pelix.framework
import pelix.shell

# COHORTE constants
import cohorte
import cohorte.repositories

# Herald
import herald

# JPype (Java bridge)
import jpype

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

ISOLATE_LOADER_FACTORY = 'cohorte-loader-java-factory'
""" Forker loader factory name """

LOADER_KIND = 'osgi'
""" Kind of isolate started with this loader """

BUNDLE_SERVICES_FOLDER = 'META-INF/services'
""" Path of the descriptions of the bundle services (in a JAR) """

FRAMEWORK_SERVICE = 'org.osgi.framework.launch.FrameworkFactory'
""" FrameworkFactory service descriptor in the framework JAR file """

FRAMEWORK_SYSTEMPACKAGES_EXTRA = "org.osgi.framework.system.packages.extra"
""" OSGi extra system packages """

PYTHON_BRIDGE_BUNDLE_API = "org.cohorte.pyboot.api"
""" Name of the Python bridge API bundle """

PYTHON_BRIDGE_BUNDLE = "org.cohorte.pyboot"
""" Name of the Python bridge bundle """

PYTHON_JAVA_BRIDGE_INTERFACE = "org.cohorte.pyboot.api.IPyBridge"
""" Interface of the Python - Java bridge """

HERALD_EVENT_BUNDLE_API = "org.cohorte.herald.eventapi"
""" Name of the bundle and package which contain the Herald Event API """

HERALD_EVENT_INTERFACE = "org.cohorte.herald.eventapi.IEvent"
""" Interface of an Herald Event """

HERALD_EVENT_FACTORY_INTERFACE = "org.cohorte.herald.eventapi.IEventFactory"
""" Interface of the Herald EventFactory service """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


class PyBridge(object):
    """
    Python - Java bridge service implementation
    """
    # pylint: disable=invalid-name
    # Implemented Java interface
    JAVA_INTERFACE = PYTHON_JAVA_BRIDGE_INTERFACE

    def __init__(self, context, jvm, java_configuration, configuration_parser,
                 callback):
        """
        Sets up the bridge

        :param context: The bundle context
        :param jvm: The JVM wrapper
        :param java_configuration: Java boot configuration
        :param callback: Method to call back on error or success
        """
        # Bundle context
        self._context = context

        # Java class
        self.ArrayList = jvm.load_class("java.util.ArrayList")
        self.Component = jvm.load_class("org.cohorte.pyboot.api.ComponentBean")
        self.HashMap = jvm.load_class("java.util.HashMap")

        # Prepare members
        self._callback = callback
        self._components = {}
        self._parser = configuration_parser

        # Convert stored components
        self._java_boot_config = self._to_java(java_configuration)
        self._prepare_components(java_configuration.composition)

    def _prepare_components(self, raw_components):
        """
        Converts the Python Component objects into Java Component beans

        :param raw_components: Python components representations
        """
        for component in raw_components:
            # Convert properties
            properties = self.HashMap()
            for key, value in component.properties.items():
                properties.put(key, value)

            # Store the component bean
            self._components[component.name] = \
                self.Component(component.factory, component.name, properties)

    def _to_java(self, data):
        """
        Recursively converts lists and maps to Java ones

        :param data: Data to be converted
        :return: Converted data
        """
        try:
            # Named tuple (in theory)
            as_dict = getattr(data, '_asdict')
        except AttributeError:
            # Keep data as is
            pass
        else:
            data = as_dict()

        if isinstance(data, dict):
            # Convert a dictionary
            converted = self.HashMap()
            for key, value in data.items():
                # Convert entry
                converted.put(self._to_java(key), self._to_java(value))
            return converted

        elif isinstance(data, (list, tuple, set)):
            # Convert a list
            converted = self.ArrayList()
            for item in data:
                # Convert each item
                converted.add(self._to_java(item))
            return converted

        else:
            # No conversion
            return data

    @staticmethod
    def debug(message, values):
        """
        Logs a debug message
        """
        _logger.debug(message.format(*values))

    @staticmethod
    def error(message, values):
        """
        Logs an error message
        """
        _logger.error(message.format(*values))

    def getComponents(self):
        """
        Retrieves the components to instantiate (Java API)

        :return: An array of components
        """
        # Create a list
        result = self.ArrayList()
        for component in self._components.values():
            result.add(component)
        return result

    def getStartConfiguration(self):
        """
        Retrieves the configuration used to start this isolate as a map

        :return: The configuration used to start this isolate
        """
        return self._java_boot_config

    @staticmethod
    def getPid():
        """
        Retrieves the Process ID of this isolate

        :return: The isolate PID
        """
        return os.getpid()

    def getRemoteShellPort(self):
        """
        Returns the port used by the Pelix remote shell, or -1 if the shell is
        not active

        :return: The port used by the remote shell, or -1
        """
        ref = self._context.get_service_reference(
            pelix.shell.REMOTE_SHELL_SPEC)
        if ref is None:
            return -1

        try:
            # Get the service
            shell = self._context.get_service(ref)

            # Get the shell port
            port = shell.get_access()[1]

            # Release the service
            self._context.unget_service(ref)
            return port
        except pelix.framework.BundleException:
            # Service lost (called while the framework was stopping)
            return -1

    def onComponentStarted(self, name):
        """
        Called when a component has been started

        :param name: Name of the started component
        """
        if name in self._components:
            del self._components[name]

        if not self._components:
            self._callback(True, "All components have been instantiated")

    def onError(self, error):
        """
        Called when an error has occurred

        :param error: An error message
        """
        self._callback(False, error)

    def prepareIsolate(self, uid, name, node, kind, level, sublevel,
                       bundles, composition):
        """
        Prepares the configuration dictionary of an isolate
        """
        try:
            conf = self._parser.prepare_isolate(
                uid, name, node, kind, level, sublevel, bundles, composition)
        except:
            _logger.exception("Error preparing isolate...")
            return None

        return self._to_java(conf)

    def readConfiguration(self, filename):
        """
        Reads the given configuration file

        :param filename: A configuration file name
        :return: The parsed configuration map
        """
        # Load the file
        raw_dict = self._parser.read(filename)

        # Convert the dictionary to Java
        return self._to_java(raw_dict)

# ------------------------------------------------------------------------------


class EventFactory(object):
    """
    Implementation of org.cohorte.herald.eventapi.IEventFactory
    """
    JAVA_INTERFACE = HERALD_EVENT_FACTORY_INTERFACE

    def __init__(self, java_svc):
        """
        Sets up members
        """
        self._java = java_svc

    def createEvent(self):
        """
        Creates an event for the Java world
        """
        return self._java.make_proxy(EventProxy())

    def sleep(self, milliseconds):
        """
        Sleeps the given number of milliseconds
        """
        time.sleep(milliseconds / 1000.)

    def toString(self):
        """
        Java toString() method
        """
        return "Python Event Factory for Herald"


class EventProxy(object):
    """
    Implementation of org.cohorte.herald.eventapi.IEvent
    """
    JAVA_INTERFACE = HERALD_EVENT_INTERFACE

    def __init__(self):
        """
        Sets up members
        """
        self.__event = threading.Event()

        # Common names
        for method in ('clear', 'isSet', 'set'):
            setattr(self, method, getattr(self.__event, method))

    def waitEvent(self, timeout_ms=None):
        """
        Proxy to call the wait() method of the event
        """
        if timeout_ms is None or timeout_ms < 0:
            return self.__event.wait()
        else:
            return self.__event.wait(timeout_ms / 1000.)

    def toString(self):
        """
        Java toString() method
        """
        return "Python EventProxy for Herald"

# ------------------------------------------------------------------------------


@ComponentFactory(ISOLATE_LOADER_FACTORY)
@Provides(cohorte.SERVICE_ISOLATE_LOADER)
@Property('_handled_kind', cohorte.SVCPROP_ISOLATE_LOADER_KIND, LOADER_KIND)
@Requires('_java', cohorte.SERVICE_JAVA_RUNNER)
@Requires('_repository', cohorte.repositories.SERVICE_REPOSITORY_ARTIFACTS,
          spec_filter="({0}=java)"
          .format(cohorte.repositories.PROP_REPOSITORY_LANGUAGE))
@Requires('_config', cohorte.SERVICE_CONFIGURATION_READER)
@Requires('_finder', cohorte.SERVICE_FILE_FINDER)
class JavaOsgiLoader(object):
    """
    Pelix isolate loader. Needs a configuration to be given as a parameter of
    the load() method.
    """

    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._java = None
        self._config = None
        self._finder = None
        self._repository = None

        # Pelix bundle context
        self._context = None

        # OSGi Framework
        self._osgi = None

        # Bridge service registration
        self._bridge_reg = None

    @staticmethod
    def _setup_vm_properties(properties):
        """
        Sets up the JVM system properties dictionary (not the arguments)

        :param properties: Configured properties
        :return: VM properties dictionary
        """
        # Prepare the dictionary
        return properties.copy() if properties else {}

    def _setup_osgi_properties(self, properties, allow_bridge, extra_packages=None):
        """
        Sets up the OSGi framework properties and converts them into a Java
        HashMap.

        :param properties: Configured framework properties
        :param allow_bridge: If True, the bridge API package will be exported
                             by the framework.
        :return: The framework properties as a Java Map
        """
        osgi_properties = self._java.load_class("java.util.HashMap")()
        for key, value in properties.items():
            if value is not None:
                osgi_properties.put(key, str(value))

        # Inherit some Pelix properties
        for key in (cohorte.PROP_HOME, cohorte.PROP_BASE,
                    cohorte.PROP_UID, cohorte.PROP_NAME,
                    cohorte.PROP_NODE_UID, cohorte.PROP_NODE_NAME,
                    cohorte.PROP_NODE_DATA_DIR,
                    cohorte.PROP_DUMPER_PORT,
                    cohorte.PROP_FORKER_HTTP_PORT,
                    herald.FWPROP_PEER_UID, herald.FWPROP_PEER_NAME,
                    herald.FWPROP_NODE_UID, herald.FWPROP_NODE_NAME,
                    herald.FWPROP_APPLICATION_ID):
            value = self._context.get_property(key)
            if value is not None:
                # Avoid empty values
                osgi_properties.put(key, str(value))

        # Special case: Herald groups (comma-separated list)
        value = self._context.get_property(herald.FWPROP_PEER_GROUPS)
        if value:
            osgi_properties.put(herald.FWPROP_PEER_GROUPS,
                                ','.join(str(group) for group in value))

        new_extra_packages = None
        if allow_bridge:
            # Prepare the "extra system package" framework property
            if extra_packages:
                new_extra_packages = "{0}; version=1.0.0, {1}; version=1.0.0,{2}".format(
                    PYTHON_BRIDGE_BUNDLE_API, HERALD_EVENT_BUNDLE_API, extra_packages)
            else:
                new_extra_packages = "{0}; version=1.0.0, {1}; version=1.0.0".format(
                    PYTHON_BRIDGE_BUNDLE_API, HERALD_EVENT_BUNDLE_API)
        else:
            if extra_packages:
                new_extra_packages = "{0}".format(extra_packages)

        if new_extra_packages:
            _logger.debug(
                "Framework extra-packages={0}".format(new_extra_packages))
            osgi_properties.put(
                FRAMEWORK_SYSTEMPACKAGES_EXTRA, new_extra_packages)
        else:
            _logger.debug("No extra-packages!")

        return osgi_properties

    def _start_jvm(self, vm_args, classpath, properties):
        """
        Starts the JVM, with the given file in the class path

        :param vm_args: JVM arguments
        :param classpath: A list of JAR files
        :param properties: Java system properties
        :raise KeyError: Error starting the JVM
        :raise ValueError: Invalid JAR file
        """
        # Start a JVM if necessary
        if not self._java.is_running():
            # Arguments given to the Java runner
            java_args = []

            if vm_args:
                # VM specific arguments first
                java_args.extend(vm_args)

            # DEBUG: Remote debug server
            # java_args.append("-Xdebug")
            # java_args.append("-Xrunjdwp:transport=dt_socket,"
            #                  "server=y,suspend=y,address=5005")

            # Set the class path as a parameter
            java_args.append(self._java.make_jvm_classpath(classpath))

            # Prepare the JVM properties definitions
            for key, value in self._setup_vm_properties(properties).items():
                java_args.append(self._java.make_jvm_property(key, value))

            self._java.start(None, *java_args)
        else:
            # Add the JAR to the class path
            for jar_file in classpath:
                self._java.add_jar(jar_file)

    def _close_osgi(self):
        """
        Stops the OSGi framework and clears all references to it
        """
        # Unregister services
        if self._bridge_reg is not None:
            self._bridge_reg.unregister()
            self._bridge_reg = None

        # Stop the framework
        if self._osgi is not None:
            self._osgi.stop()
            self._osgi = None

    def _register_bridge(self, context, java_configuration):
        """
        Instantiates and registers the iPOJO components instantiation handler
        inside the OSGi framework

        :param context: An OSGi bundle context
        :param java_configuration: The Java boot configuration
        """
        # Make a Java proxy of the bridge
        bridge_java = self._java.make_proxy(
            PyBridge(self._context, self._java, java_configuration,
                     self._config, self._bridge_callback))

        # Register it to the framework
        self._bridge_reg = context.registerService(
            PyBridge.JAVA_INTERFACE, bridge_java, None)

    def _register_herald_bridge(self, context):
        """
        Registers the Herald EventFactory service inside the OSGi framework

        :param context: An OSGi bundle context
        """
        # Make a Java proxy of the Herald bridge
        herald_java = self._java.make_proxy(EventFactory(self._java))

        # Register it to the framework
        props = self._java.load_class("java.util.Hashtable")()
        props.put("service.ranking", 1000)
        self._bridge_reg = context.registerService(
            EventFactory.JAVA_INTERFACE, herald_java, props)

    @staticmethod
    def _bridge_callback(success, message):
        """
        Called back by the Python-Java bridge

        :param success: If True, all components have been started, else an
                        error occurred
        :param message: A call back message
        """
        if success:
            _logger.debug("Bridge success: %s", message)
        else:
            _logger.warning("Bridge error: %s", message)

    def _find_osgi_jar(self, osgi_jar, symbolic_name):
        """
        Looks for the OSGi framework JAR file matching the given parameters

        :param osgi_jar: An OSGi framework JAR file name
        :param symbolic_name: An OSGi framework symbolic name
        :return: A (file name, framework factory) tuple
        :raise ValueError: No OSGi framework found
        """
        try:
            # We've been given a specific JAR file or symbolic name
            osgi_bundle = self._repository.get_artifact(symbolic_name,
                                                        filename=osgi_jar)
        except ValueError:
            # Bundle not found
            for bundle in self._repository.filter_services(FRAMEWORK_SERVICE):
                # Get the first found framework
                osgi_bundle = bundle
                break
            else:
                # No match found
                raise ValueError("No OSGi framework found in repository")

        # Found !
        return osgi_bundle.file, osgi_bundle.get_service(FRAMEWORK_SERVICE)

    def load(self, configuration):
        """
        Loads the Java OSGi isolate

        :param configuration: Isolate configuration dictionary (required)
        :raise KeyError: A mandatory property is missing
        :raise ValueError: Invalid parameter/file encountered or the JVM
                           can't be loaded
        :raise BundleException: Error installing a bundle
        :raise Exception: Error instantiating a component
        """
        if not configuration:
            raise KeyError("A configuration is required to load a "
                           "Java OSGi isolate")

        # Parse the configuration (boot-like part) -> Might raise error
        java_config = self._config.load_boot_dict(configuration)

        # Find the OSGi JAR file to use
        osgi_jar_file, factory_name = self._find_osgi_jar(
            configuration.get('osgi_jar'), configuration.get('osgi_name'))

        _logger.debug("Using OSGi JAR file: %s", osgi_jar_file)

        # Prepare the VM arguments
        classpath = [osgi_jar_file]

        # Find the bridge API JAR file
        api_jar = self._repository.get_artifact(PYTHON_BRIDGE_BUNDLE_API)
        if api_jar:
            # Add the bundle to the class path...
            classpath.append(api_jar.file)
        else:
            raise Exception("Python bridge API bundle is missing")

        # Find the Herald API JAR file
        herald_event_jar = self._repository.get_artifact(
            HERALD_EVENT_BUNDLE_API)
        if herald_event_jar:
            # Add the bundle to the class path...
            classpath.append(herald_event_jar.file)
        else:
            raise Exception("Herald Event API bundle is missing")

        # Start the JVM
        _logger.debug("Starting JVM...")
        self._start_jvm(configuration.get('vm_args'), classpath,
                        configuration.get('vm_properties'))

        # Patch for Mac OS X:
        # GUI library must be loaded early in the main thread
        if sys.platform == 'darwin':
            # We need this dark magic stuff for dummy OSes
            self._java.load_class("java.awt.Color")

        # Load the FrameworkFactory implementation
        _logger.debug("Loading OSGi FrameworkFactory: %s", factory_name)
        factory_class = self._java.load_class(factory_name)
        factory = factory_class()

        # Retrieve extra packages
        vm_args = configuration.get('vm_args')
        tmp = []
        if vm_args:
            tmp = [vm_arg for vm_arg in configuration.get('vm_args')
                    if FRAMEWORK_SYSTEMPACKAGES_EXTRA in vm_arg]
        extra_packages = ""
        if len(tmp) > 0:
            extra_packages = tmp[0].split("=")[1]

        # Framework properties
        osgi_properties = self._setup_osgi_properties(java_config.properties,
                                                      api_jar is not None,
                                                      extra_packages)

        # Start a framework, with the given properties
        self._osgi = factory.newFramework(osgi_properties)
        self._osgi.start()
        context = self._osgi.getBundleContext()

        # Register the Herald Event API bridge
        self._register_herald_bridge(context)

        # Install bundles
        java_bundles = []

        # Install the bridge
        bundle = self._repository.get_artifact(PYTHON_BRIDGE_BUNDLE)
        if not bundle:
            _logger.warning("No Python bridge bundle found")
        else:
            _logger.debug("Installing PyBridge bundle: %s", bundle.url)
            java_bundles.append(context.installBundle(bundle.url))

        # Install the configured bundles
        for bundle_conf in java_config.bundles:
            bundle = self._repository.get_artifact(
                bundle_conf.name, bundle_conf.version, bundle_conf.filename)
            if not bundle:
                if not bundle_conf.optional:
                    raise ValueError("Bundle not found: {0}"
                                     .format(bundle_conf))
                else:
                    _logger.warning("Bundle not found: %s", bundle_conf)
            elif bundle.file == osgi_jar_file:
                _logger.debug("OSGi framework is already installed.")
            else:
                _logger.debug("Installing Java bundle %s (is_fragment=%s)...", bundle.name, bundle.is_fragment())
                b = context.installBundle(bundle.url)                
                if not bundle.is_fragment():
                    java_bundles.append(b)

        try:
            # Start the bundles
            for bundle in java_bundles:
                _logger.debug("Starting %s...", bundle.getSymbolicName())                
                bundle.start()
        except jpype.JavaException as ex:
            # Log the bundle exception and its cause
            _logger.error("Error starting bundle: %s",
                          ex.__javaobject__.toString())
            cause = ex.__javaobject__.getCause()
            while cause is not None:
                _logger.error("... caused by: %s", cause.toString())
                cause = cause.getCause()

            # Raise exception to the caller
            raise

        # Start the component instantiation handler
        # (once all bundles have been installed)
        self._register_bridge(context, java_config)

    def wait(self):
        """
        Waits for the isolate to stop
        """
        if not self._osgi:
            # Nothing to do
            return

        # Wait for the OSGi framework to stop
        try:
            self._osgi.waitForStop(0)
        except Exception as ex:
            _logger.exception("Error waiting for the OSGi framework "
                              "to stop: %s", ex)
            raise

    @Validate
    def validate(self, context):
        """
        Component validated

        :param context: The bundle context
        """
        # Update the finder
        self._finder.update_roots()

        # Store the framework access
        self._context = context

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated

        :param context: The bundle context
        """
        # Stop the framework
        self._close_osgi()

        # Clear the JVM
        self._java.stop()

        # Clear the framework access
        self._context = None
