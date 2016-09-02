#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Monitor: State machines definitions

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

# State machine
from cohorte.utils.statemachine import StateMachine

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

# Isolate states
ISOLATE_STATE_WAITING = "WAITING"
ISOLATE_STATE_REQUESTED = "REQUESTED"
ISOLATE_STATE_STARTING = "STARTING"
ISOLATE_STATE_READY = "READY"
ISOLATE_STATE_STOPPING = "STOPPING"
ISOLATE_STATE_GONE = "GONE"

# Isolate events
ISOLATE_EVENT_REQUESTED = "/requested"
ISOLATE_EVENT_STARTING = "/starting"
ISOLATE_EVENT_READY = "/ready"
ISOLATE_EVENT_STOPPING = "/stopping"
ISOLATE_EVENT_GONE = "/gone"


def make_isolate_fsm(uid):
    """
    Prepares an isolate FSM.

    :param uid: UID of the isolate
    """
    fsm = StateMachine("Agent-{0}".format(uid), uid)

    # Add states
    for name in (ISOLATE_STATE_WAITING, ISOLATE_STATE_REQUESTED,
                 ISOLATE_STATE_STARTING, ISOLATE_STATE_READY,
                 ISOLATE_STATE_STOPPING, ISOLATE_STATE_GONE):
        fsm.add_state(name)

    # Add transitions
    fsm.add_transition(ISOLATE_STATE_WAITING, ISOLATE_EVENT_REQUESTED,
                       ISOLATE_STATE_REQUESTED)

    # Failed instantiation request
    fsm.add_transition(ISOLATE_STATE_REQUESTED, ISOLATE_EVENT_GONE,
                       ISOLATE_STATE_WAITING)

    # Forker request succeeded
    fsm.add_transition(ISOLATE_STATE_REQUESTED, ISOLATE_EVENT_STARTING,
                       ISOLATE_STATE_STARTING)

    fsm.add_transition(ISOLATE_STATE_STARTING, ISOLATE_EVENT_READY,
                       ISOLATE_STATE_READY)
    fsm.add_transition(ISOLATE_STATE_STARTING, ISOLATE_EVENT_GONE,
                       ISOLATE_STATE_GONE)

    fsm.add_transition(ISOLATE_STATE_READY, ISOLATE_EVENT_STOPPING,
                       ISOLATE_STATE_STOPPING)
    fsm.add_transition(ISOLATE_STATE_READY, ISOLATE_EVENT_GONE,
                       ISOLATE_STATE_GONE)

    fsm.add_transition(ISOLATE_STATE_STOPPING, ISOLATE_EVENT_GONE,
                       ISOLATE_STATE_GONE)

    # Ugly trick : on fast systems, the isolate can be ready before the forker
    # returns a result
    fsm.add_transition(ISOLATE_STATE_REQUESTED, ISOLATE_EVENT_READY,
                       ISOLATE_STATE_READY)
    fsm.add_transition(ISOLATE_STATE_READY, ISOLATE_EVENT_STARTING,
                       ISOLATE_STATE_READY)
    fsm.add_transition(ISOLATE_STATE_READY, ISOLATE_EVENT_READY,
                       ISOLATE_STATE_READY)

    fsm.add_transition(ISOLATE_STATE_STOPPING, ISOLATE_EVENT_STOPPING,
                       ISOLATE_STATE_STOPPING)
    fsm.add_transition(ISOLATE_STATE_GONE, ISOLATE_EVENT_GONE,
                       ISOLATE_STATE_GONE)

    # Start state
    fsm.set_start(ISOLATE_STATE_WAITING)
    return fsm
