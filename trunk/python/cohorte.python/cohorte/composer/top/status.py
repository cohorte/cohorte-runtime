#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top Composer: Top status storage

Gives orders to the node composer

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
import uuid

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_STATUS_TOP)
@Instantiate('cohorte-composer-top-status')
class TopStatusStorage(object):
    """
    Stores the state of all compositions handled by this top composer
    """
    def __init__(self):
        """
        Sets up members
        """
        # Known composition names
        self._names = set()

        # UID -> {Node -> RawComponent[]}
        self._storage = {}


    def store(self, composition, distribution):
        """
        Stores a new distribution

        :param composition0: The RawComposition bean
        :param distribution: A {node -> RawComponent[]} dictionary
        :return: The UID associated to this distribution
        :raise: ValueError: The composition name is already known
        """
        # Check if the composition name has already been taken
        name = composition.name
        if name in self._names:
            raise ValueError("Already used composition name: {0}".format(name))
        else:
            # Store it
            self._names.add(name)

        # Generate a UUID
        uid = uuid.uuid4()

        # Store the composition
        self._storage[uid] = distribution
        return uid


    def get(self, uid):
        """
        Retrieves the distribution with the given UID

        :param uid: UID of the composition
        :return: The stored distribution dictionary
        :raise KeyError: Unknown UID
        """
        return self._storage[uid]


    def pop(self, uid):
        """
        Retrieves the composition with the given UID

        :param uid: UID of the composition
        :return: The stored distribution dictionary
        :raise KeyError: Unknown UID
        """
        return self._storage.pop(uid)


    def get_components_for_node(self, node_name):
        """
        Retrieves the components assigned to the given node

        :param node_name: A node name
        :return: The set of RawComponent beans associated to the node,
                 or an empty list
        """
        return {components
                for node, components in self._storage.values().items()
                if node == node_name}
