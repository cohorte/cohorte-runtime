#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
JSON composition description parser

:author: Thomas Calmant
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Instantiate, Provides, \
    Requires

import collections
import json
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

# TODO: put Composite & Component another bundle

class Composite(object):
    """
    Represents a composite
    """
    def __init__(self, parent, name):
        """
        Sets up the composite
        """
        # Name -> component
        self._components = {}
        self._composites = []
        self._name = name
        self._parent = parent


    def add_component(self, component):
        """
        Adds a component to this composite
        """
        if component:
            component.set_parent(self)
            self._components[component.get_name()] = component


    def get_all_components(self):
        """
        Recursively populates the list with defined components
        
        :return: All components under this composite
        """
        components = self._components.values()[:]

        for composite in self._composites:
            components.extend(composite.get_all_components())

        return components


    def get_components(self):
        """
        Returns the components, direct children of this composite
        """
        return self._components.values()[:]


    def get_composites(self):
        """
        Returns the composites, direct children of this one
        """
        return self._composites[:]


    def set_components(self, components):
        """
        Sets the components, direct children of this composite
        """
        self._components.clear()

        if components:
            # Update the component parent
            map(lambda x: x.set_parent(self), components)
            self._components.update(((component.get_name(), component)
                                     for component in components))


    def set_composites(self, composites):
        """
        Sets the composites, direct children of this composite 
        """
        del self._composites[:]

        if composites:
            # Update the composite parent
            map(lambda x: x.set_parent(self), composites)
            self._composites.extend(composites)


    def set_parent(self, parent):
        """
        Sets the parent of this composite
        """
        self._parent = parent


class Component(object):
    """
    Represents a component
    """
    def __init__(self, parent, name):
        """
        Sets up the members
        """
        self._name = name
        self._parent = parent

        self._properties = {}
        self._field_filters = {}
        self._wires = {}

        self.type = None
        self.isolate = None


    def set_properties(self, properties):
        """
        Sets the properties of this component
        """
        self._properties.clear()

        if properties:
            self._properties.update(properties)


    def set_wires(self, wires):
        """
        Sets the wires of this components
        """
        self._wires.clear()

        if wires:
            self._wires.update(wires)


    def set_parent(self, parent):
        """
        Sets the parent of this component
        """
        self._parent = parent

# ------------------------------------------------------------------------------

@ComponentFactory('psem2m-composer-config-json-factory')
@Provides('org.psem2m.composer.config.IComposerConfigHandler')
@Requires('_finder', 'org.psem2m.isolates.services.dirs.IFileFinderSvc')
@Instantiate('psem2m-composer-config-json')
class JsonComposerConfigHandler(object):
    """
    Reads the JSON configuration file of the PSEM2M Composer.
    
    Based on the Java class:
    org.psem2m.composer.config.impl.JsonComposerConfigHandler
    """
    def __init__(self):
        """
        Sets up members
        """
        self._finder = None

        # File inclusion stack, for relative paths
        self.__include_stack = collections.deque()


    def can_handle(self, filename):
        """
        Tests if the given file could be handled by this configuration handler
        
        :param filename: A basic file name
        :return: True if the name of the file ends with '.js'
        """
        return filename.endswith('.js') or filename.endswith('.json')


    def load(self, filename):
        """
        Finds and loads the first file found in the platform directories
        matching the given name.
        
        :param filename: The name of a configuration file
        :return: The root Composite descriptor
        """
        _logger.info('Loading composition file: "%s"', filename)

        try:
            self._parse_composite(None, self._read_json_file(filename))

        except ValueError as ex:
            _logger.exception('Error parsing JSON file "%s": %s', filename, ex)

        return


    def write(self, composite, filename):
        """
        Writes the composition model configuration into the given file. Creates
        the file and parent folders if needed. If the given file name is null,
        the handler should write the result in the standard output.
        
        :param composite: A Composite descriptor
        :param filename: An output file name, can be null
        :raise IOError: An error occurred while writing the configuration file
        """
        # TODO:
        pass


    def _parse_component(self, parent, json_object):
        """
        Parses a component
        
        :param parent: The parent composite
        :param json_array: A JSON representation of the component
        :return: The parsed component
        :raise IOError:
        :raise KeyError:
        :raise ValueError:
        """
        name = json_object['name']
        _logger.debug('ParseComponent: %s', name)

        component = Component(parent, name)
        component.type = json_object['type']
        component.isolate = json_object.get('isolate')

        properties = json_object.get('properties')
        if properties:
            component.set_properties(properties)

        filters = json_object.get('filters')
        if filters:
            component.set_filters(filters)

        wires = json_object.get('wires')
        if wires:
            component.set_wires(wires)

        return component


    def _parse_components(self, parent, json_array):
        """
        Parses a set of components from a JSON array
        
        :param parent: The parent composite
        :param json_array: A JSON array of components
        :return: The parsed components
        :raise IOError:
        :raise KeyError:
        :raise ValueError:
        """
        components = []

        for json_object in json_array:
            component = self._parse_component(parent, json_object)
            if component is not None:
                components.append(component)

        return components


    def _parse_composite(self, parent, json_object):
        """
        Parses a composite JSON object
        
        :param parent: The parent composite
        :param json_object: A JSON representation of the composite
        :raise KeyError:
        :raise IOError:
        """
        name = json_object['name']
        _logger.debug('ParseComposite: %s', name)

        composite = Composite(parent, name)

        # Get the 'from'
        from_file = json_object.get('from')
        if from_file:
            # Read the 'distant' composite
            content = self._parse_composite(composite,
                                            self._read_json_file(from_file))
            composite.set_components(content.get_components())
            composite.set_composites(content.get_composites())

        else:
            # Get the components
            components = json_object.get('components')
            if components:
                composite.set_components(name,
                                         self._parse_components(components))

            # Get the sub-sets
            subsets = json_object.get('composets')
            if subsets:
                composite.set_composites(self._parse_composites(composite,
                                                                subsets))

        if not composite.is_empty():
            return composite

        return None


    def _parse_composites(self, parent, json_array):
        """
        Parses a set of composites from a JSON array
        
        :param parent: The parent composite
        :param json_array: A JSON array of composites
        :return: The parsed composites
        :raise IOError:
        :raise KeyError:
        :raise ValueError:
        """
        composites = []

        for json_object in json_array:
            composite = self._parse_composite(parent, json_object)
            if composite is not None:
                composites.add(composite)

        return composites


    def _read_json_file(self, filename):
        """
        Reads and parses the content of the given JSON file
        
        :param filename: The name of a JSON file
        :return: The parsed content of the file
        :raise IOError: Error accessing the file
        :raise ValueError: Error parsing the file
        """
        # Search for the file using a file finder and the include stack
        if self.__include_stack:
            base_file = self.__include_stack[-1]

        else:
            base_file = 'conf'

        found_files = self._finder.find(filename, base_file)
        if not found_files:
            raise IOError('File not found: {0}'.format(filename))

        # Parse the file
        with open(found_files[0]) as fp:
            return json.loads(fp.read())
