#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top Composer: Top Commander

Gives orders to the node composer

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
import threading

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate, BindField, UpdateField, UnbindField, Invalidate, Validate

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
@Provides(cohorte.composer.SERVICE_COMMANDER_TOP)
@Requires('_status', cohorte.composer.SERVICE_STATUS_TOP)
@Requires('_injected_composers', cohorte.composer.SERVICE_COMPOSER_NODE,
          aggregate=True, optional=True)
@Instantiate('cohorte-composer-top-commander')
class TopCommander(object):
    """
    Gives orders to the node composers
    """
    def __init__(self):
        """
        Sets up members
        """
        self._status = None

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
        node_name = service_reference.get_property(
            cohorte.composer.PROP_NODE_NAME)
        if not node_name:
            # No node name given, ignore it
            return

        with self.__lock:
            self._node_composers.setdefault(node_name, []).append(service)

        if self.__validated:
            # Late composer: give it its order
            self._late_composer(node_name, service)

    @UpdateField('_injected_composers')
    def _update_composer(self, field, service, service_reference,
                         old_properties):
        """
        Called by iPOPO when the properties of a bound composer changed
        """
        old_name = old_properties.get(cohorte.composer.PROP_NODE_NAME)
        new_name = service_reference.get_property(
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
        node_name = service_reference.get_property(
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
        with self.__lock:
            self.__validated = False

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        with self.__lock:
            self.__validated = True

            # Call all bound node composers
            for node, composers in self._node_composers.items():
                for composer in composers:
                    self._late_composer(node, composer)

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

    @staticmethod
    def __start(composer, components):
        """
        Tells the given composer to start the given components

        :param composer: The node composer to call
        :param components: The composer to start
        """
        composer.instantiate(components)

    @staticmethod
    def __stop(composer, components):
        """
        Tells the given composer to stop all of the given components

        :param composer: The node composer to call
        :param components: The composer to stop
        """
        composer.kill(components)

    def _late_composer(self, node_name, composer):
        """
        Pushes orders to a newly bound composer

        :param node_name: Name of the node hosting the composer
        :param composer: The composer service
        """
        components = self._status.get_components_for_node(node_name)
        if components:
            try:
                self.__start(composer, components)
            except Exception as ex:
                _logger.exception("Error calling composer on node %s: %s",
                                  node_name, ex)

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

        :param new: A Node -> RawComponent[] dictionary, the distribution of
                    the additional components
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
