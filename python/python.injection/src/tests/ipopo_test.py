#!/usr/bin/python3
#-- Content-Encoding: utf-8 --
"""
Created on 3 févr. 2012

@author: Thomas Calmant
"""

from psem2m.component import constants, decorators
from psem2m.component.ipopo import IPopoEvent
from psem2m.services.pelix import FrameworkFactory, BundleContext
from tests.interfaces import IEchoService
import logging
import unittest

# ------------------------------------------------------------------------------

__version__ = (1, 0, 0)

# Set logging level
logging.basicConfig(level=logging.DEBUG)

NAME_A = "componentA"
NAME_B = "componentB"

# ------------------------------------------------------------------------------

def install_bundle(framework, bundle_name="tests.ipopo_bundle"):
    """
    Installs and starts the test bundle and returns its module

    @param framework: A Pelix framework instance
    @param bundle_name: A bundle name
    @return: The installed bundle Python module
    """
    context = framework.get_bundle_context()

    bid = context.install_bundle(bundle_name)
    bundle = context.get_bundle(bid)
    bundle.start()

    return bundle.get_module()


def install_ipopo(framework):
    """
    Installs and starts the iPOPO bundle. Returns the iPOPO service

    @param framework: A Pelix framework instance
    @return: The iPOPO service
    @raise Exception: The iPOPO service cannot be found
    """
    context = framework.get_bundle_context()
    assert isinstance(context, BundleContext)

    # Install & start the bundle
    bid = context.install_bundle("psem2m.component.ipopo")
    bundle = context.get_bundle(bid)
    bundle.start()

    # Get the service
    ref = context.get_service_reference(constants.IPOPO_SERVICE_SPECIFICATION)
    if ref is None:
        raise Exception("iPOPO Service not found")

    return context.get_service(ref)

# ------------------------------------------------------------------------------

class DecoratorsTest(unittest.TestCase):
    """
    Tests the iPOPO decorators
    """

    def testCallbacks(self):
        """
        Tests callbacks definitions
        """
        # Define what the method should contain
        callbacks = {
                    decorators.Bind: constants.IPOPO_CALLBACK_BIND,
                    decorators.Unbind: constants.IPOPO_CALLBACK_UNBIND,
                    decorators.Validate: constants.IPOPO_CALLBACK_VALIDATE,
                    decorators.Invalidate: constants.IPOPO_CALLBACK_INVALIDATE
                    }

        # Define some non decorable types
        class BadClass:
            pass

        bad_types = (None, 12, "Bad", BadClass)

        # Define a decorable method
        def empty_method():
            """
            Dummy method
            """
            pass

        self.assertFalse(hasattr(empty_method, \
                                 constants.IPOPO_METHOD_CALLBACKS), \
                         "The method is already tagged")

        for decorator, callback in callbacks.items():

            # Decorate the method
            decorated = decorator(empty_method)

            # Assert the method is the same
            self.assertIs(decorated, empty_method, "Method ID changed")

            # Assert the decoration has been done
            self.assertIn(callback, getattr(empty_method, \
                                            constants.IPOPO_METHOD_CALLBACKS), \
                          "Decoration failed")

            # Assert that the decorator raises a TypeError on invalid elements
            for bad in bad_types:
                self.assertRaises(TypeError, decorator, bad)

# ------------------------------------------------------------------------------

