#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
The Isolate Composer

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
import sys

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate, Property, Validate, Invalidate, BindField

# Pelix remote services
import pelix.constants
import pelix.remote

# Composer
import cohorte
import cohorte.composer
import cohorte.composer.beans as beans
from cohorte.repositories.beans import Version

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

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

        # Bundle context
        self._context = None

        # Remaining components
        self._remaining = set()
        self.__lock = threading.RLock()

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context
        self._node_uid = context.get_property(cohorte.PROP_NODE_UID)
        self._node_name = context.get_property(cohorte.PROP_NODE_NAME)
        self._isolate_name = context.get_property(cohorte.PROP_NAME)

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._remaining.clear()

        self._context = None
        self._node_uid = None
        self._node_name = None
        self._isolate_name = None

    @BindField('_agent', if_valid=True)
    def _bind_agent(self, field, service, svc_ref):
        """
        An agent has been bound
        """
        # Tell it to handle remaining components
        service.handle(self._remaining)
        self._remaining.clear()

    def _install_bundles(self, bundles):
        """
        Installs & starts the requested bundles, if necessary

        :param bundles: A list of (name, version) tuples
        """
        # Convert to dictionaries, for easier filtering
        pre_installed = {
            bundle.get_symbolic_name(): Version(bundle.get_version())
            for bundle in self._context.get_bundles()}
        to_install = {name: Version(version) for name, version in bundles}

        for name, installed_version in pre_installed.items():
            try:
                # Check the version of the bundle indicated by components
                version = to_install[name]
                if installed_version < version:
                    _logger.warning("Using an older version of %s", name)

                # No need to install it
                del to_install[name]
            except KeyError:
                # Bundle not used here
                pass

        # Install missing bundles
        new_installed = []
        for name in to_install:
            try:
                new_installed.append(self._context.install_bundle(name))
                _logger.info("Isolate Composer installed bundle %s", name)
            except pelix.constants.BundleException as ex:
                _logger.error("Error installing bundle %s: %s", name, ex)

        # Start installed bundles
        for bundle in new_installed:
            try:
                bundle.start()
            except pelix.constants.BundleException as ex:
                _logger.error("Error starting bundle %s: %s",
                              bundle.get_symbolic_name(), ex)

    def get_isolate_uid(self):
        """
        Returns the UID of the isolate hosting this composer

        :return: An isolate UID
        """
        return self._context.get_property(cohorte.PROP_UID)

    def get_isolate_info(self):
        """
        Returns an Isolate bean corresponding to this composer

        :return: An Isolate bean
        """
        # Language
        if sys.version_info[0] >= 3:
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

            # Install required bundles
            bundles = set((component.bundle_name, component.bundle_version)
                          for component in components)
            self._install_bundles(bundles)

            if self._agent is not None:
                # Instantiate them
                self._agent.handle(components)
            else:
                # Wait for an agent to come
                self._remaining.update(components)

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
                self._remaining.difference_update(
                    component for component in self._remaining
                    if component.name in names)
