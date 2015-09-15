#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top Composer: Top status storage

Stores the distributions computed by the Top Composer and assigns them an UUID

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 3.0.0

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

# Standard library
import logging
import uuid

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate

# Composer
import cohorte.composer

# ------------------------------------------------------------------------------

# Module version
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

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
        # Known compositions names
        self._names = set()

        # UID -> Name
        self._uids = {}

        # UID -> {Node -> set(RawComponent)}
        self._storage = {}

        # Listeners
        self.__listeners = set()

    def dump(self):
        """
        Dumps the content of the storage
        """
        lines = ['Names:']
        lines.extend('\t- {0}'.format(name) for name in self._names)
        lines.append('')
        lines.append('Storage:')
        for uid, distribution in self._storage.items():
            lines.append('\t- {0}'.format(uid))
            for node, components in distribution.items():
                lines.append('\t\t- {0}'.format(node))
                lines.extend('\t\t\t- {0}'.format(component)
                             for component in components)

        return '\n'.join(lines)

    def add_listener(self, listener):
        """
        Registers a Top status listener

        :param listener: The new listener
        """
        if listener is not None:
            self.__listeners.add(listener)

    def remove_listener(self, listener):
        """
        Removes a Top status listener

        :param listener: Listener to remove
        """
        self.__listeners.discard(listener)

    def store(self, name, distribution, uid=None):
        """
        Stores a new distribution

        :param name: The name of the composition
        :param distribution: A {node -> set(RawComponent)} dictionary
        :param uid: A forced UID (for reloaded compositions)
        :return: The UID associated to this distribution
        :raise KeyError: UID already taken
        :raise ValueError: The composition name is already known
        """
        # Check if the composition name has already been taken
        if name in self._names:
            raise ValueError("Already used composition name: {0}".format(name))

        # Generate a UUID
        if not uid:
            uid = str(uuid.uuid4())

        if uid in self._uids:
            raise KeyError("UID conflict: %s", uid)

        # Store the composition
        self._names.add(name)
        self._uids[uid] = name
        self._storage[uid] = distribution

        # Notify listeners
        for listener in self.__listeners:
            try:
                listener.distribution_added(uid, name, distribution.copy())
            except Exception as ex:
                _logger.exception("Error notifying status addition: %s", ex)

        return uid

    def get(self, uid):
        """
        Retrieves the distribution with the given UID

        :param uid: UID of the composition
        :return: The stored distribution dictionary
        :raise KeyError: Unknown UID
        """
        return self._storage[uid]

    def get_name(self, uid):
        """
        Returns the name of the composition associated to the given UID

        :param uid: UID of a composition
        :return: The name of the composition
        :raise KeyError: Unknown UID
        """
        return self._uids[uid]

    def list(self):
        """
        Returns the list of stored UIDs

        :return: A list of UIDs of compositions
        """
        return list(self._storage.keys())

    def pop(self, uid):
        """
        Retrieves the composition with the given UID

        :param uid: UID of the composition
        :return: The stored distribution dictionary
        :raise KeyError: Unknown UID
        """
        # Remove the name entry
        name = self._uids.pop(uid)
        self._names.remove(name)

        # Notify listeners
        for listener in self.__listeners:
            try:
                listener.distribution_removed(uid)
            except Exception as ex:
                _logger.exception("Error notifying status removal: %s", ex)

        # Remove and return the distribution
        return self._storage.pop(uid)

    def get_components_for_node(self, node_name):
        """
        Retrieves the components assigned to the given node

        :param node_name: A node name
        :return: The set of RawComponent beans associated to the node,
                 or an empty set
        """
        return {component
                for distribution in self._storage.values()
                for component in distribution.get(node_name, [])}
