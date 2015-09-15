#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
The Node Composer

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
import time

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate, Property, Validate, Invalidate

# Pelix remote services
import pelix.remote
import pelix.threadpool

# Composer
import cohorte
import cohorte.composer
import cohorte.composer.node.beans as beans
import cohorte.forker
import cohorte.monitor

# Herald
import herald

# ------------------------------------------------------------------------------

# Module version
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

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
        super(FactoriesMissing, self).__init__(str(self))

    def __str__(self):
        """
        String representation
        """
        return "Missing factories:\n{0}"\
            .format('\n'.join('\t- {0}'.format(factory)
                              for factory in self.factories))

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.composer.SERVICE_COMPOSER_NODE, "_controller")
@Provides(cohorte.forker.SERVICE_WATCHER_LISTENER)
@Property('_node_uid', cohorte.composer.PROP_NODE_UID)
@Property('_node_name', cohorte.composer.PROP_NODE_NAME)
@Property('_export', pelix.remote.PROP_EXPORTED_INTERFACES,
          [cohorte.composer.SERVICE_COMPOSER_NODE])
@Property('_export_name', pelix.remote.PROP_ENDPOINT_NAME,
          'composer-node-composer')
@Requires('_finder', cohorte.composer.SERVICE_COMPONENT_FINDER)
@Requires('_distributor', cohorte.composer.SERVICE_DISTRIBUTOR_ISOLATE)
@Requires('_status', cohorte.composer.SERVICE_STATUS_NODE)
@Requires('_commander', cohorte.composer.SERVICE_COMMANDER_NODE)
@Requires('_monitor', cohorte.monitor.SERVICE_MONITOR)
@Requires('_directory', herald.SERVICE_DIRECTORY)
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
        self._node_uid = None
        self._export = None
        self._export_name = None

        # Injected services
        self._finder = None
        self._distributor = None
        self._status = None
        self._commander = None
        self._monitor = None
        self._directory = None

        # Thread to start isolates
        self._pool = pelix.threadpool.ThreadPool(
            3, logname="NodeComposer-Starter")

        # Redistribution timer
        self._delay = 120
        self._timer = None
        self._lock = threading.Lock()

        self._controller = True

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.__stop_timer()
        self._pool.stop()
        self._node_name = None
        self._node_uid = None

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._node_name = context.get_property(cohorte.PROP_NODE_NAME)
        self._node_uid = context.get_property(cohorte.PROP_NODE_UID)
        try:
            self._delay = int(context.get_property(
                cohorte.composer.node.PROP_RECOMPOSITION_DELAY))
        except (ValueError, TypeError):
            # Keep default value if given one is unreadable
            self._delay = 120

        self._pool.start()
        self._controller = True

    def set_platform_stopping(self):
        """
        Platform is stopping: stop providing the Node Composer service
        """
        self._controller = False
        # self.invalidate(None)

    def __start_timer(self, delay=None):
        """
        Starts the redistribution timer

        :param delay: Delay until the next call for redistribution
                      (default: 120)
        """
        if self._timer is not None:
            self._timer.cancel()

        if delay is None:
            delay = self._delay

        self._timer = threading.Timer(delay, self._redistribute)
        self._timer.start()

    def __stop_timer(self):
        """
        Stops the redistribution timer
        """
        if self._timer is not None:
            self._timer.cancel()
            self._timer = None

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
            except AttributeError:
                # _finder could be None when stopping the platform
                pass

        if not_found:
            raise FactoriesMissing(not_found)

        return bundles

    @staticmethod
    def _compute_kind(isolate):
        """
        Computes the kind of an isolate according to its language

        :param isolate: An Isolate bean
        :return: The kind of isolate
        """
        language = isolate.language
        if language in cohorte.composer.LANGUAGES_PYTHON:
            return 'pelix'
        elif language == cohorte.composer.LANGUAGE_JAVA:
            return 'osgi'
        else:
            return 'boot'

    def get_running_isolates(self):
        """
        Retrieves the set of isolate beans describing the current status
        """
        return self._commander.get_running_isolates()

    def _distribute(self, components, existing_isolates):
        """
        Computes the distribution of the given components into isolates

        :param components: A list of RawComponent beans
        :param existing_isolates: A list of Isolate beans, corresponding to
                                  already running isolates
        :return: A tuple (distribution, new_isolates), both parts being sets of
                 Isolate beans
        """
        # Convert existing isolates into eligible ones
        eligible_isolates = [beans.WrappedEligibleIsolate(isolate)
                             for isolate in existing_isolates]

        # Distribute components
        updated_isolates, new_isolates = self._distributor.distribute(
            components, eligible_isolates)

        # Generate the name of new isolates
        for isolate in new_isolates:
            isolate.generate_name(self._node_name)

        # Convert back to composer-level beans
        new_beans = set(isolate.to_isolate() for isolate in new_isolates)
        dist_beans = set(new_beans)

        for updated in updated_isolates:
            isolate = updated.to_isolate()
            isolate.components.update(updated.new_components)
            dist_beans.add(isolate)

        # FIXME: enhance returned tuple
        return dist_beans, new_beans

    def _start_isolate(self, isolate, bundles):
        """
        Starts the given isolate, using the monitor

        :param isolate: Isolate to start
        :param bundles: Dictionary: Component -> Bundle
        """
        _logger.debug("Starting isolate: %s -- kind=%s -- language=%s",
                      isolate, self._compute_kind(isolate),
                      isolate.language)

        # Compute the bundles required for this isolate
        isolate_bundles = {bundles[component]
                           for component in isolate.components}

        # Start the isolate (should be done asynchronously)
        self._monitor.start_isolate(
            isolate.name, self._compute_kind(isolate), isolate.language,
            'isolate', isolate_bundles)

    def instantiate(self, components):
        """
        Instantiates the given components

        :param components: A list of RawComponent beans
        :return: Missing factories
        """
        with self._lock:
            # Stop the running timer, if any
            self.__stop_timer()
            # wait already created isolates to get up.
            # TODO: should enhance this solution!
            time.sleep(5)
            try:
                # Compute the implementation language of the components
                bundles = self._compute_bundles(components)
            except FactoriesMissing as ex:
                _logger.error("Error computing bundles providing components: "
                              "%s", ex)
                return ex.factories

            # Prepare the list of existing isolates (and their languages)
            running = self.get_running_isolates()

            # Compute the distribution
            distribution, new_isolates = self._distribute(components, running)

            # Kill the previous isolates
            unused_isolates = set(running).difference(distribution)
            self.kill_isolates(isolate.name for isolate in unused_isolates
                               if not isolate.components)

            # Store the distribution
            self._status.store(distribution)

            # Tell the commander to start the instantiation on existing
            # isolates
            self._commander.start(distribution.difference(new_isolates))

            # Tell the monitor to start the new isolates.
            # The commander will send their orders once there composer will be
            # bound
            for isolate in new_isolates:
                self._pool.enqueue(self._start_isolate, isolate, bundles)

            # Schedule next redistribution
            self.__start_timer()

    def _redistribute(self):
        """
        Redistributes the components of this node
        """
        with self._lock:
            _logger.debug("!! Node Composer starts redistribution !!!")

            # Get components
            components = self._status.get_components()

            # Compute a distribution
            running = self.get_running_isolates()
            distribution, new_isolates = self._distribute(components, running)

            # Compute the differences with the current distribution
            isolates = set(distribution).difference(new_isolates)
            removed_isolates = set(running).difference(distribution)

            # Temporary lists
            extended_isolates = []
            all_to_remove = []

            for isolate in isolates:
                current_components = set(
                    self._status.get_components_for_isolate(isolate.name))

                added = set(isolate.components).difference(current_components)
                removed = current_components.difference(isolate.components)

                if added:
                    # Isolate components extended
                    extended_isolates.append(isolate)

                if removed:
                    # Some of its components have to be removed
                    all_to_remove.extend(removed)

            if not any((all_to_remove, extended_isolates, new_isolates)):
                _logger.debug("No modification to do")

                # Schedule next redistribution
                self.__start_timer()
                return

            # Incomplete distribution
            _logger.debug("Storing new distribution: %s", distribution)

            # Kill components on removed isolates
            for isolate in removed_isolates:
                self._commander.kill(isolate.components)

            if all_to_remove:
                # Kill moved components
                self._commander.kill(all_to_remove)

            # Store the new distribution
            self._status.clear()
            self._status.store(distribution)

            # Stop removed isolates
            self.kill_isolates(isolate.name for isolate in removed_isolates)

            if extended_isolates:
                # Start moved components
                self._commander.start(extended_isolates)

            # Prepare new isolates
            if new_isolates:
                # Compute the implementation language of the components
                bundles = self._compute_bundles(components)

                # Tell the monitor to start the new isolates.
                # The commander will send their orders once there composer will
                # be bound
                for isolate in new_isolates:
                    self._pool.enqueue(self._start_isolate, isolate, bundles)

            # Schedule next redistribution
            self.__start_timer()

    def kill_isolates(self, names):
        """
        Kills the isolates with the given names on the local node

        :param names: A list of isolate names
        """
        peers = self._directory.get_peers_for_node(self._node_uid)
        for peer in peers:
            if peer.name in names:
                try:
                    self._monitor.stop_isolate(peer.uid)
                except Exception as ex:
                    _logger.exception("Error stopping isolate: %s", ex)

    def kill(self, components):
        """
        Kills the given components

        :param components: A list of RawComponent beans
        """
        with self._lock:
            if self._timer is not None:
                self._timer.cancel()

            # Tell all isolate composers to stop their components
            # The commander update the status
            self._commander.kill(components)

            # Clear status
            self._status.clear()

    def handle_lost_isolate(self, uid, name):
        """
        Called by the forker when an isolate has been lost

        :param uid: Isolate UID
        :param name: Isolate name
        """
        _logger.debug("Node composer lost an isolate: %s (%s)", name, uid)

        # Notify the commander
        self._commander.isolate_lost(name)

        # Get the lost components beans
        lost = self._status.get_components_for_isolate(name)
        if not lost:
            # No known components on this isolate, ignore it
            _logger.debug("No known component in the lost isolate %s", name)
            return

        _logger.debug("We lost components: %s",
                      [component.name for component in lost])

        # Remove them from the status storage
        self._status.remove(component.name for component in lost)

        # Notify electors
        event = beans.Event(name, "isolate.lost", False)
        event.components = lost
        self._distributor.handle_event(event)

        # Recompute the clustering of the original components
        self.instantiate(lost)
