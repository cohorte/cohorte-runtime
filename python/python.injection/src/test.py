#!/usr/bin/python3
#-- Content-Encoding: UTF-8 --

from psem2m.component.constants import IPOPO_INSTANCE_NAME
from psem2m.component.decorators import ComponentFactory, Property, Validate, \
    Invalidate, Provides, Requires, Bind, Unbind
from psem2m.component.ipopo import instantiate, kill
from psem2m.utilities import SynchronizedClassMethod

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
@Requires(field="service", specification=IHello, optional=False, spec_filter=("(To=Master)"))
class Test:

    def __init__(self):
        # Will be overridden by @Property
        self.id = 12

    @Validate
    def start(self):
        """
        Component starts
        """
        print("!!! Component '%s' is started !!!" % self.name)
        # Toto is injected by Requires, just before calling this method


    @Invalidate
    def stop(self):
        """
        Component stops
        """
        print("!!! Component '%s' is stopped !!!" % self.name)


    @Bind
    def bind(self, svc):
        # print(">>> Bound to", svc.getName())
        svc.sayHello()

    @Unbind
    def unbind(self, svc):
        # print("<<< Unbound of", svc.getName())
        svc.sayBye()


    def test(self):
        if not self.service:
            print(">> Required service is missing <<")
        else:
            self.service.sayHello()

# ------------------------------------------------------------------------------

# import threading

@ComponentFactory(name=HELLO_IMPL_FACTORY)
@Provides(specifications=IHello)
@Property(field="name", name=IPOPO_INSTANCE_NAME)
@Property(field="_to", name="To", value="World")
@Property(field="_count", name="Count")
class HelloImpl:

    @Validate
    def validate(self):
        # self.lock = threading.RLock()
        self._count = 0


    def getName(self):
        return self.name

    # @SynchronizedClassMethod('lock')
    def sayHello(self):
        """
        Says hello
        """
        self._count += 1
        print("Hello, %s ! (%d)" % (self._to, self._count))


    def sayBye(self):
        """
        Says bye
        """
        self._count -= 1
        print("Good bye, %s ! (%d)" % (self._to, self._count))

# ------------------------------------------------------------------------------

print("-- Test --")

instantiate(CONSUMER_FACTORY, "Consumer")
import time
time.sleep(1)

hello = instantiate(HELLO_IMPL_FACTORY, "HelloInstance", {"To": "Master"})

time.sleep(.5)

print("--- PROPERTY   ---")
hello._to = "World"

print("--- INVALIDATE ---")
kill("HelloInstance")
