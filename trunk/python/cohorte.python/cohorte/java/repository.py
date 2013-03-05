#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Java JARs repository utility module

**TODO:**
* Enhance API & code
* Convert into a service ?

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# Pelix utilities
from pelix.utilities import is_string

# Manifest parser
import cohorte.java.manifest as manifest_parser

# Standard library
import contextlib
import operator
import os
import sys
import zipfile

# ------------------------------------------------------------------------------

# Empty bundle representing the system
SYSTEM_BUNDLE = object()

# Path of the manifest in a JAR
MANIFEST_FILE = 'META-INF/MANIFEST.MF'

# Path of the descriptions of the bundle services
BUNDLE_SERVICES_FOLDER = 'META-INF/services'

# FrameworkFactory service descriptor in the framework JAR file
FRAMEWORK_SERVICE = 'org.osgi.framework.launch.FrameworkFactory'

PYTHON3 = (sys.version_info[0] == 3)

# ------------------------------------------------------------------------------

class Bundle(object):
    """
    Represents a bundle
    """
    def __init__(self, jar_file, manifest):
        """
        Sets up the bundle details
        
        :param jar_file: Path to the JAR file
        :param manifest: The parsed Manifest of the JAR file
        :raise ValueError: Invalid manifest argument
        """
        if not manifest:
            raise ValueError("Manifest can't be None")

        # Store informations
        self._manifest = manifest
        self.filename = jar_file

        # Extract the bundle name, ignoring attributes
        self.name = self._manifest.get('Bundle-SymbolicName', '').split(';')[0]
        if not self.name:
            raise ValueError('{0} has no bundle name'.format(jar_file))

        try:
            self.version = Version(self._manifest.get('Bundle-Version'))
        except ValueError as ex:
            # TODO:
            import logging
            logging.error("BUNDLE PARSE ERROR in %s", jar_file)
            raise

        self._export = self._manifest.extract_packages_list('Export-Package')
        self._import = self._manifest.extract_packages_list('Import-Package')
        self._require = self._manifest.extract_packages_list('Require-Bundle')


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
                if Version(provided).matches(requirement):
                    return True

        return False


    def is_fragment(self):
        """
        Tests if this bundle is a fragment
        
        :return: True if this bundle is a fragment
        """
        return 'Fragment-Host' in self._manifest.entries


    def get_exported_packages(self):
        """
        Retrieves a Name -> Version dictionary of the exported packages
        
        :return: A Name -> Version dictionary
        """
        return ((name, Version(attributes.get('version')))
                for name, attributes in self._export.items())


    def get_ipojo_root(self):
        """
        Retrieves the iPOJO root element from the Manifest file, if present
        
        :return: The iPOJO root Element, or None
        """
        if self._manifest:
            return self._manifest.get_ipojo_root()


    def get_service(self, service_name):
        """
        Retrieves the content of a service description file (like the
        FrameworkFactory service)
        
        :param service_name: The name of a service
        :return: The content of the service description file, or None
        """
        if not service_name:
            return None

        jar_file = zipfile.ZipFile(self.filename)
        try:
            service_factory = jar_file.read('{0}/{1}' \
                                            .format(BUNDLE_SERVICES_FOLDER,
                                                    service_name))
            if PYTHON3:
                service_factory = str(service_factory, 'UTF-8')

            return service_factory.strip()

        except KeyError:
            # Not a framework JAR
            return None

        finally:
            jar_file.close()


    def get_attribute(self, key, default=None):
        """
        Reads an entry from the Manifest of the bundle
        """
        return self._manifest.get(key, default)


    def get_url(self):
        """
        Retrieves the file:/ URL for this bundle
        """
        return 'file:{0}'.format(self.filename.replace(' ', '%20'))

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
            version = '{0}-{1}'.format(version, self.qualifier)

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

        if is_string(other):
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
        in_qualifier = False
        version = []
        qualifier = []
        for part in self.version:

            if in_qualifier:
                qualifier.append(part)

            else:
                # Not yet in the qualifier
                try:
                    version.append(int(part))

                except ValueError:
                    # Integer conversion error -> begin qualifier mode
                    in_qualifier = True
                    qualifier.append(part)

        # Don't forget the current qualifier
        if self.qualifier:
            qualifier.append(self.qualifier)

        # Update members
        self.version = tuple(version)
        self.qualifier = '.'.join(qualifier)

        if not any(self.version):
            # Version is only zeros (0.0.0)
            self.version = None
            self.qualifier = ''

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

        # File -> Bundle
        self._files = {}

        # Name -> [(Package Version, Bundle)]
        self._packages = {}


    def __contains__(self, item):
        """
        Tests if the given item is in the repository
        
        :param item: Item to be tested
        :return: True if the item is in the repository
        """
        if isinstance(item, Bundle):
            # Test the Bundle object
            return item in self._bundles.values()

        elif item in self._bundles:
            # Item matches a bundle name
            return True

        else:
            # Test the file name
            for name in (item, os.path.realpath(item)):
                if name in self._files:
                    return True

        # No match
        return False


    def __add_bundle(self, bundle, bundle_registry=None, package_registry=None):
        """
        Adds a bundle to the given registry
        
        :param bundle: A Bundle object
        :param bundle_registry: Registry where to store the bundle
        :param package_registry: Registry where to store the packages of this
                                 bundle
        """
        if bundle_registry is None:
            bundle_registry = self._bundles

        # Add the bundle to the dictionary
        bundle_list = bundle_registry.setdefault(bundle.name, [])
        bundle_list.append(bundle)
        bundle_list.sort(reverse=True)

        # Associate the file name with the bundle
        self._files[bundle.filename] = bundle

        # Store exported packages
        for name, version in bundle.get_exported_packages():
            self.__add_package(package_registry, name, version, bundle)


    def __add_package(self, registry, name, version, bundle):
        """
        Adds a Java package to the given registry
        
        :param registry: Registry where to store the package details
        :param name: Name of the package
        :param version: Version of the package
        :param bundle: Bundle providing this package
        """
        if registry is None:
            registry = self._packages

        package_list = registry.setdefault(name, [])
        package_list.append((version, bundle))
        package_list.sort(key=operator.itemgetter(0), reverse=True)


    def add_file(self, filename):
        """
        Adds a JAR file to the repository
        
        :param filename: A JAR file name
        """
        # Compute the real name of the JAR file
        filename = os.path.realpath(filename)

        # Parse the manifest
        manifest = manifest_parser.Manifest()
        with contextlib.closing(zipfile.ZipFile(filename)) as jar:
            manifest.parse(jar.read(MANIFEST_FILE))

        # Store the bundle
        self.__add_bundle(Bundle(filename, manifest))


    def clear(self):
        """
        Clears the repository content
        """
        self._files.clear()
        self._bundles.clear()
        self._packages.clear()


    def filter_services(self, service):
        """
        Generator to find all bundles that declares an implementation of the
        given service (in META-INF/services)
        
        :param service: A service name
        :return: A generator of bundles (can be empty)
        """
        for bundles in self._bundles.values():
            # Multiple bundles with the same name
            for bundle in bundles:
                if bundle.get_service(service) is not None:
                    yield bundle


    def find_ipojo_factories(self, factories):
        """
        Returns the list of bundles that provides the given factories
        
        :param factories: A list of iPOJO factory names
        :return: A tuple ({Name -> [Bundles]}, [Not found factories])
        """
        factories_set = set(factories)
        resolution = {}

        if not factories:
            # Nothing to do...
            return resolution, factories_set

        for bundle_list in self._bundles.values():
            for bundle in bundle_list:
                # Get the iPOJO root element
                ipojo_root = bundle.get_ipojo_root()
                if not ipojo_root:
                    # No iPOJO entry in this bundle
                    continue

                for component in ipojo_root.get_elements('component'):
                    factory_attr = component.get_attribute('name')

                    if factory_attr and factory_attr.value in factories_set:
                        # Add the bundle to the factory providers
                        resolution.setdefault(factory_attr.value, []) \
                                                                .append(bundle)

        # Sort the providers
        for bundles_list in resolution.values():
            bundles_list.sort(reverse=True)

        return resolution, factories_set.difference(set(resolution.keys()))


    def get_bundle(self, name=None, version=None, filename=None, registry=None):
        """
        Retrieves a bundle from the repository

        :param name: The bundle symbolic name (mutually exclusive with filename)
        :param version: The bundle version (None or '0.0.0' for any), ignored if
                        filename is used
        :param filename: The bundle file name (mutually exclusive with name)
        :param registry: Registry where to look for the bundle
        :return: The first matching bundle
        :raise ValueError: If the bundle can't be found
        """
        if registry is None:
            registry = self._bundles

        if filename:
            # Use the file name (direct search)
            bundle = self._files.get(filename)
            if bundle:
                # Found it
                return bundle

            for bundle_file in self._files:
                # Search by file base name
                if os.path.basename(bundle_file) == filename:
                    return self._files[bundle_file]

        if not name:
            # Not found by file name, and no name to look for
            from pprint import pformat
            raise ValueError("Bundle file not found: {0}\n{1}".format(filename, pformat(self._files)))

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
                        if provider not in to_install:
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
