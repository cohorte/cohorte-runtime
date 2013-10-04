#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: Rule engine beans

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# Standard library
import time

# ------------------------------------------------------------------------------

class Action(object):
    """
    Represents an action to give to the ActionExecutor
    """
    def __init__(self, isolate, kind, data):
        """
        Sets up members
        
        :param isolate: Isolate where the action will be executed
        :param kind: Kind of action
        :param data: Action parameter
        """
        self.__isolate = isolate
        self.__kind = kind
        self.__data = data


    @property
    def isolate(self):
        """
        The isolate targeted by the action
        """
        return self.__isolate


    @property
    def kind(self):
        """
        The kind of action
        """
        return self.__kind


    @property
    def data(self):
        """
        The parameter associated to the action
        """
        return self.__data


    def __repr__(self):
        """
        String representation of the action
        """
        return "Action({0}, {1}, {2})".format(self.__isolate, self.__kind,
                                              repr(self.__data))

# ------------------------------------------------------------------------------

class BasicEvent(object):
    """
    Common class for events
    """
    ISOLATE_LOST = "isolate.lost"

    ISOLATE_QUIT = "isolate.quit"

    ISOLATE_ALERT = "isolate.alert"

    COMPONENT_GONE = "component.gone"

    COMPONENT_ALERT = "component.alert"

    def __init__(self, kind, isolate_uid, isolate_node,
                 component=None, factory=None):
        """
        Sets up the event
        
        :param kind: Kind of event
        :param isolate_uid: UID of the isolate that is associated to this event
        :param isolate_node: Node of the isolate
        :param component: UID of the associated component (optional)
        :param factory: Factory of the associated component (optional)
        """
        # Event
        self.__timestamp = time.time()
        self.__kind = kind
        self.__data = {}

        # Source isolate
        self.__isolate = isolate_uid
        self.__node = isolate_node

        # Associated component (optional)
        self.__component_uid = None
        self.__component_factory = None


    @property
    def kind(self):
        """
        Kind of event
        """
        return self.__kind


    @property
    def timestamp(self):
        """
        Time of creation of this event
        """
        return self.__timestamp


    @property
    def data(self):
        """
        The data associated to the event (dictionary)
        """
        return self.__data


    @property
    def component(self):
        """
        The UID of the component associated to the event (optional)
        """
        return self.__component_uid


    @property
    def factory(self):
        """
        The factory of the component associated to the event (optional)
        """
        return self.__component_factory


    @property
    def isolate(self):
        """
        The isolate that is associated to this event
        """
        return self.__isolate


    @property
    def node(self):
        """
        The node hosting the isolate
        """
        return self.__node

# ------------------------------------------------------------------------------

class __Event(object):
    """
    Common class for events
    """
    def __init__(self, isolate, kind):
        """
        Sets up the event
        
        :param isolate: UID of the isolate that is associated to this event
        :param kind: Kind of event
        """
        self.__kind = kind
        self.__isolate = isolate
        self.__timestamp = time.time()


    @property
    def isolate(self):
        """
        The isolate that is associated to this event
        """
        return self.__isolate


    @property
    def kind(self):
        """
        Kind of event
        """
        return self.__kind


    @property
    def timestamp(self):
        """
        Time of creation of this event
        """
        return self.__timestamp


class AgentEvent(__Event):
    """
    Composer Agent availability event
    """
    LOST = 0
    """ Component lost """

    READY = 1
    """ Component ready to handle orders """


class FactoryEvent(__Event):
    """
    Component factory availability event
    """
    UNREGISTERED = 0
    """ Factory gone """

    REGISTERED = 1
    """ Factory ready to be used on an isolate """

    def __init__(self, isolate, kind, factory):
        """
        :param isolate: Isolate hosting the factory
        :param kind: Kind of event
        :param factory: Name of the factory associated to the event
        """
        __Event.__init__(self, isolate, kind)
        self.__factory = factory


    @property
    def factory(self):
        """
        The name of the component factory
        """
        return self.__factory


class MatchEvent(__Event):
    """
    A component compatibility check must be done
    """
    def __init__(self, composition, componentA, componentB):
        """
        Sets up the members
        
        :param composition: Loaded composition
        :param componentA: A Component bean
        :param componentB: A Component bean
        """
        __Event.__init__(self, None, "compatibility_check")

        # Loaded component information
        self.__composition = composition
        self.__componentA = componentA
        self.__componentB = componentB

        # Component isolate
        self.__isolate = None


    @property
    def componentA(self):
        """
        The component being check
        """
        return self.__componentA


    @property
    def componentB(self):
        """
        The component to check against
        """
        return self.__componentB


    @property
    def composition(self):
        """
        The composition being loaded
        """
        return self.__composition


    @property
    def isolate(self):
        """
        The computed isolate ID for this component
        """
        return self.__isolate


    @isolate.setter
    def isolate(self, isolate):
        """
        Sets the computed isolate ID for this component
        """
        self.__isolate = isolate
