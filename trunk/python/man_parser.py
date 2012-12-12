#!/usr/bin/python
# -- Content-Encoding: UTF-8 --

from pprint import pprint

import ipojo_parser
import operator
import os
import shlex
import sys
import zipfile

PYTHON3 = (sys.version_info[0] == 3)
if PYTHON3:
    # Python 3
    from io import StringIO

else:
    # Python 2
    from StringIO import StringIO

# ------------------------------------------------------------------------------

# Empty bundle representing the system
SYSTEM_BUNDLE = object()

# FrameworkFactory service descriptor in the framework JAR file
FRAMEWORK_FACTORY_SERVICE_FILE = 'META-INF/services/org.osgi.framework.launch.FrameworkFactory'

# ------------------------------------------------------------------------------

class Manifest(object):
    """
    Java Manifest parser
    """
    def __init__(self):
        """
        Sets up the parser
        """
        self.entries = {}


    def get(self, key, default=None):
        """
        Retrieves the value associated to the given key, or the default value

        :param key: A key in the manifest
        :param default: A default value
        :return: The manifest or the default value
        """
        return self.entries.get(key, default)


    def read_from_jar(self, jar_file):
        """
        Reads the Manifest of a Jar file
        """
        self.entries.clear()

        jar = zipfile.ZipFile(jar_file)
        try:
            manifest = jar.read('META-INF/MANIFEST.MF')
            self.parse(manifest)

        finally:
            jar.close()


    def parse(self, manifest):
        """
        Parses the given Manifest file content

        :param manifest: The content of a Manifest file
        """
        # Clear current entries
        self.entries.clear()

        if PYTHON3 and not isinstance(manifest, str):
            # Python 3 doesn't like bytes
            manifest = str(manifest, 'UTF-8')

        manifest_io = StringIO(manifest)
        key = None
        for line in manifest_io.readlines():

            if key is not None and line[0] == ' ':
                # Line continuation
                self.entries[key] += line.strip()

            else:
                # Strip the line
                line = line.strip()
                if not line:
                    # Empty line
                    key = None
                    continue

                # We have a key
                key, value = line.split(':', 1)

                # Strip values
                self.entries[key] = value.strip()

        manifest_io.close()


    def get_packages_list(self, manifest_key):
        """
        Retrieves a list of packages and their attributes

        :param manifest_key: Name of the package list in the manifest
        :return: A dictionary: package -> dictionary of attributes
        """
        parsed_list = {}
        packages_list = self.entries.get(manifest_key, '').strip()

        if packages_list:

            # Use shlex to handle quotes
            parser = shlex.shlex(packages_list, posix=True)
            parser.whitespace = ','
            parser.whitespace_split = True

            for package_str in parser:
                # Extract import values
                package_info = package_str.strip().split(';')

                name = package_info[0]
                attributes = {}
                for value in package_info[1:]:
                    if value:
                        attr_name, attr_value = value.split('=', 1)
                        if attr_name[-1] == ':':
                            # Remove the ':' of ':=' in some attributes
                            attr_name = attr_name[:-1].strip()

                        attributes[attr_name] = attr_value.strip()

                parsed_list[name] = attributes

        return parsed_list



    def format(self):
        """
        Formats the entries to be Manifest format compliant
        """
        # Format values
        lines = []

        # First line: Manifest version
        lines.append(': '.join(('Manifest-Version',
                                self.entries.get('Manifest-Version', '1.0'))))

        # Sort keys, except the version
        keys = [key.strip() for key in self.entries.keys()
                if key != 'Manifest-Version']
        keys.sort()

        # Wrap values
        for key in keys:
            line = ': '.join((key, self.entries[key].strip()))
            lines.extend(self.wrap_line(line))

        return '\n'.join(lines)


    def wrap_line(self, line):
        """
        Wraps a line, Manifest style

        :param line: The line to wrap
        :return: The wrapped line
        """
        lines = []
        # 70 chars for the first line
        lines.append(line[:70])

        # space + 69 chars for the others
        chunk = line[70:]
        while chunk:
            lines.append(' ' + chunk[:69])
            chunk = chunk[69:]

        return lines

