#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
PSEM2M Compiler: iPOJO Extension

Adds the iPOJO manipulation task for Eclipse projects with iPOJO Nature

:author: Thomas Calmant
"""

import compiler.antutils as ant
import compiler.eclipseutils as eclipse

import logging
import os

# ------------------------------------------------------------------------------

EXTENSION_CLASS = 'IPOJOExt'
""" PSEM2M Compiler extension class """

IPOJO_ANT_TASK = "org.apache.felix.ipojo.task.IPojoTask"
""" iPOJO Ant task class """

IPOJO_NATURE = "org.ow2.chameleon.eclipse.ipojo.iPojoNature"
""" iPOJO project nature in Eclipse """

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

class IPOJOExt(object):
    """
    iPOJO extension for PSEM2M Compiler
    """
    def __init__(self, parameters):
        """
        Sets up the extension.
        
        :param parameters: The PSEM2M Compiler configuration
        """
        self.ant_task_jar = parameters.get('ipojo', 'ant_task_jar')
        self.ipojo_annotations_jar = parameters.get('ipojo', 'annotations_jar')


    def _append_ipojo_target(self, document, project):
        """
        Appends the iPOJO Ant target to the given DOM document.
        
        :param document: Ant document
        :param project: The Eclipse project
        """
        # Modify the 'package' target
        target = ant.get_target(document, 'package')
        if not target:
            self.logger.warning("%s: 'package' target not found", project.name)
            return

        # Find the output JAR file
        bundle_prop = ant.get_property(document, 'bundle')
        if bundle_prop:
            outfile = bundle_prop.getAttribute('value')

        else:
            _logger.warning("%s: Can't get the project bundle file",
                            project.name)
            return

        # Add the definition of the task
        task_name = 'ipojo'
        task_def_attrs = {'name': task_name,
                          'classname': IPOJO_ANT_TASK,
                          'classpath': self.ant_task_jar}

        target.appendChild(ant.create_element(document, 'taskdef',
                                              task_def_attrs))

        # Add the task
        task = ant.create_element(document, task_name, {'input': outfile})

        # If found, use the meta data file
        metadata_file = os.path.join(project.path, "metadata.xml")
        if os.path.exists(metadata_file):
            task.setAttribute("metadata", metadata_file)

        target.appendChild(task)


    def finalize_bundle(self, document, bundle):
        """
        Called once the whole Ant document has been generated, for late
        operations.
        
        :param document: A Ant DOM Document for the given bundle project
        :param bundle: A Bundle object
        """
        project = eclipse.get_project(bundle.root)
        if project is None:
            # Nothing to do
            return

        if IPOJO_NATURE in project.natures:
            # Add the iPOJO task
            self._append_ipojo_target(document, project)


    def write_classpath(self, document, bundle, path_element):
        """
        Called after the basic class path has been written by the Ant generator
        
        :param document: The Ant DOM document
        :param bundle: A Bundle object
        :param path_element: The class path element in the document
        """
        # Find the project
        project = eclipse.get_project(bundle.root)
        if project is None:
            # Nothing to do
            return

        if IPOJO_NATURE in project.natures:
            # Add the iPOJO annotations JAR to the class path
            path_element.appendChild(
                            ant.create_element(document, 'pathElement',
                                    {'location': self.ipojo_annotations_jar}))
