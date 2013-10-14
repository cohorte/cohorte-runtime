#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Node Commander

Gives orders to the isolate composer

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
import cohorte.composer.isolate.beans as beans

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Property, Instantiate, BindField, UpdateField, UnbindField, \
    Invalidate, Validate

# Pelix
import pelix.remote

# Standard library
import logging
import threading

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_COMMANDER_NODE)
@Property('_export', pelix.remote.PROP_EXPORTED_INTERFACES, '*')
@Property('_export_name', pelix.remote.PROP_ENDPOINT_NAME,
          'composer-node-commander')
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

        # Injected (for naming only)
        self._injected_composers = []

        # Isolate name -> NodeComposer
        self._isolate_composer = {}

        # Export flags
        self._export = None
        self._export_name = None

        # Validation flag
        self.__validated = False

        # Lock
        self.__lock = threading.Lock()


    @BindField('_injected_composers')
    def _bind_composer(self, _, service, service_reference):
        """
        Called by iPOPO when a new composer is bound
        """
        isolate_name = service_reference.get_property(\
                                            cohorte.composer.PROP_ISOLATE_NAME)
        if not isolate_name:
            # No node name given, ignore it
            return

        with self.__lock:
            self._isolate_composer[isolate_name] = service

        if self.__validated:
            # Late composer: give it its order
            self._late_composer(isolate_name, service)


    @UpdateField('_injected_composers')
    def _update_composer(self, field, service, service_reference,
                         old_properties):
        """
        Called by iPOPO when the properties of a bound composer changed
        """
        old_name = old_properties.get(cohorte.composer.PROP_ISOLATE_NAME)
        new_name = service_reference.get_property(\
                                            cohorte.composer.PROP_ISOLATE_NAME)
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
        isolate_name = service_reference.get_property(\
                                            cohorte.composer.PROP_ISOLATE_NAME)
        if not isolate_name:
            # No node name given, ignore it
            return

        with self.__lock:
            # Forget the composer
            del self._isolate_composer[isolate_name]


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

            # Call all bound isolate composers
            for isolate, composer in self._isolate_composer.items():
                self._late_composer(isolate, composer)


    def __start(self, composer, components):
        """
        Tells the given composer to start the given components

        :param composer: The node composer to call
        :param components: The composer to start
        """
        # Send converted beans
        composer.instantiate({beans.IsolateComponent().from_raw(component)
                              for component in components})


    def __stop(self, composer, components):
        """
        Tells the given composer to stop all of the given components

        :param composer: The node composer to call
        :param components: The composer to stop
        """
        composer.kill({component.name for component in components})


    def _late_composer(self, isolate_name, composer):
        """
        Pushes orders to a newly bound composer

        :param isolate_name: Name of the isolate hosting the composer
        :param composer: The composer service
        """
        components = self._status.get_components_for_isolate(isolate_name)
        if components:
            try:
                self.__start(composer, components)

            except Exception as ex:
                _logger.exception("Error calling composer on isolate %s: %s",
                                  isolate_name, ex)


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
        pass
