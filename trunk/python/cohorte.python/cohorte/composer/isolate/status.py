#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Isolate Composer: Isolate status storage

Simply stores the components associated to the current isolate

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

# Composer
import cohorte.composer

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_STATUS_ISOLATE)
@Instantiate('cohorte-composer-isolate-status')
class IsolateStatusStorage(object):
    """
    Associates components to their hosting isolate
    """
    def __init__(self):
        """
        Sets up members
        """
        # Component name -> RawComponent
        self._components = {}

        # Component to be instantiated
        self._remaining = set()

        # Instantiated components
        self._instantiated = set()


    def dump(self):
        """
        Dumps the content of the storage
        """
        lines = ['Components:']
        lines.extend('\t- {0} ({1})'.format(component,
                                            component in self._instantiated)
                     for component in self._components.values())

        return '\n'.join(lines)


    def store(self, components):
        """
        Stores the given components in the storage. Ignores already stored
        beans.

        :param components: A set of RawComponent beans
        :return: The set of components that wasn't already known
        """
        added_components = set()

        for component in components:
            name = component.name
            if name not in self._components:
                # Store if not yet known
                self._components[name] = component
                self._instantiated.add(component)
                added_components.add(component)

        return added_components


    def remove(self, names):
        """
        Removes the given components from the storage

        :param names: A set of names of components
        """
        for name in names:
            try:
                component = self._components.pop(name)
                self._remaining.discard(component)
                self._instantiated.discard(component)

            except KeyError:
                _logger.warning("Unknown component: %s", name)


    def set_running(self, name):
        """
        Considers the component instantiated. Does nothing if the component
        was already considered running.

        :param name: The name of the instantiated component
        :raise KeyError: Unknown component
        """
        component = self._components[name]
        self._remaining.discard(component)
        self._instantiated.add(component)


    def set_killed(self, name):
        """
        Considers the component killed. Does nothing if the component
        was already considered killed.

        :param name: The name of the killed component
        :raise KeyError: Unknown component
        """
        component = self._components[name]
        self._instantiated.discard(component)
        self._remaining.add(component)


    def get_components(self):
        """
        Retrieves the set of all components associated to this isolate

        :return: A set of RawComponent beans
        """
        return set(self._components.values())


    def get_remaining(self):
        """
        Returns the components that still need to be instantiated

        :return: A copy of the remaining components set
        """
        return self._remaining.copy()


    def get_running(self):
        """
        Returns the components that are instantiated

        :return: A copy of the instantiated components set
        """
        return self._instantiated.copy()
