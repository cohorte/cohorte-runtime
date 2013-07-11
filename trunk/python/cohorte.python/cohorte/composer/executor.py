#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: Agent order sender

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
import cohorte.composer.core.fsm as fsm

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    BindField, Invalidate, Instantiate

# Standard library
import logging
import threading

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory('cohorte-composer-executor-factory')
@Requires('_agents', 'cohorte.composer.Agent', aggregate=True, optional=True)
@Requires('_status', cohorte.composer.core.SERVICE_STATUS)
@Instantiate('cohorte-composer-executor')
class Executor(object):
    """
    Agent order sender
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._status = None
        self._agents = []

        # Lock between validation and bound agent
        self._lock = threading.Lock()
        self._validated = False


    @BindField('_agents')
    def bind_agent(self, field, service, svc_ref):
        """
        Called when an Agent service is bound
        """
        with self._lock:
            if self._validated:
                # Only if validated...
                self._send_orders(service)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        with self._lock:
            self._validated = True

            if self._agents:
                for agent in self._agents:
                    self._send_orders(agent)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        with self._lock:
            self._validated = False


    def _send_orders(self, agent):
        """
        Sends its orders to the given agent
        
        :param agent: An agent service
        """
        try:
            # Get the agent isolate UID
            uid, name = agent.get_isolate()
            _logger.debug("Sending orders to composer agent - %s (%s)",
                          name, uid)

            # Get the list of components that must be started on this isolate
            components = []
            for component in self._status.get_components(\
                                                 fsm.COMPONENT_STATE_ASSIGNED):
                if component.isolate in (uid, name):
                    # Mark the component as requested
                    components.append(component.as_dict())
                    self._status.component_event(component.uid,
                                                 fsm.COMPONENT_EVENT_REQUESTED)

            # Send the order to the agent
            if components:
                agent.instantiate(components, True)

        except Exception as ex:
            _logger.exception("Error sending order to agent: %s - %s",
                              type(ex).__name__, ex)
