#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Node status storage

Simply stores the components associated to local isolates

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
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Requires

# Composer
import cohorte.composer

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.composer.SERVICE_STATUS_NODE)
@Requires('_history', cohorte.composer.SERVICE_HISTORY_NODE, optional=True)
@Instantiate('cohorte-composer-node-status')
class NodeStatusStorage(object):
    """
    Associates components to their hosting isolate
    """
    def __init__(self):
        """
        Sets up members
        """
        # History service
        self._history = None

        # Component name -> Isolate name
        self._component_isolate = {}

        # Component name -> RawComponent
        self._components = {}

        # Isolate name -> set(RawComponent)
        self._isolate_components = {}

        # Safety...
        self.__lock = threading.Lock()

    def clear(self):
        """
        Clears the status of all of its information
        """
        with self.__lock:
            self._component_isolate.clear()
            self._components.clear()
            self._isolate_components.clear()

    def dump(self):
        """
        Dumps the content of the storage
        """
        with self.__lock:
            lines = ['Isolates:']
            for isolate, components in self._isolate_components.items():
                lines.append('\t- {0}'.format(isolate))
                lines.extend('\t\t- {0}'.format(component)
                             for component in components)

            return '\n'.join(lines)

    def store(self, isolates):
        """
        Updates the storage with the given isolate distribution

        :param isolates: A set of Isolate beans
        """
        with self.__lock:
            for isolate in isolates:
                # Isolate name -> Components
                isolate_name = isolate.name
                self._isolate_components.setdefault(isolate_name, set()) \
                    .update(isolate.components)

                # Component name -> Isolate name / RawComponent
                for component in isolate.components:
                    component_name = component.name
                    self._component_isolate[component_name] = isolate_name
                    self._components[component_name] = component

            # Store the distribution in history
            if self._history is not None:
                # Make a complete dictionary
                distribution = {
                    isolate_name: sorted(component.name
                                         for component in components)
                    for isolate_name, components
                    in self._isolate_components.copy().items()}

                self._history.store(distribution)

    def remove(self, names):
        """
        Removes the given components from the storage

        :param names: A set of names of components
        """
        with self.__lock:
            for name in names:
                try:
                    # Remove from the component from the lists
                    isolate = self._component_isolate.pop(name)
                    component = self._components.pop(name)

                    isolate_components = self._isolate_components[isolate]
                    isolate_components.remove(component)
                    if not isolate_components:
                        # No more component on this isolate
                        del self._isolate_components[isolate]
                except KeyError:
                    _logger.warning("Unknown component: %s", name)

    def get_isolates(self):
        """
        Returns the list of names of known isolates

        :return: A list of isolate names
        """
        return sorted(self._isolate_components.keys())

    def get_components(self):
        """
        Returns the list of all known components

        :return: A list of RawComponent beans
        """
        return list(self._components.values())

    def get_components_for_isolate(self, isolate_name):
        """
        Retrieves the components assigned to the given node

        :param isolate_name: The name of an isolate
        :return: The set of RawComponent beans associated to the isolate,
                 or an empty set
        """
        # Return a copy
        return self._isolate_components.get(isolate_name, set()).copy()

    def get_isolate_for_component(self, component_name):
        """
        Retrieves the isolate that must host the given component

        :param component_name: Name of a component
        :return: The name of the isolate that must host it
        :raise KeyError: Unknown component
        """
        return self._component_isolate[component_name]

    def neighbours(self, components):
        """
        Checks if the given components are in the same isolate

        :param components: A list of components names
        """
        if not components:
            return False

        try:
            isolate = self._component_isolate[components[0]]
            return components in self._isolate_components[isolate]
        except KeyError:
            # Unknown isolate/components
            return False
