#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
The Node Composer

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
import cohorte.monitor

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

class FactoriesMissing(Exception):
    """
    Some factories are missing
    """
    def __init__(self, factories):
        """
        Sets up members

        :param factories: Missing factories
        """
        self.factories = factories


    def __str__(self):
        """
        String representation
        """
        missing = '\n'.join('\t- {0}'.format(factory)
                            for factory in self.factories)
        return "Missing factories:\n{0}".format(missing)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_COMPOSER_NODE)
@Property('_node_name', cohorte.composer.PROP_NODE_NAME)
@Property('_export', pelix.remote.PROP_EXPORTED_INTERFACES, '*')
@Property('_export_name', pelix.remote.PROP_ENDPOINT_NAME,
          'composer-node-distributor')
@Requires('_finder', cohorte.composer.SERVICE_COMPONENT_FINDER)
@Requires('_distributor', cohorte.composer.SERVICE_DISTRIBUTOR_ISOLATE)
@Requires('_status', cohorte.composer.SERVICE_STATUS_NODE)
@Requires('_commander', cohorte.composer.SERVICE_COMMANDER_NODE)
@Requires('_monitor', cohorte.monitor.SERVICE_MONITOR)
@Instantiate('cohorte-composer-node')
class NodeComposer(object):
    """
    The Node Composer entry point
    """
    def __init__(self):
        """
        Sets up components
        """
        # Service properties
        self._node_name = None
        self._export = None
        self._export_name = None

        # Injected services
        self._finder = None
        self._distributor = None
        self._status = None
        self._commander = None
        self._monitor = None


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._node_name = None


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._node_name = context.get_property(cohorte.PROP_NODE)


    def _compute_bundles(self, components):
        """
        Normalizes the component beans by looking for their implementation
        bundle.

        :param components: A set of components (beans modified in-place)
        :return: A Component bean -> Bundle bean dictionary
        :raise FactoriesMissing: Some factories are missing
        """
        bundles = {}
        not_found = set()

        for component in components:
            try:
                # TODO: try to reuse existing bundles (same name)
                bundles[component] = self._finder.normalize(component)

            except ValueError:
                # Factory not found
                not_found.add(component.factory)

        if not_found:
            raise FactoriesMissing(not_found)

        return bundles


    def _compute_kind(self, isolate):
        """
        Computes the kind of an isolate according to its language

        :param isolate: An Isolate bean
        :return: The kind of isolate
        """
        language = isolate.language
        if language in ('python', 'python3'):
            return 'pelix'

        elif language == 'java':
            return 'osgi'

        else:
            return 'boot'


    def instantiate(self, components):
        """
        Instantiates the given components

        :param components: A list of RawComponent beans
        :return: Missing factories
        """
        try:
            # Compute the implementation language of the components
            bundles = self._compute_bundles(components)

        except FactoriesMissing as ex:
            _logger.error("Error computing bundles providing components: %s",
                          ex)
            return ex.factories

        # Distribute components
        isolates = self._distributor.distribute(components)

        # Generate the name of isolates
        for isolate in isolates:
            isolate.generate_name(self._node_name)

        # Store the distribution
        self._status.store(isolates)

        # Tell the monitor to start the isolates
        for isolate in isolates:
            _logger.debug("Starting isolate: %s -- kind=%s -- language=%s",
                          isolate, self._compute_kind(isolate),
                          isolate.language)

            # Compute the bundles required for this isolate
            isolate_bundles = {bundles[component]
                               for component in isolate.components}

            # Start the isolate (should be done asynchronously)
            self._monitor.start_isolate(isolate.name, self._node_name,
                                        self._compute_kind(isolate),
                                        isolate.language, 'isolate',
                                        isolate_bundles)

        # Tell the commander to start the instantiation on existing isolates
        self._commander.start(isolates)


    def kill(self, components):
        """
        Kills the given components

        :param components: A list of RawComponent beans
        """
        # Tell all isolate composers to stop their components
        self._commander.kill(components)
        self._status.remove(components)
