#!/usr/bin/python3
#-- Content-Encoding: utf-8 --
"""
Created on 1 févr. 2012

@author: Thomas Calmant
"""

from psem2m.services.pelix import FrameworkFactory, Bundle, BundleException, \
    BundleContext, BundleEvent
from tests.interfaces import IEchoService
import logging
import unittest

# ------------------------------------------------------------------------------

# Set logging level
logging.basicConfig(level=logging.DEBUG)

__version__ = (1, 0, 0)
# ------------------------------------------------------------------------------

def get_module(bundle):
    """
    Retrieves the internal member __module of a bundle
    """
    return getattr(bundle, "_Bundle__module")

# ------------------------------------------------------------------------------

class BundlesTest(unittest.TestCase):
    """
    Pelix bundle registry tests
    """

    def setUp(self):
        """
        Called before each test. Initiates a framework.
        """
        self.framework = FrameworkFactory.get_framework()
        self.test_bundle_name = "tests.simple_bundle"


    def tearDown(self):
        """
        Called after each test
        """
        self.framework.stop()
        FrameworkFactory.delete_framework(self.framework)


    def testLifeCycle(self, test_bundle_id=False):
        """
        Tests a bundle installation + start + stop
        
        @param test_bundle_id: If True, also tests if the test bundle ID is 1
        """
        # Install the bundle
        context = self.framework.get_bundle_context()
        assert isinstance(context, BundleContext)

        bid = context.install_bundle(self.test_bundle_name)
        if test_bundle_id:
            self.assertEqual(bid, 1, "Not the first bundle in framework")

        bundle = context.get_bundle(bid)
        assert isinstance(bundle, Bundle)

        # Get the internal module
        module = get_module(bundle)

        # Assert initial state
        self.assertFalse(module.started, "Bundle should not be started yet")
        self.assertFalse(module.stopped, "Bundle should not be stopped yet")

        # Activator
        bundle.start()

        self.assertTrue(module.started, "Bundle should be started now")
        self.assertFalse(module.stopped, "Bundle should not be stopped yet")

        # De-activate
        bundle.stop()

        self.assertTrue(module.started, "Bundle should be changed")
        self.assertTrue(module.stopped, "Bundle should be stopped now")

        # Uninstall (validated in another test)
        bundle.uninstall()


    def testUninstallInstall(self):
        """
        Runs the life-cycle test twice.
        
        The bundle is installed then un-installed twice. started and stopped
        values of the bundle should be reset to False.
        
        Keeping two separate calls instead of using a loop allows to see at
        which pass the test have failed
        """
        # Pass 1 : normal test
        self.testLifeCycle(True)

        # Pass 2 : refresh test
        self.testLifeCycle(False)


    def testVersion(self):
        """
        Tests if the version is correctly read from the bundle
        """
        # Install the bundle
        bid = self.framework.install_bundle(self.test_bundle_name)
        self.assertEqual(bid, 1, "Invalid first bundle ID '%d'" % bid)

        # Get the bundle
        bundle = self.framework.get_bundle_context().get_bundle(bid)
        assert isinstance(bundle, Bundle)

        # Get the internal module
        module = get_module(bundle)

        # Validate the bundle name
        self.assertEquals(bundle.get_symbolic_name(), self.test_bundle_name, \
                          "Location and name are different (%s / %s)" \
                          % (bundle.get_symbolic_name(), self.test_bundle_name))

        # Validate the version number
        self.assertEqual(bundle.get_version(), module.__version__, \
                         "Different versions found (%s / %s)" \
                         % (bundle.get_version(), module.__version__))

        # Remove the bundle
        bundle.uninstall()


    def testUninstallWithStartStop(self):
        """
        Tests if a bundle is correctly uninstalled and if it is really
        unaccessible after its uninstallation.
        """
        context = self.framework.get_bundle_context()
        assert isinstance(context, BundleContext)

        # Install the bundle
        bid = context.install_bundle(self.test_bundle_name)
        self.assertEqual(bid, 1, "Invalid first bundle ID '%d'" % bid)

        # Get the bundle
        bundle = context.get_bundle(bid)
        assert isinstance(bundle, Bundle)

        # Test state
        self.assertEqual(bundle.get_state(), Bundle.RESOLVED, \
                         "Invalid fresh install state %d" % bundle.get_state())

        # Start
        bundle.start()
        self.assertEqual(bundle.get_state(), Bundle.ACTIVE, \
                         "Invalid fresh start state %d" % bundle.get_state())

        # Stop
        bundle.stop()
        self.assertEqual(bundle.get_state(), Bundle.RESOLVED, \
                         "Invalid fresh stop state %d" % bundle.get_state())

        # Uninstall
        bundle.uninstall()
        self.assertEqual(bundle.get_state(), Bundle.UNINSTALLED, \
                         "Invalid fresh stop state %d" % bundle.get_state())

        # The bundle must not be accessible through the framework
        self.assertRaises(BundleException, context.get_bundle, bid)

        self.assertRaises(BundleException, self.framework.get_bundle_by_id, bid)

        found_bundle = self.framework.get_bundle_by_name(self.test_bundle_name)
        self.assertIsNone(found_bundle, "Bundle is still accessible by name " \
                          "through the framework")


