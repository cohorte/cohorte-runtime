#!/usr/bin/python3
#-- Content-Encoding: utf-8 --
"""
Created on 3 févr. 2012

@author: Thomas Calmant
"""

import logging
import unittest
from psem2m.services.pelix import FrameworkFactory, BundleContext
from psem2m.component.ipopo import instantiate, IPopoEvent, kill, invalidate
from tests.interfaces import IEchoService
from psem2m.component import ipopo

# ------------------------------------------------------------------------------

__version__ = (1, 0, 0)

# Set logging level
logging.basicConfig(level=logging.DEBUG)

NAME_A = "componentA"
NAME_B = "componentB"

# ------------------------------------------------------------------------------

def install_bundle(framework, bundle_name="tests.ipopo_bundle"):
        """
        Installs the test bundle and returns its module
        """
        context = framework.get_bundle_context()

        bid = context.install_bundle(bundle_name)
        bundle = context.get_bundle(bid)

        return bundle.get_module()


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
        self.assertFalse(ipopo._Registry.is_registered_instance(NAME_A), \
                        "Instance is already in the registry")

        # Instantiate the component
        compoA = instantiate(self.module.FACTORY_A, NAME_A)
        self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.VALIDATED], \
                         compoA.states, \
                         "Invalid component states : %s" % compoA.states)
        compoA.reset()

        # Assert it is in the registry
        self.assertTrue(ipopo._Registry.is_registered_instance(NAME_A), \
                        "Instance is not in the registry")

        # Invalidate the component
        invalidate(NAME_A)
        self.assertEqual([IPopoEvent.INVALIDATED], compoA.states, \
                         "Invalid component states : %s" % compoA.states)
        compoA.reset()

        # Assert it is still in the registry
        self.assertTrue(ipopo._Registry.is_registered_instance(NAME_A), \
                        "Instance is not in the registry")

        # Kill (remove) the component
        kill(NAME_A)

        # No event
        self.assertEqual([], compoA.states, \
                         "Invalid component states : %s" % compoA.states)

        # Assert it has been removed of the registry
        self.assertFalse(ipopo._Registry.is_registered_instance(NAME_A), \
                        "Instance is still in the registry")


    def testSingleKill(self):
        """
        Test a single component life cycle
        """
        # Assert it is not yet in the registry
        self.assertFalse(ipopo._Registry.is_registered_instance(NAME_A), \
                        "Instance is already in the registry")

        # Instantiate the component
        compoA = instantiate(self.module.FACTORY_A, NAME_A)
        self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.VALIDATED], \
                         compoA.states, \
                         "Invalid component states : %s" % compoA.states)
        compoA.reset()

        # Assert it is in the registry
        self.assertTrue(ipopo._Registry.is_registered_instance(NAME_A), \
                        "Instance is not in the registry")

        # Kill the component without invalidating it
        kill(NAME_A)
        self.assertEqual([IPopoEvent.INVALIDATED], compoA.states, \
                         "Invalid component states : %s" % compoA.states)
        compoA.reset()

        # Assert it has been removed of the registry
        self.assertFalse(ipopo._Registry.is_registered_instance(NAME_A), \
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
        compoA = instantiate(module.FACTORY_A, NAME_A)

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
            invalidate(NAME_A)

            # Service should not be there anymore
            self.assertIsNone(context.get_service_reference(IEchoService), \
                              "Service is still registered")

        finally:
            try:
                kill(NAME_A)
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

        compoA = None
        compoB = None

        try:
            # Instantiate A (validated)
            compoA = instantiate(module.FACTORY_A, NAME_A)
            self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.VALIDATED], \
                             compoA.states, \
                             "Invalid component states : %s" % compoA.states)
            compoA.reset()

            # Instantiate B (bound then validated)
            compoB = instantiate(module.FACTORY_B, NAME_B)
            self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.BOUND, \
                              IPopoEvent.VALIDATED], compoB.states, \
                             "Invalid component states : %s" % compoB.states)
            compoB.reset()

            # Invalidate B
            invalidate(NAME_B)
            self.assertEqual([IPopoEvent.INVALIDATED], compoB.states, \
                             "Invalid component states : %s" % compoB.states)
            compoB.reset()

            # Uninstantiate B
            kill(NAME_B)
            self.assertEqual([IPopoEvent.UNBOUND], compoB.states, \
                             "Invalid component states : %s" % compoB.states)

            # Uninstantiate A
            kill(NAME_A)
            self.assertEqual([IPopoEvent.INVALIDATED], compoA.states, \
                             "Invalid component states : %s" % compoA.states)

        finally:
            for compo in (NAME_A, NAME_B):
                try:
                    kill(compo)
                except:
                    pass

    def testCycleOuterEnd(self):
        """
        Tests if the required service is correctly unbound after the component
        invalidation
        """
        module = install_bundle(self.framework)

        compoA = None
        compoB = None

        try:
            # Instantiate A (validated)
            compoA = instantiate(module.FACTORY_A, NAME_A)
            self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.VALIDATED], \
                             compoA.states, \
                             "Invalid component states : %s" % compoA.states)
            compoA.reset()

            # Instantiate B (bound then validated)
            compoB = instantiate(module.FACTORY_B, NAME_B)
            self.assertEqual([IPopoEvent.INSTANTIATED, IPopoEvent.BOUND, \
                              IPopoEvent.VALIDATED], compoB.states, \
                             "Invalid component states : %s" % compoA.states)
            compoB.reset()

            # Uninstantiate A
            kill(NAME_A)
            self.assertEqual([IPopoEvent.INVALIDATED], compoA.states, \
                             "Invalid component states : %s" % compoA.states)

            self.assertEqual([IPopoEvent.INVALIDATED, IPopoEvent.UNBOUND], \
                             compoB.states, \
                             "Invalid component states : %s" % compoB.states)
            compoB.reset()

            # Uninstantiate B
            kill(NAME_B)
            self.assertEqual([], compoB.states, \
                             "Invalid component states : %s" % compoA.states)

        finally:
            for compo in (NAME_A, NAME_B):
                try:
                    kill(compo)
                except:
                    pass


# ------------------------------------------------------------------------------

if __name__ == "__main__":
    unittest.main()
