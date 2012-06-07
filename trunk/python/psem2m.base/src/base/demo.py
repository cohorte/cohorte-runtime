#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Created on 29 févr. 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import logging
from pelix.ipopo import constants
_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Provides, \
    Validate, Invalidate, Property, Requires

# ------------------------------------------------------------------------------

SPEC_ICOMPONENT = "org.psem2m.composer.demo.IComponent"

@ComponentFactory("fall-back")
@Provides(SPEC_ICOMPONENT)
@Property("_name", constants.IPOPO_INSTANCE_NAME)
@Property("__export", "service.exported.interfaces", "*")
@Requires("second", SPEC_ICOMPONENT)
@Requires("next", SPEC_ICOMPONENT, optional=True)
class FallBack(object):

    def __init__(self):
        """
        Constructor
        """
        self.second = None
        self.next = None
        self.logger = None
        self._name = None


    def computeResult(self, component_context):
        """
        Implementation of org.psem2m.composer.demo.IComponent
        
        :param component_context: A ComponentContext object
        :return: A ComponentContext object
        """
        self.logger.warning("computeResult :\n%s", component_context)

        result = component_context
        use_fallback = True

        if self.next is not None:
            self.logger.info("First choice seems present")

            try:
                result = self.next.computeResult(component_context)
                use_fallback = result is None or \
                                (not result["results"] and result["errors"])

            except:
                self.logger.exception("Error calling the primary component")

        if use_fallback:
            # Reset context if needed
            if result["errors"]:
                result.errors = []

            if result["results"]:
                result["results"] = []

            self.logger.info("Using fall-back")
            result = self.second.computeResult(component_context)

        self.logger.debug("Fallback result : %s", result)
        return result


    def getName(self):
        """
        Retrieve the component name
        
        :return: The component name
        """
        return self._name


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self.logger = logging.getLogger(self._name)
        self.logger.info("Python FallBack Proxy ON")
        self.logger.debug("> next = %s", self.next)
        self.logger.debug("> fall = %s", self.second)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.logger.info("Python FallBack Proxy OFF")
        self.logger = None