# ------------------------------------------------------------------------------

class Version(object):
    """
    Represents a version (OSGi style)
    """
    def __init__(self, version_str=None):
        """
        Parses the given version string, if given
        """
        self.version = None
        self.qualifier = ''

        if version_str is not None:
            self.parse(version_str)


    def __str__(self):
        """
        String representation
        """
        if self.version is None:
            return '0.0.0'

        version = '.'.join((str(version) for version in self.version))
        if self.qualifier:
            version += '-' + self.qualifier

        return version


    def __repr__(self):
        """
        Object string representation
        """
        return "{0}.Version('{1}')".format(__name__, self.__str__())


    def __normalize_cmp(self, other):
        """
        Returns a version of both local and other version with the same tuple
        """
        local_version = self.version
        other_version = other.version

        if local_version is None or other_version is None:
            # Do nothing if one of the version is None
            return local_version, other_version

        local_len = len(self.version)
        other_len = len(other.version)

        # Add the missing length
        if local_len < other_len:
            local_version = list(self.version)
            local_version.extend([0] * (other_len - local_len))
            local_version = tuple(local_version)

        elif local_len > other_len:
            other_version = list(other.version)
            other_version.extend([0] * (local_len - other_len))
            other_version = tuple(other_version)

        return local_version, other_version


    def __lt__(self, other):
        """
        Compares this version with another
        """
        if not isinstance(other, Version):
            # Not a version
            return NotImplemented

        local_version, other_version = self.__normalize_cmp(other)
        if local_version == other_version:
            return self.qualifier < other.qualifier

        return local_version < other_version


    def __le__(self, other):
        """
        Compares this version with another
        """
        equals = self.__eq__(other)
        if equals is NotImplemented:
            return NotImplemented

        return equals or self.__lt__(other)


    def __eq__(self, other):
        """
        Compares this version with another
        """
        if not isinstance(other, Version):
            # Not a version
            return NotImplemented

        local_version, other_version = self.__normalize_cmp(other)
        return local_version == other_version \
            and self.qualifier == other.qualifier


    def __ne__(self, other):
        """
        Compares this version with another
        """
        equality = self.__eq__(other)
        if equality is NotImplemented:
            return NotImplemented

        return not equality


    def __gt__(self, other):
        """
        Compares this version with another
        """
        if not isinstance(other, Version):
            # Not a version
            return NotImplemented

        local_version, other_version = self.__normalize_cmp(other)

        if local_version == other_version:
            return self.qualifier > other.qualifier

        return local_version > other_version


    def __ge__(self, other):
        """
        Compares this version with another
        """
        equals = self.__eq__(other)
        if equals is NotImplemented:
            return NotImplemented

        return equals or self.__gt__(other)


    def __add__(self, other):
        """
        Adds a version tuple or object. Trims or enlarges the current version
        tuple to the size of the given one.
        """
        if not isinstance(other, Version):
            other = Version(other)

        if other is None:
            return

        # Compute lengths
        local_len = len(self.version)
        other_len = len(other.version)

        i = 0
        new_version = []
        while i < other_len:
            if i < local_len:
                local_part = self.version[i]

            else:
                local_part = None

            if i < other_len:
                other_part = other.version[i]

                if local_part is not None:
                    # Increment version number
                    new_version.append(local_part + other_part)

                else:
                    # All current parts added
                    new_version.append(other_part)

            else:
                # All other parts added
                new_version.append(local_part)

            i += 1

        while len(new_version) < local_len:
            # Fill with zeros, to match the previous size
            new_version.append(0)

        # Add the qualifier, if any
        qualifier = other.qualifier or self.qualifier
        if qualifier:
            new_version.append(qualifier)

        return Version(new_version)


    def matches(self, other):
        """
        Tests if this version matches the given one
        """
        if other is None:
            # None matches everything
            return True

        if self.version is None:
            # No version matches any version
            return True

        if isinstance(other, str):
            # The given string can be either a version or a range
            other = other.strip()
            if other[0] == '[':
                # Range comparison
                inclusive = (other[-1] == ']')
                versions = other[1:-1].split(',')

                # Convert boundaries
                minimum = Version(versions[0])
                maximum = Version(versions[1])

            else:
                minimum = Version(other)
                # Allow binding up to the next major version (excluded)
                maximum = Version(other) + (1,)
                inclusive = False

        if isinstance(other, Version):
            # Compared to another version
            if other.version is None:
                # Other matches any version
                return True

            else:
                # Allow binding up to the next major version (excluded)
                minimum = other
                maximum = other + (1,)
                inclusive = False

        if minimum is not None and self < minimum:
            # We're under the minimal version
            return False

        elif maximum is not None and self > maximum:
            # We're above the maximal version
            return False

        elif not inclusive and self == maximum:
            # We're at the upper boundary
            return False

        else:
            # Range tests passed
            return True


    def parse(self, version_str):
        """
        Parses the given version string
        """
        # Reset values
        self.version = None
        self.qualifier = ''

        if not version_str:
            # Nothing to do
            return

        if isinstance(version_str, Version):
            # Direct copy
            self.version = version_str.version
            self.qualifier = version_str.qualifier
            return

        if isinstance(version_str, (tuple, list)):
            # We have a raw version tuple
            if isinstance(version_str[-1], str):
                # ... with a qualifier
                self.version = tuple(version_str[:-1])
                self.qualifier = version_str[-1]

            else:
                # ... without it
                self.version = tuple(version_str)

        else:
            # String version
            version_str = str(version_str)

            # Separate pars
            version = version_str.split('.')
            if '-' in version[-1]:
                # Qualifier in the last element
                last_part, self.qualifier = version[-1].split('-')
                version[-1] = last_part

            else:
                try:
                    # Try a conversion of the last part
                    int(version[-1])

                except ValueError:
                    # Last part is not an integer, so it a qualifier
                    self.qualifier = version[-1]
                    version = version[:-1]

            self.version = version

        # Normalize the version
        self.version = tuple((int(part) for part in self.version))

        if not any(self.version):
            # Version is only zeros (0.0.0)
            self.version = None
            self.qualifier = ''

