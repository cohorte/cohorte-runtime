#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
PSEM2M Compiler: iPOJO Extension

Adds Eclipse class path and properties to the Ant script

:author: Thomas Calmant
"""

import compiler.antutils as ant
import compiler.eclipseutils as eclipse

import logging

# ------------------------------------------------------------------------------

EXTENSION_CLASS = 'EclipseExt'
""" PSEM2M Compiler extension class """

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

class EclipseExt(object):
    """
    Eclipse projects extension for PSEM2M Compiler
    """
    def __init__(self, parameters):
        """
        Sets up the extension
        
        :param parameters: The PSEM2M Compiler configuration
        """
        # Default patterns
        self.patterns = ['**/*.properties', '**/*.png', '**/*.xml',
                         '**/*.js']

        # Extend the patterns
        extra = parameters.get_list('eclipse', 'extra_includes')
        if extra:
            for pattern in extra:
                if pattern not in self.patterns:
                    self.patterns.append(pattern)


    def _copy_resources(self, document, target, project):
        """
        Appends the copy of the resources in the compile Ant target of the given
        ant document
        
        <target name="compile" depends="init">
            <javac srcdir="${src}" destdir="${build}" classpathRef="classpath"/>
            <copy todir="${build}">
                <fileset dir="${src}">
                    <include name="**/*.properties"/>
                    <include name="**/*.png"/>
                    <include name="**/*.xml"/>
                </fileset>
            </copy>
        </target>
        
        :param document: The Ant DOM document
        :param target: The Ant target element to add the nodes to
        :param project: The Eclipse project
        """
        # Prepare the "copy" element
        copy_element = ant.create_element(document, 'copy',
                                          {'todir':'${build}'})
        target.appendChild(copy_element)

        # Prepare the "fileset" element
        fileset = ant.create_element(document, 'fileset', {'dir': '${src}'})
        copy_element.appendChild(fileset)

        # Add include rules
        for pattern in self.patterns:
            # Default ones...
            fileset.appendChild(ant.create_element(document, 'include',
                                                   {'name': pattern}))

        # FIXME: Handle the build.properties file


    def _reset_sources(self, document, target, project):
        """
        Sets up the "src" Ant property to correspond to the project source
        folders.
        
        :param document: The Ant DOM document
        :param target: The Ant target element to add the nodes to
        :param project: The Eclipse project
        """
        # Get the 'javac' node
        javac_node = target.getElementsByTagName('javac')
        if javac_node is None or len(javac_node) != 1:
            _logger.debug("%s: No an understandable 'javac' compilation",
                          project.name)
            return

        javac_node = javac_node[0]

        # Find the "src" property
        src_prop = ant.get_property(document, 'src')

        classpath = project.classpath
        if classpath is None or not classpath.src:
            # No class path given, look for the src property
            if not src_prop:
                # Nothing given in the source property -> remove the javac task
                target.removeChild(javac_node)

        else:
            # Remove the "srcdir" attribute
            javac_node.removeAttribute("srcdir")

            # Make src child nodes
            for srcdir in classpath.src:
                path = '/'.join(('${basedir}', srcdir))
                javac_node.appendChild(ant.create_element(document, 'src',
                                                          {'path': path}))


    def finalize_bundle(self, document, bundle):
        """
        Sets up the "src" Ant property to correspond to the project source
        folders.
        
        Called once the whole Ant document has been generated, for late
        operations.
        
        :param document: The Ant DOM document
        :param bundle: A Bundle object
        """
        # Find the project
        project = eclipse.get_project(bundle.root)
        if project is None:
            # Nothing to do
            return

        # Get the "compile" target
        target = ant.get_target(document, 'compile')
        if target is None:
            # Nothing to do...
            _logger.warning("%s: No 'compile' target found.", project.name)
            return

        # Change the source path(s)
        self._reset_sources(document, target, project)

        # Copy resources in the output directory
        self._copy_resources(document, target, project)
