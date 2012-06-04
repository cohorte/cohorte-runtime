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

@ComponentFactory("demo.consumer")
@Property("_name", constants.IPOPO_INSTANCE_NAME)
@Property("__export", "service.exported.interfaces", "*")
@Requires("provider", SPEC_PROVIDER)
class DemoConsumer(object):
    """
    Demo provider
    """
    def __init__(self):
        """
        Constructor
        """
        self._name = None
        self.provider = None
        self.thread = None
        self._thread_run = False


    def run(self):
        """
        Thread loop
        """
        while self.provider is not None and self._thread_run:
            _logger.info("Calling Provider.echo('%s')", self._name)
            result = self.provider.echo(self._name)
            _logger.info("Provider.echo('%s') = %s", self._name, result)

            time.sleep(.5)

    @Validate
    def validate(self, context):
        """
        Validation
        """
        _logger.info("Component %s validated", self._name)
        self.thread = threading.Thread(target=self.run,
                                       name="Consumer_" + self._name)

        self._thread_run = True
        self.thread.start()


    @Invalidate
    def invalidate(self, context):
        """
        Invalidation
        """
        self._thread_run = False
        _logger.info("Component %s invalidated", self._name)
