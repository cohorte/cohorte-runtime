#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: State machines definitions

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# State machine
import cohorte.utils.statemachine as statemachine

# ------------------------------------------------------------------------------

# Agent states
AGENT_STATE_REQUESTED = "REQUESTED"
AGENT_STATE_READY = "READY"
AGENT_STATE_STOPPING = "STOPPING"
AGENT_STATE_STOPPED = "STOPPED"
AGENT_STATE_GONE = "GONE"

# Agent events
AGENT_EVENT_READY = "/ready"
AGENT_EVENT_STOPPING = "/stopping"
AGENT_EVENT_STOPPED = "/stopped"
AGENT_EVENT_GONE = "/gone"

def make_agent_fsm(uid):
    """
    Prepares an Agent FSM.
    
    :param uid: UID of the isolate hosting the agent
    """
    fsm = statemachine.StateMachine("Agent-{0}".format(uid), uid)

    # Add states
    for name in (AGENT_STATE_REQUESTED, AGENT_STATE_READY, AGENT_STATE_STOPPING,
                 AGENT_STATE_STOPPED, AGENT_STATE_GONE):
        fsm.add_state(name)

    # Add transitions
    fsm.add_transition(AGENT_STATE_REQUESTED, AGENT_EVENT_READY,
                       AGENT_STATE_READY)
    fsm.add_transition(AGENT_STATE_REQUESTED, AGENT_EVENT_GONE,
                       AGENT_STATE_GONE)

    fsm.add_transition(AGENT_STATE_READY, AGENT_EVENT_STOPPING,
                       AGENT_STATE_STOPPING)
    fsm.add_transition(AGENT_STATE_READY, AGENT_EVENT_GONE,
                       AGENT_STATE_GONE)

    fsm.add_transition(AGENT_EVENT_STOPPING, AGENT_EVENT_STOPPED,
                       AGENT_STATE_STOPPED)
    fsm.add_transition(AGENT_EVENT_STOPPING, AGENT_EVENT_GONE,
                       AGENT_STATE_GONE)

    fsm.add_transition(AGENT_EVENT_STOPPED, AGENT_EVENT_READY,
                       AGENT_STATE_READY)
    fsm.add_transition(AGENT_EVENT_STOPPED, AGENT_EVENT_GONE,
                       AGENT_STATE_GONE)

    # Start state
    fsm.set_start(AGENT_STATE_REQUESTED)
    return fsm

# ------------------------------------------------------------------------------

# Component states
COMPONENT_STATE_PARSED = "PARSED"
COMPONENT_STATE_ASSIGNED = "ASSIGNED"
COMPONENT_STATE_RESQUESTED = "RESQUESTED"
COMPONENT_STATE_INSTANTIATED = "INSTANTIATED"
COMPONENT_STATE_VALID = "VALID"

# Component events
COMPONENT_EVENT_ASSIGNED = "/assigned"
COMPONENT_EVENT_REQUESTED = "/requested"
COMPONENT_EVENT_INSTANTIATED = "/instantiated"
COMPONENT_EVENT_VALIDATED = "/validated"
COMPONENT_EVENT_INVALIDATED = "/invalidated"
COMPONENT_EVENT_GONE = "/gone"

def make_component_fsm(component):
    """
    Prepares an Component FSM.
    
    :param component: A component bean
    """
    fsm = statemachine.StateMachine("Component-{0}".format(component.uid),
                                    component)

    # Add states
    for name in (COMPONENT_STATE_PARSED, COMPONENT_STATE_ASSIGNED,
                 COMPONENT_STATE_RESQUESTED, COMPONENT_STATE_INSTANTIATED,
                 COMPONENT_STATE_VALID):
        fsm.add_state(name)

    # Add transitions
    fsm.add_transition(COMPONENT_STATE_PARSED, COMPONENT_EVENT_ASSIGNED,
                       COMPONENT_STATE_ASSIGNED)

    fsm.add_transition(COMPONENT_STATE_ASSIGNED, COMPONENT_EVENT_REQUESTED,
                       COMPONENT_STATE_RESQUESTED)

    fsm.add_transition(COMPONENT_STATE_RESQUESTED, COMPONENT_EVENT_INSTANTIATED,
                       COMPONENT_STATE_INSTANTIATED)
    fsm.add_transition(COMPONENT_STATE_RESQUESTED, COMPONENT_EVENT_GONE,
                       COMPONENT_STATE_PARSED)

    fsm.add_transition(COMPONENT_STATE_INSTANTIATED, COMPONENT_EVENT_VALIDATED,
                       COMPONENT_STATE_VALID)
    fsm.add_transition(COMPONENT_STATE_INSTANTIATED, COMPONENT_EVENT_GONE,
                       COMPONENT_STATE_PARSED)

    fsm.add_transition(COMPONENT_STATE_VALID, COMPONENT_EVENT_INVALIDATED,
                       COMPONENT_STATE_INSTANTIATED)
    fsm.add_transition(COMPONENT_STATE_VALID, COMPONENT_EVENT_GONE,
                       COMPONENT_STATE_PARSED)

    # Start state
    fsm.set_start(COMPONENT_STATE_PARSED)
    return fsm

# ------------------------------------------------------------------------------

# Composite states
COMPOSITE_STATE_PARSED = "PARSED"
COMPOSITE_STATE_COMPUTED = "COMPUTED"
COMPOSITE_STATE_RESQUESTED = "RESQUESTED"
COMPOSITE_STATE_RUNNING = "RUNNING"
COMPOSITE_STATE_ERROR = "ERROR"

# Composite events
COMPOSITE_EVENT_ASSIGNED = "/computed"
COMPOSITE_EVENT_REQUESTED = "/all-requested"
COMPOSITE_EVENT_INSTANTIATED = "/all-instantiated"
COMPOSITE_EVENT_ERROR = "/error"
COMPOSITE_EVENT_HANDLED = "/error/handled"
COMPOSITE_EVENT_GONE = "/gone"

def make_composite_fsm(composite):
    """
    Prepares an Composite FSM.
    
    :param composite: A composite bean
    """
    fsm = statemachine.StateMachine("Composite-{0}".format(composite.uid),
                                    composite)

    # Add states
    for name in (COMPOSITE_STATE_PARSED, COMPOSITE_STATE_COMPUTED,
                 COMPOSITE_STATE_RESQUESTED, COMPOSITE_STATE_RUNNING,
                 COMPOSITE_STATE_ERROR):
        fsm.add_state(name)

    # Add transitions
    fsm.add_transition(COMPOSITE_STATE_PARSED, COMPOSITE_EVENT_ASSIGNED,
                       COMPOSITE_STATE_COMPUTED)

    fsm.add_transition(COMPOSITE_STATE_COMPUTED, COMPOSITE_EVENT_REQUESTED,
                       COMPOSITE_STATE_RESQUESTED)

    fsm.add_transition(COMPOSITE_STATE_RESQUESTED, COMPOSITE_EVENT_INSTANTIATED,
                       COMPOSITE_STATE_RUNNING)
    fsm.add_transition(COMPOSITE_STATE_RESQUESTED, COMPOSITE_EVENT_ERROR,
                       COMPOSITE_STATE_ERROR)

    fsm.add_transition(COMPOSITE_STATE_RUNNING, COMPOSITE_EVENT_ERROR,
                       COMPOSITE_STATE_ERROR)

    fsm.add_transition(COMPOSITE_STATE_ERROR, COMPOSITE_EVENT_HANDLED,
                       COMPOSITE_STATE_PARSED)

    # Start state
    fsm.set_start(COMPOSITE_STATE_PARSED)
    return fsm
