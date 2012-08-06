#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
A set of utility methods for Python isolates

:author: Thomas Calmant
"""

import sys

# ------------------------------------------------------------------------------

if sys.version_info[0] == 3:
    # Python 3
    def to_bytes(data, encoding="UTF-8"):
        """
        Converts the given string to an array of bytes.
        Returns the first parameter if it is already an array of bytes.
        
        :param data: A unicode string
        :param encoding: The encoding of data
        :return: The corresponding array of bytes
        """
        if type(data) is bytes:
            # Nothing to do
            return data

        return data.encode(encoding)


    def to_str(data, encoding="UTF-8"):
        """
        Converts the given parameter to a string.
        Returns the first parameter if it is already an instance of ``str``.
        
        :param data: A string
        :param encoding: The encoding of data
        :return: The corresponding string
        """
        if type(data) is str:
            # Nothing to do
            return data

        return str(data, encoding)


    def to_unicode(data, encoding="UTF-8"):
        """
        Converts the given string to an unicode string using ``str(data, enc)``.
        Returns the first parameter if it is already an instance of ``str``.
        
        :param data: A string
        :param encoding: The encoding of data
        :return: The corresponding ``str`` string
        """
        if type(data) is str:
            # Nothing to do
            return data

        return str(data, encoding)

# ------------------------------------------------------------------------------

else:
    # Python 2
    def to_bytes(data, encoding="UTF-8"):
        """
        Converts the given unicode string to a string using ``unicode.encode()``
        Returns the first parameter if it is already an instance of ``str``.
        
        :param data: A unicode string
        :param encoding: The encoding of data
        :return: The corresponding array of bytes
        """
        if type(data) is str:
            # Nothing to do
            return data

        return data.encode(encoding)


    def to_str(data, encoding="UTF-8"):
        """
        Converts the given parameter to a string.
        Returns the first parameter if it is already an instance of ``str``.
        
        :param data: A string
        :param encoding: The encoding of data
        :return: The corresponding string
        """
        if type(data) is str:
            # Nothing to do
            return data

        return data.encode(encoding)


    def to_unicode(data, encoding="UTF-8"):
        """
        Converts the given string to an unicode string using ``str.decode()``.
        Returns the first parameter if it is already an instance of ``unicode``.
        
        :param data: A string
        :param encoding: The encoding of data
        :return: The corresponding ``unicode`` string
        """
        if type(data) is unicode:
            # Nothing to do
            return data

        return data.decode(encoding)
