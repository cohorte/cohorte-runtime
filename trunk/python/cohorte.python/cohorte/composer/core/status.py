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
    Composer core status
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Agent UID -> State
        self._agents = {}

        # Composition UID -> Composition
        self._composition = {}

        # Composites UID -> State
        self._composites = {}

        # Component UID -> State
        self._components = {}


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


    def composite_event(self, uid, event):
        """
        Changes the state of a composite
        
        :param uid: UID of the composite
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


    def add_composition(self, composition):
        """
        Stores a composition in the status
        
        :param composition: A composition bean
        """
        # Store the composition
        self._composition[composition.uid] = composition

        # Store its composites and components
        self.__store_composite(composition.root)


    def remove_composition(self, composition):
        """
        Removes a composition from the status
        
        :param composition: A composition bean
        :raise KeyError: Unknown composition
        """
        del self._composition[composition.uid]
        self.__remove_composite(composition.root)


    def __store_composite(self, composite):
        """
        Recursively stores the composite and its children composites in the
        status
        
        :param composite: A composite
        """
        # Store the composite itself
        self.composite_requested(composite)

        # Store its composites
        for child in composite.composites:
            self.__store_composite(child)


    def __remove_composite(self, composite):
        """
        Recursively removes the composite from the status
        
        :param composite: A composite
        """
        # Remove the composite itself
        del self._composites[composite.uid]

        # Remove its composites
        for child in composite.composites:
            self.__remove_composite(child)


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
        # Clear storage
        self._agents.clear()
        self._components.clear()
        self._composites.clear()
        self._composition.clear()

        _logger.info("Composer status invalidated")
