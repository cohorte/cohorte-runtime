#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Defines a bean that represents an Isolate during an election.
The modification of its name is limited.

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 3.0.0

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
import itertools

# Composer beans
import cohorte.composer.beans as beans

# ------------------------------------------------------------------------------

# Module version
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------


class Event(object):
    """
    A composition or component event bean
    """
    def __init__(self, name, kind, good):
        """
        Sets up members

        :param name: Source isolate name
        :param kind: Kind of event (string ID)
        :param good: True if this is a betterment event
        """
        # Source Isolate information
        self.isolate_name = name

        # Kind of event
        self.kind = kind
        self.good = good

        # Associated RawComponent beans
        self.components = None

        # Event custom details
        self.data = None

    def __str__(self):
        """
        String representation
        """
        return "Event on {0}: {2} ({3})".format(self.isolate_name,
                                                self.kind, self.good)

# ------------------------------------------------------------------------------


class EligibleIsolate(object):
    """
    Represents an isolate to be elected
    """
    # The isolate counter
    __counter = itertools.count(1)

    def __init__(self, name=None, language=None, components=None):
        """
        Sets up members

        :param name: The name of the isolate
        :param language: The language of components in this isolate
        :param components: A set of pre-existing components
        """
        # The configured isolate name
        self.__name = name

        # Proposed name, if the current vote passes
        self.__proposed_name = None

        # Language of components hosted by this isolate
        self.language = language

        # Components hosted by this isolate
        if components is None:
            self.__components = set()
        else:
            self.__components = set(components)

        # Components returned by self.components()
        self.__visible_components = set(self.__components)

    def __str__(self):
        """
        String representation
        """
        if not self.language:
            return "Neutral"

        return "{0} {1}".format(self.__name, self.language)

    def __repr__(self):
        """
        String representation
        """
        if not self.language:
            return "NeutralIsolate"

        return "EligibleIsolate({0}, {1}, {2})" \
            .format(self.__name, self.language, self.__components)

    def to_isolate(self):
        """
        Returns the corresponding Isolate bean
        """
        return beans.Isolate(self.__name, self.language,
                             self.__visible_components)

    def hide(self, components):
        """
        Hides the given components from the visible list

        :param components: A list of components
        """
        self.__visible_components.difference_update(components)

    def unhide(self, component):
        """
        Unhides the given component. The component must be in the original list
        or this method does nothing

        :param component: A component to unhide
        """
        if component in self.__components:
            self.__visible_components.add(component)

    def accepted_rename(self):
        """
        Possible name accepted

        :raise ValueError: A name was already given
        """
        if self.__name:
            raise ValueError("Isolate already have a name: {0}"
                             .format(self.__name))

        self.__name = self.__proposed_name
        self.__proposed_name = None

    def propose_rename(self, new_name):
        """
        Proposes the renaming of the isolate

        :raise ValueError: A name was already given to this isolate
        :return: True if the proposal is acceptable
        """
        if self.__name:
            raise ValueError("Isolate already have a name: {0}"
                             .format(self.__name))

        if self.__proposed_name:
            return False

        self.__proposed_name = new_name
        return True

    def rejected_rename(self):
        """
        Possible name rejected
        """
        self.__proposed_name = None

    def generate_name(self, node):
        """
        Generates a name for this isolate (to be called) after votes.
        Does nothing if a name was already assigned to the isolate

        :param node: The node name
        :return: The (generated) isolate name
        """
        if not self.__name:
            # Need to generate a name
            self.__name = '{node}-{language}-auto{count:02d}' \
                          .format(node=node, language=self.language,
                                  count=next(EligibleIsolate.__counter))

        return self.__name

    @property
    def name(self):
        """
        Returns the name of the isolate
        """
        return self.__name

    @property
    def proposed_name(self):
        """
        Returns the currently proposed name, or None
        """
        return self.__proposed_name

    @property
    def components(self):
        """
        Returns the (frozen) set of components associated to this isolate
        """
        return frozenset(self.__visible_components)

    @property
    def new_components(self):
        """
        Returns the (frozen) set of components added to this isolate
        """
        return frozenset(self.__visible_components)

    @property
    def factories(self):
        """
        Returns the (frozen) set of the factories required to instantiate
        the components associated to this isolate
        """
        return frozenset(component.factory for component in self.__components)

    def add_component(self, component):
        """
        Adds a component to the isolate
        """
        if self.language is None:
            # First component tells which language this isolate hosts
            self.language = component.language

        self.__components.add(component)
        self.__visible_components.add(component)

# ------------------------------------------------------------------------------


class WrappedEligibleIsolate(EligibleIsolate):
    """
    An existing isolate is proposed to the vote
    """
    def __init__(self, isolate):
        """
        Sets up members

        :param isolate: An Isolate bean
        """
        super(WrappedEligibleIsolate, self).__init__(
            isolate.name, isolate.language, isolate.components)
        self.__isolate = isolate
        self.__added_components = set()

    def to_isolate(self):
        """
        Returns the corresponding Isolate bean
        """
        self.__isolate.components = set(self.components)
        return self.__isolate

    @property
    def new_components(self):
        """
        Returns the (frozen) set of components added to this isolate
        """
        return frozenset(self.__added_components)

    def add_component(self, component):
        """
        Adds a component to the isolate
        """
        super(WrappedEligibleIsolate, self).add_component(component)
        self.__added_components.add(component)
