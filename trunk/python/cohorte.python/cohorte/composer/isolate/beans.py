#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Beans to store the description of a component on the isolate side

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 3.0.0

..

    This file is part of Cohorte.

    Cohorte is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cohorte is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cohorte. If not, see <http://www.gnu.org/licenses/>.
"""

# Module version
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

class IsolateComponent(object):
    """
    Represents a component from the isolate side: an even more basic information
    storage
    """
    # String fields
    DICT_COPY = ('name', 'factory')

    # Dictionary fields
    DICT_UPDATE = ('filters', 'wires', 'properties')

    def __init__(self):
        """
        Sets up members
        """
        self.name = None
        self.factory = None
        self.properties = {}
        self.filters = {}
        self.wires = {}


    def __str__(self):
        """
        String representation
        """
        return "IsolateComponent({0}, {1})".format(self.factory, self.name)


    def from_dictionary(self, dictionary):
        """
        Constructs this object from a dictionary

        :param dictionary: A dictionary
        :return: This object
        :raise AttributeError: A field is missing
        """
        # Copy strings
        for field in IsolateComponent.DICT_COPY:
            setattr(self, field, dictionary[field])

        # Copy dictionaries
        for field in IsolateComponent.DICT_UPDATE:
            getattr(self, field).update(dictionary[field])

        return self


    def from_raw(self, component):
        """
        Constructs this object from a RawComponent bean

        :param component: A RawComponent bean
        :return: This object
        :raise AttributeError: A field is missing
        """
        # Copy strings
        for field in IsolateComponent.DICT_COPY:
            setattr(self, field, getattr(component, field))

        # Copy dictionaries
        for field in IsolateComponent.DICT_UPDATE:
            getattr(self, field).update(getattr(component, field))

        return self
