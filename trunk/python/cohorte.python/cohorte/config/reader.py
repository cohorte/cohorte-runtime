#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE JSON configuration file reader

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
    Requires, Property

# Python standard library
import json
import logging

# ------------------------------------------------------------------------------

KEY_FILE_FROM = 'from-file'
"""
Key used in configuration file to replace a JSON object by the content of
another file
"""

KEY_FILE_IMPORT = 'import-file'
"""
Key used to merge a JSON object with the content of another file
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
@Requires('_finder', cohorte.SERVICE_FILE_FINDER)
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
        # The file finder
        self._finder = None

        # File inclusion stack
        self._include_stack = []


    def _compute_overridden_props(self, json_object, overriding_props):
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


    def _do_recursive_imports(self, filename, json_data, overridden_props):
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
                new_data = self._do_recursive_imports(filename, entry,
                                                      overridden_props)
                if new_data is not entry:
                    # The entry has been updated
                    json_data[i] = new_data

                i += 1

            return json_data

        elif type(json_data) is dict:
            # We have an object
            from_filename = json_data.get(KEY_FILE_FROM)
            import_filename = json_data.get(KEY_FILE_IMPORT)

            if from_filename:
                # Load & return the content of the imported file
                # (this method will be called by _load_file)
                new_props = self._compute_overridden_props(json_data,
                                                           overridden_props)
                imported_data = self._load_file(from_filename, filename,
                                                new_props)

                # Update properties
                self._update_properties(imported_data, overridden_props)

                # Return the imported object
                return imported_data

            elif import_filename:
                # Load the content of the imported file and merge with local
                # values, i.e. add entries existing only in the imported file
                # and merge arrays
                new_props = self._compute_overridden_props(json_data,
                                                           overridden_props)
                imported_data = self._load_file(import_filename, filename,
                                                new_props)

                # Update properties in imported data
                self._update_properties(imported_data, overridden_props)

                # Remove import keys
                del json_data[KEY_FILE_IMPORT]
                if KEY_OVERRIDDEN_PROPERTIES in json_data:
                    del json_data[KEY_OVERRIDDEN_PROPERTIES]

                # Merge arrays with imported data
                json_data = self._merge_object(json_data, imported_data)

                # Do the recursive import
                for key, value in json_data.items():
                    new_value = self._do_recursive_imports(filename, value,
                                                           overridden_props)
                    if new_value is not value:
                        # The value has been changed
                        json_data[key] = value

                return json_data

            else:
                # Standard object, look into its entries
                for key, value in json_data.items():
                    new_value = self._do_recursive_imports(filename, value,
                                                           overridden_props)
                    if new_value is not value:
                        # The value has been changed
                        json_data[key] = value

                return json_data

        # Nothing to do
        return json_data


    def _find_equivalent(self, searched_dict, dicts_list):
        """
        Finds the item in the given list which has the same ID than the given
        dictionary.
        
        A dictionary is equivalent to another if they have the same value for
        one of the following keys: 'id', 'uid', 'name'.
        
        :param searched_dict: The dictionary to look for into the list
        :param dicts_list: A list of potential equivalents
        :return: The first item found in the list equivalent to the given
                 dictionary, or None
        """
        for id_key in ('id', 'uid', 'name'):
            # Recognize the ID key used, if any
            local_id = searched_dict.get(id_key)
            if local_id:
                # Found an ID
                for other_item in dicts_list:
                    if other_item.get(id_key) == local_id:
                        # Found an item with the same ID
                        return other_item

        # Found nothing
        return None


    def _merge_object(self, local, imported):
        """
        Merges recursively two JSON objects.
        
        The local values have priority on imported ones.
        Arrays of objects are also merged. 
        
        :param local: The local object, which will receive get the merged values
        :param imported: The imported object, which will be merged into local
        :return: The merge result, i.e. local
        """
        for key, imp_value in imported.items():

            if key not in local:
                # Missing key
                local[key] = imp_value

            else:
                # Get current value
                cur_value = local[key]
                cur_type = type(cur_value)

                if cur_type is not type(imp_value):
                    # Different types found
                    _logger.warning('Trying to merge different types. Ignoring.'
                                    ' (key: %s, types: %s / %s)',
                                    key, cur_type, type(imp_value))
                    continue

                if cur_type is dict:
                    # Merge children
                    local[key] = self._merge_object(cur_value, imp_value)

                elif cur_type is list:
                    # Merge arrays
                    new_array = imp_value[:]

                    for cur_item in cur_value:
                        # Merge items
                        if type(cur_item) is dict:
                            # Recognize the ID key used
                            imp_item = self._find_equivalent(cur_item,
                                                             imp_value)

                            if not imp_item:
                                # No equivalent found, append the item
                                new_array.append(cur_item)

                            elif imp_item != cur_item:
                                # Found an equivalent that must be merged
                                self._merge_object(cur_item, imp_item)

                                # Replace the existing entry
                                idx = new_array.index(imp_item)
                                del new_array[idx]
                                new_array.insert(idx, cur_item)

                        elif cur_item not in imp_value:
                            # Append new values
                            new_array.append(cur_item)

                    # Update the object
                    local[key] = new_array

        return local


    def _parse_file(self, filename, overridden_props):
        """
        Returns the parsed JSON content of the given file
        
        :param filename: A JSON file to parse
        :param overridden_props: Properties to override in imported files
        :return: The parsed content (array or dictionary)
        :raise ValueError: Error parsing the file
        :raise IOError: Error reading an imported JSON file
        """
        # Read the file content, removing commented lines
        lines = []
        with open(filename) as filep:
            # Comment block flag
            commented = False
            for line in filep:
                if not commented:
                    # Non commented line
                    lstrip_line = line.lstrip()

                    if lstrip_line.startswith('/*'):
                        # Comment block start (can be on one line)
                        commented = not line.rstrip().endswith('*/')

                    elif not lstrip_line.startswith('//'):
                        # Normal line
                        lines.append(line)

                else:
                    # We're in a comment block
                    if line.rstrip().endswith('*/'):
                        # End of the comment
                        commented = False

        # Load the JSON data
        json_data = json.loads(''.join(lines))

        # Check imports
        return self._do_recursive_imports(filename, json_data, overridden_props)


    def _load_file(self, filename, base_file, overridden_props):
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
        conffile = next(self._finder.find_rel(filename, base_file), None)
        if not conffile:
            raise IOError("File not found: '{0}' (base: {1})" \
                          .format(filename, base_file))

        if conffile in self._include_stack:
            # The file is already in the inclusion stack
            raise ValueError("Recursive import detected: '{0}' - '{1}'" \
                             .format(conffile, self._include_stack))

        else:
            # Store the file in the inclusion stack
            self._include_stack.append(conffile)

        # Parse the file and resolve inclusions
        json_data = self._parse_file(conffile, overridden_props)

        # Remove the top of the stack before returning
        self._include_stack.pop()
        return json_data


    def load_file(self, filename, base_file=None, overridden_props=None):
        """
        Parses a configuration file.
        If a configuration entry has a "from" key, then this entry is replaced
        by the content of the linked file.
        
        :param filename: Base name or relative name of the file to load
        :param base_file: If given, search the file near the base file first
        :param overridden_props: Properties to override in imported files
        :return: The parsed JSON content.
        :raise ValueError: Error parsing a JSON file
        :raise IOError: Error reading the configuration file
        """
        try:
            # Load the file
            return self._load_file(filename, base_file, overridden_props)

        except ValueError as ex:
            # Log parsing errors
            _logger.error("Error parsing file '%s': %s",
                          self._include_stack[-1], ex)

        except IOError as ex:
            # Log access errors
            if self._include_stack:
                _logger.error("Error looking for file imported by '%s': %s",
                              self._include_stack[-1], ex)

            else:
                _logger.error("Can't read file '%s': %s", filename, ex)

            # Re-raise the error
            raise

        finally:
            # Clear the stack content in any case
            del self._include_stack[:]