# ------------------------------------------------------------------------------

class BundleEventTest(unittest.TestCase):
    """
    Pelix bundle event tests
    """

    def setUp(self):
        """
        Called before each test. Initiates a framework.
        """
        self.framework = FrameworkFactory.get_framework()
        self.test_bundle_name = "tests.simple_bundle"

        self.bundle = None
        self.received = []


    def tearDown(self):
        """
        Called after each test
        """
        self.framework.stop()
        FrameworkFactory.delete_framework(self.framework)


    def reset_state(self):
        """
        Resets the flags
        """
        del self.received[:]


    def testBundleEvents(self):
        """
        Tests if the signals are correctly received
        """
        context = self.framework.get_bundle_context()
        assert isinstance(context, BundleContext)

        # Register to events
        self.assertTrue(context.add_bundle_listener(self), \
                        "Can't register the bundle listener")

        # Install the bundle
        bid = context.install_bundle(self.test_bundle_name)
        self.bundle = bundle = context.get_bundle(bid)
        assert isinstance(bundle, Bundle)
        # Assert the Install events has been received
        self.assertEqual([BundleEvent.INSTALLED], \
                          self.received, "Received %s" % self.received)
        self.reset_state()

        # Start the bundle
        bundle.start()
        # Assert the events have been received
        self.assertEqual([BundleEvent.STARTING, BundleEvent.STARTED], \
                          self.received, "Received %s" % self.received)
        self.reset_state()

        # Stop the bundle
        bundle.stop()
        # Assert the events have been received
        self.assertEqual([BundleEvent.STOPPING, BundleEvent.STOPPED], \
                          self.received, "Received %s" % self.received)
        self.reset_state()


        # Uninstall the bundle
        bundle.uninstall()
        # Assert the events have been received
        self.assertEqual([BundleEvent.UNINSTALLED], \
                          self.received, "Received %s" % self.received)
        self.reset_state()


        # Unregister from events
        context.remove_bundle_listener(self)


    def bundle_changed(self, event):
        """
        Called by the framework when a bundle event is triggered
        
        @param event: The BundleEvent
        """
        assert isinstance(event, BundleEvent)

        bundle = event.get_bundle()
        kind = event.get_kind()
        if self.bundle is not None \
        and kind == BundleEvent.INSTALLED:
            # Bundle is not yet locally known...
            self.assertIs(self.bundle, bundle, \
                          "Received an event for an other bundle.")

        self.assertNotIn(kind, self.received, "Event received twice")
        self.received.append(kind)




# ------------------------------------------------------------------------------

