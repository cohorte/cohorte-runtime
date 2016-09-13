#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE configuration file parser: converts a parsed configuration file to
beans

:author: Thomas Calmant
:license: Apache Software License 2.0

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

# Python standard library
import collections
import logging
import uuid

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Requires

# COHORTE constants
import cohorte

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

# Component to be instantiated
Component = collections.namedtuple(
    'Component', ('factory', 'name', 'properties'))

# Bundle to be installed
Bundle = collections.namedtuple(
    'Bundle', ('name', 'filename', 'properties', 'version', 'optional'))

# Simplest configuration possible
BootConfiguration = collections.namedtuple(
    'BootConfiguration', ('bundles', 'composition', 'properties',
                          'environment', 'boot_args'))

# Boot configuration + Isolate basic description
Isolate = collections.namedtuple(
    'Isolate', BootConfiguration._fields + ('name', 'kind', 'node',
                                            'level', 'sublevel'))


def _recursive_namedtuple_convert(data):
    """
    Recursively converts the named tuples in the given object to dictionaries

    :param data: An object in a named tuple or its children
    :return: The converted object
    """
    if isinstance(data, list):
        # List
        return [_recursive_namedtuple_convert(item) for item in data]
    elif hasattr(data, '_asdict'):
        # Named tuple
        dict_value = dict(data._asdict())
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

    @staticmethod
    def _parse_bundle(json_object):
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

        return Bundle(name=json_object['name'],
                      filename=json_object.get('file'),
                      properties=properties,
                      version=json_object.get('version'),
                      optional=json_object.get('optional', False))

    def _parse_bundles(self, bundles):
        """
        Parses the bundles in the given list. Returns an empty list if the
        given one is None or empty.

        :param bundles: A list of bundles representations
        :return: A list of Bundle objects
        :raise KeyError: A mandatory parameter is missing
        """
        if not bundles:
            return []

        return [self._parse_bundle(bundle) for bundle in bundles]

    @staticmethod
    def _parse_component(json_object):
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

    def _parse_components(self, components):
        """
        Parses the components in the given list. Returns an empty list if the
        given one is None or empty.

        :param components: A list of components representations
        :return: A list of Component objects
        :raise KeyError: A mandatory parameter is missing
        """
        if not components:
            return []
        return [self._parse_component(component) for component in components]

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
                       **boot_config._asdict())

    def _prepare_configuration(self, uid, name, kind,
                               bundles=None, composition=None,
                               base_configuration=None):
        """
        Prepares and returns a configuration dictionary to be stored in the
        configuration broker, to start an isolate of the given kind.

        :param uid: The isolate UID
        :param name: The isolate name
        :param kind: The kind of isolate to boot
        :param bundles: Extra bundles to install
        :param composition: Extra components to instantiate
        :param base_configuration: Base configuration (to override)
        :return: A configuration dictionary
                 (updated base_configuration if given)
        :raise IOError: Unknown/unaccessible kind of isolate
        :raise KeyError: A parameter is missing in the configuration files
        :raise ValueError: Error reading the configuration
        """
        if isinstance(base_configuration, dict):
            configuration = base_configuration
        else:
            configuration = {}

        # Set up isolate properties
        configuration['uid'] = uid \
            or configuration.get('custom_uid') or str(uuid.uuid4())
        configuration['name'] = name
        configuration['kind'] = kind

        # Boot configuration for this kind
        new_boot = configuration.setdefault('boot', {})
        new_boot.update(_recursive_namedtuple_convert(self.load_boot(kind)))

        # Add bundles (or an empty list)
        if bundles:
            new_bundles = configuration.setdefault('bundles', [])
            new_bundles.extend(_recursive_namedtuple_convert(
                [self.normalize_bundle(bundle) for bundle in bundles]))

        # Add components (or an empty list)
        if composition:
            new_compo = configuration.setdefault('composition', [])
            new_compo.extend(_recursive_namedtuple_convert(composition))

        # Return the configuration dictionary
        return configuration

    @staticmethod
    def normalize_bundle(bundle):
        """
        Make a Bundle object from the given Bundle-like object attributes,
        using default values when necessary.

        :param bundle: A Bundle-like object
        :return: A Bundle object
        :raise AttributeError: A mandatory attribute is missing
        :raise ValueError: Invalid attribute value
        """
        if isinstance(bundle, Bundle):
            # Already a bundle
            return bundle

        # Bundle name is mandatory
        name = bundle.name
        if not name:
            raise ValueError("A bundle must have a name: {0}".format(bundle))

        # Get the filename
        for fileattr in ('filename', 'file'):
            filename = getattr(bundle, fileattr, None)
            if filename:
                break

        # Normalize bundle properties
        properties = getattr(bundle, 'properties', {})
        if not isinstance(properties, dict):
            properties = {}

        # Normalize bundle version
        version = getattr(bundle, 'version', None)
        if version is not None:
            version = str(version)

        return Bundle(name, filename, properties, version,
                      getattr(bundle, 'optional', False))

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
        bundles = self._parse_bundles(dict_config.get('bundles'))
        composition = self._parse_components(dict_config.get('composition'))
        return BootConfiguration(bundles=bundles,
                                 composition=composition,
                                 boot_args=dict_config.get('boot_args'),
                                 environment=environment,
                                 properties=properties)

    def prepare_isolate(self, uid, name, kind, level, sublevel,
                        bundles=None, composition=None):
        """
        Prepares and returns a configuration dictionary to be stored in the
        configuration broker, to start an isolate of the given kind.

        :param uid: The isolate UID
        :param name: The isolate name
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

        try:
            # Try to load the isolate-specific configuration
            # without logging "file not found" errors
            isolate_conf = self.read(name + ".js", False)
        except IOError:
            # Ignore I/O errors (file not found)
            # Propagate ValueError (parsing errors)
            pass
        else:
            # Merge the configurations: this method considers that the first
            # parameter has priority on the second
            configuration = self._reader.merge_object(isolate_conf,
                                                      configuration)

        # Extend with the boot configuration
        return self._prepare_configuration(uid, name, kind,
                                           bundles, composition, configuration)

    def read(self, filename, reader_log_error=True):
        """
        Reads the content of the given file, without parsing it.

        :param filename: A configuration file name
        :param reader_log_error: If True, the reader will log I/O errors
        :return: The dictionary read from the file
        """
        return self._reader.load_file(filename, 'conf',
                                      log_error=reader_log_error)
