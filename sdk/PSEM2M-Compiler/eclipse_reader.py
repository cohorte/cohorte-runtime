#-- Content-Encoding: utf-8 --
'''
Created on 8 déc. 2011

@author: Thomas Calmant
'''

import logging
import os.path
import shutil
import xml.dom.minidom as dom

class Link(object):
    """
    Represents a link in an Eclipse project
    """

    # Link type : file
    FILE = 1

    # Link type : folder
    FOLDER = 2

    # Virtual resource link
    VIRTUAL = "virtual:/virtual"


    def __init__(self):
        """
        Constructor
        """
        self.name = ""
        self.type = Link.FILE
        self.location = ""
        self.location_uri = ""


    def is_file(self):
        """
        Returns True if this link links to a file
        """
        return self.type == Link.FILE


    def is_folder(self):
        """
        Returns True if this link links to a folder
        """
        return self.type == Link.FOLDER


    def is_virtual(self):
        """
        Returns True if this link represents a virtual resource in the project
        """
        return self.location_uri == Link.VIRTUAL


    def resolve_uri(self, workspace_projects):
        """
        Tries to resolve the link URI
        """
        path_parts = self.location_uri.split("/")
        if "PROJECT_LOC" not in path_parts[0]:
            raise Exception("Unknown location URI format : %s" \
                            % self.location_uri)

        project_name = path_parts[1]
        resource_path = os.sep.join(path_parts[2:])

        for project in workspace_projects:
            if project.name == project_name:
                self.location = project.path + os.sep + resource_path
                return True

        return False


    def __repr__(self):
        """
        String representation
        """
        return self.__str__()


    def __str__(self):
        """
        String representation
        """
        return "Link(name='%s', type=%d, location='%s', uri='%s')" \
            % (self.name, self.type, self.location, self.location_uri)


class EclipseProject(object):
    """
    Represents an Eclipse project
    """

    __slots__ = ("path", "name", "natures", "links", "to_remove", "classpath")

    def __init__(self, path):
        """
        Constructor
        """
        self.path = os.path.dirname(path)

        self.name = "<no-name>"
        self.natures = []
        self.links = []
        self.to_remove = []
        self.classpath = None


    def clean(self, remove_build_xml=False):
        """
        Deletes all created temporary files
        
        @param remove_build_xml: If True, the project build.xml file is also
        removed
        """

        if remove_build_xml:
            build_xml = os.path.normpath(self.path + os.sep + "build.xml")
            if os.path.isfile(build_xml):
                os.remove(build_xml)

        # Remove links
        for path in self.to_remove:

            if os.path.isfile(path) or os.path.islink(path):
                # Remove the file / symbolic link
                os.remove(path)

            elif os.path.isdir(path):
                # Remove a full path
                shutil.rmtree(path, onerror=self.__log_rmtree_error)

        # Clean up the list
        self.to_remove = []


    def resolve_links(self, workspace_projects):
        """
        Tries to resolve links according to all projects in the workspace
        """

        for link in self.links:
            assert isinstance(link, Link)

            if link.is_virtual():
                # Ignore virtual links
                self._make_virtual_folder(link.name)

            elif link.location != None:
                # Direct file link
                self._create_link(link.location, link.name)

            elif link.location_uri != None:
                # Path must be computed
                link.resolve_uri(workspace_projects)
                self._create_link(link.location, link.name)

            else:
                logging.warn("Invalid path : %s / %s", link.location, \
                             link.location_uri)


    def _create_link(self, target, link_name):
        """
        Creates a symbolic link link_name -> target
        
        @param target: Link target
        @param link_name: Name of the symbolic link
        """
        logging.debug("%s: Create link '%s' -> '%s'", self.name, link_name, \
                      target)

        full_link_name = self.path + os.sep + link_name

        if not os.path.exists(full_link_name):
            os.symlink(target, full_link_name)
            self.to_remove.append(full_link_name)

        else:
            logging.warn("%s: Symbolic link already exists '%s'", self.name, \
                         full_link_name)


    def _make_virtual_folder(self, name):
        """
        Creates a folder and set it to be removed later
        
        @param name: Folder name
        """
        logging.debug("%s: Create folder '%s'", self.name, name)

        full_folder_name = self.path + os.sep + name

        if not os.path.exists(full_folder_name):
            os.makedirs(full_folder_name)
            self.to_remove.append(full_folder_name)

        else:
            logging.warn("%s: Path already exists '%s'", self.name, \
                         full_folder_name)


    def __log_rmtree_error(self, function, path, excinfo):
        """
        Logs an error raised when calling shutil.rmtree()
        """
        logging.error("%s: Error deleting '%s'.", self.name, path, excinfo=True)


    def __str__(self):
        """
        String representation
        """
        return "Project(name='%s')" % self.name


class EclipseClasspath(object):
    """
    Eclipse Java project class path file
    """

    __slots__ = ("src", "lib")

    def __init__(self):
        """
        Constructor
        """
        self.src = []
        self.lib = []


def get_first_child_text(element, tag_name):
    """
    Retrieves the text data of the first child of the given element
    """
    assert isinstance(element, dom.Element)

    children = element.getElementsByTagName(tag_name)
    if not children:
        return None

    return get_text(children[0])


def get_text(element):
    """
    Retrieves the text data of the given element
    """
    assert isinstance(element, dom.Element)

    if element.nodeType == element.TEXT_NODE:
        return element.nodeValue

    if element.childNodes:
        text = ""

        for node in element.childNodes:
            if node.nodeType != element.TEXT_NODE:
                return None

            text += node.nodeValue

        return text

    return None


def read_project(project_file):
    """
    Parses the given project file
    """
    root = dom.parse(project_file).documentElement
    if root.nodeName != "projectDescription":
        raise Exception("Invalid project file : %s", project_file)

    project = EclipseProject(project_file)
    # Project name
    project.name = get_first_child_text(root, "name")

    # Natures, if any
    nature_nodes = root.getElementsByTagName("nature")
    for nature_node in nature_nodes:
        assert isinstance(nature_node, dom.Element)
        project.natures.append(get_text(nature_node))

    # Links
    link_nodes = root.getElementsByTagName("link")
    for link_node in link_nodes:
        assert isinstance(link_node, dom.Element)

        link = Link()
        link.name = get_first_child_text(link_node, "name")
        link.type = int(get_first_child_text(link_node, "type"))
        link.location = get_first_child_text(link_node, "location")
        link.location_uri = get_first_child_text(link_node, "locationURI")

        project.links.append(link)

    # Try to read the associated class path
    classpath_file = os.path.join(os.path.dirname(project_file), ".classpath")
    if os.path.exists(classpath_file):
        project.classpath = read_project_classpath(classpath_file)

    return project


def read_project_classpath(classpath_file):
    """
    Parse the given class path file and returns its representation
    """
    # Parse the XML file
    root = dom.parse(classpath_file).documentElement
    if root.nodeName != "classpath":
        raise Exception("Invalid classpath file : %s", classpath_file)

    classpath = EclipseClasspath()

    # Entries
    entry_nodes = root.getElementsByTagName("classpathentry")
    for entry in entry_nodes:
        if entry.getAttribute("kind") == "src":
            path = entry.getAttribute("path")
            if path:
                classpath.src.append(path)

    return classpath
