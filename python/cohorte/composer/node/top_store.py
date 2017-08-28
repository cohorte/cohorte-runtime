#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top composer distribution storage, hosted by node composers

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

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Property, \
    Instantiate

# Pelix
import pelix.remote

# Cohorte
import cohorte.composer

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

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
        _logger.info("Retrieving top composition %s", uid)
        return self.__content.get(uid)

    def remove(self, uid):
        """
        Removes the content associated to the given UID

        :param uid: A UID
        :return: True if the UID was known
        """
        try:
            _logger.info("Removing top composition %s", uid)
            del self.__content[uid]
            return True
        except KeyError:
            return False

    def store(self, uid, content):
        """
        Stores the given content, associated to the given UID. Overwrites the
        previous content, if any.
        """
        _logger.info("Storing top composition %s", uid)
        self.__content[uid] = content
