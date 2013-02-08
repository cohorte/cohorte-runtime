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

class __Event(object):
    """
    Common class for events
    """
    def __init__(self, isolate, kind):
        """
        Sets up the event
        
        :param isolate: Isolate that is associated to this event
        :param kind: Kind of event
        """
        self.__isolate = isolate
        self.__kind = kind
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
