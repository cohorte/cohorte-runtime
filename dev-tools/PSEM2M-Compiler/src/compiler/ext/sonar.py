#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
PSEM2M Compiler: iPOJO Extension

Adds a Sonar task for the compiled projects.

:author: Thomas Calmant
"""

import compiler.antutils as ant

import os

# ------------------------------------------------------------------------------

EXTENSION_CLASS = 'SonarExt'
""" PSEM2M Compiler extension class """

SONAR_TASK_RESOURCE = 'org/sonar/ant/antlib.xml'
""" XML file in the Ant task Jar to define the task """

SONAR_ANT_TASK = "org.sonar.ant.SonarTask"
""" Sonar Ant task class """

# ------------------------------------------------------------------------------

class SonarExt(object):
    """
    Sonar extension for PSEM2M Compiler
    """
    def __init__(self, parameters):
        """
        Sets up the extension
        
        :param parameters: The PSEM2M Compiler configuration
        """
        self.compilation_name = parameters.get('main', 'name')
        self.ant_task_jar = parameters.get('sonar', 'ant.task.jar')
        self.sonar_host = parameters.get_default('sonar', 'sonar.host.url')


    def finalize_bundle(self, document, bundle):
        """
        Called once the whole Ant document has been generated, for late
        operations.
        
        :param document: A Ant DOM Document for the given bundle project
        :param bundle: A Bundle object
        """
        # Add the properties
        ant.add_property(document, 'sonar.sources', '${src}')
        ant.add_property(document, 'sonar.binaries', '${build}')
        ant.add_property(document, 'sonar.projectName', bundle.sym_name)
        ant.add_property(document, 'sonar.sourceEncoding', 'UTF-8')

        if self.sonar_host:
            # Add the Sonar host, if given
            ant.add_property(document, 'sonar.host.url', self.sonar_host)

        # Shouldn't be there...
        project_key = ':'.join((self.compilation_name, bundle.sym_name))
        ant.add_property(document, 'sonar.projectKey', project_key)
        ant.add_property(document, 'sonar.version', str(bundle.version))

        # Add the class path
        path = ant.create_element(document, 'path', {'id': 'sonar.libraries'})
        path.appendChild(ant.create_element(document, 'path',
                                            {'refId': 'classpath'}))
        document.documentElement.appendChild(path)

        # Add the target...
        sonar = ant.add_target(document, 'sonar', 'Runs Sonar analysis')

        # Add the task definition
        task_name = 'sonar'
        taskdef = ant.create_element(document, 'taskdef',
                                     {'resource': SONAR_TASK_RESOURCE})

        taskdef.appendChild(ant.create_element(document, 'classpath',
                                               {'path': self.ant_task_jar}))
        sonar.appendChild(taskdef)

        # Add the task
        attrs = {'key': project_key + '-key',
                 'version': str(bundle.version)}
        sonar.appendChild(ant.create_element(document, task_name, attrs))


    def finalize_master(self, document, generated_files):
        """
        Called once the whole Ant master file has been generated
        
        :param document: The Master Ant file DOM Document
        :param generated_files: Contains generated build files
        """
        # Add the 'modules' property (seems to add a duplication bug)
        # ant.add_property(document, 'sonar.modules', ','.join(generated_files))

        # Add the task
        target = ant.add_target(document, 'sonar', 'Runs Sonar')

        for script in generated_files:
            root = os.path.dirname(script)
            antfile = os.path.basename(script)

            attrs = {'dir': root, 'target': 'sonar', 'antfile': antfile}
            target.appendChild(ant.create_element(document, 'ant', attrs))
