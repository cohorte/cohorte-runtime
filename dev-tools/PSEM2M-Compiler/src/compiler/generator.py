#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
PSEM2M Compiler: Ant Generator

Module defining the AntGenerator class, to generate all Ant scripts for the
given source projects and librairies.

:author: Thomas Calmant
"""

import compiler.antutils as ant

import logging
import os

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

class AntGenerator(object):
    """
    Generates an Ant Script file per project and a master Ant script
    """
    def __init__(self, project_name, source_bundles, target_platform,
                 master_build_file_root, build_file="build.xml"):
        """
        Sets up the Ant generator
        
        :param project_name: Name of the compilation project
        :param source_bundles: List of Bundle objects
        :param target_platform: Target platform map:
                                file -> (root, file, extra lib flag)
        :param master_build_file_root: Directory of the master build file
        """
        # Keep parameters
        self.project_name = project_name
        self.src = source_bundles
        self.target_platform = target_platform
        self.master_root = master_build_file_root

        # Ensure we have a valid build file name
        build_file = os.path.basename(build_file or "build.xml")
        if not build_file:
            build_file = "build.xml"

        self.build_file_name = build_file

        # Extensions
        self._extensions = []

        # Generated files
        self._generated_files = []

        # The Master Build File
        self.master_build_xml = None


    def __call_extensions(self, method_name, *args):
        """
        Calls extensions with the given method and arguments
        
        :param method_name: Method to call in extensions
        :param args: Arguments to pass to the method
        """
        # Call extensions
        for ext in self._extensions:
            method = getattr(ext, method_name, None)
            if method is not None:
                try:
                    method(*args)

                except Exception as ex:
                    _logger.exception("Error calling extension: %s", ex)


    def __append_to_master(self, bundle, antfile):
        """
        Appends the given bundle to the Master build file targets
        
        :param bundle: A Bundle object
        :param antfile: The bundle Ant file name
        """
        doc = self.master_build_xml
        root = bundle.root

        for name in ('compile', 'test', 'clean', 'package'):
            target = ant.get_target(doc, name)
            if target is not None:
                # Call Ant sub-script with the same task
                attrs = {'dir': root, 'target': name, 'antfile': antfile}
                target.appendChild(ant.create_element(doc, 'ant', attrs))

        # Call extensions
        self.__call_extensions('add_bundle', doc, bundle)


    def __prepare_master(self):
        """
        Prepares the master Ant script
        
        Does the same as __create_master_build_file_header() 
        """
        # Create the DOM document
        doc = self.master_build_xml = ant.new_ant_document(self.project_name)

        # Add properties
        ant.add_property(doc, 'lib', self.master_root + '/lib')

        # Prepare the 'init' target
        init = ant.add_target(doc, 'init')
        for action in ('delete', 'mkdir'):
            element = doc.createElement(action)
            element.setAttribute('dir', '${lib}')
            init.appendChild(element)

        # Prepare other targets
        ant.add_target(doc, 'compile', 'Main compilation task')
        ant.add_target(doc, 'test', 'Run unit tests')
        ant.add_target(doc, 'clean', 'Clean up')
        ant.add_target(doc, 'package', 'Packages bundles', ['init'])

        # Call extensions
        self.__call_extensions('prepare_master', doc)


    def __finalize_master(self):
        """
        Adds final tasks to the master Ant script
        """
        # The 'package' task  must finally copy all files from the target
        # platform
        doc = self.master_build_xml
        target = ant.get_target(doc, 'package')
        if not target:
            raise Exception("Can't find the 'package' task in the master DOM")

        for root, filename, is_dir in self.target_platform.values():
            srcname = os.path.join(root, filename)

            if is_dir:
                # Copy the directory
                outname = '/'.join('${lib}', filename)
                dirattrs = {'dir':outname}

                # Make the output directory
                target.appendChild(ant.create_element(doc, 'mkdir', dirattrs))

                # Copy into the directory
                copy = ant.create_element(doc, 'copy', dirattrs)
                copy.appendChild(ant.create_element(doc, 'fileset',
                                                    {'dir': srcname}))

                target.appendChild(copy)

            else:
                # Copy the file
                target.appendChild(ant.create_element(doc, 'copy',
                                                      {'file': srcname,
                                                       'todir':'${lib}',
                                                       'overwrite':'true'}))

        # Call extensions
        self.__call_extensions('finalize_master', doc, self._generated_files)


    def __build_classpath(self, bundle):
        """
        Prepares the class path of the given bundle
        
        :param bundle: A Bundle object
        """
        if bundle.classpath is None:
            bundle.classpath = {}

            for lib in bundle.extra_libs.keys():
                bundle.classpath[lib] = lib

            if bundle.binary_bundle_dir:
                for lib in bundle.classpath_jars:
                    qlib = os.path.join(bundle.root, bundle.file, lib)
                    bundle.classpath[qlib] = qlib

            for dep in bundle.deps:
                if dep.classpath is None:
                    self.__build_classpath(dep)

                for clazz1 in dep.classpath.keys():
                    bundle.classpath[clazz1] = clazz1

                clazz = os.path.join(dep.root, dep.file)
                if not dep.is_binary_bundle:
                    clazz = os.path.join(clazz, 'bin')

                if clazz not in bundle.classpath:
                    bundle.classpath[clazz] = clazz

        if len(bundle.junit_tests) > 0:
            clazz = os.path.join(bundle.root, 'bin')
            bundle.classpath[clazz] = clazz


    def __write_properties(self, document, bundle):
        """
        Writes properties of a bundle script file.
        
        :param document: A Ant DOM Document
        :param bundle: A Bundle object
        """
        root = '${basedir}'
        bundle_name = str(bundle.sym_name) + '_' + str(bundle.version) + '.jar'

        ant.add_property(document, 'lib', '/'.join((self.master_root, 'lib')))
        ant.add_property(document, 'src', '/'.join((root, 'src')))
        ant.add_property(document, 'build', '/'.join((root, 'bin')))
        ant.add_property(document, 'manifest', '/'.join((root, 'META-INF',
                                                         'MANIFEST.MF')))
        ant.add_property(document, 'metainf', '/'.join((root, '/META-INF')))
        ant.add_property(document, 'bundle', '/'.join((self.master_root,
                                                       'lib', bundle_name)))

        # Call extensions
        self.__call_extensions('write_properties', document, bundle)


    def __write_classpath(self, document, bundle):
        """
        Writes the class path of a bundle script file
        
        :param document: A Ant DOM Document
        :param bundle: A Bundle object
        """
        path = ant.create_element(document, 'path', {'id':'classpath'})
        document.documentElement.appendChild(path)

        for location in bundle.classpath.keys():
            entry = document.createElement('pathElement')
            entry.setAttribute('location', str(location))
            path.appendChild(entry)

        # Call extensions
        self.__call_extensions('write_classpath', document, bundle, path)


    def __write_init_target(self, document):
        """
        Writes the ``init`` target of a bundle script file
        
        :param document: A Ant DOM Document
        """
        target = ant.add_target(document, "init", "Build preparation",
                                ['clean'])
        target.appendChild(document.createElement('tstamp'))
        target.appendChild(ant.create_element(document, 'mkdir',
                                              {'dir': '${build}'}))
        document.documentElement.appendChild(target)


    def __write_clean_target(self, document):
        """
        Writes the ``clean`` target of a bundle script file
        
        :param document: A Ant DOM Document
        """
        target = ant.add_target(document, 'clean', 'Clean up')
        target.appendChild(ant.create_element(document, 'delete',
                                              {'dir':'${build}'}))
        document.documentElement.appendChild(target)


    def __write_compile_target(self, document):
        """
        Writes the ``compile`` target of a bundle script file
        
        :param document: A Ant DOM Document
        """
        target = ant.add_target(document, "compile", "Project build", ['init'])
        target.appendChild(ant.create_element(document, 'javac',
                                              {'srcdir': '${src}',
                                               'destdir':'${build}',
                                               'classpathRef': 'classpath'}))
        document.documentElement.appendChild(target)


    def __write_test_target(self, document, bundle):
        """
        Writes the ``test`` target of a bundle script file
        
        :param document: A Ant DOM Document
        :param bundle: A Bundle object
        """
        target = ant.add_target(document, 'test', 'Runs unit tests',
                                ['compile'])

        if len(bundle.junit_tests) > 0:
            junit = ant.create_element(document, 'junit',
                                    {'fork': 'yes', 'haltonfailure': 'yes'})

            for test in bundle.junit_tests:
                junit.appendChild(ant.create_element(document, 'test',
                            {'name': "'{0}'.'{1}'".format(test[0], test[1])}))

            junit.appendChild(ant.create_element(document, 'formatter',
                                                 {'type': 'plain',
                                                  'usefile':'false'}))
            junit.appendChild(ant.create_element(document, 'classpath',
                                                 {'refid':'classpath'}))
            target.appendChild(junit)

        # Keep the target even if it is empty, to avoid errors in the master
        # script
        document.documentElement.appendChild(target)


    def __write_jar_target(self, document, bundle):
        """
        Writes the ``package`` target of a bundle script file
        
        :param document: A Ant DOM Document
        :param bundle: A Bundle object
        """
        target = ant.add_target(document, 'package', 'Make the JAR',
                                ['compile'])

        # extra_libs are jar files that are not OSGi bundles, which must be
        # compiled.
        # However, all jar files should be converted to OSGi bundles.
        if bundle.extra_libs:
            for lib in bundle.extra_libs:
                rel_file_path = os.path.relpath(lib, bundle.root)
                split_path = os.path.split(rel_file_path)
                if len(split_path) == 1:
                    parent_path = ""
                else:
                    parent_path = split_path[0]

                target.appendChild(ant.create_element(document, 'copy',
                            {'todir': '/'.join(('${build}', parent_path)),
                             'file': rel_file_path}))

        jar_task = ant.create_element(document, 'jar',
                                      {'destfile': '${bundle}',
                                       'basedir': '${build}',
                                       'manifest': '${manifest}'})

        jar_task.appendChild(ant.create_element(document, 'metainf',
                                                {'dir':'${metainf}'}))

        target.appendChild(jar_task)


    def add_extension(self, extension):
        """
        Adds an extensions to the build process
        
        :param extension: An extension
        """
        if extension is not None and extension not in self._extensions:
            self._extensions.append(extension)


    def generate_build_files(self):
        """
        Generates Ant scripts
        
        :return: The path to the master Ant script
        """
        # Prepare the Master build file
        _logger.info('Preparing master script...')
        self.__prepare_master()

        for bundle in self.src:
            _logger.info('Working on bundle: %s...', bundle.sym_name)

            assert bundle.root != ''

            # Compute the script file name
            filename = os.path.join(bundle.root, self.build_file_name)

            # Append the bundle to the master script tasks
            self.__append_to_master(bundle, filename)

            # Prepare the bundle class path
            self.__build_classpath(bundle)

            # Create the document
            doc = ant.new_ant_document(bundle.sym_name, basedir=bundle.root)
            self.__write_properties(doc, bundle)
            self.__write_classpath(doc, bundle)
            self.__write_init_target(doc)
            self.__write_clean_target(doc)
            self.__write_compile_target(doc)
            self.__write_test_target(doc, bundle)
            self.__write_jar_target(doc, bundle)

            # Call extensions
            self.__call_extensions('finalize_bundle', doc, bundle)

            # Write the XML file
            _logger.debug('Writing script: %s...', filename)
            ant.write_xml(doc, filename)
            self._generated_files.append(filename)

        # Add last elements
        _logger.info('Finalizing master script...')
        self.__finalize_master()

        # write the master XML file
        filename = os.path.join(self.master_root, self.build_file_name)
        ant.write_xml(self.master_build_xml, filename)
        self._generated_files.append(filename)

        _logger.info('All scripts generated.')
        return filename


    def clean(self):
        """
        Removes all generated Ant scripts
        """
        for path in self._generated_files:
            if os.path.isfile(path):
                # Remove all found files
                os.remove(path)

        # Clean up the list
        del self._generated_files[:]


    def post_build(self):
        """
        Do post-build operations
        """
        master_file = os.path.join(self.master_root, self.build_file_name)

        # Call extensions
        self.__call_extensions('post_build', master_file,
                               self._generated_files[:])
