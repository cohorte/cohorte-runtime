#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: Action executor

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

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-core-executor-factory")
@Provides(cohorte.composer.core.SERVICE_EXECUTOR)
class ActionExecutor(object):
    """
    Composer core action executor
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Agent/ Isolate UID -> FSM
        self._agents = {}

        # Component UID -> FSM
        self._components = {}


    def execute(self, action):
        """
        Executes the given action
        
        :param action: The representation of an action
        """
        try:
            # Execute the handling method
            method = "handle_{0}".format(action.kind.lower())
            getattr(self, method)(action.isolate, action.data)

        except AttributeError:
            # Method not found, call the dispatcher
            self._dispatch(action)


    def _dispatch(self, action):
        """
        Method called to handle an action if no 'handle_kind' method exists
        
        :param action: The action to execute
        """
        _logger.debug("Should dispatch -> %s", action)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        _logger.info("Action executor validated")


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Clear storage
        self._agents.clear()
        self._components.clear()

        _logger.info("Action executor invalidated")
