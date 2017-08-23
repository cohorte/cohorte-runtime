#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE JSON configuration file reader.

Parses JSON files and handles "import-files" and "from-file" special fields.

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
import cohorte
import json
import logging
import os
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Requires, Property

import cohorte.config.common as common


# iPOPO Decorators
# COHORTE constants
# ------------------------------------------------------------------------------
# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

KEY_FILE_FROM = 'from-file'
"""
Key used in configuration file to replace a JSON object by the content of
another file
"""

KEY_FILES_IMPORT = 'import-files'
"""
Key used to merge a JSON object with the content of other files
"""

KEY_OVERRIDDEN_PROPERTIES = 'overriddenProperties'
"""
Key used in configuration file to override the properties of imported files
"""

KEY_PROPERTIES = 'properties'
""" Standard key to store object properties (used by overriddenProperties) """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory('cohorte-reader-json-factory')
@Provides(cohorte.SERVICE_FILE_READER)
@Property('_handled_formats', 'file.format', ('json', 'js'))
@Requires('_includer', cohorte.SERVICE_FILE_INCLUDER)
@Instantiate('cohorte-reader-json')
class ConfigurationFileReader(object):
    """
    Reader for isolate configuration files, returning the combined and parsed
    JSON content.

    It handles file imports using the 'from' key
    The overridden properties, defined using the 'overriddenProperties' key,
    are applied on the 'properties' field, then on the object itself.
    """
    def __init__(self):
        """
        Sets up the parser
        """
    
        # the File includer
        self._includer = None


    @staticmethod
    def _compute_overridden_props(json_object, overriding_props):
        """
        Parses the given properties object and overrides it with the given
        properties

        :param json_object: A properties JSON object (can't be None)
        :param overriding_props: Overriding properties (can be None)
        :return: The overridden properties
        """
        overridden_props = json_object.get(KEY_OVERRIDDEN_PROPERTIES, None)
        if not overridden_props:
            return overriding_props

        if overriding_props is not None:
            overridden_props = overridden_props.copy()
            overridden_props.update(overriding_props)

        return overridden_props

    def _update_properties(self, json_data, overridden_props):
        """
        Updates the properties in the given parsed JSON content in-place

        :param json_data: A parsed JSON content
        :param overridden_props: Dictionary to describe the new values of the
                                 parsed file properties
        """
        if not overridden_props:
            # Nothing to do
            return

        if type(json_data) is list:
            # We have an array, update all of its children
            for json_object in json_data:
                self._update_properties(json_object, overridden_props)
        elif type(json_data) is dict:
            # Update the 'properties' field first
            properties_dict = json_data.get(KEY_PROPERTIES, None)
            if properties_dict and type(properties_dict) is dict:
                properties_dict.update(overridden_props)

            # Update the JSON object fields
            for key, value in overridden_props.items():
                if key in json_data:
                    json_data[key] = value

    def _do_recursive_imports(self, filename, json_data, overridden_props,
                              include_stack):
        """
        Recursively does the file merging according to the indications in the
        given JSON data (object or array).

        **TODO:**
        * Refactor to avoid duplicated lines

        :param filename: The name of the file used to parse this data
                         (can be None)
        :param json_data: A parsed JSON data (object or array)
        :param overridden_props: Properties to override in imported files
        :return: The combined JSON result
        :raise ValueError: Error parsing an imported JSON file
        :raise IOError: Error reading an imported JSON file
        """
        if type(json_data) is list:
            # We have an array, update all of its children
            i = 0
            while i < len(json_data):
                entry = json_data[i]
                new_data = self._do_recursive_imports(
                    filename, entry, overridden_props, include_stack)
                if new_data is not entry:
                    # The entry has been updated
                    json_data[i] = new_data

                i += 1

            return json_data

        elif type(json_data) is dict:
            # We have an object
            from_filename = json_data.get(KEY_FILE_FROM)
            import_filenames = json_data.get(KEY_FILES_IMPORT)

            if from_filename and not filename.endswith(from_filename):
                # Load & return the content of the imported file
                # (this method will be called by _load_file)
                new_props = self._compute_overridden_props(json_data,
                                                           overridden_props)


                imported_data = self._load_file(from_filename, filename,
                                                new_props, include_stack)

                # Update properties
                self._update_properties(imported_data, new_props)

                # Return the imported object
                return imported_data

            elif import_filenames:
                # Load the content of the imported file and merge with local
                # values, i.e. add entries existing only in the imported file
                # and merge arrays
                if type(import_filenames) is not list:
                    import_filenames = [import_filenames]

                # Remove import keys
                del json_data[KEY_FILES_IMPORT]
                if KEY_OVERRIDDEN_PROPERTIES in json_data:
                    del json_data[KEY_OVERRIDDEN_PROPERTIES]

                for import_filename in import_filenames:
                    if not filename.endswith(import_filename):
                        # Import files
                        imported_data = self._load_file(
                            import_filename, filename, overridden_props,
                            include_stack)
    
                        # Update properties in imported data
                        self._update_properties(imported_data, overridden_props)
    
                        # Merge arrays with imported data
                        json_data = common.merge_object(json_data, imported_data)
    
                        # Do the recursive import
                        for key, value in json_data.items():
                            new_value = self._do_recursive_imports(
                                filename, value, overridden_props, include_stack)
                            if new_value is not value:
                                # The value has been changed
                                json_data[key] = value

                return json_data

            else:
                # Standard object, look into its entries
                for key, value in json_data.items():
                    new_value = self._do_recursive_imports(
                        filename, value, overridden_props, include_stack)
                    if new_value is not value:
                        # The value has been changed
                        json_data[key] = new_value

                return json_data

        # Nothing to do
        return json_data

 

   
 
    def _load_file(self, filename, base_file, overridden_props, include_stack):
        """
        Parses a configuration file.
        This method shall not be called directly, as it doesn't performs clean
        up if an error occurs

        :param filename: Base name or relative name of the file to load
        :param base_file: If given, search the file near the base file first
        :param overridden_props: Properties to override in imported files
        :return: The parsed JSON content.
        :raise ValueError: Error parsing a JSON file
        :raise IOError: Error reading the configuration file
        """
        # Parse the first matching file
        # get base dir of the base file to retrieve the include one 
        dirpath = os.sep.join(base_file.split(os.sep)[:-1]) + os.sep if base_file != None and base_file.find(os.sep) != -1 else ""
        fullfilename = dirpath + filename 

        json_data = self._includer.get_content(fullfilename, True)
        # Parse the file and resolve inclusions
        self._do_recursive_imports(fullfilename, json_data,
                                          overridden_props, include_stack)  
        # Remove the top of the stack before returning
        # include_stack.pop()
        
        return json_data

    def load_file(self, filename, base_file=None, overridden_props=None,
                  log_error=True):
        """
        Parses a configuration file.
        If a configuration entry has a "from" key, then this entry is replaced
        by the content of the linked file.

        :param filename: Base name or relative name of the file to load
        :param base_file: If given, search the file near the base file first
        :param overridden_props: Properties to override in imported files
        :param log_error: If True, log the encountered I/O error (if any)
        :return: The parsed JSON content.
        :raise ValueError: Error parsing a JSON file
        :raise IOError: Error reading the configuration file
        """
        include_stack = []

        try:
            # Load the file
            return self._load_file("conf" + os.sep + filename, base_file, overridden_props,
                                   include_stack)
        except ValueError as ex:
            # Log parsing errors
            _logger.error("Error parsing file '%s': %s", include_stack[-1], ex)
            raise
        except IOError as ex:
            if log_error:
                # Log the access error
                if include_stack:
                    _logger.error("Error looking for file imported by '%s': "
                                  "%s", include_stack[-1], ex)
                else:
                    _logger.error("Can't read file '%s': %s", filename, ex)

            # Re-raise the error
            raise