class LifeCycleTest(unittest.TestCase):
    """
    Tests the component life cyle
    """
    def setUp(self):
        """
        Called before each test. Initiates a framework.
        """
        self.framework = FrameworkFactory.get_framework()
        self.ipopo = install_ipopo(self.framework)
        self.module = install_bundle(self.framework)

    def tearDown(self):
        """
        Called after each test
        """
        self.framework.stop()
        FrameworkFactory.delete_framework(self.framework)


    def testSingleNormal(self):
        """
        Test a single component life cycle
        """
        # Assert it is not yet in the registry
        self.assertFalse(self.ipopo.is_registered_instance(NAME_A), \
                        "Instance is already in the registry")

        # Instantiate the component
        compoA = self.ipopo.instantiate(self.module.FACTORY_A, NAME_A)
        self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.VALIDATED], \
                         compoA.states, \
                         "Invalid component states : %s" % compoA.states)
        compoA.reset()

        # Assert it is in the registry
        self.assertTrue(self.ipopo.is_registered_instance(NAME_A), \
                        "Instance is not in the registry")

        # Invalidate the component
        self.ipopo.invalidate(NAME_A)
        self.assertEqual([IPopoEvent.INVALIDATED], compoA.states, \
                         "Invalid component states : %s" % compoA.states)
        compoA.reset()

        # Assert it is still in the registry
        self.assertTrue(self.ipopo.is_registered_instance(NAME_A), \
                        "Instance is not in the registry")

        # Kill (remove) the component
        self.ipopo.kill(NAME_A)

        # No event
        self.assertEqual([], compoA.states, \
                         "Invalid component states : %s" % compoA.states)

        # Assert it has been removed of the registry
        self.assertFalse(self.ipopo.is_registered_instance(NAME_A), \
                        "Instance is still in the registry")


    def testSingleKill(self):
        """
        Test a single component life cycle
        """
        # Assert it is not yet in the registry
        self.assertFalse(self.ipopo.is_registered_instance(NAME_A), \
                        "Instance is already in the registry")

        # Instantiate the component
        compoA = self.ipopo.instantiate(self.module.FACTORY_A, NAME_A)
        self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.VALIDATED], \
                         compoA.states, \
                         "Invalid component states : %s" % compoA.states)
        compoA.reset()

        # Assert it is in the registry
        self.assertTrue(self.ipopo.is_registered_instance(NAME_A), \
                        "Instance is not in the registry")

        # Kill the component without invalidating it
        self.ipopo.kill(NAME_A)
        self.assertEqual([IPopoEvent.INVALIDATED], compoA.states, \
                         "Invalid component states : %s" % compoA.states)
        compoA.reset()

        # Assert it has been removed of the registry
        self.assertFalse(self.ipopo.is_registered_instance(NAME_A), \
                        "Instance is still in the registry")



# ------------------------------------------------------------------------------

class ProvidesTest(unittest.TestCase):
    """
    Tests the component "provides" behavior
    """
    def setUp(self):
        """
        Called before each test. Initiates a framework.
        """
        self.framework = FrameworkFactory.get_framework()
        self.ipopo = install_ipopo(self.framework)

    def tearDown(self):
        """
        Called after each test
        """
        self.framework.stop()
        FrameworkFactory.delete_framework(self.framework)


    def testProvides(self):
        """
        Tests if the provides decorator works
        """
        module = install_bundle(self.framework)
        context = self.framework.get_bundle_context()
        assert isinstance(context, BundleContext)

        # Assert that the service is not yet available
        self.assertIsNone(context.get_service_reference(IEchoService), \
                          "Service is already registered")

        # Instantiate the component
        compoA = self.ipopo.instantiate(module.FACTORY_A, NAME_A)

        try:
            # Service should be there
            ref = context.get_service_reference(IEchoService)
            self.assertIsNotNone(ref, "Service hasn't been registered")

            svc = context.get_service(ref)
            self.assertIs(svc, compoA, \
                          "Different instances for service and component")
            context.unget_service(ref)
            svc = None

            # Invalidate the component
            self.ipopo.invalidate(NAME_A)

            # Service should not be there anymore
            self.assertIsNone(context.get_service_reference(IEchoService), \
                              "Service is still registered")

        finally:
            try:
                self.ipopo.kill(NAME_A)
            except:
                pass

# ------------------------------------------------------------------------------

