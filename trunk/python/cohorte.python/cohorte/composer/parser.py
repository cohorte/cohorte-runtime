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
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides

# Standard library
import logging
import time
import uuid

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory('cohorte-composer-parser-factory')
@Provides(cohorte.composer.SERVICE_COMPOSITION_PARSER)
@Requires('_reader', cohorte.SERVICE_CONFIGURATION_READER)
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


    def load(self, filename):
        """
        Finds and loads the first file found in the platform directories
        matching the given name.
        
        :param filename: The name of a configuration file
        :return: The parsed Composition bean, or None
        """
        try:
            # Read the composition file
            _logger.info('Loading composition file: "%s"', filename)
            data = self._reader.load_file(filename)

        except IOError as ex:
            # Access error
            _logger.exception("Error reading a composition file: %s", ex)

        except ValueError as ex:
            # JSON error
            _logger.exception("Error parsing a composition file: %s", ex)

        else:
            try:
                # Parse the content
                return self._parse_composition(filename, data)

            except ValueError as ex:
                # Content error
                _logger.exception("Error in the composition file: %s", ex)


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
        components_array = composite_dict.get('components')
        for component_dict in components_array:
            component = self._parse_component(composite, component_dict)
            if component is not None:
                composite.add_component(component)
                empty_composite = False

        # Parse the composites
        composites_array = composite_dict.get('composites')
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

        return component

