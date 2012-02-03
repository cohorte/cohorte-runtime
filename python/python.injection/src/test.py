#!/usr/bin/python3
#-- Content-Encoding: UTF-8 --

from psem2m.component.constants import IPOPO_INSTANCE_NAME
from psem2m.component.decorators import ComponentFactory, Property, Validate, \
    Invalidate, Provides, Requires, Bind, Unbind
from psem2m.component.ipopo import instantiate, kill

import logging
from psem2m.services import pelix
from psem2m.services.pelix import Framework

# ------------------------------------------------------------------------------


HELLO_IMPL_FACTORY = "HelloImplFactory"
CONSUMER_FACTORY = "ConsumerFactory"

# Set logging level
logging.basicConfig(level=logging.DEBUG)

# Start Pelix here !
framework = pelix.FrameworkFactory.get_framework({'debug': True})
assert isinstance(framework, Framework)

framework.get_bundle_context().install_bundle(__name__)

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

print("-- Test --")

instantiate(CONSUMER_FACTORY, "Consumer")
import time
time.sleep(.8)

print("---> Instantiation...")
hello = instantiate(HELLO_IMPL_FACTORY, "HelloInstance", {"To": "Master"})
print("---> Done")

time.sleep(.8)

print("--- PROPERTY   ---")
hello._to = "World"

time.sleep(.8)

print("--- INVALIDATE ---")
kill("HelloInstance")
print("--- DONE ---")
