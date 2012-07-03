#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Created on 29 févr. 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import logging
import threading
import time

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

from pelix.ipopo import constants

from pelix.ipopo.decorators import ComponentFactory, Provides, \
    Validate, Invalidate, Property, Requires

# ------------------------------------------------------------------------------

SPEC_PROVIDER = "org.psem2m.dist.demo.provider"

@ComponentFactory("demo.provider")
@Provides(SPEC_PROVIDER)
@Property("_name", constants.IPOPO_INSTANCE_NAME)
@Property("__export", "service.exported.interfaces", "*")
class DemoProvider(object):
    """
    Demo provider
    """
    def __init__(self):
        """
        Constructor
        """
        self._name = None
        self._count = 0


    def echo(self, name):
        """
        Says hello locally and returns the hello sentence
        """
        self._count += 1
        return "Hello from %s, %s ! You are the visitor %03d" \
            % (self._name, name, self._count)

    @Validate
    def validate(self, context):
        """
        Validation
        """
        self._count = 0
        _logger.info("Component %s validated", self._name)


    @Invalidate
    def invalidate(self, context):
        """
        Invalidation
        """
        _logger.info("Component %s invalidated after %d visits", self._name,
                     self._count)
