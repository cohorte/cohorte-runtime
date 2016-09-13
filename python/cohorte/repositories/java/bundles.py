#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Java JARs repository utility module

**TODO:**
* Enhance API & code

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
import contextlib
import logging
import operator
import os
import zipfile

# Pelix
from pelix.utilities import to_str
from pelix.ipopo.decorators import ComponentFactory, Provides, Property, \
    Invalidate, Validate

# Cohorte
import cohorte
import cohorte.repositories
from cohorte.repositories.beans import Artifact, Version
from cohorte.repositories.java.manifest import Manifest

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

# Empty bundle representing the system
SYSTEM_BUNDLE = object()

# Path of the manifest in a JAR
MANIFEST_FILE = 'META-INF/MANIFEST.MF'

# Path of the descriptions of the bundle services
BUNDLE_SERVICES_FOLDER = 'META-INF/services'

# FrameworkFactory service descriptor in the framework JAR file
FRAMEWORK_SERVICE = 'org.osgi.framework.launch.FrameworkFactory'

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


class Bundle(Artifact):
    """
    Represents an OSGi bundle
    """
    def __init__(self, jar_file, manifest):
        """
        Sets up the bundle details

        :param jar_file: Path to the JAR file (can be empty, but shouldn't)
        :param manifest: The parsed Manifest of the JAR file (mandatory)
        :raise ValueError: Invalid manifest argument
        """
        # Validate parameters
        if not manifest:
            raise ValueError("Manifest can't be None")

        # Extract information from the manifest
        self._manifest = manifest
        name = self._manifest.get('Bundle-SymbolicName', '').split(';', 2)[0]
        version = Version(self._manifest.get('Bundle-Version'))

        # Configure the Artifact
        Artifact.__init__(self, "java", name, version, jar_file)

        # Store Package information
        self.all_exports = \
            self._manifest.extract_packages_list('Export-Package')
        self.all_imports = \
            self._manifest.extract_packages_list('Import-Package')
        self.all_require = \
            self._manifest.extract_packages_list('Require-Bundle')

    def exports(self, package_name, required_version=None):
        """
        Tests if the given package is exported by this bundle

        :param package_name: The name of a package
        :param required_version: The required version/range of this package
        :return: True if the package is exported by this bundle
        """
        if package_name not in self.all_exports:
            return False

        version = Version(self.all_exports[package_name].get('version'))
        return version.matches(required_version)

    def imports(self, other_bundle):
        """
        Tests if this bundle should imports the given one

        :return: True if this bundle imports the other one through
                 Require-Bundle or Import-Package
        """
        if other_bundle.name in self.all_require:
            # Bundle referenced with a Require-Bundle
            requirement = self.all_require[other_bundle.name]
            if other_bundle.version.matches(requirement.get('version')):
                # The given bundle matches our requirements
                return True

        # Try with the packages
        for package in self.all_imports:
            if package in other_bundle.all_exports:
                # Get the required version
                requirement = self.all_imports[package].get('version')

                # Parse the provided version
                provided = other_bundle.all_exports[package].get('version')
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
                for name, attributes in self.all_exports.items())

    def get_manifest(self):
        """
        Retrieves the parsed Manifest
        """
        return self._manifest

    def get_service(self, service_name):
        """
        Retrieves the content of a service description file (like the
        FrameworkFactory service)

        :param service_name: The name of a service
        :return: The content of the service description file, or None
        """
        if not service_name:
            return None

        jar_file = zipfile.ZipFile(self.file)
        try:
            service_factory = jar_file.read(
                '{0}/{1}'.format(BUNDLE_SERVICES_FOLDER, service_name))
            service_factory = to_str(service_factory)
            return service_factory.strip()
        except KeyError:
            # Not a framework JAR
            return None
        finally:
            jar_file.close()

# ------------------------------------------------------------------------------


