#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Forker isolate state directory

Stores the state of isolates started by the forker, until there dead.

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

# Python standard library
import logging
import threading

# Pelix framework
from pelix.ipopo.decorators import ComponentFactory, Validate, Invalidate, \
    Provides

# Cohorte boot constants
import cohorte.boot.constants as constants

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory('cohorte-forker-state-factory')
@Provides('cohorte.forker.state')
class IsolateStateDirectory(object):
    """
    Isolate -> state directory
    """
    def __init__(self):
        """
        Set up members
        """
        # Isolates directory
        self._directory = {}
        self._directory_lock = threading.RLock()

        # Waiters
        self._waiters = {}

    def prepare_isolate(self, uid):
        """
        Inserts an isolate in the UID, in the INEXISTANT state

        :param uid: An isolate UID
        :raise ValueError: The isolate is already known and in another state
        """
        with self._directory_lock:
            # Test if the isolate is already known
            cur_state = self._directory.get(uid)
            if cur_state is not None \
                    and cur_state != constants.STATE_NONEXISTENT:
                raise ValueError('{0} is already known in state {1}'
                                 .format(uid, cur_state))

            # Store the isolate and prepare its waiter
            self._directory[uid] = constants.STATE_NONEXISTENT
            self._waiters[uid] = threading.Event()

    def knows(self, uid):
        """
        Tests if the given UID is in the directory

        :param uid: An isolate UID
        :return: True if the isolate is known
        """
        with self._directory_lock:
            return uid in self._directory

    def get_state(self, uid):
        """
        Gets the state of the given UID

        :param uid: An isolate UID
        :raise KeyError: Unknown UID
        """
        with self._directory_lock:
            return self._directory[uid]

    def change_state(self, uid, new_state):
        """
        Sets the new state of the given isolate

        :param uid: An isolate UID
        :param new_state: The new state of the isolate
        :raise KeyError: Unknown isolate
        :raise ValueError: Invalid new state
        """
        with self._directory_lock:
            # Check the state
            cur_state = self._directory[uid]
            if new_state >= cur_state:
                # Apply the change
                self._directory[uid] = new_state
                if new_state >= constants.STATE_LOADED:
                    # Isolate is loaded: release waiters
                    self._waiters[uid].set()

            elif new_state == constants.STATE_FAILED:
                # Forget about it
                del self._directory[uid]

                # Notify waiters
                self._waiters[uid].set()
                del self._waiters[uid]

    def clear_isolate(self, uid):
        """
        Clear all references to the given isolate

        :param uid: An isolate UID
        :return: True on success, False if it was unknown
        """
        with self._directory_lock:
            if uid in self._waiters:
                # Set the event, if someone is waiting for it
                self._waiters[uid].set()
                del self._waiters[uid]

            if uid in self._directory:
                del self._directory[uid]

    def wait_for(self, uid, timeout=None):
        """
        Waits for the given isolate to show up

        :param uid: Isolate UID
        :param timeout: An optional wait time out (in seconds)
        :raise KeyError: Unknown UID
        :raise ValueError: Timeout expired
        """
        with self._directory_lock:
            # Grab the waiter thread-safely (can raise a KeyError)
            event = self._waiters[uid]

        # Wait the event to come
        event.wait(timeout)
        if not event.is_set():
            raise ValueError("Unknown UID after timeout: %s", uid)
        elif uid not in self._directory:
            # We have been awaken by clear_isolate
            raise ValueError("UID %s has been cleared.", uid)

        # Just in case someone uses an if...
        return True

    @Validate
    def validate(self, context):
        """
        Component validated

        :param context: The bundle context
        """
        _logger.debug("Isolate directory validated")

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated

        :param context: The bundle context
        """
        # Unlock all waiters
        for waiter in self._waiters.values():
            waiter.set()

        self._directory.clear()
        self._waiters.clear()
        _logger.debug("Isolate directory invalidated")
