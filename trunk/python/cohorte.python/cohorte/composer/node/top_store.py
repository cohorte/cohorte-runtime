#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top composer distribution storage, hosted by node composers

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

# Cohorte
import cohorte.composer

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Property, \
    Instantiate

# Pelix
import pelix.remote

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_TOP_STORAGE)
@Property('_export', pelix.remote.PROP_EXPORTED_INTERFACES, '*')
@Property('_export_name', pelix.remote.PROP_ENDPOINT_NAME,
          'composer-top-storage')
@Instantiate('cohorte-composer-top-storage')
class TopStorage(object):
    """
    Stores distributions computed by the top composer
    """
    def __init__(self):
        """
        Sets up members
        """
        # Export properties
        self._export = None
        self._export_name = None

        # UID -> content
        self.__content = {}


    def get_uids(self):
        """
        Returns the list of stored UIDs

        :return: A list of UIDs
        """
        return list(self.__content.keys())


    def load(self, uid):
        """
        Returns the content associated to the given UID, or None

        :param uid: A UID
        :return: The associated content or None
        """
        _logger.critical("Loading %s", uid)
        return self.__content.get(uid)


    def remove(self, uid):
        """
        Removes the content associated to the given UID

        :param uid: A UID
        :return: True if the UID was known
        """
        try:
            _logger.critical("Removing %s", uid)
            del self.__content[uid]
            return True

        except KeyError:
            return False


    def store(self, uid, content):
        """
        Stores the given content, associated to the given UID. Overwrites the
        previous content, if any.
        """
        _logger.critical("Storing %s", uid)
        self.__content[uid] = content
