#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: Event dispatcher

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE
import cohorte.composer.core
import cohorte.composer.core.events as events
import cohorte.monitor

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Provides, Instantiate

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-core-dispatcher-factory")
@Provides('forker.isolate_lost.listener')
@Provides('cohorte.agent.listener')
@Requires('_distributor', cohorte.composer.SERVICE_DISTRIBUTOR)
@Requires('_receiver', cohorte.SERVICE_SIGNALS_RECEIVER)
@Requires('_status', cohorte.composer.core.SERVICE_STATUS)
@Requires('_ratings', cohorte.composer.SERVICE_UPDATABLE_RATING,
          aggregate=True, optional=True)
@Instantiate('cohorte-composer-dispatcher')
class EventDispatcher(object):
    """
    Components events/signals dispatcher
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._distributor = None
        self._receiver = None
        self._status = None
        self._ratings = []


    def handle_component_event(self, isolate_uid, node, component_uid, kind):
        """
        Called by an agent when a component event occurs
        
        :param isolate_uid: Component host isolate UID
        :param node: Isolate node
        :param component_uid: Component UID
        :param kind: Kind of event
        :return: The original component bean if it needs to be redistributed,
                 else None
        """
        if kind and kind[0] == '/':
            kind = kind[1:]

        if kind in ("instantiated", "running"):
            # Use the UID of the isolate
            component = self._status.get_component(component_uid)
            component.isolate = isolate_uid

        elif kind in ("validated", "invalidated"):
            # Update status
            self._status.component_event(component_uid, "/" + kind)

        elif kind in ("gone", "lost"):
            # Component lost
            # Update status
            component = self._status.get_component(component_uid)
            self._status.remove_component(component.uid)

            # Update ratings
            event = events.BasicEvent(events.BasicEvent.COMPONENT_GONE,
                                      isolate_uid, node,
                                      {"uid": component_uid})
            self.__update_ratings(event)

            # Require a new distribution
            return component.original

        else:
            _logger.error("Unknown kind of component event: %s", kind)


    def __update_ratings(self, event):
        """
        Updates the ratings with the given event
        
        :param event: A composer core event
        """
        if self._ratings:
            for rating in self._ratings:
                rating.update(event)


    def handle_isolate_lost(self, uid, node):
        """
        Called by the forker when an isolate has been lost
        
        :param uid: Isolate UID
        :param node: Isolate node
        """
        _logger.debug('Isolate lost: %s', uid)

        # Get the lost components beans
        lost = [component for component in self._status.get_components()
                if component.isolate == uid]
        for component in lost:
            # Remove them from the status storage
            self._status.remove_component(component.uid)

        # Update ratings
        event = events.BasicEvent(events.BasicEvent.ISOLATE_LOST, uid, node)
        self.__update_ratings(event)

        # Recompute the clustering of the original components
        self._distributor.redistribute([component.original
                                        for component in lost])


    def handle_received_signal(self, name, signal_data):
        """
        Called when a remote services signal is received
        
        :param name: Signal name
        :param signal_data: Signal content
        """
        if name == cohorte.monitor.SIGNAL_ISOLATE_LOST:
            # Isolate lost
            self.handle_isolate_lost(signal_data['signalContent'],
                                     signal_data['senderNode'])

        elif name == cohorte.composer.SIGNAL_AGENT_EVENT:
            # Agent event
            content = signal_data['signalContent']
            isolate = signal_data['senderUID']
            node = signal_data['senderNode']

            # Handle the event
            redist_components = []
            for kind, components in content.items():
                for uid in components:
                    comp = self.handle_component_event(isolate, node, uid, kind)
                    if comp is not None:
                        redist_components.append(comp)

            # Redistribute components, if needed
            if redist_components:
                self._distributor.redistribute(redist_components)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # Register to the "isolate lost" signal
        self._receiver.register_listener(cohorte.monitor.SIGNAL_ISOLATE_LOST,
                                         self)
        self._receiver.register_listener(cohorte.composer.SIGNAL_AGENT_EVENT,
                                         self)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister from signals
        self._receiver.unregister_listener(cohorte.monitor.SIGNAL_ISOLATE_LOST,
                                           self)
        self._receiver.unregister_listener(cohorte.composer.SIGNAL_AGENT_EVENT,
                                           self)
