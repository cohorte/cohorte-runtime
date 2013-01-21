#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Created on 06 mars 2012

:author: Thomas Calmant
"""

import re

# ------------------------------------------------------------------------------

JAVA_CLASS = "javaClass"

JAVA_MAPS_PATTERN = re.compile("java\.util\.(.*Map|Properties)")
JAVA_LISTS_PATTERN = re.compile("java\.util\..*List")
JAVA_SETS_PATTERN = re.compile("java\.util\..*Set")

# ------------------------------------------------------------------------------

def to_jabsorb(value):
    """
    Adds information for Jabsorb, if needed.
    
    Converts maps and lists to a jabsorb form.
    Keeps tuples as is, to let them be considered as arrays.
    
    :param value: A Python result to send to Jabsorb
    :return: The result in a Jabsorb map format (not a JSON object)
    """
    converted_result = {}

    # None ?
    if value is None:
        return None

    # Map ?
    elif isinstance(value, dict):
        if JAVA_CLASS not in value:
            # Needs the whole transformation
            converted_result[JAVA_CLASS] = "java.util.HashMap"
            converted_result["map"] = map_pairs = {}
            for key, content in value.items():
                map_pairs[key] = to_jabsorb(content)

        else:
            # Bean representation
            for key, content in value.items():
                converted_result[key] = to_jabsorb(content)

    # List ? (consider tuples as an array)
    elif isinstance(value, list):
        converted_result[JAVA_CLASS] = "java.util.ArrayList"
        converted_result["list"] = [to_jabsorb(entry) for entry in value]

    # Set ?
    elif isinstance(value, set):
        converted_result[JAVA_CLASS] = "java.util.HashSet"
        converted_result["set"] = [to_jabsorb(entry) for entry in value]

    # Tuple ? (used as array, except if it is empty)
    elif isinstance(value, tuple):
        converted_result = [to_jabsorb(entry) for entry in value]

    # Other ?
    else:
        converted_result = value

    return converted_result


def from_jabsorb(request):
    """
    Transforms a jabsorb request into a more Python data model (converts maps
    and lists)
    
    :param request: Data coming from Jabsorb
    :return: A Python representation of the given data
    """
    if isinstance(request, list):
        # Special case : JSON arrays (Python lists)
        converted_list = []
        for element in request:
            converted_list.append(from_jabsorb(element))

        return converted_list

    if not isinstance(request, dict) or JAVA_CLASS not in request:
        # Raw element
        return request

    java_class = str(request[JAVA_CLASS])

    # Java Map ?
    if JAVA_MAPS_PATTERN.match(java_class) is not None:
        result = {}

        for key, value in request["map"].items():
            result[key] = from_jabsorb(value)

        return result

    # Java List ?
    elif JAVA_LISTS_PATTERN.match(java_class) is not None:
        result = []

        for element in request["list"]:
            result.append(from_jabsorb(element))

        return result

    # Java Set ?
    elif JAVA_SETS_PATTERN.match(java_class) is not None:
        result = set()

        for element in request["set"]:
            result.add(from_jabsorb(element))

        return result

    else:
        # Other ?
        converted_request = {}
        for key, value in request.items():
            converted_request[key] = from_jabsorb(value)

        return converted_request
