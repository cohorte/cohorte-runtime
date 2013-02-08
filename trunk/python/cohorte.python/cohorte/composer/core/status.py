#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: Composer status


:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# Composer core
import cohorte.composer.core
import cohorte.composer.core.fsm as fsm_creator

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-core-status-factory")
@Provides(cohorte.composer.core.SERVICE_STATUS)
class ComposerStatus(object):
    """
    Composer core action executor
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Agent UID -> State
        self._agents = {}

        # Component UID -> State
        self._components = {}

        # Composites UID -> State
        self._composites = {}


    def agent_event(self, uid, event):
        """
        Changes the state of an agent
        
        :param uid: UID of the isolate hosting the agent
        :param event: A transition event
        """
        self._agents[uid].handle(event)


    def component_event(self, uid, event):
        """
        Changes the state of a component
        
        :param uid: UID of the component
        :param event: A transition event
        """
        self._components[uid].handle(event)


    def composition_event(self, uid, event):
        """
        Changes the state of a component
        
        :param uid: UID of the composition
        :param event: A transition event
        """
        self._compositions[uid].handle(event)


    def agent_requested(self, uid):
        """
        A new agent has been requested
        
        :param uid: UID of the isolate hosting the agent
        """
        if uid in self._agents:
            _logger.error("Already known agent: %s", uid)
            return False

        # Prepare the agent FSM
        self._agents[uid] = fsm_creator.make_agent_fsm(uid)
        return uid


    def component_requested(self, component):
        """
        A new component has been requested
        
        :param component: A component bean
        """
        uid = component.uid
        if uid in self._components:
            _logger.error("Already known component: %s", component)
            return False

        # Prepare the component FSM
        self._components[uid] = fsm_creator.make_component_fsm(component)
        return uid


    def composite_requested(self, composite):
        """
        A new component has been requested
        
        :param component: A component bean
        """
        uid = composite.uid
        if uid in self._composites:
            _logger.error("Already known composite: %s", composite)
            return False

        # Prepare the component FSM
        self._composites[uid] = fsm_creator.make_composite_fsm(composite)
        return uid


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        _logger.info("Composer status validated")


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        _logger.info("Composer status invalidated")
