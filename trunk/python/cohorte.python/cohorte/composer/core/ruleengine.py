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
import cohorte.utils.ruleengine
RuleEngine = cohorte.utils.ruleengine.RuleEngine

# Composer core
import cohorte.composer.core

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Property, Provides

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-core-rules-factory")
@Provides(cohorte.composer.core.SERVICE_RULE_ENGINE)
@Requires('_queue', cohorte.composer.core.SERVICE_QUEUE)
@Requires('_status', cohorte.composer.core.SERVICE_STATUS)
@Property('_rules_file', cohorte.composer.core.PROP_ENGINE_FILE, None)
@Property('_kind', cohorte.composer.core.PROP_ENGINE_KIND, None)
class RuleEngineComponent(object):
    """
    Composer core action executor
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Injected services
        self._queue = None
        self._status = None

        # Component properties
        self._rules_file = None
        self._kind = None

        # Rule engine
        self._engine = None


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

        # Add action methods for rules
        for method in (self.enqueue,):
            self._engine.add_callable(method)

        # Add status methods for rules
        status_method_prefixes = ("agent", "composer", "composite")
        for name in dir(self._status):
            for prefix in status_method_prefixes:
                if name.startswith(prefix):
                    # Method matching the prefixes
                    self._engine.add_callable(getattr(self._status, name), name)

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
