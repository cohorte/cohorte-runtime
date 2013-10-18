#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Beans to store the description of compositions and components

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 3.0.0

..

    This file is part of Cohorte.

    Cohorte is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cohorte is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cohorte. If not, see <http://www.gnu.org/licenses/>.
"""

# Module version
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

import itertools

# ------------------------------------------------------------------------------

class RawComposition(object):
    """
    Represents a composition, as described in the configuration file
    """
    def __init__(self, name, filename=None):
        """
        Sets up members

        :param name: Name of the composition
        :param filename: Name of the composition root file (to be shown in logs)
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
            raise ValueError("{0}: A composite name can't contain a '.' (dot)" \
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
    # Dictionary fields (calls update())
    PARSER_UPDATE = ('properties', 'filters', 'wires')

    # String fields (simple assignment)
    PARSER_COPY = ('bundle.name', 'bundle.version',
                   'language', 'isolate', 'node')

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

# ------------------------------------------------------------------------------

class Isolate(object):
    """
    Represents an isolate to be instantiated
    """
    # The isolate counter
    __counter = itertools.count(1)

    def __init__(self, name=None, language=None, components=None):
        """
        Sets up members

        :param name: The name of the isolate
        :param language: The language of components in this isolate
        :param components: A set of pre-existing components
        """
        # The configured isolate name
        self.__name = name

        # Proposed name, if the current vote passes
        self.__proposed_name = None

        # Language of components hosted by this isolate
        self.language = language

        # Components hosted by this isolate
        if components is None:
            self.__components = set()
        else:
            self.__components = set(components)


    def __str__(self):
        """
        String representation
        """
        if not self.language:
            return "NeutralIsolate"

        return "Isolate({0}, {1})".format(self.__name, self.language)


    def accepted_rename(self):
        """
        Possible name accepted

        :raise ValueError: A name was already given
        """
        if self.__name:
            raise ValueError("Isolate already have a name: {0}" \
                             .format(self.__name))

        self.__name = self.__proposed_name
        self.__proposed_name = None


    def propose_rename(self, new_name):
        """
        Proposes the renaming of the isolate

        :raise ValueError: A name was already given to this isolate
        :return: True if the proposal is acceptable
        """
        if self.__name:
            raise ValueError("Isolate already have a name: {0}" \
                             .format(self.__name))

        if self.__proposed_name:
            return False

        self.__proposed_name = new_name
        return True


    def rejected_rename(self):
        """
        Possible name rejected
        """
        self.__proposed_name = None


    def generate_name(self, node):
        """
        Generates a name for this isolate (to be called) after votes.
        Does nothing if a name was already assigned to the isolate

        :param node: The node name
        :return: The (generated) isolate name
        """
        if not self.__name:
            # Need to generate a name
            self.__name = '{node}-{language}-auto{count:02d}' \
                          .format(node=node, language=self.language,
                                  count=next(Isolate.__counter))

        return self.__name


    @property
    def name(self):
        """
        Returns the name of the isolate
        """
        return self.__name


    @property
    def proposed_name(self):
        """
        Returns the currently proposed name, or None
        """
        return self.__proposed_name


    @property
    def components(self):
        """
        Returns the (frozen) set of components associated to this isolate
        """
        return frozenset(self.__components)


    @property
    def factories(self):
        """
        Returns the (frozen) set of the factories required to instantiate
        the components associated to this isolate
        """
        return frozenset(component.factory for component in self.__components)


    def add_component(self, component):
        """
        Adds a component to the isolate
        """
        if self.language is None:
            # First component tells which language this isolate hosts
            self.language = component.language

        self.__components.add(component)