@ComponentFactory("cohorte-repository-artifacts-java-factory")
@Provides(cohorte.repositories.SERVICE_REPOSITORY_ARTIFACTS)
@Property('_language', cohorte.repositories.PROP_REPOSITORY_LANGUAGE, "java")
class OSGiBundleRepository(object):
    """
    Represents a repository
    """
    def __init__(self):
        """
        Sets up the repository
        """
        # Language (property)
        self._language = None

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

    def __len__(self):
        """
        Length of a repository <=> number of individual artifacts
        """
        return sum((len(bundles) for bundles in self._bundles.values()))

    def __add_bundle(self, bundle, bundle_registry=None,
                     package_registry=None):
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
        self._files[bundle.file] = bundle

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
        manifest = Manifest()
        with contextlib.closing(zipfile.ZipFile(filename)) as jar:
            manifest.parse(jar.read(MANIFEST_FILE))

        # Store the bundle
        self.__add_bundle(Bundle(filename, manifest))

    def add_directory(self, dirname):
        """
        Recursively adds all .jar bundles found in the given directory into the
        repository

        :param dirname: A path to a directory
        """
        for root, _, filenames in os.walk(dirname, followlinks=True):
            for filename in filenames:
                if os.path.splitext(filename)[1] == '.jar':
                    fullname = os.path.join(root, filename)
                    try:
                        self.add_file(fullname)

                    except ValueError as ex:
                        _logger.warning(ex)

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

    def get_artifact(self, name=None, version=None, filename=None,
                     registry=None):
        """
        Retrieves a bundle from the repository

        :param name: The bundle symbolic name (mutually exclusive with
                     filename)
        :param version: The bundle version (None or '0.0.0' for any), ignored
                        if filename is used
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
            raise ValueError("Bundle file not found: {0}".format(filename))

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

        raise ValueError('Bundle {0} not found for version {1}'
                         .format(name, version))

    def get_language(self):
        """
        Retrieves the language of the artifacts stored in this repository
        """
        return self._language

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

    def resolve_installation(self, bundles, system_artifacts=None,
                             system_packages=None):
        """
        Returns all the bundles that must be installed in order to have the
        given bundles resolved.

        To simplify the work, the OSGi framework should be the first one in
        the list.

        :param bundles: A list of bundles to be resolved
        :param system_packages: Packages considered available by the framework
        :return: A tuple: (bundles, dependencies, missing artifacts,
                 missing packages)
        :raise ValueError: One of the given bundles is unknown
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

        # Consider system bundles already installed
        if system_artifacts:
            for artifact in system_artifacts:
                if isinstance(artifact, Bundle):
                    # Got a bundle
                    self.__add_bundle(artifact, local_bundles, local_packages)
                elif isinstance(artifact, Artifact):
                    # Got an artifact
                    bundle = self.get_artifact(artifact.name, artifact.version,
                                               artifact.file)
                    if bundle:
                        self.__add_bundle(bundle, local_bundles,
                                          local_packages)
                    else:
                        _logger.warning("Unknown system bundle: %s", artifact)
                else:
                    _logger.warning("Unhandled system artifact: %s", artifact)

        # Resolution loop
        to_install = [self.get_artifact(name) for name in bundles]
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
            for required in bundle.all_require:
                # Get the required version
                required_version = bundle.all_require[required].get('version')

                # Find the bundle
                registry = None
                provider = None
                for registry in (local_bundles, self._bundles):
                    try:
                        provider = self.get_artifact(required,
                                                     required_version,
                                                     None, registry)
                        # Found one
                        break
                    except ValueError:
                        # Try next
                        pass
                else:
                    # No provider found
                    missing_bundles.add(required)
                    continue

                if provider is SYSTEM_BUNDLE:
                    # Nothing to do with system bundles
                    continue

                # Store the bundle we found
                dependencies[bundle].append(provider)

                if registry is self._bundles:
                    # The provider was found in the global registry, store it
                    self.__add_bundle(provider,
                                      local_bundles, local_packages)

                    # The new bundle will be resolved later
                    if provider not in to_install:
                        # We'll have to resolve it
                        to_install.append(provider)

            # Resolve Import-Package
            for imported in bundle.all_imports:
                # Get the required version
                pkg_version = bundle.all_imports[imported].get('version')

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

        return to_install, dependencies, missing_bundles, missing_packages

    def walk(self):
        """
        # Walk through the known artifacts
        """
        for bundles in self._bundles.values():
            for bundle in bundles:
                yield bundle

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # Home/Base repository
        for key in (cohorte.PROP_BASE, cohorte.PROP_HOME):
            repository = os.path.join(context.get_property(key), "repo")
            self.add_directory(repository)

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.clear()
