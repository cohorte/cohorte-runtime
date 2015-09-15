#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Commander; gives orders to isolate composers

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
    Property, Instantiate, BindField, UpdateField, UnbindField, \
    Invalidate, Validate

# Composer
import cohorte
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
@Provides(cohorte.composer.SERVICE_COMMANDER_NODE)
@Property('_node_uid', cohorte.composer.PROP_NODE_UID)
@Requires('_status', cohorte.composer.SERVICE_STATUS_NODE)
@Requires('_injected_composers', cohorte.composer.SERVICE_COMPOSER_ISOLATE,
          aggregate=True, optional=True)
@Instantiate('cohorte-composer-node-commander')
class NodeCommander(object):
    """
    Gives orders to the isolate composers
    """
    def __init__(self):
        """
        Sets up members
        """
        self._status = None

        # Injected services
        self._injected_composers = []

        # NodeComposer -> ServiceReference
        self._injected_composers_refs = {}

        # Isolate name -> NodeComposer
        self._isolate_composer = {}

        # Thread protection
        self._lock = threading.RLock()

        # Validation flag
        self.__validated = False

        # Node UID
        self._node_uid = None

    @BindField('_injected_composers')
    def _bind_composer(self, _, service, svc_ref):
        """
        Called by iPOPO when a new composer is bound
        """
        with self._lock:
            # Check node UID
            if not self.__check_node(svc_ref):
                # Different node: ignore this isolate composer
                return

            # Store the service reference
            self._injected_composers_refs[service] = svc_ref

            if self.__validated:
                # Handle the new composer
                self.__handle_composer(svc_ref, service)

    @UpdateField('_injected_composers')
    def _update_composer(self, field, service, svc_ref, old_properties):
        """
        Called by iPOPO when the properties of a bound composer changed
        """
        if not self.__validated:
            # Do nothing if not in a valid state
            return

        with self._lock:
            # Check node UID
            if not self.__check_node(svc_ref):
                # Different node: ignore this isolate composer
                return

            old_name = old_properties.get(cohorte.composer.PROP_ISOLATE_NAME)
            new_name = svc_ref.get_property(cohorte.composer.PROP_ISOLATE_NAME)
            if old_name == new_name:
                # Nothing to do
                return

            if not old_name:
                # Previously ignored
                self._bind_composer(field, service, svc_ref)
            elif not new_name:
                # Now ignored
                self._unbind_composer(field, service, svc_ref)
            else:
                # Changed node name
                self._unbind_composer(field, service, svc_ref)
                self._bind_composer(field, service, svc_ref)

    @UnbindField('_injected_composers')
    def _unbind_composer(self, _, service, svc_ref):
        """
        Called by iPOPO when a bound composer is gone
        """
        with self._lock:
            try:
                # Remove its reference
                del self._injected_composers_refs[service]
            except KeyError:
                # Already removed by isolate_lost()
                return

            # Get its name
            name = svc_ref.get_property(cohorte.composer.PROP_ISOLATE_NAME)
            if not name:
                # No node name given, ignore it
                return

            # Check if the name is the one we expect
            if self._isolate_composer[name] is service:
                # Forget this composer
                del self._isolate_composer[name]

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.__validated = False
        self._node_uid = None

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self.__validated = True
        self._node_uid = context.get_property(cohorte.PROP_NODE_UID)

        # Handle already injected services
        for service, svc_ref in self._injected_composers_refs.items():
            self.__handle_composer(svc_ref, service)

    def __check_node(self, svc_ref):
        """
        Compares the node UID of the given service reference with the local one

        :param svc_ref: A service reference
        :return: True if the given service is on the local node
        """
        return self._node_uid == \
            svc_ref.get_property(cohorte.composer.PROP_NODE_UID)

    def __handle_composer(self, svc_ref, composer):
        """
        Stores the given composer into the isolate -> composer dictionary.
        Does nothing if the composer node doesn't match our host

        :param svc_ref: Isolate Composer service reference
        :param composer: Isolate Composer service
        """
        # Check node UID
        if not self.__check_node(svc_ref):
            # Different node: ignore this isolate composer
            return

        isolate_name = svc_ref.get_property(cohorte.composer.PROP_ISOLATE_NAME)
        if not isolate_name:
            # No node name given, ignore it
            return

        # Store the composer according to its name
        self._isolate_composer[isolate_name] = composer

        # Give it its orders
        self.__push_orders(isolate_name, composer)

    def __push_orders(self, isolate_name, composer):
        """
        Pushes orders to a newly bound composer

        :param isolate_name: Name of the isolate hosting the composer
        :param composer: The composer service
        """
        components = self._status.get_components_for_isolate(isolate_name)
        if components:
            try:
                composer.instantiate(components)
            except Exception as ex:
                _logger.exception("Error calling composer on isolate %s: %s",
                                  isolate_name, ex)

    def isolate_lost(self, name):
        """
        An isolate has been lost: remove it from the internal lists

        :param name: Name of the lost isolate
        """
        with self._lock:
            try:
                # Remove its references
                service = self._isolate_composer.pop(name)
                del self._injected_composers_refs[service]
            except KeyError:
                _logger.debug("No composer associated to isolate %s", name)

    def get_running_isolates(self):
        """
        Returns the list of running isolates

        :return: A set of isolate beans
        """
        with self._lock:
            composers = list(self._isolate_composer.values())

        isolates = set()
        for composer in composers:
            try:
                # Request the description of the composer
                isolate_info = composer.get_isolate_info()
            except Exception as ex:
                # Something went wrong
                _logger.error("Error retrieving information about a composer: "
                              "%s", ex)
            else:
                # Type enforcement
                isolate_info.components = set(isolate_info.components)
                isolates.add(isolate_info)

        return isolates

    def start(self, isolates):
        """
        Starts the given distribution

        :param isolates: A set of Isolate beans
        """
        for isolate in isolates:
            try:
                # Try to call the bound composer
                self._isolate_composer[isolate.name] \
                    .instantiate(isolate.components)
            except KeyError:
                # Unknown node
                pass
            except Exception as ex:
                # Error calling the composer
                _logger.exception("Error calling isolate %s: %s", isolate, ex)

    def kill(self, components):
        """
        Stops the given components

        :param components: A set of RawComponent beans
        """
        # Compute a dictionary: isolate -> component names
        distribution = {}
        for component in components:
            try:
                name = component.name
                isolate = self._status.get_isolate_for_component(name)
                distribution.setdefault(isolate, set()).add(name)
            except KeyError:
                # Component has not been bound...
                pass

        # Call the composer
        for isolate, names in distribution.items():
            try:
                # Get the service
                composer = self._isolate_composer[isolate]
            except KeyError:
                _logger.error("No composer for isolate %s", isolate)
            else:
                try:
                    # Call it
                    composer.kill(names)

                    # Update the status
                    self._status.remove(names)
                except Exception as ex:
                    _logger.exception("Error calling composer on %s: %s",
                                      isolate, ex)
