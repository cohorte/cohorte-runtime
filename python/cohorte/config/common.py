#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE config utilities


:author: Aurélien PISU
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

import logging
import re

_logger = logging.getLogger(__name__)

regexp_replace_var = re.compile("\\$\\{(.+?)\\}", re.MULTILINE)


def replace_vars(params, contents):
    """
    replace variable like ${myvar} by the value in the map if the key myvar exists else by an empty string
    @param params : a dictionnary 
    @param content : content with variable to be replace 
    @return a string with variable identified by k from the query string by the value 
    """
    if isinstance(contents, str):
        contents = [contents]
    replace_contents = []

    if params != None and contents != None:
        for content in contents:
            replace_content = content
            for match in regexp_replace_var.findall(content):
                if match in params:
                    w_param = params[match][0].__str__()
                    _logger.debug("match variable {} , replace by {}".format(match, w_param))
                    if not w_param.isdigit():
                        w_param = w_param.replace("\"", "").replace("\'", "").replace("\\", "\\\\")
                        replace_content = replace_content.replace("${" + match + "}", w_param)
                    else:
                        replace_content = replace_content.replace("\"${" + match + "}\"", w_param)
                    
                else:
                    replace_content = replace_content.replace("${" + match + "}", "") 
            _logger.debug("replace_content={}".format(replace_content))
            replace_contents.append(replace_content)
    return replace_contents


def _find_equivalent(searched_dict, dicts_list):
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
    
    # Found nothings
    return None

  
def merge_object(local, imported):
    """
    Merges recursively two JSON objects.

    The local values have priority on imported ones.
    Arrays of objects are also merged.

    :param local: The local object, which will receive the merged values
                  (modified in-place)
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

            if cur_type is dict:
                # Merge children
                local[key] = merge_object(cur_value, imp_value)
            elif cur_type is list:
                # Merge arrays
                new_array = imp_value[:]

                for cur_item in cur_value:
                    # Merge items
                    if type(cur_item) is dict:
                        # Recognize the ID key used
                        imp_item = _find_equivalent(cur_item,
                                                         imp_value)
                        if not imp_item:
                            # No equivalent found, append the item
                            new_array.append(cur_item)

                        elif imp_item != cur_item:
                            # Found an equivalent that must be merged
                            merge_object(cur_item, imp_item)

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
