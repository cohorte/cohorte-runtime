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

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate, Property, Validate, Invalidate

# Pelix remote services
import pelix.remote

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_COMPOSER_ISOLATE)
@Property('_node_name', cohorte.composer.PROP_NODE_NAME)
@Property('_isolate_name', cohorte.composer.PROP_ISOLATE_NAME)
@Property('_export', pelix.remote.PROP_EXPORTED_INTERFACES, '*')
@Property('_export_name', pelix.remote.PROP_ENDPOINT_NAME,
          'composer-isolate-composer')
@Requires('_agents', cohorte.composer.SERVICE_AGENT_ISOLATE,
          aggregate=True, optional=True)
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
        self._node_name = None
        self._isolate_name = None
        self._export = None
        self._export_name = None

        # Injected services
        self._agents = []
        self._status = None


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._node_name = context.get_property(cohorte.PROP_NODE)
        self._isolate_name = context.get_property(cohorte.PROP_NAME)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._node_name = None
        self._isolate_name = None


    def instantiate(self, components):
        """
        """
        from pprint import pformat
        _logger.debug("Must instantiate: %s", pformat(components))