# ------------------------------------------------------------------------------

class Bundle(object):
    """
    Represents a bundle
    """
    def __init__(self, jar_file):
        """
        Represents a bundle
        """
        self._manifest = Manifest()
        self._manifest.read_from_jar(jar_file)

        self.filename = jar_file

        # Extract the bundle name, ignoring attributes
        self.name = self._manifest.get('Bundle-SymbolicName', '').split(';')[0]
        if not self.name:
            raise ValueError('{0} has no bundle name'.format(jar_file))

        self.version = Version(self._manifest.get('Bundle-Version'))
        self._export = self._manifest.get_packages_list('Export-Package')
        self._import = self._manifest.get_packages_list('Import-Package')
        self._require = self._manifest.get_packages_list('Require-Bundle')


    def __str__(self):
        """
        String representation of the bundle
        """
        return '{self.name}-{self.version}'.format(self=self)

    __repr__ = __str__


    def exports(self, package_name, required_version=None):
        """
        Tests if the given package is exported by this bundle

        :param package_name: The name of a package
        :param required_version: The required version/range of this package
        :return: True if the package is exported by this bundle
        """
        if package_name not in self._export:
            return False

        version = Version(self._export[package_name].get('version', None))
        return version.matches(required_version)


    def imports(self, other_bundle):
        """
        Tests if this bundle should imports the given one

        :return: True if this bundle imports the other one through
                 Require-Bundle or Import-Package
        """
        if other_bundle.name in self._require:
            # Bundle referenced with a Require-Bundle
            requirement = self._require[other_bundle.name]
            if other_bundle.version.matches(requirement.get('version', None)):
                # The given bundle matches our requirements
                return True

        # Try with the packages
        for package in self._import:
            if package in other_bundle._export:
                # Get the required version
                requirement = self._import[package].get('version', None)

                # Parse the provided version
                provided = other_bundle._export[package].get('version', None)
                if Version(provided_version).matches(requirement):
                    return True

        return False


    def is_fragment(self):
        """
        Tests if this bundle is a fragment
        """
        return 'Fragment-Host' in self._manifest.entries


    def is_framework(self):
        """
        Tests if this bundle is an OSGi framework
        """
        framework = False

        jar_file = zipfile.ZipFile(self.filename)
        try:
            # Try to retrieve the FrameworkFactory service descriptor
            if jar_file.getinfo(FRAMEWORK_FACTORY_SERVICE_FILE):
                # No error
                framework = True

        except KeyError:
            # File not found
            framework = False

        finally:
            jar_file.close()

        return framework


    def get_exported_packages(self):
        """
        Retrieves a Name -> Version dictionary of the exported packages
        """
        return ((name, Version(attributes.get('version')))
                for name, attributes in self._export.items())


    def get_framework_factory_name(self):
        """
        Retrieves the content of the FrameworkFactory service file
        """
        jar_file = zipfile.ZipFile(self.filename)
        try:
            framework_factory = jar_file.read(FRAMEWORK_FACTORY_SERVICE_FILE)
            if PYTHON3:
                framework_factory = str(framework_factory, 'UTF-8')

            return framework_factory.strip()

        except KeyError:
            # Not a framework JAR
            return None

        finally:
            jar_file.close()


    def get_manifest_attribute(self, key, default=None):
        """
        Reads an entry from the Manifest of the bundle
        """
        return self._manifest.get(key, default)

