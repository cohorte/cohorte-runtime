#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Beans to store the description of compositions and components

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 3.0.0

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

# Module version
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------


class RawComposition(object):
    """
    Represents a composition, as described in the configuration file
    """
    def __init__(self, name, filename=None):
        """
        Sets up members

        :param name: Name of the composition
        :param filename: Name of the composition root file
                         (to be shown in logs)
        :raise ValueError: Invalid parameters
        """
        if not name:
            raise ValueError("Composition name can't be empty")

        # Read-only
        self.__name = name

        # Root composite
        self.root = None

        # Source file name
        self.filename = filename

    def __repr__(self):
        """
        String representation of the composition
        """
        return "RawComposition({0}, {1})".format(self.__name, self.filename)

    @property
    def name(self):
        """
        The name of the composition
        """
        return self.__name

    def all_components(self):
        """
        Generator to recursively visit all components of this composition
        """
        if self.root is not None:
            for component in self.root.all_components():
                yield component

# ------------------------------------------------------------------------------


class RawComposite(object):
    """
    Represents a composite, as described in the configuration file
    """
    def __init__(self, name, parent):
        """
        Sets up members

        :param name: Name of composite
        :param parent: Parent of this composite
        :raise ValueError: Invalid parameters
        """
        if not name:
            raise ValueError("Composite name can't be empty")

        elif '.' in name:
            raise ValueError("{0}: A composite name can't contain a '.' (dot)"
                             .format(name))

        # Read-only
        self.__name = name
        self.__parent = parent

        # Content of the composite: Name -> Bean
        self.components = {}
        self.composites = {}

    def __repr__(self):
        """
        String representation of the composite
        """
        return "RawComposite({0}, {1})".format(self.__name, self.__parent)

    @property
    def name(self):
        """
        The name of the composite
        """
        return self.__name

    @property
    def parent(self):
        """
        The parent of this composite
        """
        return self.__parent

    def all_components(self):
        """
        Generator to recursively visit all components of this composition
        """
        for component in self.components.values():
            yield component

        for composite in self.composites.values():
            for component in composite.all_components():
                yield component

# ------------------------------------------------------------------------------


class RawComponent(object):
    """
    Represents a component, as described in the configuration file.
    This bean is meant to be transmitted by remote services.
    """
    # Java class name
    javaClass = 'org.cohorte.composer.api.RawComponent'

    def __init__(self, factory=None, name=None):
        """
        Sets up members

        :param factory: Name of the component factory
        :param name: Name of the component instance
        :raise ValueError: Invalid parameters
        """
        # Initial values
        self.factory = factory
        self.name = name

        # Component properties
        self.properties = {}

        # Implementation information
        self.bundle_name = None
        self.bundle_version = None
        self.language = None

        # Dispatcher information
        self.isolate = None
        self.node = None

        # Filters: Field name -> LDAP Filter
        self.filters = {}

        # Wires: Field name -> Component name
        self.wires = {}

    def __repr__(self):
        """
        String representation of the component
        """
        return "RawComponent({0}, {1})".format(self.factory, self.name)

    def __hash__(self):
        """
        A component is unique on a node by its name
        """
        return hash(self.name)

    def __eq__(self, other):
        """
        A component is unique on a node by its name
        """
        return self.name == other.name

# ------------------------------------------------------------------------------


class Isolate(object):
    """
    Represents an isolate to be instantiated
    """

    # Java bean class name
    javaClass = "org.cohorte.composer.api.Isolate"

    def __init__(self, name=None, language=None, components=None):
        """
        Sets up members

        :param name: The name of the isolate
        :param language: The language of components in this isolate
        :param components: A set of pre-existing components
        """
        # The configured isolate name
        self.name = name

        # Language of components hosted by this isolate
        self.language = language

        # Components hosted by this isolate
        if components is None:
            self.components = set()
        else:
            self.components = set(components)

    def __repr__(self):
        """
        String representation
        """
        return "Isolate({0}, {1}, {2})".format(self.name, self.language,
                                               self.components)

    def __hash__(self):
        """
        An isolate is unique on a node by its name
        """
        return hash(self.name)

    def __eq__(self, other):
        """
        An isolate is unique on a node by its name
        """
        return self.name == other.name
