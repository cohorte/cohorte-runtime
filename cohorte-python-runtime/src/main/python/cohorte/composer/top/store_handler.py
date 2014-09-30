#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top Composer: Top storage client

Stores the distributions computed by the Top Composer and assigns them an UUID

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
from pelix.ipopo.decorators import ComponentFactory, Requires, Instantiate, \
    BindField, Validate, Invalidate

# Standard
import logging
import threading

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory()
@Requires('_composer', cohorte.composer.SERVICE_COMPOSER_TOP)
@Requires('_status', cohorte.composer.SERVICE_STATUS_TOP)
@Requires('_stores', cohorte.composer.SERVICE_TOP_STORAGE,
          aggregate=True, optional=True)
@Instantiate('cohorte-composer-top-storage-handler')
class TopStorageHandler(object):
    """
    Stores the state of all compositions handled by this top composer
    """
    def __init__(self):
        """
        Sets up members
        """
        # Top Composer
        self._composer = None

        # Top status
        self._status = None

        # Top storage services
        self._stores = []

        # Thread safety
        self.__lock = threading.RLock()

    @BindField('_stores', if_valid=True)
    def _bind_store(self, field, svc, svc_ref):
        """
        New Top storage service bound
        """
        with self.__lock:
            # Read what it has to give
            self.handle_store(svc)

            # Store what we started
            self.store_all(svc)

    @Validate
    def _validate(self, context):
        """
        Component validated
        """
        with self.__lock:
            if self._stores:
                # Handle already bound stores
                for store in self._stores:
                    # Read stored information
                    self.handle_store(store)

                    # Store the current information
                    self.store_all(store)

        # Register to status modifications
        self._status.add_listener(self)

    @Invalidate
    def _invalidate(self, context):
        """
        Component invalidated
        """
        self._status.remove_listener(self)

    def distribution_added(self, uid, name, distribution):
        """
        Called by the Top Status when a distribution has been stored

        :param uid: UID of the new composition
        :param name: Name of the new composition
        :param distribution: Computed distribution
        """
        with self.__lock:
            content = {'name': name, 'distribution': distribution}
            for storage in self._stores:
                try:
                    storage.store(uid, content)
                except Exception as ex:
                    _logger.error("Error storing distribution: %s", ex)

    def distribution_removed(self, uid):
        """
        Called by the Top Status when a distribution has been removed

        :param uid: UID of the removed composition
        """
        with self.__lock:
            for store in self._stores:
                store.remove(uid)

    def handle_store(self, store):
        """
        Handles a new Top Storage service

        :param store: A Top Storage service
        """
        # Get storage UIDs
        uids = set(store.get_uids())

        # Remove known UIDs
        uids.difference_update(self._status.list())

        for uid in uids:
            # Reload all stored compositions
            stored = store.load(uid)

            name = stored['name']
            distribution = stored['distribution']

            try:
                _logger.debug("Reloading %s - %s...", name, uid)
                self._composer.reload_distribution(name, distribution, uid)
            except KeyError:
                # Already known distribution
                pass

    def store_all(self, store):
        """
        Stores the content of the status to the given top storage

        :param store: A top storage service
        """
        for uid in self._status.list():
            distribution = self._status.get(uid)
            name = self._status.get_name(uid)

            # Store data
            store.store(uid, {'name': name, 'distribution': distribution})
