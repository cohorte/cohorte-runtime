#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top Composer: Top Commander

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
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate, BindField, UpdateField, UnbindField, Invalidate, Validate

# Standard library
import logging
import threading

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Requires('_injected_composers', cohorte.composer.SERVICE_COMMANDER_NODE,
          aggregate=True, optional=True)
@Provides(cohorte.composer.SERVICE_COMMANDER_TOP)
@Instantiate('cohorte-composer-top-commander')
class TopCommander(object):
    """
    Gives orders to the node composers
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected (for naming only)
        self._injected_composers = []

        # Node name -> NodeComposer[]
        self._node_composers = {}

        # Validation flag
        self.__validated = False

        # Lock
        self.__lock = threading.Lock()


    @BindField('_injected_composers')
    def _bind_composer(self, _, service, service_reference):
        """
        Called by iPOPO when a new composer is bound
        """
        node_name = service_reference.get_property(\
                                                cohorte.composer.PROP_NODE_NAME)
        if not node_name:
            # No node name given, ignore it
            return

        with self.__lock:
            self._node_composers.setdefault(node_name, []).append(service)


    @UpdateField('_injected_composers')
    def _update_composer(self, field, service, service_reference, old_properties):
        """
        Called by iPOPO when the properties of a bound composer changed
        """
        old_name = old_properties.get(cohorte.composer.PROP_NODE_NAME)
        new_name = service_reference.get_property(\
                                                cohorte.composer.PROP_NODE_NAME)
        if old_name == new_name:
            # Nothing to do
            return

        if not old_name:
            # Previously ignored
            self._bind_composer(field, service, service_reference)

        elif not new_name:
            # Now ignored
            self._unbind_composer(field, service, service_reference)

        else:
            # Changed node name
            self._unbind_composer(field, service, service_reference)
            self._bind_composer(field, service, service_reference)


    @UnbindField('_injected_composers')
    def _unbind_composer(self, _, service, service_reference):
        """
        Called by iPOPO when a bound composer is gone
        """
        node_name = service_reference.get_property(\
                                                cohorte.composer.PROP_NODE_NAME)
        if not node_name:
            # No node name given, ignore it
            return

        with self.__lock:
            # Remove the service from the lists
            node_composers = self._node_composers[node_name]
            node_composers.remove(service)

            if not node_composers:
                # Clean up the dictionary if necessary
                del self._node_composers[node_name]


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.__validated = False


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self.__validated = True


    def __call_for_node(self, node_name, per_composer_method, components):
        """
        Calls the given method for all composers of the given the node
        """
        for composer in self._node_composers[node_name]:
            try:
                per_composer_method(composer, components)

            except Exception as ex:
                _logger.exception("Error calling composer on node %s: %s",
                                  node_name, ex)


    def __start(self, composer, components):
        """
        Tells the given composer to start the given components

        :param composer: The node composer to call
        :param components: The composer to start
        """
        composer.instantiate(components)


    def __stop(self, composer, components):
        """
        Tells the given composer to stop all of the given components

        :param composer: The node composer to call
        :param components: The composer to stop
        """
        composer.kill(components)


    def start(self, distribution):
        """
        Starts the given distribution

        :param distribution: A Node -> RawComponent[] dictionary
        """
        for node, components in distribution.items():
            try:
                self.__call_for_node(node, self.__start, components)

            except KeyError:
                # Unknown node
                pass


    def update(self, new, moved, stopped):
        """
        Updates the distribution of the given components

        :param new: A Node -> RawComponent[] dictionary, the distribution of the
                    additional components
        :param moved: A RawComponent -> (OldNode, NewNode) dictionary
        :param stopped: A Node -> RawComponent[] dictionary
        """
        # Stop components
        self.stop(stopped)

        # Move components
        for component, nodes in moved.items():
            # 1. stop the old one
            components = [component]
            self.__call_for_node(nodes[0], self.__stop, components)

            # 2. start the new one
            self.__call_for_node(nodes[1], self.__start, components)

        # Start new components
        self.start(new)


    def stop(self, distribution):
        """
        Stops the given distribution

        :param distribution: A Node -> RawComponent[] dictionary
        """
        for node, components in distribution.items():
            try:
                self.__call_for_node(node, self.__stop, components)

            except KeyError:
                # Unknown node
                pass