# ------------------------------------------------------------------------------

class Repository(object):
    """
    Represents a repository
    """
    def __init__(self):
        """
        Sets up the repository
        """
        # Name -> [Bundle]
        self._bundles = {}

        # Name -> [(Package Version, Bundle)]
        self._packages = {}


    def __add_bundle(self, bundle, bundle_registry=None, package_registry=None):
        """
        Adds a bundle to the given registry
        """
        if bundle_registry is None:
            bundle_registry = self._bundles

        bundle_list = bundle_registry.setdefault(bundle.name, [])
        bundle_list.append(bundle)
        bundle_list.sort(reverse=True)

        # Store exported packages
        for name, version in bundle.get_exported_packages():
            self.__add_package(package_registry, name, version, bundle)


    def __add_package(self, registry, name, version, bundle):
        """
        Adds a package to the given registry
        """
        if registry is None:
            registry = self._packages

        package_list = registry.setdefault(name, [])
        package_list.append((version, bundle))
        package_list.sort(key=operator.itemgetter(0), reverse=True)


    def find_ipojo_factories(self, factories):
        """
        Returns the list of bundles that provides the given factories
        
        :param factories: A list of iPOJO factory names
        :return: A tuple ({Name -> [Bundles]}, [Not found factories])
        """
        factories_set = set(factories)
        resolution = {}

        # Prepare the parser
        parser = ipojo_parser.IPojoMetadataParser()

        for bundle_list in self._bundles.values():
            for bundle in bundle_list:
                # Get the manifest entry
                ipojo_entry = bundle.get_manifest_attribute('iPOJO-Components',
                                                            None)
                if not ipojo_entry:
                    # No iPOJO entry in this bundle
                    continue

                # Parse it
                ipojo_root = parser.parse(ipojo_entry)

                # Find the factories
                bundle_factories = []

                for component in ipojo_root.get_elements('component'):
                    factory_attr = component.get_attribute('name')

                    if factory_attr and factory_attr.value in factories_set:
                        # Add the bundle to the factory providers
                        resolution.setdefault(factory_attr.value, []).append(bundle)

        # Sort the providers
        for bundles_list in resolution.values():
            bundles_list.sort(reverse=True)

        return resolution, factories_set.difference(set(resolution.keys()))


    def get_bundle(self, name, version=None, registry=None):
        """
        Retrieves a bundle from the repository

        :param name: The bundle symbolic name
        :param version: The bundle version (None or '0.0.0' for any)
        :return: The first matching bundle
        :raise ValueError: If the bundle can't be found
        """
        if registry is None:
            registry = self._bundles

        if isinstance(name, Bundle):
            # Got a bundle
            bundle = name
            if bundle in registry:
                return bundle

            else:
                # Use the bundle symbolic name and version
                name = bundle.name
                version = bundle.version

        matching = registry.get(name, None)
        if not matching:
            raise ValueError('Bundle {0} not found.'.format(name))

        for bundle in matching:
            if bundle.version.matches(version):
                return bundle
        else:
            raise ValueError('Bundle {0} not found for version {1}' \
                             .format(name, version))


    def get_package(self, name, version=None,
                    bundle_registry=None, package_registry=None):
        """
        Retrieves a bundle from the repository providing the given package

        :param name: The package name
        :param version: The package version (None or '0.0.0' for any)
        :return: The first bundle providing the package, None if not found
        """
        if bundle_registry is None:
            bundle_registry = self._bundles

        if package_registry is None:
            package_registry = self._packages

        matching = package_registry.get(name, None)
        if not matching:
            return None

        for pkg_version, pkg_bundle in matching:
            if pkg_version.matches(version):
                return pkg_bundle

        return None


    def load_folder(self, folder):
        """
        Reads all bundles in the given folder (not recursive)

        :param folder: A repository folder
        :raise ValueError: Invalid parameter
        """
        real_folder = os.path.realpath(folder)
        if not os.path.isdir(real_folder):
            raise ValueError('Not a directory: {0}'.format(folder))

        # Load all bundles
        for filename in os.listdir(real_folder):
            if filename.endswith('.jar'):
                # Found a JAR file
                try:
                    # Parse the manifest
                    bundle = Bundle(os.path.join(real_folder, filename))

                    # Store the bundle
                    self.__add_bundle(bundle)

                except ValueError:
                    # Not a bundle
                    raise


    def resolve_installation(self, bundles, system_packages=None):
        """
        Returns all the bundles that must be installed in order to have the
        given bundles resolved.

        To simplify the work, the OSGi framework should be the first one in
        the list.

        :param bundles: A list of bundles to be resolved
        :param system_packages: Packages considered available by the framework
        :return: A tuple: (to install, dependencies,
                (missing bundles, missing packages))
        """
        # Name -> Bundle for this resolution
        local_bundles = {}
        # Name -> (Version, Bundle)
        local_packages = {}

        # Bundle -> [Bundles]
        dependencies = {}

        # Missing elements
        missing_bundles = set()
        missing_packages = set()

        # Consider system packages already installed
        if system_packages:
            for name in system_packages:
                self.__add_package(local_packages, name, Version(None),
                                   SYSTEM_BUNDLE)

        # Resolution loop
        to_install = [self.get_bundle(name) for name in bundles]
        i = 0
        while i < len(to_install):
            # Loop control
            bundle = to_install[i]
            i += 1

            if bundle is None:
                # Ignore None bundle (system bundle)
                continue

            # Add the current bundle
            self.__add_bundle(bundle, local_bundles, local_packages)
            dependencies[bundle] = []

            # Resolve Require-Bundle
            for required in bundle._require:
                # Get the required version
                required_version = bundle._require[required].get('version',
                                                                 None)

                # Work only if necessary
                provider = self.get_bundle(required, required_version,
                                           local_bundles)

                if provider:
                    # Found the bundle in the resolved bundles
                    if provider is not SYSTEM_BUNDLE:
                        dependencies[bundle].append(provider)

                else:
                    # Find it
                    provider = self.get_bundle(required, required_version,
                                               self._bundles)
                    if provider and provider is not SYSTEM_BUNDLE:
                        # Store the bundle
                        self.__add_bundle(provider,
                                          local_bundles, local_packages)

                        # Store the dependency
                        dependencies[bundle].append(provider)

                        # The new bundle will be resolved later
                        if added not in to_install:
                            # We'll have to resolve it
                            to_install.append(provider)

                    else:
                         # No match found
                         missing_bundles.add(required)

            # Resolve Import-Package
            for imported in bundle._import:
                # Get the required version
                pkg_version = bundle._import[imported].get('version', None)

                # Self-import ?
                if bundle.exports(imported, pkg_version):
                    # Nothing to do for this package
                    continue

                # Work only if necessary
                provider = self.get_package(imported, pkg_version,
                                            local_bundles, local_packages)

                if provider:
                    # Found the package in the resolved bundles
                    if provider is not SYSTEM_BUNDLE:
                        dependencies[bundle].append(provider)

                else:
                    # Find it
                    provider = self.get_package(imported, pkg_version,
                                                self._bundles, self._packages)

                    if not provider:
                        # Missing
                        missing_packages.add(imported)

                    elif provider is not SYSTEM_BUNDLE:
                        # Store the bundle
                        self.__add_bundle(provider,
                                          local_bundles, local_packages)

                        # Store the dependency
                        dependencies[bundle].append(provider)

                        if provider not in to_install:
                            # We'll have to resolve it
                            to_install.append(provider)

        return to_install, dependencies, (missing_bundles, missing_packages)

