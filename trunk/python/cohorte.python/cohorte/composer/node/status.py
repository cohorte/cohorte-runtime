#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Node status storage

Simply stores the components associated to local isolates

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

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_STATUS_NODE)
@Instantiate('cohorte-composer-node-status')
class NodeStatusStorage(object):
    """
    Associates components to their hosting isolate
    """
    def __init__(self):
        """
        Sets up members
        """
        # RawComponent -> Isolate name
        self._configuration = {}

        # Isolate name -> set(RawComponent)
        self._isolate_conf = {}


    def dump(self):
        """
        Dumps the content of the storage
        """
        lines = ['Isolates:']
        for isolate, components in self._storage.items():
            lines.append('\t- {0}'.format(isolate))
            lines.extend('\t\t- {0}'.format(component)
                         for component in components)

        return '\n'.join(lines)


    def store(self, distribution):
        """
        Updates the storage with the given distribution

        :param distribution: A {Isolate name -> set(RawComponent)} dictionary
        """
        for isolate, components in distribution:
            # Isolate -> Components
            self._isolate_conf.setdefault(isolate, set()).update(components)

            # Component -> Isolate
            for component in components:
                self._configuration[component] = isolate


    def remove(self, components):
        """
        Removes the given components from the storage
        """
        for isolate_components in self._isolate_conf.values():
            isolate_components.difference_update(components)

        for component in components:
            del self._configuration[component]


    def get_components_for_isolate(self, isolate_name):
        """
        Retrieves the components assigned to the given node

        :param isolate_name: The name of an isolate
        :return: The set of RawComponent beans associated to the isolate,
                 or an empty set
        """
        # Return a copy
        return self._isolate_conf.get(isolate_name, set()).copy()
