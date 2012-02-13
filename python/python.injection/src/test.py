#!/usr/bin/python3
#-- Content-Encoding: UTF-8 --

from psem2m.component import constants
from psem2m.component.constants import IPOPO_INSTANCE_NAME
from psem2m.component.decorators import ComponentFactory, Property, Validate, \
    Invalidate, Provides, Requires, Bind, Unbind
from psem2m.services import pelix
from psem2m.services.pelix import Framework
import logging
import time


# ------------------------------------------------------------------------------


HELLO_IMPL_FACTORY = "HelloImplFactory"
CONSUMER_FACTORY = "ConsumerFactory"

# Set logging level
logging.basicConfig(level=logging.DEBUG)

# ------------------------------------------------------------------------------

class IHello:
    """
    Interface test
    """

    def sayHello(self):
        """
        Prints "Hello, world !"
        """

    def sayBye(self):
        """
        Prints "Good bye !"
        """

    def getName(self):
        """
        Get the component name
        """

# ------------------------------------------------------------------------------

@ComponentFactory(name=CONSUMER_FACTORY)
@Property(field="name", name=IPOPO_INSTANCE_NAME)
@Requires(field="service", specification=IHello, optional=False, spec_filter=("(To=World)"))
class Test:

    def __init__(self):
        # Will be overridden by @Property
        self.name = "Me"

    @Validate
    def start(self, context):
        """
        Component starts
        """
        print("VALIDATE: Component %s validated" % self.name)


    @Invalidate
    def stop(self, context):
        """
        Component stops
        """
        print("INVALIDATE: Component %s invalidated" % self.name)


    @Bind
    def bind(self, svc):
        print("BIND: %s gets a service" % self.name)
        svc.sayHello()

    @Unbind
    def unbind(self, svc):
        print("UNBIND: %s lost a service" % self.name)
        svc.sayBye()


    def test(self):
        if not self.service:
            print(">> Required service is missing <<")
        else:
            self.service.sayHello()

# ------------------------------------------------------------------------------

@ComponentFactory(name=HELLO_IMPL_FACTORY)
@Provides(specifications=IHello)
@Property(field="name", name=IPOPO_INSTANCE_NAME)
@Property(field="_to", name="To", value="World")
@Property(field="_count", name="Count")
class HelloImpl:

    @Validate
    def start(self, context):
        """
        Component starts
        """
        self._count = 0
        print("VALIDATE: Component %s validated" % self.name)


    @Invalidate
    def stop(self, context):
        """
        Component stops
        """
        print("INVALIDATE: Component %s invalidated" % self.name)


    def getName(self):
        return self.name


    def sayHello(self):
        """
        Says hello
        """
        self._count += 1
        print("CALL (%s) : Hello, %s ! (%d)" \
              % (self.name, self._to, self._count))


    def sayBye(self):
        """
        Says bye
        """
        self._count -= 1
        print("CALL (%s) : Good bye, %s ! (%d)" \
              % (self.name, self._to, self._count))

# ------------------------------------------------------------------------------

class Activator:
    """
    The bundle activator
    """
    def __init__(self):
        """
        Constructor
        """
        self.ref = None
        self.ipopo = None

    def start(self, bundle_context):
        """
        The bundle is started
        """
        self.ref = bundle_context.get_service_reference(\
                                        constants.IPOPO_SERVICE_SPECIFICATION)

        if self.ref is not None:
            self.ipopo = bundle_context.get_service(self.ref)

        print("-- Test --")
        self.ipopo.instantiate(CONSUMER_FACTORY, "Consumer")
        time.sleep(.8)

        print("---> Instantiation...")
        hello = self.ipopo.instantiate(HELLO_IMPL_FACTORY, "HelloInstance", \
                                       {"To": "Master"})
        print("---> Done")

        time.sleep(.8)

        print("--- PROPERTY   ---")
        hello._to = "World"

        time.sleep(.8)

        print("--- INVALIDATE ---")
        self.ipopo.kill("HelloInstance")
        print("--- DONE ---")



    def stop(self, bundle_context):
        """
        The bundle is stopped
        """
        if self.ref is not None:
            bundle_context.unget_service(self.ref)

# ------------------------------------------------------------------------------

def setup_framework():
    """
    Prepares a framework and installs iPOPO and the current module
    """
    # Start Pelix here !
    framework = pelix.FrameworkFactory.get_framework({'debug': True})
    assert isinstance(framework, Framework)

    # Install iPOPO
    bid = framework.get_bundle_context().install_bundle("psem2m.component.ipopo")
    framework.get_bundle_by_id(bid).start()

    # Install the current module
    bid = framework.get_bundle_context().install_bundle(__name__)
    framework.get_bundle_by_id(bid).start()

# ------------------------------------------------------------------------------

activator = Activator()
setup_framework()