# ------------------------------------------------------------------------------

def find_psem2m_factories(monitor=False):
    """
    Finds the PSEM2M iPOJO factories
    """
    # Resolve component factories
    factories = ['psem2m-config-factory',
                 'psem2m-config-json-factory',
                 'psem2m-composer-agent-factory',
                 'psem2m-slave-agent-core-factory']

    # ... signals
    factories += ['psem2m-signal-receiver-factory',
                  'psem2m-signal-broadcaster-factory',
                  'psem2m-signals-directory-factory',
                  'psem2m-signals-directory-updater-factory',
                  'psem2m-remote-signal-sender-http-factory',
                  'psem2m-remote-signal-receiver-http-factory',
                  'psem2m-signals-data-java-factory',
                  'psem2m-signals-data-json-factory']

    # ... remote services
    factories += ['psem2m-remote-rsb-factory',
                  'psem2m-remote-rsb-signal-handler-factory',
                  'psem2m-remote-service-exporter-factory',
                  'psem2m-remote-service-importer-factory',
                  'psem2m-remote-endpoint-handler-jsonrpc-factory',
                  'psem2m-remote-client-handler-jsonrpc-factory',
                  'psem2m-remote-rsr-factory']

    if monitor:
        # Monitor
        factories += ['psem2m-forker-aggregator-factory',
                      'psem2m-monitor-base-factory',
                      'psem2m-monitor-logic-factory',
                      'psem2m-monitor-status-factory',
                      'psem2m-status-storage-creator-factory']

        # Composer
        factories += ['psem2m-composer-config-json-factory',
                      'psem2m-composer-status-factory',
                      'psem2m-composer-logic-factory',
                      'psem2m-composer-monitor-factory',
                      'psem2m-composer-config-json-factory']

    # Find'em
    factories, unresolved = repo.find_ipojo_factories(factories)
    if unresolved:
        raise KeyError('Some factories are missing: {0}'.format(unresolved))

    # Return a list of bundles
    return [bundles_list[0] for bundles_list in factories.values()]


