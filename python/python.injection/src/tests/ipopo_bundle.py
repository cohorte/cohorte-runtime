#-- Content-Encoding: utf-8 --
"""
Created on 3 févr. 2012

@author: Thomas Calmant
"""
from psem2m.component.constants import IPOPO_INSTANCE_NAME
from psem2m.component.decorators import ComponentFactory, Property, Provides, \
    Requires, Validate, Invalidate, Unbind, Bind
from psem2m.component.ipopo import IPopoEvent
from psem2m.services.pelix import BundleContext
from tests.interfaces import IEchoService

# ------------------------------------------------------------------------------

__version__ = (1, 0, 0)

FACTORY_A = "ipopo.tests.a"
FACTORY_B = "ipopo.tests.b"
PROP_USABLE = "usable"

# ------------------------------------------------------------------------------

class TestComponentFactory:
    """
    Parent class of components
    """
    def __init__(self):
        """
        Constructor
        """
        self.states = []
        self.states.append(IPopoEvent.INSTANTIATED)

    @Validate
    def validate(self, context):
        """
        Validation
        """
        self.states.append(IPopoEvent.VALIDATED)

    @Invalidate
    def invalidate(self, context):
        """
        Invalidation
        """
        self.states.append(IPopoEvent.INVALIDATED)


    def reset(self):
        """
        Resets the states list
        """
        del self.states[:]

# ------------------------------------------------------------------------------

@ComponentFactory(name=FACTORY_A)
@Property("name", IPOPO_INSTANCE_NAME)
@Property("usable", PROP_USABLE, True)
@Provides(specifications=IEchoService)
class ComponentFactoryA(TestComponentFactory, IEchoService):
    """
    Sample Component A
    """
    def echo(self, value):
        """
        Implementation of IEchoService
        """
        return value


    def change(self, usable):
        """
        Changes the usable property
        """
        self.usable = usable


@ComponentFactory(name=FACTORY_B)
@Requires(field="service", specification=IEchoService)
class ComponentFactoryB(TestComponentFactory):
    """
    Sample Component B
    """
    @Bind
    def bind(self, svc):
        """
        Bound
        """
        self.states.append(IPopoEvent.BOUND)

        # Assert that the service is already usable
        assert self.service.echo(True)

    @Unbind
    def unbind(self, svc):
        """
        Unbound
        """
        self.states.append(IPopoEvent.UNBOUND)

        # Assert that the service is still usable
        assert self.service.echo(True)

# ------------------------------------------------------------------------------

class ActivatorTest:
    """
    Test activator
    """

    def __init__(self):
        """
        Constructor
        """
        self.context = None


    def start(self, context):
        """
        Bundle started
        """
        assert isinstance(context, BundleContext)
        self.context = context

        global started
        started = True


    def stop(self, context):
        """
        Bundle stopped
        """
        assert isinstance(context, BundleContext)
        assert self.context is context

        global stopped
        stopped = True

# Prepare the activator
activator = ActivatorTest()
