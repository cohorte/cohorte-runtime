#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE configuration file parser

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version__ = '1.0.0'

# ------------------------------------------------------------------------------

# COHORTE constants
import cohorte

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Requires

# Python standard library
import collections
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

# Component to be instantiated
Component = collections.namedtuple('Component', ('factory', 'name',
                                                 'properties'))

# Bundle to be installed
Bundle = collections.namedtuple('Bundle', ('name', 'filename', 'properties',
                                           'version', 'optional'))

# Simplest configuration possible
BootConfiguration = collections.namedtuple('BootConfiguration',
                                           ('bundles', 'composition',
                                            'properties', 'environment',
                                            'boot_args'))

# Boot configuration + Isolate basic description
Isolate = collections.namedtuple('Isolate', BootConfiguration._fields
                                            + ('name', 'kind', 'node',
                                               'level', 'sublevel'))

def _recursive_namedtuple_convert(data):
    """
    Recursively converts the named tuples in the given object to dictionaries
    
    :param data: An object in a named tuple or its children
    :return: The converted object
    """
    if type(data) is list:
        # List
        return [_recursive_namedtuple_convert(item) for item in data]

    elif hasattr(data, '_asdict'):
        # Named tuple
        dict_value = data._asdict()
        for key, value in dict_value.items():
            dict_value[key] = _recursive_namedtuple_convert(value)

        return dict_value

    else:
        # Standard object
        return data

# ------------------------------------------------------------------------------