class LocalBundleTest(unittest.TestCase):
    """
    Tests the installation of the __main__ bundle
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


    def testLocalBundle(self):
        """
        Tests the correctness of the __main__ bundle objects in the framework
        """
        fw_context = self.framework.get_bundle_context()
        assert isinstance(fw_context, BundleContext)

        # Install local bundle in framework (for service installation & co)
        self.bid = fw_context.install_bundle(__name__)

        # Get a reference to the bundle
        self.bundle = fw_context.get_bundle(self.bid)

        # Validate the symbolic name
        self.assertEquals(self.bundle.get_symbolic_name(), __name__, \
                          "Bundle (%s) and module (%s) are different" \
                          % (self.bundle.get_symbolic_name(), __name__))

        # Validate get_bundle() via bundle context
        context_bundle = self.bundle.get_bundle_context().get_bundle()
        self.assertIs(self.bundle, context_bundle, \
                      "Not the same bundle :\n%d / %s\n%d / %s" \
                      % (id(self.bundle), self.bundle, \
                         id(context_bundle), context_bundle))

# ------------------------------------------------------------------------------

class ServicesTest(unittest.TestCase):
    """
    Pelix services registry tests
    """

    def setUp(self):
        """
        Called before each test. Initiates a framework and loads the current
        module as the first bundle
        """
        self.test_bundle_name = "tests.service_bundle"

        self.framework = FrameworkFactory.get_framework()
        fw_context = self.framework.get_bundle_context()
        assert isinstance(fw_context, BundleContext)

        # Install local bundle in framework (for service installation & co)
        fw_context.install_bundle(__name__)


    def tearDown(self):
        """
        Called after each test
        """
        self.framework.stop()
        FrameworkFactory.delete_framework(self.framework)


    def testBundleRegister(self):
        """
        Test the service registration, request and unregister in a well formed
        bundle (activator that unregisters the service during the stop call)
        """
        svc_filter = "(test=True)"

        context = self.framework.get_bundle_context()
        assert isinstance(context, BundleContext)

        # Install the service bundle
        bid = context.install_bundle(self.test_bundle_name)
        bundle = context.get_bundle(bid)
        module = get_module(bundle)

        # Assert we can't access the service
        ref1 = context.get_service_reference(IEchoService)
        self.assertIsNone(ref1, "get_service_reference found : %s" % ref1)

        ref2 = context.get_service_reference(IEchoService, svc_filter)
        self.assertIsNone(ref2, "get_service_reference, filtered found : %s" \
                          % ref2)

        refs = context.get_all_service_references(IEchoService, None)
        self.assertIsNone(refs, "get_all_service_reference found : %s" % refs)

        refs = context.get_all_service_references(IEchoService, svc_filter)
        self.assertIsNone(refs, "get_all_service_reference, filtered found : %s" \
                          % refs)

        # --- Start it (registers a service) ---
        bundle.start()

        # Get the reference
        ref1 = context.get_service_reference(IEchoService)
        self.assertIsNotNone(ref1, "get_service_reference found nothing")

        ref2 = context.get_service_reference(IEchoService, svc_filter)
        self.assertIsNotNone(ref2, \
                             "get_service_reference, filtered found nothing")

        # Assert we found the same references
        self.assertIs(ref1, ref2, "References are not the same")

        refs = context.get_all_service_references(IEchoService, None)
        # Assert we found only one reference
        self.assertIsNotNone(refs, "get_all_service_reference filtered found nothing")

        refs = context.get_all_service_references(IEchoService, svc_filter)
        # Assert we found only one reference
        self.assertIsNotNone(refs, "get_all_service_reference filtered, filtered found nothing")

        # Assert that the first found reference is the first of "all" references
        self.assertIs(ref1, refs[0], "Not the same references through get and get_all")

        # Get the service
        svc = context.get_service(ref1)
        assert isinstance(svc, IEchoService)

        # Validate the reference
        self.assertIs(svc, module.service, "Not the same service instance...")

        # --- Stop it (unregisters a service) ---
        bundle.stop()

        # Assert we can't access the service
        ref1 = context.get_service_reference(IEchoService)
        self.assertIsNone(ref1, "get_service_reference found : %s" % ref1)

        ref2 = context.get_service_reference(IEchoService, svc_filter)
        self.assertIsNone(ref2, "get_service_reference, filtered found : %s" \
                          % ref2)

        refs = context.get_all_service_references(IEchoService, None)
        self.assertIsNone(refs, "get_all_service_reference found : %s" % refs)

        refs = context.get_all_service_references(IEchoService, svc_filter)
        self.assertIsNone(refs, "get_all_service_reference, filtered found : %s" \
                          % refs)

        # --- Uninstall it ---
        bundle.uninstall()


    def testBundleUninstall(self):
        """
        Tests if a registered service is correctly removed, even if its
        registering bundle doesn't have the code for that
        """
        context = self.framework.get_bundle_context()
        assert isinstance(context, BundleContext)

        # Install the service bundle
        bid = context.install_bundle(self.test_bundle_name)
        bundle = context.get_bundle(bid)
        module = get_module(bundle)

        # --- Start it (registers a service) ---
        bundle.start()

        self.assertIsNotNone(module.service, "The service instance is missing")

        # Get the reference
        ref = context.get_service_reference(IEchoService)
        self.assertIsNotNone(ref, "get_service_reference found nothing")

        # --- Uninstall the bundle without stopping it first ---
        bundle.uninstall()

        # The service should be deleted
        ref = context.get_service_reference(IEchoService)
        self.assertIsNone(ref, "get_service_reference found : %s" % ref)

# ------------------------------------------------------------------------------

if __name__ == "__main__":
    unittest.main()
