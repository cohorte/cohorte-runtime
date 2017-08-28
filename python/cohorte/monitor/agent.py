#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Monitor: Isolate Agent

To instantiate in every Python isolate

:author: Thomas Calmant
:license: Apache Software License 2.0

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
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Provides, Property

# Cohorte & Herald
import cohorte
import cohorte.monitor
import herald
import herald.exceptions
import herald.beans as beans

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory("cohorte-isolate-agent-factory")
@Requires('_sender', herald.SERVICE_HERALD)
@Requires('_directory', herald.SERVICE_DIRECTORY)
@Provides((herald.SERVICE_LISTENER, herald.SERVICE_DIRECTORY_LISTENER))
@Property('_filters', herald.PROP_FILTERS,
          [cohorte.monitor.SIGNAL_STOP_ISOLATE])
class IsolateAgent(object):
    """
    Isolate agent component
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Injected services
        self._sender = None
        self._directory = None

        # Node UID
        self.__node_uid = None

        # Bundle context
        self._context = None

    def herald_message(self, herald_svc, message):
        """
        Handles a signal

        :param herald_svc: The Herald service
        :param message: The received message bean
        """
        if message.subject == cohorte.monitor.SIGNAL_STOP_ISOLATE:
            # Isolate must stop
            # Let the method return first,
            # in order to return to the caller immediately
            threading.Thread(name="monitor-agent-stop",
                             target=self.stop).start()

    def stop(self):
        """
        Stops the whole isolate
        """
        # Send the "isolate stopping" signal
        _logger.warning(">>> Isolate will stop <<<")
        message = beans.Message(cohorte.monitor.SIGNAL_ISOLATE_STOPPING,
                                self._context.get_property(cohorte.PROP_UID))
        self._sender.fire_group('all', message)

        _logger.warning(">>> STOPPING isolate <<<")
        self._context.get_bundle(0).stop()

    def peer_registered(self, peer):
        """
        A new Herald directory group has been set.

        Sends the "Ready" message to monitors, as soon as one of their peers
        has been detected
        """
        if peer.node_uid == self.__node_uid \
                and peer.name == cohorte.FORKER_NAME:
            # Send the "ready" signal
            try:
                self._sender.fire(
                    peer, beans.Message(cohorte.monitor.SIGNAL_ISOLATE_READY))
            except herald.exceptions.NoTransport as ex:
                _logger.error("No transport to notify monitors: %s", ex)
            else:
                _logger.info("Monitors notified of isolate readiness")

    @staticmethod
    def peer_updated(peer, access_id, data, previous):
        """
        A peer has been updated: nothing to do
        """
        pass

    @staticmethod
    def peer_unregistered(peer):
        """
        A peer has been unregistered: nothing to do
        """
        pass

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # Store the context
        self._context = context

        # Store local node UID
        self.__node_uid = self._directory.get_local_peer().node_uid
        _logger.info("Isolate agent validated")

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        try:
            # Send the stopping signal
            message = beans.Message(cohorte.monitor.SIGNAL_ISOLATE_STOPPING)
            self._sender.fire_group('monitors', message)
        except:
            _logger.info("Herald transports are not bound.")

        # Clear the context
        self._context = None
        self.__node_uid = None
        _logger.info("Isolate agent invalidated")