@ComponentFactory('cohorte-config-parser-factory')
@Provides(cohorte.SERVICE_CONFIGURATION_READER)
@Requires('_reader', cohorte.SERVICE_FILE_READER)
@Instantiate('cohorte-config-parser')
class BootConfigParser(object):
    """
    Boot configuration parser
    """
    def __init__(self):
        """
        Sets up the members
        """
        # File reader
        self._reader = None

        # Loaded isolates configurations
        self._isolates = None


    def _parse_bundle(self, json_object):
        """
        Reads the given JSON object and returns its Bundle representation
        
        :param json_object: A parsed JSON object
        :return: A Bundle object
        :raise KeyError: A mandatory parameter is missing
        """
        # Use a copy of the properties
        properties = {}
        json_properties = json_object.get('properties')
        if json_properties:
            properties.update(json_properties)

        return Bundle(# Mandatory
                      name=json_object['name'],

                      # Optional
                      filename=json_object.get('file'),
                      properties=properties,
                      version=json_object.get('version'),
                      optional=json_object.get('optional', False))


    def _parse_bundles(self, bundles_list):
        """
        Parses the bundles in the given list. Returns an empty list if the
        given one is None or empty.
        
        :param bundles_list: A list of bundles representations
        :return: A list of Bundle objects
        :raise KeyError: A mandatory parameter is missing
        """
        if not bundles_list:
            return []

        bundles = []
        for bundle in bundles_list:
            bundles.append(self._parse_bundle(bundle))

        return bundles


    def _parse_component(self, json_object):
        """
        Reads the given JSON object and returns its Component representation
        
        :param json_object: A parsed JSON object
        :return: A Component object
        :raise KeyError: A mandatory parameter is missing
        """
        # Mandatory values
        factory = json_object['factory']

        # Computed name (if needed)
        name = json_object.get('name', factory + '-instance')

        # Use a copy of the properties
        properties = {}
        json_properties = json_object.get('properties')
        if json_properties:
            properties.update(json_properties)

        return Component(factory=factory, name=name, properties=properties)


    def _parse_components(self, components_list):
        """
        Parses the components in the given list. Returns an empty list if the
        given one is None or empty.
        
        :param components_list: A list of components representations
        :return: A list of Component objects
        :raise KeyError: A mandatory parameter is missing
        """
        if not components_list:
            return []

        components = []
        for component in components_list:
            components.append(self._parse_component(component))

        return components


    def _parse_isolate(self, json_object):
        """
        Reads the given JSON object and returns its Isolate representation
        
        :param json_object: A parsed JSON object
        :return: An Isolate object
        :raise KeyError: A mandatory parameter is missing
        """
        # Reuse the boot parser
        boot_config = self.load_boot_dict(json_object)

        return Isolate(name=json_object['name'],
                       kind=json_object['kind'],
                       level=json_object['level'],
                       sublevel=json_object['sublevel'],

                       # Reuse boot configuration values
                       ** boot_config._asdict())


    def _prepare_configuration(self, uid, name, node, kind,
                              bundles=None, composition=None):
        """
        Prepares and returns a configuration dictionary to be stored in the
        configuration broker, to start an isolate of the given kind.
        
        :param uid: The isolate UID
        :param name: The isolate name
        :param node: The isolate node name
        :param kind: The kind of isolate to boot
        :param bundles: Extra bundles to install
        :param composition: Extra components to instantiate
        :return: A configuration dictionary
        :raise IOError: Unknown/unaccessible kind of isolate
        :raise KeyError: A parameter is missing in the configuration files
        :raise ValueError: Error reading the configuration
        """
        configuration = {}

        # Set up isolate properties
        configuration['uid'] = uid
        configuration['name'] = name
        configuration['node'] = node
        configuration['kind'] = kind

        # Boot configuration for the given kind
        boot = self.load_boot(kind)

        # Convert the boot configuration to a dictionary
        configuration['boot'] = _recursive_namedtuple_convert(boot)

        # Add bundles (or an empty list)
        if bundles:
            configuration['bundles'] = bundles

        # Add components (or an empty list)
        if composition:
            configuration['composition'] = composition or []

        # Return the configuration dictionary
        return configuration


    def load_boot(self, kind):
        """
        Loads the boot configuration for the given kind of isolate, or returns
        the one in the cache.
        
        :param kind: The kind of isolate to boot
        :return: The loaded BootConfiguration object
        :raise IOError: Unknown/unaccessible kind of isolate
        :raise KeyError: A parameter is missing in the configuration files
        :raise ValueError: Error reading the configuration
        """
        # Prepare & store the bean representation
        return self.load_boot_dict(self.load_conf_raw('boot', kind))


    def load_conf_raw(self, level, kind):
        """
        Loads the boot configuration for the given kind of isolate, or returns
        the one in the cache.
        
        :param level: The level of configuration (boot, java, python)
        :param kind: The kind of isolate to boot
        :return: The loaded BootConfiguration object
        :raise IOError: Unknown/unaccessible kind of isolate
        :raise KeyError: A parameter is missing in the configuration files
        :raise ValueError: Error reading the configuration
        """
        # Load the boot file
        return self.read('{0}-{1}.js'.format(level, kind))


    def load_boot_dict(self, dict_config):
        """
        Parses a boot configuration from the given dictionary
        
        :param dict_config: A configuration dictionary
        :return: The parsed BootConfiguration object
        :raise KeyError: A parameter is missing in the configuration files
        :raise ValueError: Error reading the configuration
        """
        # Use a copy of environment
        environment = {}
        json_env = dict_config.get('environment')
        if json_env:
            environment.update(json_env)

        # Parse the properties
        properties = {}
        dict_properties = dict_config.get('properties')
        if dict_properties:
            properties.update(dict_properties)

        # Prepare the bean representation
        return BootConfiguration(bundles=self._parse_bundles(
                                               dict_config.get('bundles')),
                                 composition=self._parse_components(
                                               dict_config.get('composition')),
                                 boot_args=dict_config.get('boot_args'),
                                 environment=environment,
                                 properties=properties)


    def prepare_isolate(self, uid, name, node, kind, level, sublevel,
                        bundles=None, composition=None):
        """
        Prepares and returns a configuration dictionary to be stored in the
        configuration broker, to start an isolate of the given kind.
        
        :param uid: The isolate UID
        :param name: The isolate name
        :param node: The isolate node name
        :param kind: The kind of isolate to boot (pelix, osgi, ...)
        :param level: The level of configuration (boot, java, python, ...)
        :param sublevel: Category of configuration (monitor, isolate, ...)
        :param bundles: Extra bundles to install
        :param composition: Extra components to instantiate
        :return: A configuration dictionary
        :raise IOError: Unknown/unaccessible kind of isolate
        :raise KeyError: A parameter is missing in the configuration files
        :raise ValueError: Error reading the configuration
        """
        # Load the isolate model file
        configuration = self.load_conf_raw(level, sublevel)

        # Extend with the boot configuration
        configuration.update(self._prepare_configuration(uid, name, node, kind,
                                                         bundles, composition))
        return configuration


    def read(self, filename):
        """
        Reads the content of the given file, without parsing it.
        
        :param filename: A configuration file name
        :return: The dictionary read from the file
        """
        return self._reader.load_file(filename, 'conf')
