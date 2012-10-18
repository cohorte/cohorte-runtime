#!/usr/bin/env python
#
#    James Percent (james@empty-set.net)
#    Copyright 2010, 2011 James Percent
#
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

from pprint import pformat
import os
import re
import logging
import manifest
import tempfile
import shutil
import subprocess

# ------------------------------------------------------------------------------

logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

class Dependencies:
    def __init__(self, jars, src, target):
        self.jars = jars
        self.src = src
        self.exports = {}
        self.bundles = {}
        self.required_jars = {}
        self.target_platform = target

    def __add_package__(self, packages, package, bundle):
        #package.name -> [(package, bundle), (package, bundle)]
        if package.name in packages:
            inserted = False
            for pentry, bentry in packages[package.name]:
                index = packages[package.name].index((pentry, bentry))
                assert index >= 0 and index <= len(packages[package.name])
                if package.b_version.is_equal(pentry.b_version):
                    if bundle.is_binary_bundle:
                        packages[package.name].insert(index, (package, bundle))
                    else:
                        packages[package.name].insert(index + 1, (package, bundle))
                    inserted = True
                    break
                elif package.b_version.is_less(pentry.b_version):
                    packages[package.name].insert(index, (package, bundle))
                    inserted = True
                    break
            if inserted == False:
                packages[package.name].append((package, bundle))
        else:
            packages[package.name] = [(package, bundle)]

    def __partially_order__(self, bundle):
        ret = False
        for dep_bundle in bundle.deps:

            if dep_bundle == bundle:
                # Ignore self-dependency
                continue

            for dep_dep_bundle in dep_bundle.deps:
                if dep_dep_bundle == bundle:
                    logger.error('circular dependencies are not supported.')
                    assert False
                logger.debug('bundle ' + str(bundle.sym_name) + '=' + \
                    str(bundle.build_level) + ', dep bundle ' + \
                    str(dep_bundle.sym_name) + '=' + \
                    str(dep_bundle.build_level) + ', dep dep bundle ' + \
                    str(dep_dep_bundle.sym_name))

            if dep_bundle.build_level >= bundle.build_level and not dep_bundle.is_binary_bundle:
                logger.debug('matched: ' + str(bundle.sym_name) + ' deps on ' + \
                    str(dep_bundle.sym_name))
                bundle.build_level = dep_bundle.build_level + 1
                ret = True
        return ret

    def sort(self):
        #for bundle in src.bundles:
        #    logger.debug(bundle.sym_name+','+str(bundle.build_level))
        h4x0r = True
        while h4x0r:
            h4x0r = False
            for bundle in self.src.bundles:
                if self.__partially_order__(bundle):
                    h4x0r = True

        self.src.bundles = sorted(self.src.bundles, key=lambda bundle : bundle.build_level)
        #for bundle in src.bundles:
        #    logger.debug(bundle.sym_name+','+str(bundle.build_level))
        return True

    def populate_source_bundles(self):
        for bundle in self.src.bundles:
            logger.debug(bundle.sym_name)
            assert not bundle.sym_name in self.bundles
            self.bundles[bundle.sym_name] = bundle

            for package in bundle.epackages:
                self.__add_package__(self.exports, package, bundle)

    def populate_binary_bundles(self):
        for bundle in self.jars.bundles:
            logger.debug('--->' + str(bundle.sym_name) + '<---' + str(bundle))
            if not bundle.sym_name in self.bundles:
                self.bundles[bundle.sym_name] = bundle
            else:
                logger.info('Bundle ' + str(bundle.sym_name) + \
                    ' found both binary and src;' + \
                    ' using the src version (this should be an option)')

                assert os.path.join(bundle.root, bundle.file) in self.target_platform
                del self.target_platform[os.path.join(bundle.root, bundle.file)]

            # logger.debug(bundle.display())
            for package in bundle.epackages:
                self.__add_package__(self.exports, package, bundle)

    def resolve_required_bundles(self, bundle):

        for required_bundle_info in bundle.rbundles:
            found = False
            if required_bundle_info.name in self.bundles and \
                required_bundle_info.is_in_range(\
                    self.bundles[required_bundle_info.name].version):
                found = True
                if bundle.fragment and\
                    bundle.sym_name == 'com.ambient.labtrack.test':
                    logger.info('adding dep ' + str(required_bundle_info.name) + \
                       '-' + str(self.bundles[required_bundle_info.name].version) + \
                       ' to ' + str(bundle.sym_name))

                logger.info('Adding the dep bundle = ' + \
                            str(required_bundle_info.name) + \
                        str(self.bundles[required_bundle_info.name]))
                bundle.add_dep(self.bundles[required_bundle_info.name])
                if self.bundles[required_bundle_info.name].is_binary_bundle:
                    self.required_jars[self.bundles[required_bundle_info.name].sym_name] = \
                    self.bundles[required_bundle_info.name]

            if not found:
                logger.error('could not find matching required bundle ' + \
                    str(required_bundle_info.name) + str(required_bundle_info))
                return False
        return True

    def resolve_packages(self, bundle):
        for package in bundle.ipackages:
            found = False
            version_found = []
            if package.name in self.exports:
                for ex_package, ex_bundle in self.exports[package.name]:
                    if package.is_in_range(ex_package.b_version):
                        found = True
                        #print 'adding dep '+ex_bundle.sym_name+' to '+\
                        #bundle.sym_name, 'because of package ', package.name
                        bundle.add_dep(ex_bundle)
                        if ex_bundle.is_binary_bundle:
                            self.required_jars[ex_bundle.sym_name] = ex_bundle
                    else:
                        version_found.append(ex_package)

                if not found:
                    found_str = ''
                    for i in version_found:
                        found_str += i.__str__() + ', '

                    logger.critical('cannot find the correct version of ' + \
                          package.name + ' for ' + bundle.sym_name + \
                          '; requires ' + package.__str__() + ' found = ' + found_str)
                    return False
            else:
                logger.debug(str(re.match(r'javax.xml.namespace', str(self.exports))))
                logger.debug('cannot resolve package ' + str(package.name) + \
                            ' for bundle ' + bundle.sym_name + '; skipping it')
        return True

    def resolve(self):
        self.populate_source_bundles()
        self.populate_binary_bundles()

        for bundle in self.src.bundles:
            if not self.resolve_required_bundles(bundle):
                return False

            if not self.resolve_packages(bundle):
                return False

        logger.debug(self.required_jars)
        return True

