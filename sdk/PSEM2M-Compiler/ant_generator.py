#-- Content-Encoding: utf-8 --
'''
Created on 8 déc. 2011

@author: Thomas Calmant
'''

from auto_builder import BinaryBundleFinder, SourceBundleFinder, Dependencies
from eclipse_reader import EclipseProject
from generator import AntGenerator, FileWriter
import logging
import os.path
import xml.dom.minidom as dom
from xml.dom.minidom import Element, Document

# ------------------------------------------------------------------------------

logger = logging.getLogger("psem2m.compiler.ant_generator")

# ------------------------------------------------------------------------------

def append_classpath_entries(ant_document, libraries):
    """
    Appends the given libraries to the class path entries
    
    @param ant_document: Ant document
    @param libraries: Libraries to add in the class path
    """
    if not libraries:
        # Do nothing...
        return

    classpath_node = None
    ant_root = ant_document.documentElement

    path_nodes = ant_document.getElementsByTagName("path")
    if path_nodes != None:
        for node in path_nodes:
            if node.getAttribute("id") == "classpath":
                classpath_node = node

    if classpath_node == None:
        # Create the node if needed
        classpath_node = ant_document.createElement("path")
        classpath_node.setAttribute("id", "classpath")
        ant_root.appendChild(classpath_node)

    # Create path elements
    for library in libraries:
        lib_node = ant_document.createElement("pathElement")
        lib_node.setAttribute("location", library)
        classpath_node.appendChild(lib_node)


def get_ant_target_node(ant_document, target_name):
    """
    Retrieves the XML node of the Ant target with the given name.
    Returns None if the target wasn't found
    
    @param ant_document: The Ant DOM Document
    @param target_name: Name of the Ant target
    @return: The found Node or None
    """
    ant_targets = ant_document.getElementsByTagName("target")
    if not ant_targets:
        return None

    for target in ant_targets:
        if target.getAttribute("name") == target_name:
            # Found
            return target

    return None


def setup_source_folders(ant_document, classpath):
    """
    Sets up the "src" Ant property to correspond to the project source
    folders
    
    @param ant_document: Ant document
    @param classpath: An Eclipse Java project class path
    """
    assert isinstance(ant_document, Document)

    # Generate the property string
    base_dir = "${basedir}/"

    # Find the "src" property node
    src_property = None
    property_nodes = ant_document.getElementsByTagName("property")
    for node in property_nodes:
        if node.getAttribute("name") == "src":
            # Found !
            src_property = node.getAttribute("value")
            break

    compile_node = get_ant_target_node(ant_document, "compile")
    if compile_node is None:
        logger.debug("No compile target found")
        return

    javac_node = compile_node.getElementsByTagName("javac")
    if javac_node is None or len(javac_node) != 1:
        logger.debug("No an understood javac compilation")
        return

    # Get the node
    javac_node = javac_node[0]

    if not classpath.src:
        # No classpath given, look for the src property
        if not src_property:
            # Nothing given in the source property -> remove the javac task
            compile_node.removeChild(javac_node)

    else:
        # Remove the "srcdir" attribute
        javac_node.removeAttribute("srcdir")

        # Make src child nodes
        for srcdir in classpath.src:
            src_node = ant_document.createElement("src")
            src_node.setAttribute("path", base_dir + srcdir)
            javac_node.appendChild(src_node)

# ------------------------------------------------------------------------------

