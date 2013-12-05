#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
The Isolate Composer

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
import cohorte.composer.beans as beans

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate, Property, Validate, Invalidate, BindField

# Pelix remote services
import pelix.remote

# Standard library
import logging
import threading
import sys

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_COMPOSER_ISOLATE)
@Property('_node_uid', cohorte.composer.PROP_NODE_UID)
@Property('_node_name', cohorte.composer.PROP_NODE_NAME)
@Property('_isolate_name', cohorte.composer.PROP_ISOLATE_NAME)
@Property('_export', pelix.remote.PROP_EXPORTED_INTERFACES, '*')
@Property('_export_name', pelix.remote.PROP_ENDPOINT_NAME,
          'composer-isolate-composer')
@Requires('_agent', cohorte.composer.SERVICE_AGENT_ISOLATE, optional=True)
@Requires('_status', cohorte.composer.SERVICE_STATUS_ISOLATE)
@Instantiate('cohorte-composer-isolate')
class IsolateComposer(object):
    """
    The Isolate Composer entry point
    """
    def __init__(self):
        """
        Sets up components
        """
        # Service properties
        self._node_uid = None
        self._node_name = None
        self._isolate_name = None
        self._export = None
        self._export_name = None

        # Injected services
        self._agent = None
        self._status = None

        # Remaining components
        self._remaining = set()
        self.__lock = threading.RLock()
        self.__validated = False


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._node_uid = context.get_property(cohorte.PROP_NODE_UID)
        self._node_name = context.get_property(cohorte.PROP_NODE_NAME)
        self._isolate_name = context.get_property(cohorte.PROP_NAME)
        self.__validated = True


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.__validated = False
        self._remaining.clear()

        self._node_uid = None
        self._node_name = None
        self._isolate_name = None


    @BindField('_agent')
    def _bind_agent(self, field, service, svc_ref):
        """
        An agent has been bound
        """
        with self.__lock:
            if self.__validated and self._agent is not None:
                # Tell it to handle remaining components
                self._agent.handle(self._remaining)
                self._remaining.clear()


    def get_isolate_info(self):
        """
        Returns an Isolate bean corresponding to this composer

        :return: An Isolate bean
        """
        # Language
        if sys.version_info[0] == 3:
            language = cohorte.composer.LANGUAGE_PYTHON3
        else:
            language = cohorte.composer.LANGUAGE_PYTHON

        # Make the bean
        return beans.Isolate(self._isolate_name, language,
                             self._status.get_components())


    def instantiate(self, components):
        """
        Instantiates the given components

        :param components: A set of RawComponent beans
        """
        with self.__lock:
            # Store the new components
            self._status.store(components)

            if self._agent is not None:
                # Instantiate them
                self._agent.handle(components)
            else:
                # Wait for an agent to come
                self._remaning.update(components)


    def kill(self, names):
        """
        Kills the components with the given names

        :param names: Names of the components to kill
        """
        with self.__lock:
            # Update the status storage
            self._status.remove(names)

            if self._agent is not None:
                # An agent can kill the components
                for name in names:
                    try:
                        # Kill the component
                        self._agent.kill(name)

                    except ValueError:
                        # Unknown component
                        pass

            else:
                # Update the remaining components
                self._remaining.difference_update(component
                                                for component in self._remaining
                                                if component.name in names)
