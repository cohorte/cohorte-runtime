#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Monitor: Monitor status storage (isolates finite state machines)

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

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate

# Composer core
import cohorte.monitor
import cohorte.monitor.fsm as fsm

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory("cohorte-monitor-status-factory")
@Provides(cohorte.monitor.SERVICE_STATUS)
class MonitorStatus(object):
    """
    Monitor status holder
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Isolate UID -> State
        self._isolates = {}

    def add_isolate(self, uid):
        """
        Prepares the FSM of a new isolate

        :param uid: UID of the isolate
        :return: The initial state of the isolate (waiting)
        :raise KeyError: Already known & running isolate
        """
        current_state = self._isolates.get(uid, fsm.ISOLATE_STATE_GONE)
        # Already known isolate
        if current_state != fsm.ISOLATE_STATE_GONE:
            # The isolate isn't gone
            raise KeyError("Already running isolate: {0}".format(uid))

        # Store the new FSM
        new_fsm = self._isolates[uid] = fsm.make_isolate_fsm(uid)
        _logger.debug("New isolate FSM: %s", uid)
        return new_fsm.state

    def remove_isolate(self, uid):
        """
        Removes the references to the given isolate

        :param uid: UID of the isolate
        :return: True if the isolate FSM has been deleted, else False
        """
        try:
            state = self._isolates[uid].state
            _logger.debug("Delete isolate FSM: %s - %s", uid, state)

            del self._isolates[uid]
            return True

        except KeyError:
            return False

    def isolate_requested(self, uid):
        """
        The instantiation of the isolate has been requested to a forker

        :param uid: UID of the isolate
        :raise KeyError: Unknown UID
        :raise ValueError: Invalid state transition
        """
        return self._isolates[uid].handle(fsm.ISOLATE_EVENT_REQUESTED)

    def isolate_starting(self, uid):
        """
        The forker has created the isolate process and lets it boot

        :param uid: UID of the isolate
        :raise KeyError: Unknown UID
        :raise ValueError: Invalid state transition
        """
        return self._isolates[uid].handle(fsm.ISOLATE_EVENT_STARTING)

    def isolate_ready(self, uid):
        """
        The instantiation of the isolate succeeded

        :param uid: UID of the isolate
        :raise KeyError: Unknown UID
        :raise ValueError: Invalid state transition
        """
        return self._isolates[uid].handle(fsm.ISOLATE_EVENT_READY)

    def isolate_stopping(self, uid):
        """
        The isolate declares it is stopping

        :param uid: UID of the isolate
        :raise KeyError: Unknown UID
        :raise ValueError: Invalid state transition
        """
        return self._isolates[uid].handle(fsm.ISOLATE_EVENT_STOPPING)

    def isolate_gone(self, uid):
        """
        The isolate has been lost (process killed, ...)

        :param uid: UID of the isolate
        :raise KeyError: Unknown UID
        :raise ValueError: Invalid state transition
        """
        return self._isolates[uid].handle(fsm.ISOLATE_EVENT_GONE)

    def get_running(self):
        """
        Retrieves the UIDs of running isolates, i.e. in requested or ready mode

        :return: A list of UIDs (or an empty list)
        """
        return [item[0] for item in self._isolates.items()
                if item[1].state in (fsm.ISOLATE_STATE_REQUESTED,
                                     fsm.ISOLATE_STATE_READY)]

    def get_in_state(self, state):
        """
        Retrieves the UIDs of the isolates in the given state

        :param state: A state name
        :return: A list of UIDs
        """
        return [item[0] for item in self._isolates.items()
                if item[1].state == state]

    def get_state(self, uid):
        """
        Retrieves the state of the given UID

        :param uid: UID of an isolate
        :return: The state of the isolate
        :raise KeyError: Unknown UID
        """
        return self._isolates[uid].state

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        _logger.info("Monitor status validated")

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Clear storage
        self._isolates.clear()
        _logger.info("Monitor status invalidated")