# ------------------------------------------------------------------------------

class BinaryBundleFinder:
    """
    Finds binary bundles (JAR or folders)
    """
    def __init__(self):
        """
        Sets up the finder
        """
        self.bundles = []
        self.unique_bundles = {}

        # JAR bundles
        self.jar_files = []

        # Uncompressed bundles (with a META-INF directory)
        self.target_platform = {}


    def load(self):
        """
        Parses found bundles
        """
        # Copy all found JAR files in a temporary directory
        tmp = tempfile.mkdtemp()
        for root, filename in self.jar_files:
            shutil.copy(os.path.join(root, filename), tmp)
        cdir = os.getcwd()

        # Target platform (uncompressed bundles)
        for root, directory, __ in self.target_platform.values():

            # Compute the path to the manifest
            manifest_path = os.path.join(root, directory, 'META-INF',
                                         'MANIFEST.MF')

            logger.debug(' looking up directory binary bundle: %s',
                         manifest_path)

            with open(manifest_path, 'r') as manifest_des:
                manifest_file = manifest_des.read()

            # Parse the Manifest
            parser = manifest.ManifestParser()
            bundle = parser.parse(manifest_file)

            if bundle is None:
                # Error parsing the bundle
                logger.warning('Error parsing Manifest: %s', manifest_path)
                logger.debug(manifest_file)
                continue

            # Add data...
            bundle.root = root
            bundle.file = directory
            bundle.binary_bundle_dir = True

            logger.debug("Loaded bundle %s (%s)", bundle.sym_name, bundle.root)

            # Store bundle
            assert not bundle.sym_name in self.unique_bundles
            self.unique_bundles[bundle.sym_name] = bundle
            self.bundles.append(bundle)

        # JAR files
        for root, filename in self.jar_files:
            # Work in the temporary directory
            os.chdir(tmp)

            # Extract the manifest
            manifest_relpath = os.path.join('META-INF', 'MANIFEST.MF')
            ret = subprocess.call(['jar', 'xf', os.path.join(tmp, filename),
                                   manifest_relpath])
            assert ret == 0

            # Get back to working directory
            os.chdir(cdir)

            # Read the manifest
            with open(os.path.join(tmp, manifest_relpath), 'r') as manifest_des:
                manifest_file = manifest_des.read()

            # Delete the META-INF directory
            shutil.rmtree(os.path.join(tmp, 'META-INF'))

            # Parse the manifest
            parser = manifest.ManifestParser()
            bundle = parser.parse(manifest_file)

            if bundle is None:
                # Error parsing the bundle
                logger.warning('Error parsing Manifest: %s', manifest_path)
                logger.debug(manifest_file)
                continue

            bundle.root = root
            bundle.file = filename
            bundle.is_binary_bundle = True
            if not bundle.sym_name:
                # Not an OSGi bundles
                logger.warning('Bundle %s has no symbolic name; skipping it',
                               os.path.join(root, filename))
                continue

            # Store bundle
            assert bundle.sym_name != ''
            assert not bundle.sym_name in self.unique_bundles
            self.unique_bundles[bundle.sym_name] = bundle
            self.bundles.append(bundle)
            self.target_platform[os.path.join(bundle.root, bundle.file)] = \
                    (bundle.root, bundle.file, False)

        # Remove the temporary directory
        shutil.rmtree(tmp)


    def display(self):
        """
        Prints out the found bundles
        """
        for i in self.bundles:
            i.display()
            print '-' * 80


    def find(self, jar_paths):
        """
        Searches for JAR files or folders with a META-INF sub-folder in the
        given paths.
        
        :param jar_paths: A list of paths
        """
        for i in jar_paths:
            logger.debug('Binary path: %s', i)

            for root, dirs, files in os.walk(i, followlinks=True):
                for directory in dirs:
                    if directory == 'META-INF':
                        # Uncompressed binary bundle found
                        (parent_root, parent) = os.path.split(root)
                        assert os.path.isdir(parent_root)

                        # Store the bundle
                        self.target_platform[root] = (parent_root, parent, True)

                for filename in files:
                    if filename.endswith('.jar'):
                        # Found a JAR file
                        self.jar_files.append((root, filename))