class EclipseAntGenerator(object):
    """
    The Ant file generator, based on AutoBuilder
    """

    IPOJO_ANT_TASK = "org.apache.felix.ipojo.task.IPojoTask"

    IPOJO_NATURE = "org.ow2.chameleon.eclipse.ipojo.iPojoNature"

    def __init__(self, eclipse_projects, compilation_name="", \
                 ipojo_annotations_file="", ipojo_ant_file=""):
        """
        Constructor
        """
        if not isinstance(eclipse_projects, list):
            raise TypeError("eclipse_projects argument should be a list")

        self.compilation_name = compilation_name
        self.eclipse_projects = eclipse_projects
        self.ipojo_annotations_file = ipojo_annotations_file
        self.ipojo_ant_file = ipojo_ant_file


    def append_ipojo_target(self, eclipse_project, ant_document):
        """
        Appends the iPOJO Ant target to the given DOM element
        
        @param eclipse_project: The Eclipse project
        @param ant_document: Ant document
        """
        # Modify the "compile" target
        package_target = get_ant_target_node(ant_document, "package")
        if package_target is None:
            logger.warn("%s: 'package' target not found.", eclipse_project.name)
            return

        # Add the task definition
        task_name = "ipojo"
        task_def = ant_document.createElement("taskdef")
        task_def.setAttribute("name", task_name)
        task_def.setAttribute("classname", EclipseAntGenerator.IPOJO_ANT_TASK)
        task_def.setAttribute("classpath", self.ipojo_ant_file)
        package_target.appendChild(task_def)

        # Find the JAR file
        ant_properties = ant_document.getElementsByTagName("property")
        if not ant_properties:
            logger.warn("%s: No Ant properties in build.xml", \
                        eclipse_project.name)
            return

        for ant_property in ant_properties:
            if ant_property.getAttribute("name") == "bundle":
                jar_file = ant_property.getAttribute("value")
                break
        else:
            logger.warn("%s: Can't get the project bundle name", \
                        eclipse_project.name)

        # Add the task
        ipojo_task = ant_document.createElement(task_name)
        ipojo_task.setAttribute("input", jar_file)

        # Metadata file found, use it
        metadata_file = eclipse_project.path + os.sep + "metadata.xml"
        if os.path.exists(metadata_file):
            ipojo_task.setAttribute("metadata", metadata_file)

        package_target.appendChild(ipojo_task)


    def append_copy_resources(self, eclipse_project, ant_document):
        """
        Appends the copy of the resources in the compile Ant target of the given
        ant document
        
        @param eclipse_project: The Eclipse project
        @param ant_document: Ant document
        
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
        """
        # Modify the "compile" target
        compile_target = get_ant_target_node(ant_document, "compile")
        if compile_target is None:
            logger.warn("%s: 'compile' target not found.", eclipse_project.name)
            return

        # Add the copy task
        copy_elmt = ant_document.createElement('copy')
        copy_elmt.setAttribute("todir", "${build}")
        compile_target.appendChild(copy_elmt)

        fileset_elmt = ant_document.createElement('fileset')
        fileset_elmt.setAttribute("dir", "${src}")
        copy_elmt.appendChild(fileset_elmt)

        include_elmt = ant_document.createElement('include')
        include_elmt.setAttribute("name", "**/*.properties")
        fileset_elmt.appendChild(include_elmt)

        include_elmt = ant_document.createElement('include')
        include_elmt.setAttribute("name", "**/*.png")
        fileset_elmt.appendChild(include_elmt)

        include_elmt = ant_document.createElement('include')
        include_elmt.setAttribute("name", "**/*.xml")
        fileset_elmt.appendChild(include_elmt)

        logger.info("--> Copy resources:  %s", copy_elmt.toxml())


    def prepare_ant_files(self, root_directory, libraries_paths):
        """
        Generates Ant build.xml files using auto_builder
        
        @param root_directory: Root generation directory
        @param libraries_paths: An array of string defining JAR libraries paths
        """
        # Resolve dependencies
        dependencies = self.__load_dependencies(libraries_paths)
        dependencies.resolve()
        dependencies.sort()

        # Generate Ant files with AutoBuilder
        writer = FileWriter()
        ant_generator = AntGenerator(self.compilation_name, \
                                     dependencies.src.bundles, \
                                     dependencies.target_platform, \
                                     root_directory, writer)
        ant_generator.generate_build_files()

        # Complete their class paths
        for project in self.eclipse_projects:
            self.__extend_build_file(project)


    def __extend_build_file(self, eclipse_project):
        """
        Adds missing class path entries and the iPOJO task in Ant build file, if
        needed
        
        @param eclipse_project: The Eclipse project to be extended
        """
        assert isinstance(eclipse_project, EclipseProject)

        # If the project has the iPOJO nature, add the annotations JAR file
        needs_ipojo_manipulation = False
        libraries = []

        if EclipseAntGenerator.IPOJO_NATURE in eclipse_project.natures:
            # The project has the iPOJO Nature
            needs_ipojo_manipulation = True
            libraries.append(self.ipojo_annotations_file)

        # TODO: read the .classpath file and add missing libraries.

        # Test if a modification is needed...
        # if not needs_ipojo_manipulation and not libraries:
        #    # Nothing to do
        #    return

        # Load the build.xml file
        project_build_xml = eclipse_project.path + os.sep + "build.xml"
        if not os.path.exists(project_build_xml):
            # File not found, do nothing...
            logger.warn("%s: No build.xml file found", eclipse_project.name)
            return

        # Parse the document
        try:
            ant_doc = dom.parse(project_build_xml)

        except:
            logger.warn("%s: Error reading build.xml.", eclipse_project.name, \
                         exc_info=True)
            return

        # Setup the source folders
        setup_source_folders(ant_doc, eclipse_project.classpath)

        # Append class path entries
        append_classpath_entries(ant_doc, libraries)

        if needs_ipojo_manipulation:
            # iPOJO manipulation needed : add a target and modify "package"
            self.append_ipojo_target(eclipse_project, ant_doc)

        # Append copy resources in the task "compile" just after the "javac" element
        self.append_copy_resources(eclipse_project, ant_doc)

        # Write down the new file
        with open(project_build_xml, "w") as build_xml:
            build_xml.write(ant_doc.toxml())

        logger.debug("--> End generate %s", eclipse_project.name)


    def __load_dependencies(self, libraries_paths):
        """
        Finds source and library bundles with AutoBuilder, and prepares a
        Dependencies object
        
        @param libraries_paths: An array of string defining JAR libraries paths
        @return: A dependencies resolver
        """
        # Prepare finders
        src_finder = SourceBundleFinder()
        lib_finder = BinaryBundleFinder()

        # Find JAR libraries first
        lib_finder.find(libraries_paths)
        lib_finder.load()

        # Find source bundles...
        src_finder.find([project.path for project in self.eclipse_projects])
        src_finder.load()

        # Prepare dependencies resolver
        return Dependencies(lib_finder, src_finder, lib_finder.target_platform)