def prepare_repository(*repo_dirs):
    repo = Repository()
    for repo_dir in repo_dirs:
        repo.load_folder(repo_dir)

    return repo

def get_psem2m_bundles(repository):
    # Load the repository
    repo = Repository()
    for repo_dir in repo_dirs:
        repo.load_folder(repo_dir)

    # Find the bundles providing our factories
    print('-' * 80)
    factories_bundles = find_psem2m_factories(False)

    # Packages exported by the framework
    system_packages = ('javax.security.cert', 'javax.net.ssl',
                       'javax.xml.parsers', 'org.w3c.dom', 'org.xml.sax',
                       'org.xml.sax.helpers')

    # Non-iPOJO bundles list
    basic_bundles = ['org.psem2m.isolates.constants',
                       'org.psem2m.isolates.base',
                       'org.apache.felix.shell',
                       'org.apache.felix.shell.remote']

    print('-' * 80)
    # Resolve the installation
    to_install, dependencies, missing = repo.resolve_installation(\
                                            basic_bundles + factories_bundles,
                                            system_packages)

    if missing[0]:
        print('> Missing bundles')
        pprint(missing[0])

    if missing[1]:
        print('> Missing packages')
        pprint(missing[1])

    print('-' * 80)
    print('> Bundles to install')
    pprint(to_install)

    # Get the framework JAR
    for bundle in to_install:
        if bundle.is_framework():
            framework = bundle
            break
    else:
        raise ValueError('No OSGi framework found. Abandon.')

    # Remove the framework of the bundles to install
    to_install.remove(framework)

    return framework, to_install