# ------------------------------------------------------------------------------

class SourceBundleFinder(object):
    """
    Finds bundle projects to compile
    """
    def __init__(self, ignored_projects=None):
        """
        Prepares the source bundle finder
        
        :param ignored_projects: Projects to ignore
        """
        # Ignored projects names
        self.ignored_projects = ignored_projects or []

        # List of (project root, META-INF path, {lib jar path -> lib jar path})
        self.src_manifests = []

        # List of loaded bundles
        self.bundles = []


    def find_libs(self, path):
        """
        Finds JAR files in the given path
        
        :param path: A path to a folder containing JAR files
        :return: Found JAR files (file path -> file path)
        """
        libs = {}
        for root, __, files in os.walk(path, followlinks=True):
            for filename in files:
                if filename.endswith('.jar'):
                    jar_path = os.path.join(root, filename)
                    libs[jar_path] = jar_path
        return libs


    def find_junit_tests(self, bundle):
        """
        Looks for jUnit tests in the given bundle project
        
        :param bundle: A Bundle object
        """
        for root, __, files in os.walk(bundle.root, followlinks=True):
            for filename in files:
                imports = False
                tests = False
                package = ''

                filepath = os.path.join(root, filename)

                # XXX - This parser sucks my ass, but it is what I had time to do.
                # Sometime, when I have some time I'll rewrite it...
                if filename.endswith('.java'):
                    f = open(filepath, 'r')
                    jfile = f.read()
                    jfile = re.sub(r'\r', '', jfile)
                    jfile_lines = re.split(r'\n', jfile)

                    for line in jfile_lines:
                        if re.search('import.*;', line):
                            if re.search('org.junit', line):
                                imports = True
                            elif re.search('junit.framework', line):
                                imports = True
                        elif re.search('@Test', line):
                            tests = True
                        elif re.search(\
 'class[ \t\n\r]+[a-zA-Z][a-zA-Z0-9]*[ \t\n\r]+extends[ \t\n\r]+TestCase', line):
                            tests = True
                        elif re.search('package', line):
                            package = line.split(' ')[1]
                            package = package.split(';')[0]
                            package = package.strip()

                if imports or tests:
                    file_name = re.sub(r'\.java$', '', filename)
                    bundle.junit_tests.append((root, package, file_name))
                    if not tests:
                        logger.warn('%s has junit imports but no test methods',
                                    filepath)
                    if not imports:
                        logger.warn('%s has tests but no junit imports: '
                                    'this test may not work correctly',
                                    filepath)

    def load(self):
        """
        Loads found bundles information
        """
        for root, metainf_dir, libs in self.src_manifests:
            # Read the Manifest.MF content
            manifest_path = os.path.join(root, metainf_dir, 'MANIFEST.MF')
            logger.debug("Manifest path: %s", manifest_path)

            with open(manifest_path, 'r') as manifest_des:
                manifest_file = manifest_des.read()

            # Parse it
            parser = manifest.ManifestParser()
            bundle = parser.parse(manifest_file)

            # Filter the project by symbolic name
            if bundle.sym_name in self.ignored_projects:
                logger.debug("Ignored project (by Symbolic-Name): %s",
                             bundle.sym_name)
                continue

            # Add extra information
            bundle.root = root
            logger.debug("Loaded bundle %s (%s)", bundle.sym_name, bundle.root)

            if len(libs.keys()) > 0:
                # Embedded JAR libraries
                logger.debug("Bundle %s has extra libraries: %s",
                             bundle.sym_name, pformat(libs.keys()))
                bundle.extra_libs = libs

            # jUnit tests
            self.find_junit_tests(bundle)

            # Store the bundle
            self.bundles.append(bundle)


    def display(self):
        """
        Prints out the found bundles
        """
        for i in self.bundles:
            i.display()
            print '-' * 80


    def find(self, src_paths):
        """
        Finds all bundle projects in the given paths
        
        :param src_paths: A list of source paths
        """
        for i in src_paths:
            for root, dirs, __ in os.walk(i, followlinks=True):
                libs = {}
                manifest = ()

                if 'META-INF' not in dirs:
                    # Project level must contain the META-INF directory
                    continue

                # Test project name, given its path
                project_name = os.path.basename(root)
                if project_name in self.ignored_projects:
                    # Ignored project
                    logger.debug("Filtered project (by path): %s (%s)",
                                 project_name, root)
                    continue

                for directory in dirs:
                    if directory == 'META-INF':
                        manifest = (root, directory)
                    if directory == 'lib':
                        libs = self.find_libs(os.path.join(root, directory))

                manifest += (libs,)
                self.src_manifests.append(manifest)
