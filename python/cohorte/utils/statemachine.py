#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Utilities: generic Finite State Machine class

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

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------


class State(object):
    """
    Represents a state
    """
    def __init__(self, name, data=None, callback=None):
        """
        Sets up the members

        :param name: Name of the state
        :param data: Data associated to the state (optional)
        :param callback: Method to call once the state has changed (optional)
                         -> callback(state)
        """
        self.__name = name
        self.__data = data
        self.__callback = callback
        self.__transitions = {}

    def __str__(self):
        """
        Description of the state
        """
        return "{{{0}}}".format(self.__name)

    @property
    def data(self):
        """
        Returns the data associated to the state
        """
        return self.__data

    @property
    def name(self):
        """
        Returns the name of the state
        """
        return self.__name

    def copy(self):
        """
        Makes a copy of this state
        """
        # Create a new instance
        state = State(self.__name, self.__data, self.__callback)

        # Copy the transitions
        state.__transitions = self.__transitions.copy()
        return state

    def add_transition(self, transition):
        """
        Adds a transition: if the event if encountered, then the state
        is changed to new_state and callback(state, event, new_state) is
        called.

        :param transition: A Transition object
        :raise KeyError: The transition has already been defined
        """
        event = transition.event
        if event in self.__transitions:
            raise KeyError("Already known transition: ({0}, {1})"
                           .format(self.__name, event))

        self.__transitions[event] = transition

    def get_transition(self, event):
        """
        Returns the transition matching the given event

        :param event: A transition event
        :return: The transition for the event, or None
        """
        return self.__transitions.get(event)

    def notify(self):
        """
        Calls the callback method, if any

        :raise Exception: Any exception thrown by the method.
        """
        if self.__callback is not None:
            self.__callback(self)


class Transition(object):
    """
    Represents a transition
    """
    def __init__(self, state, event, new_state, callback=None):
        """
        Sets up the transition

        :param state: Original state
        :param event: Transition event
        :param new_state: New state after transition
        :param callback: Method to call once the state changed (optional)
        """
        self.__state = state
        self.__event = event
        self.__next = new_state
        self.__callback = callback

    def __str__(self):
        """
        Description of the transition
        """
        return "({0}, {1}) -> {2}".format(self.state, self.event, self.next)

    @property
    def event(self):
        """
        Transition event
        """
        return self.__event

    @property
    def state(self):
        """
        Original state
        """
        return self.__state

    @property
    def next(self):
        """
        New state after transition
        """
        return self.__next

    def notify(self):
        """
        Calls the callback method, if any

        :raise Exception: Any exception thrown by the method.
        """
        if self.__callback is not None:
            self.__callback(self.__state, self.__event, self.__next)

# ------------------------------------------------------------------------------


class StateMachine(object):
    """
    Generic Finite State Machine
    """
    def __init__(self, name=None, data=None):
        """
        Sets up members

        :param name: FSM/log name
        """
        # Name -> State
        self.__states = {}

        # Current state
        self.__state = None

        # FSM name
        self.__name = name or "FSM"

        # FSM data
        self.__data = data

        # Prepare the logger
        self._logger = logging.getLogger(self.__name)

    def __str__(self):
        """
        Description of the state machine
        """
        return "{0}({1})".format(self.__name, self.__state)

    @property
    def data(self):
        """
        The data associated to the state
        """
        return self.__data

    @property
    def name(self):
        """
        The name of the FSM or 'FSM'
        """
        return self.__name

    @property
    def state(self):
        """
        Name of the current state
        """
        if self.__state is not None:
            return self.__state.name

    def copy(self, name=None, data=None):
        """
        Makes a simple copy of this state machine, without active state.

        If no name is given, the current one is used. The data member is not
        inherited.

        :param name: Name of the new FSM
        :param data: Data associated to the new FSM
        """
        # Make a new FSM
        fsm = StateMachine(name or self.__name, data)

        # Make a copy of the state
        fsm.__states = [state.copy() for state in fsm.__states]
        return fsm

    def add_state(self, name, data=None, callback=None):
        """
        Adds a state to the FSM

        :param name: State name
        :param data: User-defined data associated to the state*
        :param callback: Method to callback when the set comes active
        :raise KeyError: Already known state
        :raise ValueError: Invalid state name
        """
        if not name:
            raise ValueError("Empty state name")

        if name in self.__states:
            raise KeyError("Already known state: {0}".format(name))

        # Store the state object
        self.__states[name] = State(name, data, callback)

    def add_transition(self, state, event, new_state, callback=None):
        """
        Adds a transition: if the (state, event) if matched, then the state
        is changed to new_state and callback(state, event, new_state) is
        called.

        :param state: Original state
        :param event: Transition event
        :param new_state: New state after transition
        :param callback: Method to call once the state changed (optional)
        :raise KeyError: The transition has already been defined for another
                         new state
        :raise ValueError: Unknown states
        """
        try:
            # Get the states
            state = self.__states[state]
            new_state = self.__states[new_state]
        except KeyError as ex:
            # Convert the exception
            raise ValueError(ex)

        # Make the transition bean
        transition = Transition(state, event, new_state, callback)

        # Register it
        state.add_transition(transition)

    def set_start(self, name):
        """
        Sets the start state. The state must have appeared into a transition
        to be valid.

        :param name: The start state
        :raise KeyError: Current state already set
        :raise ValueError: Unknown state
        """
        if self.__state is not None:
            raise KeyError("Current state already set to {0}"
                           .format(self.__state))

        if name not in self.__states:
            raise KeyError("Unknown state: {0}".format(name))

        # Change the state and notify the change
        self.__state = self.__states[name]
        self.__state.notify()

    def handle(self, event):
        """
        Handles the event. Calls the transition call back if defined.

        :param event: A transition event
        :return: The new event
        :raise KeyError: Invalid current state
        :raise ValueError: Invalid transition
        """
        if self.__state is None:
            raise KeyError("Invalid current state: None (call set_start)")

        # Find the transition
        transition = self.__state.get_transition(event)
        if transition is None:
            raise ValueError("No transition for ({0}, {1})"
                             .format(self.__state, event))

        # Call the transition callback before changing state
        try:
            transition.notify()
        except Exception as ex:
            self._logger.exception("Error calling the transition callback: %s",
                                   ex)

        # Change the state
        self.__state = transition.next

        # Call the state callback
        try:
            self.__state.notify()
        except Exception as ex:
            self._logger.exception("Error calling the state callback: %s", ex)

        # Return the new state
        return self.__state
