#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: Rule engine

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# Rule engine
import cohorte.utils.ruleengine.RuleEngine as RuleEngine

# Composer core
import cohorte.composer.core

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Property

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-core-rules-factory")
@Requires('_queue', cohorte.composer.core.SERVICE_QUEUE)
@Property('_rules_file', 'rules.file', None)
class RuleEngineComponent(object):
    """
    Composer core action executor
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Action queue
        self._queue = None

        # Rule engine
        self._engine = None

        # Rules file
        self._rules_file = None


    def learn_rules(self, filename):
        """
        Tells the rule engine to learn the content of the given file
        
        :param filename: Name of a rule file
        :raise Exception: Error reading the file
        """
        self._engine.learn_policy(filename)


    def handle(self, event):
        """
        Calls the rule engine to handle an event
        
        :param event: An event
        """
        try:
            self._engine.learn(event)
            self._engine.reason()

        except Exception as ex:
            _logger.exception("Error running the rule engine: %s", ex)


    def enqueue(self, action):
        """
        Enqueues an action in the executor queue
        """
        if action is None:
            # Do nothing
            _logger.debug("Trying to enqueue a None action")
            return

        else:
            self._queue.enqueue(action)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # Prepare the rule engine
        self._engine = RuleEngine()

        # Learn the rules
        self.learn_rules(self._rules_file)

        # Add component methods for rules
        for method in (self.enqueue,):
            self._engine.add_callable(method)

        # Add logger methods
        for method in (_logger.debug, _logger.info, _logger.warning,
                       _logger.error):
            self._engine.add_callable(method)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Clear storage
        self._engine.clear()
        self._engine = None
