#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Utilities: Rule engine, based on Intellect

**WARNING:**
This module uses the ``Intellect`` module.

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
import keyword
import re

# Intellect (rule engine)
import intellect.Intellect

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

Intellect = intellect.Intellect.Intellect
Callable = intellect.Intellect.Callable

# ------------------------------------------------------------------------------


class RuleEngine(Intellect):
    """
    Extension of the Intellect rule engine
    """
    def __init__(self):
        """
        Sets up the Intellect
        """
        # Trick to avoid an endless recursion in getattr()
        self._knowledge = []

        # Parent constructor
        Intellect.__init__(self)

        # Dispatched methods (Name -> Method)
        self._dispatch = {}

    @staticmethod
    def __check_name(name):
        """
        Checks if the given name is a valid Python identifier

        :param name: A string
        :return: True if the string can be a valid Python identifier
        """
        if name in keyword.kwlist:
            # Keyword name -> bad
            return False

        # Match the name token
        return re.match(r'^[a-z_][a-z0-9_]*$', name, re.I) is not None

    def add_callable(self, method, name=None):
        """
        Allows a method to be used from rules

        :param method: A reference to the method
        :param name: The method name
        :return: The name of the method (given or computed)
        :raise KeyError: The method name is already used
        :raise ValueError: Invalid name or method
        """
        if method is None or not hasattr(method, '__call__'):
            raise ValueError("Invalid method reference: %s", method)

        if not name:
            # Compute the name of method
            name = method.__name__
        elif not self.__check_name(name):
            # The name can't be a Python identifier
            raise ValueError("Invalid method name: {0}".format(name))

        # Check name usage
        if name in self._dispatch:
            raise KeyError("Already known method name: {0}".format(name))

        # Add the method to the dispatch dictionary
        self._dispatch[name] = Callable(method)
        return name

    def remove_callable(self, name):
        """
        Removes the method with the given name

        :param name: A method name (result of add_callable())
        :raise KeyError: Unknown method name
        """
        del self._dispatch[name]

    def clear(self):
        """
        Clears rule engine knowledge, policies and the dispatch dictionary
        """
        self._dispatch.clear()
        self.forget_all()

    def __getattr__(self, item):
        """
        Uses the dispatch dictionary to find a method used by the rules

        :param item: Item to search for
        :return: The found item
        :raise AttributeError: Item not found
        """
        try:
            # Get the item
            return self._dispatch[item]
        except KeyError:
            # Item not found
            raise AttributeError("Unknown attribute: {0}".format(item))
