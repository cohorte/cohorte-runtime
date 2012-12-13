#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
The Java OSGi framework loader for PSEM2M.

:author: Thomas Calmant
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Instantiate, \
    Requires, Validate, Invalidate

# ------------------------------------------------------------------------------

import man_parser

import jpype
import os

# ------------------------------------------------------------------------------

__vm_started__ = False
""" Flag to indicate if this module has started a JVM """

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-java-osgi-loader-factory")
@Requires('_config', 'org.psem2m.isolates.services.conf.ISvcConfig')
@Instantiate('psem2m-java-osgi-loader')
class JavaOSGiLoader(object):
    """
    Starts a JVM and loads an OSGi framework inside it
    """
    def __init__(self):
        """
        Component set up.
        
        :raise Exception: A JVM has already been started by this module.
        """
        if __vm_started__:
            # JVM already started
            raise Exception('The Java Virtual Machine can be started only once')

        # Configuration service
        self._config = None

        # Bundle repository
        self._repository = None

        # Java VM
        self._jvm = None
        self._osgi = None


    def make_jvm_args(self, isolate_id, classpath, extra_args=None):
        """
        Prepares the arguments of the JVM
        """
        jvm_arguments = []

        # Compute basic arguments
        basic_args = {'org.psem2m.platform.isolate.id': isolate_id}

        if classpath:
            basic_args['java.class.path'] = os.path.sep.join(classpath)

        for args in (basic_args, extra_args):
            if args:
                # Convert dictionaries to JVM arguments
                jvm_arguments.extend(('-D{0}={1}'.format(key, value)
                                      for key, value in args.items()))

        return jvm_arguments


    def load_repositories(self):
        """
        Java repository loading
        """
        # Prepare the paths set
        paths = set()
        for path in (os.getenv('PSEM2M_HOME'), os.getenv('PSEM2M_BASE'),
                     os.getcwd()):
            if path:
                paths.add(os.path.abspath(path))

        # Load the bundles
        for repo_dir in paths:
            self._repository.load_folder(repo_dir)


    def get_osgi_bundles(self, specified_bundles, factories, system_packages):
        """
        Resolves the list of JARs to install according to the specified bundles
        and factories.
        
        :param specified_bundles: A list of bundles to install
        :param factories: A list of iPOJO factories
        :param system_packages: A list of packages provided by the JVM
        :raise KeyError: A factory/bundle/package is missing
        :raise ValueError: No framework found in the resolved bundles
        """
        # Normalize system packages
        if not system_packages:
            system_packages = set()

        # The packages required by iPOJO are provided by the system
        system_packages.update(('javax.security.cert', 'javax.net.ssl',
                                'javax.xml.parsers', 'org.w3c.dom',
                                'org.xml.sax', 'org.xml.sax.helpers'))

        # Use the specified bundles
        bundles_to_find = set()
        if specified_bundles:
            bundles_to_find.update(specified_bundles)

        # Find the bundles providing the specified factories
        providers, unresolved = self._repository.find_ipojo_factories(factories)
        if unresolved:
            raise KeyError('Missing factories: {0}'.format(unresolved))

        bundles_to_find.update((bundles[0] for bundles in providers.values()))

        # Resolve the installation
        resolution = self._repository.resolve_installation(bundles_to_find,
                                                           system_packages)

        # Some bundles or packages might be missing
        missing = resolution[2]
        if missing[0]:
            raise KeyError('Missing bundles: {0}'.format(missing[0]))

        elif missing[1]:
            raise KeyError('Missing packages: {0}'.format(missing[1]))

        # Find the framework bundle
        to_install = resolution[0]
        for bundle in to_install:
            if bundle.is_framework():
                framework = bundle

        else:
            raise ValueError('No OSGi framework in the resolution.')

        # Remove the framework from the list of bundles
        to_install.remove(framework)

        return framework, to_install


    def start_osgi(self, framework_descr, bundles):
        """
        Loads a OSGi framework using the given framework bundle, installs
        the specified bundles and finally starts the framework.
        """
        # Get the FrameworkFactory implementation name
        factory_class = framework_descr.get_framework_factory()

        # Load the class
        FrameworkFactory = jpype.JClass(factory_class)

        # Instantiate the framework instance
        self._osgi = FrameworkFactory().newFramework(None)
        self._osgi.init()

        # Install the bundles
        context = self._osgi.getBundleContext()
        for bundle_descr in bundles:
            context.installBundle(bundle_descr.get_url())

        # Start the framework
        self._osgi.start()


    @Validate
    def validate(self, context):
        """
        Component validated. Starts the JVM.
        """
        # Prepare the repository
        self._repository = man_parser.Repository()
        self.load_repositories()

        # Get the isolate configuration
        isolate_conf = self._config.get_current_isolate()
        isolate_id = isolate_conf.id

        # TODO: Get the isolate minimal bundles
        specified_bundles = isolate_conf.get_bundles()

        # TODO: Get the isolate factories
        factories = isolate_conf.get_base_composition().get_factories()

        # TODO: Get the system packages
        system_packages = isolate_conf.get_system_packages()

        # Resolve the list of bundles to install
        framework, bundles = self.get_osgi_bundles(specified_bundles, factories,
                                                   system_packages)

        # Start the JVM
        jvm_args = self.make_jvm_args(isolate_id, [framework.filename],
                                      {'osgi.shell.telnet.port': 6000})
        self._jvm = jpype.startJVM(jpype.getDefaultJVMPath(), *jvm_args)
        __vm_started__ = True

        # Start the OSGi framework
        self.start_osgi(framework, bundles)

        # TODO: Start the composition (or let another component do it)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated. Stops the JVM.
        """
        if self._osgi is not None:
            self._osgi.stop()
            self._osgi = None

        if self._jvm is not None:
            self._jvm.shutdownJVM()
            self._jvm = None
