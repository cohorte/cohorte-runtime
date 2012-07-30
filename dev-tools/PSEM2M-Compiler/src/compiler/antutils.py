#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Utility methods to work with Ant DOM elements

:author: Thomas Calmant
"""

import xml.dom.minidom

# ------------------------------------------------------------------------------

def new_ant_document(project_name, default='compile', basedir='.'):
        """
        Creates a new Ant DOM document
        
        :param project_name: Name of the Ant build project
        :param default: Default Ant task
        :param basedir: Ant base directory
        :return: The Ant DOM Document
        """
        dom_impl = xml.dom.minidom.getDOMImplementation()
        document = dom_impl.createDocument(None, 'project', None)

        root = document.documentElement
        root.setAttribute('name', project_name)
        root.setAttribute('default', default)
        root.setAttribute('basedir', basedir)

        return document

# ------------------------------------------------------------------------------

def create_element(document, name, attributes={}):
    """
    Creates an element with the given attributes
    
    :param document: A DOM Document
    :param name: Element tag name
    :param attributes: Dictionary of element attributes
    """
    element = document.createElement(name)
    if attributes:
        for name, value in attributes.items():
            element.setAttribute(name, value)

    return element


def add_property(document, name, value, parent=None):
    """
    Adds a property Element to the given document
    
    :param document: A DOM Document
    :param name: Property name
    :param value: Property value
    :param parent: Parent element to use if not the document root element
    :return: The property DOM Element
    """
    prop = create_element(document, 'property', {'name':name, 'value':value})
    parent = parent or document.documentElement
    parent.appendChild(prop)

    return prop


def add_target(document, name, description=None, dependencies=[], parent=None):
    """
    Adds a target Element to the given document
    
    :param document: A DOM Document
    :param name: Target name
    :param dependencies: List of target dependencies
    :param description: A small description of the target
    :param parent: Parent element to use if not the document root element
    :return: The target DOM Element
    """
    target = create_element(document, 'target', {'name':name})
    if dependencies:
        # Add dependencies, if needed
        target.setAttribute('depends', ','.join(dependencies))

    if description:
        target.setAttribute('description', description)

    parent = parent or document.documentElement
    parent.appendChild(target)

    return target

# ------------------------------------------------------------------------------

def get_property(document, name):
    """
    Retrieves the first target Element with the given name
    """
    props = document.getElementsByTagName('property')
    for prop in props:
        if prop.getAttribute('name') == name:
            return prop


def get_target(document, name):
    """
    Retrieves the first target Element with the given name
    
    :param document: A DOM Document
    :param name: Target name
    :return: The first target Element found, or None
    """
    targets = document.getElementsByTagName('target')
    for target in targets:
        if target.getAttribute('name') == name:
            return target

    return None

# ------------------------------------------------------------------------------

def write_xml(document, filename):
    """
    Writes the XML document to the given file. If file is empty, writes to
    the standard output.
    
    :param document: A DOM document
    :param filename: A file name
    """
    data = document.toprettyxml()

    if not filename:
        print(data)

    else:
        with open(filename, 'w') as fp:
            fp.write(data)
