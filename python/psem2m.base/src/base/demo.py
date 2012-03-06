#-- Content-Encoding: UTF-8 --
"""
Created on 29 févr. 2012

@author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import logging
_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

from psem2m.component.decorators import ComponentFactory, Provides, \
    Validate, Invalidate, Property

# ------------------------------------------------------------------------------

@ComponentFactory("erp-proxy-json-rpc")
@Provides("org.psem2m.composer.demo.IComponent")
@Property("__export", "service.exported.interfaces", "*")
class ErpProxy(object):

    def __init__(self):
        """
        Constructor
        """
        pass


    def computeResult(self, component_context):
        """
        Implementation of org.psem2m.composer.demo.IComponent
        """
        # TODO: implement it
        return None


    def test(self):
        """
        Dummy method
        """
        return "Hello, World !"

    def hello(self, name):
        """
        Dummy method
        """
        return "Hello, %s !" % name


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        _logger.warn("Python ERP Proxy ON")


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        _logger.warn("Python ERP Proxy OFF")
