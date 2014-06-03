#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: Composition file parser

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE
import cohorte.composer
import cohorte.composer.beans as beans

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# Standard library
import logging
import time
import uuid

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory('cohorte-composer-parser-factory')
@Provides(cohorte.composer.SERVICE_COMPOSITION_PARSER)
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
        return self._parse_composition(filename, data)


    def _parse_composition(self, filename, data):
        """
        Parses the given composition file
        
        :param filename: Source file name (informative)
        :param data: Source file content
        :return: The parsed composition
        :raise KeyError: Mandatory value missing
        :raise ValueError: Error creating the beans
        """
        # Prepare the composition bean
        timestamp = time.time()
        uid = str(uuid.uuid4())
        composition = beans.Composition(uid, data['name'], filename, timestamp)

        # Parse the root composite
        root = self._parse_composite(None, data['root'])
        if root is None:
            # Not a valid root composite
            return None

        composition.root = root
        return composition


    def _parse_composite(self, parent, composite_dict):
        """
        Parses a composite description
        
        :param parent: The parent composite
        :param composite_dict: A representation of the composite
        :return: The composite, or None if it is empty
        :raise KeyError: Mandatory value missing
        :raise ValueError: Error creating the beans
        """
        # Prepare the composite bean
        uid = str(uuid.uuid4())
        composite = beans.Composite(uid, composite_dict["name"], parent)

        # Flag indicating if the parsed composite is empty or not
        empty_composite = True

        # Parse the components
        components_array = composite_dict.get('components', [])
        for component_dict in components_array:
            component = self._parse_component(composite, component_dict)
            if component is not None:
                composite.add_component(component)
                empty_composite = False

        # Parse the composites
        composites_array = composite_dict.get('composites', [])
        for child_dict in composites_array:
            child = self._parse_composite(composite, child_dict)
            if child is not None:
                composite.add_composite(child)
                empty_composite = False

        # Return the composite only if it contains something
        if not empty_composite:
            return composite


    def _parse_component(self, parent, component_dict):
        """
        Parses a component description
        
        :param parent: The parent composite
        :param component_dict: A representation of the component
        :return: The parsed component
        :raise KeyError: Mandatory value missing
        :raise ValueError: Error creating the beans
        """
        # Set up the component bean
        component = beans.Component(component_dict['name'],
                                    component_dict['type'],
                                    component_dict.get('properties'))

        # Set filters
        filters = component_dict.get('filters', {})
        for field, ldap_filter in filters.items():
            component.set_filter(field, ldap_filter)

        # Set wires
        wires = component_dict.get('wires', {})
        for field, wire in wires.items():
            component.set_wire(field, wire)

        # Other parameters
        for entry in ('node', 'isolate', 'language'):
            value = component_dict.get(entry)
            if value is not None and hasattr(component, entry):
                setattr(component, entry, value)

        return component
