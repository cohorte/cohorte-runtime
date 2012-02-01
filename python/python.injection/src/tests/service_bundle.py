#-- Content-Encoding: utf-8 --
"""
Created on 1 févr. 2012

@author: Thomas Calmant
"""

from psem2m.services.pelix import BundleContext
from tests.interfaces import IEchoService

__version__ = (1, 0, 0)

registered = False
unregistered = False
service = None

class ServiceTest(IEchoService):
    """
    Simple test service
    """
    def __init__(self):
        """
        Constructor
        """
        self.toto = 0
        self.registration = None


    def echo(self, value):
        """
        Returns the given value
        """
        return value


class ActivatorService:
    """
    Test activator
    """

    def __init__(self):
        """
        Constructor
        """
        self.context = None
        self.svc = None


    def start(self, context):
        """
        Bundle started
        """
        assert isinstance(context, BundleContext)
        self.context = context

        # Register the service
        self.svc = ServiceTest()
        self.svc.registration = context.register_service(IEchoService, \
                                                         self.svc, \
                                                         {"test": True})

        global service
        service = self.svc


    def stop(self, context):
        """
        Bundle stopped
        """
        assert isinstance(context, BundleContext)
        self.svc.registration.unregister()



# Prepare the activator
activator = ActivatorService()
