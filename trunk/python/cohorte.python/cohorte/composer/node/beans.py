#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Beans to store the description of an isolate in the Node composer

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

class Isolate(object):
    """
    Represents an isolate to be instantiated
    """
    def __init__(self, name=None):
        """
        Sets up members

        :param name: The name of the isolate
        """
        self.name = name
        self.language = None
        self.__components = set()


    def __str__(self):
        """
        String representation
        """
        if not self.language:
            return "NeutralIsolate"

        return "Isolate({0}, {1})".format(self.name, self.language)


    @property
    def components(self):
        """
        Returns the (frozen) set of components associated to this isolate
        """
        return frozenset(self.__components)


    @property
    def factories(self):
        """
        Returns the (frozen) set of the factories required to instantiate
        the components associated to this isolate
        """
        return frozenset(component.factory for component in self.__components)


    def add_component(self, component):
        """
        Adds a component to the isolate
        """
        if self.language is None:
            # First component tells which language this isolate hosts
            self.language = component.language

        self.__components.add(component)
