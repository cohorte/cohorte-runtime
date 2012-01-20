#!/usr/bin/python3
#-- Content-Encoding: UTF-8 --

from registry import instantiate
from decorators import ComponentFactory, Property, Validate, Invalidate, Provides, Requires, \
    Bind, Unbind
import logging
import registry

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

# ------------------------------------------------------------------------------

@ComponentFactory(name=CONSUMER_FACTORY)
@Property(field="id", value=42)
@Requires(field="toto", specification=IHello, optional=True)
class Test:

    def __init__(self):
        # Will be overridden by @Property
        self.id = 12

    @Validate
    def start(self):
        """
        Component starts
        """
        print("!!! Component is started !!!")
        # Toto is injected by Requires, just before calling this method


    @Invalidate
    def stop(self):
        """
        Component stops
        """
        print("!!! Component is stopped !!!")


    @Bind
    def bind(self, svc):
        print(">>> Bound to", svc)
        self.toto.sayHello()

    @Unbind
    def unbind(self, svc):
        print("<<< Unbound of", svc)


    def test(self):
        if not self.toto:
            print(">> Required service is missing <<")
        else:
            self.toto.sayHello()

# ------------------------------------------------------------------------------

@ComponentFactory(name=HELLO_IMPL_FACTORY)
@Provides(specification=IHello)
class HelloImpl:

    def sayHello(self):
        """
        Says hello
        """
        print("Hello, World !")

# ------------------------------------------------------------------------------

print("-- Test --")

instantiate(CONSUMER_FACTORY, "Consumer")
import time
time.sleep(1)

instantiate(HELLO_IMPL_FACTORY, "HelloInstance")

time.sleep(1)

print("--- INVALIDATE ---")
registry.unregister_factory(HELLO_IMPL_FACTORY)
