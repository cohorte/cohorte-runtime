#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Provides the component that will parse compositions and components JSON
descriptions.

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

# Cohorte Composer
import cohorte.composer
import cohorte.composer.beans as beans

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_PARSER)
@Requires('_reader', cohorte.SERVICE_FILE_READER)
@Instantiate('cohorte-composer-parser')
class CompositionParser(object):
    """
    Reads the JSON configuration file of the COHORTE Composer.
    """
    def __init__(self):
        """
        Sets up members
        """
        # Configuration file reader
        self._reader = None

    def load(self, filename, base=None):
        """
        Finds and loads the first file found in the platform directories
        matching the given name.

        :param filename: The name of a configuration file
        :param base: Base directory or file where to look for the file
        :return: The parsed Composition bean, or None
        :raise IOError: Error reading the file
        :raise ValueError: Error parsing the composition
        """
        # Read the composition file
        data = self._reader.load_file(filename, base)

        # Parse the content
        return self.parse_composition(filename, data)


    def parse_composition(self, filename, data):
        """
        Parses the given composition file

        :param filename: Source file name (informative)
        :param data: Source file content
        :return: The raw composition bean
        :raise KeyError: Mandatory value missing
        :raise ValueError: Error creating the beans
        """
        # Prepare the composition bean
        composition = beans.RawComposition(data['name'], filename)

        # Parse the root composite
        root = self.parse_composite(None, data['root'])
        if root is None:
            # Not a valid root composite
            return None

        composition.root = root
        return composition


    def parse_composite(self, parent, composite_dict):
        """
        Parses a composite description

        :param parent: A parent RawComposite bean, or None
        :param composite_dict: A representation of the composite
        :return: The raw composite bean, or None if it is empty
        :raise KeyError: Mandatory value missing
        :raise ValueError: Error creating the beans
        """
        # Prepare the composite bean
        composite = beans.RawComposite(composite_dict['name'], parent)

        # Parse the components
        for component_dict in composite_dict.get('components', []):
            component = self.parse_component(component_dict)
            if component is not None:
                composite.components[component.name] = component

        # Parse the composites
        for child_dict in composite_dict.get('composites', []):
            child = self.parse_composite(composite, child_dict)
            if child is not None:
                composite.composites[child.name] = child

        # Return the composite only if it contains something
        if composite.components or composite.composites:
            return composite


    def parse_component(self, component_dict):
        """
        Parses a component description

        :param component_dict: A representation of the component
        :return: The raw component bean
        :raise KeyError: Mandatory value missing
        :raise ValueError: Error creating the beans
        """
        # Set up the component bean
        component = beans.RawComponent(component_dict['factory'],
                                       component_dict['name'])

        # Copy dictionaries
        for entry in beans.RawComponent.PARSER_UPDATE:
            try:
                setattr(component, entry, component_dict.get(entry, {}))
            except AttributeError:
                raise ValueError("Invalid component entry: {0}".format(entry))

        # Other parameters
        for entry in beans.RawComponent.PARSER_COPY:
            value = component_dict.get(entry)
            if value:
                # Copy the value only if it is valid, normalizing field names
                setattr(component, entry.replace('.', '_'), value)

        return component
