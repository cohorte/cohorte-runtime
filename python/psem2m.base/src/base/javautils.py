#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Created on 06 mars 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

JAVA_CLASS = "javaClass"

# ------------------------------------------------------------------------------

def to_jabsorb(result):
    """
    Adds information for Jabsorb, if needed.
    
    Converts maps and lists to a jabsorb form.
    Keeps tuples as is, to let them be considered as arrays.
    
    :param result: A Python result to send to Jabsorb
    :return: The result in a Jabsorb map format (not a JSON object)
    """
    converted_result = {}

    # Map ?
    if isinstance(result, dict):

        if JAVA_CLASS not in result:
            # Needs the whole transformation
            converted_result[JAVA_CLASS] = "java.util.HashMap"

            map_pairs = {}
            for key, value in result.items():
                map_pairs[key] = to_jabsorb(value)

            converted_result["map"] = map_pairs

        else:
            # Bean representation
            for key, value in result.items():
                converted_result[key] = to_jabsorb(value)

    # List ? (consider tuples as an array)
    elif isinstance(result, list):
        converted_result[JAVA_CLASS] = "java.util.ArrayList"
        converted_result["list"] = []

        for item in result:
            converted_result["list"].append(to_jabsorb(item))

    # Tuple ? (used as array, except if it is empty)
    elif isinstance(result, tuple):
        converted_result = []
        for element in result:
            converted_result.append(to_jabsorb(element))

    # Other ?
    else:
        converted_result = result

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
    if java_class.endswith("Map"):
        result = {}

        for key, value in request["map"].items():
            result[key] = from_jabsorb(value)

        return result

    # Java List ?
    elif java_class.endswith("List"):
        result = []

        for element in request["list"]:
            result.append(from_jabsorb(element))

        return result

    else:
        # Other ?
        converted_request = {}
        for key, value in request.items():
            converted_request[key] = from_jabsorb(value)

        return converted_request