class RequirementTest(unittest.TestCase):
    """
    Tests the component requirements behavior
    """

    def setUp(self):
        """
        Called before each test. Initiates a framework.
        """
        self.framework = FrameworkFactory.get_framework()
        self.ipopo = install_ipopo(self.framework)

    def tearDown(self):
        """
        Called after each test
        """
        self.framework.stop()
        FrameworkFactory.delete_framework(self.framework)


    def testCycleInner(self):
        """
        Tests if the component is bound, validated then invalidated.
        The component unbind call must come after it has been killed
        """
        module = install_bundle(self.framework)

        # Instantiate A (validated)
        compoA = self.ipopo.instantiate(module.FACTORY_A, NAME_A)
        self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.VALIDATED], \
                         compoA.states, \
                         "Invalid component states : %s" % compoA.states)
        compoA.reset()

        # Instantiate B (bound then validated)
        compoB = self.ipopo.instantiate(module.FACTORY_B, NAME_B)
        self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.BOUND, \
                          IPopoEvent.VALIDATED], compoB.states, \
                         "Invalid component states : %s" % compoB.states)
        compoB.reset()

        # Invalidate B
        self.ipopo.invalidate(NAME_B)
        self.assertEqual([IPopoEvent.INVALIDATED], compoB.states, \
                         "Invalid component states : %s" % compoB.states)
        compoB.reset()

        # Uninstantiate B
        self.ipopo.kill(NAME_B)
        self.assertEqual([IPopoEvent.UNBOUND], compoB.states, \
                         "Invalid component states : %s" % compoB.states)

        # Uninstantiate A
        self.ipopo.kill(NAME_A)
        self.assertEqual([IPopoEvent.INVALIDATED], compoA.states, \
                         "Invalid component states : %s" % compoA.states)


    def testCycleOuterEnd(self):
        """
        Tests if the required service is correctly unbound after the component
        invalidation
        """
        module = install_bundle(self.framework)

        # Instantiate A (validated)
        compoA = self.ipopo.instantiate(module.FACTORY_A, NAME_A)
        self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.VALIDATED], \
                         compoA.states, \
                         "Invalid component states : %s" % compoA.states)
        compoA.reset()

        # Instantiate B (bound then validated)
        compoB = self.ipopo.instantiate(module.FACTORY_B, NAME_B)
        self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.BOUND, \
                          IPopoEvent.VALIDATED], compoB.states, \
                         "Invalid component states : %s" % compoA.states)
        compoB.reset()

        # Uninstantiate A
        self.ipopo.kill(NAME_A)
        self.assertEqual([IPopoEvent.INVALIDATED], compoA.states, \
                         "Invalid component states : %s" % compoA.states)

        self.assertEqual([IPopoEvent.INVALIDATED, IPopoEvent.UNBOUND], \
                         compoB.states, \
                         "Invalid component states : %s" % compoB.states)
        compoB.reset()

        # Uninstantiate B
        self.ipopo.kill(NAME_B)
        self.assertEqual([], compoB.states, \
                         "Invalid component states : %s" % compoA.states)


    def testCycleOuterStart(self):
        """
        Tests if the required service is correctly bound after the component
        instantiation
        """
        module = install_bundle(self.framework)

        # Instantiate B (no requirement present)
        compoB = self.ipopo.instantiate(module.FACTORY_B, NAME_B)
        self.assertEqual([IPopoEvent.INSTANTIATED], compoB.states, \
                         "Invalid component states : %s" % compoB.states)
        compoB.reset()

        # Instantiate A (validated)
        compoA = self.ipopo.instantiate(module.FACTORY_A, NAME_A)
        self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.VALIDATED], \
                         compoA.states, \
                         "Invalid component states : %s" % compoA.states)
        compoA.reset()

        # B must have been validated
        self.assertEqual([IPopoEvent.BOUND, IPopoEvent.VALIDATED], \
                         compoB.states, \
                         "Invalid component states : %s" % compoB.states)
        compoB.reset()

        # Invalidate B
        self.ipopo.invalidate(NAME_B)
        self.assertEqual([IPopoEvent.INVALIDATED], compoB.states, \
                         "Invalid component states : %s" % compoB.states)
        compoB.reset()

        # Uninstantiate B
        self.ipopo.kill(NAME_B)
        self.assertEqual([IPopoEvent.UNBOUND], compoB.states, \
                         "Invalid component states : %s" % compoB.states)

        # Uninstantiate A
        self.ipopo.kill(NAME_A)
        self.assertEqual([IPopoEvent.INVALIDATED], compoA.states, \
                         "Invalid component states : %s" % compoA.states)


    def testConfiguredInstance(self):
        """
        Tests if the filter can be overridden by instance properties
        """
        module = install_bundle(self.framework)

        # The module filter
        properties_b = {constants.IPOPO_REQUIRES_FILTERS: \
                        {"service": "(%s=True)" % module.PROP_USABLE}}

        # Instantiate A (validated)
        compoA = self.ipopo.instantiate(module.FACTORY_A, NAME_A)

        # Set A unusable
        compoA.change(False)

        # Instantiate B (must not be bound)
        compoB = self.ipopo.instantiate(module.FACTORY_B, NAME_B, \
                                        properties_b)
        self.assertEqual([IPopoEvent.INSTANTIATED], compoB.states, \
                         "Invalid component states : %s" % compoB.states)
        compoB.reset()

        # Set A usable
        compoA.change(True)

        # B must be bound and validated
        self.assertEqual([IPopoEvent.BOUND, IPopoEvent.VALIDATED], \
                         compoB.states, \
                         "Invalid component states : %s" % compoB.states)
        compoB.reset()

# ------------------------------------------------------------------------------

if __name__ == "__main__":
    unittest.main()
