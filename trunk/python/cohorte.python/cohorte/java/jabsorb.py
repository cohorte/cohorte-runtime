#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Remote Services: JSON-RPC in Jabsorb format

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# Standard library
import re

# ------------------------------------------------------------------------------

JAVA_CLASS = "javaClass"
"""
Dictionary key used by Jabsorb to indicate which Java class corresponds to its
content
"""

JAVA_MAPS_PATTERN = re.compile("java\.util\.(.*Map|Properties)")
""" Pattern to detect standard Java classes for maps """

JAVA_LISTS_PATTERN = re.compile("java\.util\..*List")
""" Pattern to detect standard Java classes for lists """

JAVA_SETS_PATTERN = re.compile("java\.util\..*Set")
""" Pattern to detect standard Java classes for sets """

# ------------------------------------------------------------------------------

class __hashabledict(dict):
    """
    Small workaround because dictionaries are not hashable in Python
    """
    def __hash__(self):
        """
        Computes the hash of the dictionary
        """
        return hash(tuple(sorted(self.items())))


class __hashableset(set):
    """
    Small workaround because sets are not hashable in Python
    """
    def __hash__(self):
        """
        Computes the hash of the set
        """
        return hash(tuple(sorted(self)))


class __hashablelist(list):
    """
    Small workaround because lists are not hashable in Python
    """
    def __hash__(self):
        """
        Computes the hash of the list
        """
        return hash(tuple(sorted(self)))

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

    elif hasattr(value, JAVA_CLASS):
        # Class with a Java class hint: convert into a dictionary
        converted_result = __hashabledict((key, to_jabsorb(content))
                                  for key, content in value.__dict__.items())

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
    if isinstance(request, (list, set, tuple)):
        # Special case : JSON arrays (Python lists)
        return [from_jabsorb(element) for element in request]

    elif not isinstance(request, dict):
        # Only handle dictionaries after this point
        return request

    java_class = request.get(JAVA_CLASS)

    if java_class:
        # Java Map ?
        if JAVA_MAPS_PATTERN.match(java_class) is not None:
            return __hashabledict((from_jabsorb(key), from_jabsorb(value))
                                  for key, value in request["map"].items())

        # Java List ?
        elif JAVA_LISTS_PATTERN.match(java_class) is not None:
            return __hashablelist(from_jabsorb(element)
                                  for element in request["list"])

        # Java Set ?
        elif JAVA_SETS_PATTERN.match(java_class) is not None:
            return __hashableset(from_jabsorb(element)
                                 for element in request["set"])

    # Any other case
    return __hashabledict((from_jabsorb(key), from_jabsorb(value))
                          for key, value in request.items())
