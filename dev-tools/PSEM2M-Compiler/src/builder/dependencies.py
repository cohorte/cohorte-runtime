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

import os
import re
import logging
import manifest
import tempfile
import shutil
import subprocess

from os.path import join

logging.basicConfig()
logger = logging.getLogger(__name__)
def set_logger_level(logLevel):
    logger.setLevel(logLevel)

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

                assert join(bundle.root, bundle.file) in self.target_platform
                del self.target_platform[join(bundle.root, bundle.file)]

            logger.debug(bundle.display())
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


class BinaryBundleFinder:
    def __init__(self):
        self.jar_files = []
        self.bundles = []
        self.unique_bundles = {}
        self.target_platform = {}

    def load(self):
        tmp = tempfile.mkdtemp()
        for root, file in self.jar_files:
            shutil.copy(join(root, file), tmp)
        cdir = os.getcwd()

        for root, dir, extra_lib_flag in self.target_platform.values():
            # print root, dir
            logger.debug(' looking up directory binary bundle: ' + \
                         join(root, dir, 'META-INF', 'MANIFEST.MF'))
            manifest_des = open(join(root, dir, 'META-INF', 'MANIFEST.MF'), 'r')
            manifest_file = manifest_des.read()
            logger.debug(manifest_file)
            parser = manifest.ManifestParser()
            bundle = parser.parse(manifest_file)
            if bundle == None:
                continue
            bundle.root = root
            bundle.file = dir
            logger.debug(bundle, bundle.sym_name)
            bundle.binary_bundle_dir = True
            assert not bundle.sym_name in self.unique_bundles
            self.unique_bundles[bundle.sym_name] = bundle
            self.bundles.append(bundle)

        for root, file in self.jar_files:
            os.chdir(tmp)
            ret = subprocess.call(['jar', 'xf', join(tmp, file),
                                   join('META-INF', 'MANIFEST.MF')])
            assert ret == 0
            os.chdir(cdir)
            manifest_des = open(join(tmp, 'META-INF', 'MANIFEST.MF'), 'r')
            manifest_file = manifest_des.read()
            shutil.rmtree(join(tmp, 'META-INF'))
            logger.debug(manifest_file)
            parser = manifest.ManifestParser()
#            print 'parsing ', root, file
            bundle = parser.parse(manifest_file)
            if bundle == None:
                continue
            bundle.root = root
            bundle.file = file
            bundle.is_binary_bundle = True
            if bundle.sym_name == '':
                logger.info('Bundle ' + join(root, file) + ' has no symbolic name;'\
                            ' skipping it')
                continue

            assert bundle.sym_name != ''
            assert not bundle.sym_name in self.unique_bundles
            self.unique_bundles[bundle.sym_name] = bundle
            self.bundles.append(bundle)
            #if not(bundle.file in do_not_package_libs):
            self.target_platform[join(bundle.root, bundle.file)] = \
                    (bundle.root, bundle.file, False)
        shutil.rmtree(tmp)

    def display(self):
        for i in self.bundles:
            i.display()
            print '-' * 80

    def find(self, jar_path):
        for i in jar_path:
            logger.debug('jar_path: ' + str(i))
            for root, dirs, files in os.walk(i):
                for dir in dirs:
                    if dir == 'META-INF':
                        assert os.path.isdir(root)
                        (parent_root, parent) = os.path.split(root)
                        assert os.path.isdir(parent_root)
                        self.target_platform[join(parent_root, parent)] = \
                            (parent_root, parent, True)
                for file in files:
                    if file.endswith(r'.jar'):
                        self.jar_files.append((root, file))


class SourceBundleFinder:
    def __init__(self):
        self.src_manifests = []
        self.bundles = []

    def find_libs(self, path):
        libs = {}
        for root, dirs, files in os.walk(path):
            for file in files:
                if file.endswith(r'.jar'):
                    libs[join(root, file)] = join(root, file)
        return libs

    def find_junit_tests(self, bundle):
        depth = 1
        for root, dirs, files in os.walk(bundle.root):
            for file in files:
                imports = False
                tests = False
                package = ''

                # XXX - This parser sucks my ass, but it is what I had time to do.
                # Sometime, when I have some time I'll rewrite it...
                if file.endswith(r'.java'):
                    f = open(join(root, file), 'r')
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
                    file_name = re.sub(r'\.java$', '', file)
                    bundle.junit_tests.append((root, package, file_name))
                    if not tests:
                        logger.warn(join(root, file) + \
                                'has junit imports but no test methods')
                    if not imports:
                        logger.warn(join(root, file) + \
                                'has tests but no junit imports; this test ' + \
                                'may not work correctly')

    def load(self):
        for root, dir, libs in self.src_manifests:
            logger.debug(join(root, dir, 'MANIFEST.MF'))
            manifest_des = open(join(root, dir, 'MANIFEST.MF'), 'r')
            manifest_file = manifest_des.read()
            logger.debug(manifest_file)
            parser = manifest.ManifestParser()
            bundle = parser.parse(manifest_file)
            bundle.root = root
            logger.debug(bundle, bundle.sym_name)
            if libs.keys().__len__() > 0:
                #print libs
                bundle.extra_libs = libs
                #assert False

            self.find_junit_tests(bundle)
            self.bundles.append(bundle)

    def display(self):
        for i in self.bundles:
            i.display()
            print '-' * 80

    def find(self, src_path):
        for i in src_path:
            for root, dirs, files in os.walk(i):
                libs = {}
                manifest = ()
                manifest_found = False

                if 'META-INF' in dirs:
                    manifest_found = True
                else:
                    continue

                for dir in dirs:
                    if dir == 'META-INF':
                        manifest = (root, dir)
                    if dir == 'lib':
                        libs = self.find_libs(join(root, dir))

                manifest += (libs,)
                self.src_manifests.append(manifest)