def run_isolate(isolate_id, framework_jar, bundles_jars):
    # Run the framework
    import jpype
    from jpype import java

    # Start the JVM
    print('Loading JVM...')
    jpype.startJVM(jpype.getDefaultJVMPath(),
                   '-Djava.class.path=' + framework_jar.filename,
                   '-Dorg.psem2m.platform.isolate.id=' + isolate_id,
                   '-Dosgi.shell.telnet.port=6000')

    # Load the FrameworkFactory implementation
    print('Looking for the FrameworkFactory implementation...')
    framework_factory_class = framework_jar.get_framework_factory_name()

    print('Loading FrameworkFactory class: {0}'.format(framework_factory_class))
    FrameworkFactory = jpype.JClass(framework_factory_class)

    # Instantiate the factory
    factory = FrameworkFactory()

    # Instantiate a framework instance
    print('Creating framework...')
    felix = factory.newFramework(None)

    # Initialize it
    print('Init...')
    felix.init()

    print('Installing bundles...')
    context = felix.getBundleContext()
    installed = {}
    for bundle in bundles_jars:
        if bundle is not framework:
            fw_bundle = context.installBundle('file:' + bundle.filename.replace(' ', '%20'))
            installed[fw_bundle] = bundle

    print_bundles(context)
    raw_input('Press Enter to continue')

    print('Start !')
    felix.start()

    print_bundles(context)
    raw_input('Press Enter to continue')

    print('Stopping framework')
    felix.stop()

    print('Stopping JVM')
    jpype.shutdownJVM()
    print('Done')


def print_bundles(context):
    for bundle in context.getBundles():
        print('> Bundle {0:2d}: {1} ({2})'.format(bundle.getBundleId(),
                                                  bundle.getSymbolicName(),
                                                  bundle.getState()))

# ------------------------------------------------------------------------------

if __name__ == "__main__":

    #    repo_dirs = ['/home/thomas/boulot/git/psem2m/platforms/felix',
    #                 '/home/thomas/Bureau/test']

    repo_dirs = ['/home/tcalmant/programmation/workspaces/psem2m/platforms/felix',
                 '/home/tcalmant/programmation/workspaces/psem2m/platforms/base-demo-july2012/repo']

    repo = prepare_repository(*repo_dirs)
    framework, bundles = get_psem2m_bundles(repo)
    run_isolate('toto', framework, bundles)
