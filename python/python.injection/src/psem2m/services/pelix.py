"""
Core module for Pelix.

Pelix is a Python framework that aims to act as OSGi as much as possible

@author: Thomas Calmant
"""

import imp
import logging
import os
import sys

from psem2m import ldapfilter

ACTIVATOR = "activator"

OBJECTCLASS = "objectClass"
SERVICE_ID = "service.id"
SERVICE_RANKING = "service.ranking"

# ------------------------------------------------------------------------------

# Module version
__version__ = (1, 0, 0)

# Prepare the module logger
_logger = logging.getLogger("pelix.main")

# ------------------------------------------------------------------------------

class BundleException(Exception):
    """
    The base of all framework exceptions
    """
    def __init__(self, content):
        """
        Sets up the exception
        """
        Exception.__init__(self, content)


# ------------------------------------------------------------------------------

class Bundle:
    """
    Represents a "bundle" in Pelix
    """

    UNINSTALLED = 1
    """ The bundle is uninstalled and may not be used """

    INSTALLED = 2
    """ The bundle is installed but not yet resolved """

    RESOLVED = 4
    """ The bundle is resolved and is able to be started """

    STARTING = 8
    """ The bundle is in the process of starting """

    STOPPING = 16
    """ The bundle is in the process of stopping """

    ACTIVE = 32
    """ The bundle is now running """


    def __init__(self, framework, bundle_id, module):
        """
        Sets up the bundle descriptor
        
        @param framework: The host framework
        @param bundle_id: The bundle ID in the host framework
        @param module: A Python module object (the 'bundle')
        """
        # Bundle
        self.__context = None
        self.__framework = framework
        self.__id = bundle_id
        self.__module = module
        self.__state = Bundle.RESOLVED

        # Services
        self.__registered_services = []
        self.__imported_services = []


    def __fire_bundle_event(self, kind):
        """
        Fires a bundle event of the given kind
        
        @param kind: Kind of event
        """
        self.__framework.fire_bundle_event(BundleEvent(kind, self))


    def get_bundle_context(self):
        """
        Retrieves the bundle context
        """
        return self.__context


    def get_bundle_id(self):
        """
        Retrieves the bundle ID
        
        @return: The bundle ID
        """
        return self.__id


    def get_location(self):
        """
        Retrieves the location of this module
        
        @return: The location of the Pelix module, or an empty string
        """
        return getattr(self.__module, '__file__', "")


    def get_state(self):
        """
        Retrieves the bundle state
        """
        return self.__state


    def get_symbolic_name(self):
        """
        Retrieves the bundle symbolic name
        """
        return self.__module.__name__


    def get_version(self):
        """
        Retrieves the bundle version
        
        @return: The bundle version, (0,0,0) by default
        """
        return getattr(self.__module, "__version__", (0, 0, 0))


    def set_context(self, context):
        """
        Sets the bundle context. Does nothing if a context has already been set.
        
        @param context: The bundle context
        """
        if self.__context is None:
            self.__context = context


    def start(self):
        """
        Starts the bundle
        """
        # Starting...
        self.__state = Bundle.STARTING
        self.__fire_bundle_event(BundleEvent.STARTING)

        # Call the activator, if any
        activator = getattr(self.__module, ACTIVATOR, None)
        starter = getattr(activator, 'start', None)

        if starter is not None:
            try:
                # Call the start method
                starter(self.__context)

            except Exception as ex:
                _logger.exception("Error calling the activator")
                raise BundleException(ex)

        # Bundle is now active
        self.__state = Bundle.ACTIVE
        self.__fire_bundle_event(BundleEvent.STARTED)


    def stop(self):
        """
        Stops the bundle
        """
        # Stopping...
        self.__state = Bundle.STOPPING
        self.__fire_bundle_event(BundleEvent.STOPPING)

        # Call the activator, if any
        activator = getattr(self.__module, ACTIVATOR, None)
        stopper = getattr(activator, 'stop', None)

        if stopper is not None:
            try:
                # Call the start method
                stopper(self.__context)

            except Exception as ex:
                _logger.exception("Error calling the activator")
                raise BundleException(ex)

        # Bundle is now stopped
        self.__state = Bundle.RESOLVED
        self.__fire_bundle_event(BundleEvent.STOPPED)


    def store_registration(self, registration):
        """
        Stores a service reference
        
        @param registration: A service registration
        """
        if isinstance(registration, ServiceRegistration) \
        and registration not in self.__registered_services:
            self.__registered_services.append(registration)


    def uninstall(self):
        """
        Uninstall the bundle
        """
        # Remove all services
        for registration in self.__registered_services:
            registration.unregister()

        # Clear the dictionary
        del self.__registered_services[:]

        # Change the bundle state
        self.__state = Bundle.UNINSTALLED

        # Call the framework
        self.__framework.uninstall_bundle(self)


    def update(self):
        """
        Updates the bundle
        """
        # Stop the bundle
        self.stop()

        # Reload the module
        imp.reload(self.__module)

        # Re-start the bundle
        self.start()


    def unuses_service(self, reference):
        """
        The bundle doesn't use the given service reference anymore
        
        @param reference: A service reference
        """
        if reference in self.__imported_services:
            self.__imported_services.remove(reference)


    def uses_service(self, reference):
        """
        The bundle uses the given service reference
        
        @param reference: A service reference
        """
        if reference not in self.__imported_services:
            self.__imported_services.append(reference)


# ------------------------------------------------------------------------------

def _add_listener(listeners_registry, listener):
    """
    Adds a listener in the registry, if it is not yet in
    
    @return: True if the listener has been added
    """
    if listener is None or listener in listeners_registry:
        return False

    listeners_registry.append(listener)
    return True


def _remove_listener(listeners_registry, listener):
    """
    Removes a listener from the registry
    
    @return: True if the listener was in the list
    """
    if listener is not None and listener in listeners_registry:
        listeners_registry.remove(listener)
        return True

    return False


class Framework(Bundle):
    """
    The Pelix framework (main) class. It must be instantiated using
    FrameworkFactory
    """
    def __init__(self, properties={}):
        """
        Sets up the framework.
        
        @param properties: The framework properties
        """
        # Framework
        Bundle.__init__(self, self, 0, sys.modules[__name__])

        # Framework context
        self.set_context(BundleContext(self, self))

        if not isinstance(properties, dict):
            self.__properties = {}
        else:
            self.__properties = properties

        # Bundles
        self.__next_bundle_id = 1

        # bundle ID -> Bundle object
        self.__bundles = {}

        # Listeners
        self.__bundle_listeners = []
        self.__service_listeners = []

        # Listener -> filter (LDAPFilter)
        self.__service_listeners_filters = {}

        # Main service registry
        self.__next_service_id = 1

        # ServiceReference -> service instance
        self.__registry = {}


    def add_bundle_listener(self, listener):
        """
        Registers a bundle listener
        
        @param listener: The bundle listener
        """
        _add_listener(self.__bundle_listeners, listener)


    def add_service_listener(self, listener, ldap_filter=None):
        """
        Registers a service listener
        
        @param listener: The service listener
        @param ldap_filter: Listener
        """
        if _add_listener(self.__service_listeners, listener):
            # Set the associated filter
            try:
                self.__service_listeners_filters[listener] = \
                                    ldapfilter.get_ldap_filter(ldap_filter)

            except ValueError:
                # Invalid filter
                _remove_listener(self.__service_listeners, listener)
                _logger.exception("Invalid service listener filter")
                return


    def find_service_references(self, clazz=None, ldap_filter=None):
        """
        Finds all services references matching the given filter.
        
        @param clazz: Class implemented by the service
        @param ldap_filter: Service filter
        @return: A list of found reference, or None
        @raise BundleException: An error occurred looking for service references
        """
        if clazz is None and ldap_filter is None:
            # Return a sorted copy of the keys list
            return sorted(self.__registry.keys())

        # Escape the class name
        clazz = ldapfilter.escape_LDAP(clazz)

        if clazz is None:
            # Directly use the given filter
            new_filter = ldap_filter

        elif ldap_filter is None:
            # Make a filter for the object class
            new_filter = "(%s=%s)" % (OBJECTCLASS, clazz)

        else:
            # Combine filter with a AND operator
            new_filter = ldapfilter.combine_filters(\
                                    ["(%s=%s)" % (OBJECTCLASS, clazz), \
                                     ldap_filter])

        # Parse the filter
        try:
            new_filter = ldapfilter.get_ldap_filter(new_filter)

        except ValueError as ex:
            raise BundleException(ex)
        
        if new_filter is None:
            # Normalized filter is None : return everything
            return sorted(self.__registry.keys())

        # Find a reference that matches
        result = []
        for ref in self.__registry:
            if new_filter.matches(ref.get_properties()):
                result.append(ref)

        if not result:
            # No result found
            return None

        # Sort the results
        result.sort()
        return result


    def fire_bundle_event(self, event):
        """
        Fires a Bundle event
        
        @param event: The sent event
        """
        assert(isinstance(event, BundleEvent))

        for listener in self.__bundle_listeners:
            try:
                listener.bundle_changed(event)

            except:
                _logger.exception("An error occurred calling one of the " \
                                  "bundle listeners")


    def fire_service_event(self, event):
        """
        Fires a service event
        
        @param event: The sent event
        """
        assert(isinstance(event, ServiceEvent))

        # Get the service properties
        properties = event.get_service_reference().get_properties()
        previous = None
        endmatch_event = None

        if event.get_type() == ServiceEvent.MODIFIED:
            # Modified service event : prepare the end match event
            previous = event.get_previous_properties()
            endmatch_event = ServiceEvent(ServiceEvent.MODIFIED_ENDMATCH, \
                                          event.get_service_reference(), \
                                          previous)

        for listener in self.__service_listeners:

            # Default event to send : the one we received
            sent_event = event

            # Test if the service properties matches the filter
            ldap_filter = self.__service_listeners_filters.get(listener, None)

            if ldap_filter is not None and not ldap_filter.matches(properties):
                # Event doesn't match listener filter...

                if previous is not None and ldap_filter.matches(previous):
                    # ... but previous properties did match
                    sent_event = endmatch_event

                else:
                    # Not an end match, ignore...
                    continue

            try:
                listener.service_changed(sent_event)

            except:
                _logger.exception("An error occurred calling one of the " \
                                  "service listeners")


    def get_bundle_by_id(self, bundle_id):
        """
        Retrieves the bundle with the given ID
        
        @param id: ID of an installed bundle
        @return: The requested bundle
        @raise BundleException: The ID is invalid
        """
        if bundle_id not in self.__bundles:
            raise BundleException("Invalid bundle ID %d" % bundle_id)

        return self.__bundles[bundle_id]


    def get_bundle_by_name(self, bundle_name):
        """
        Retrieves the bundle with the given name
        
        @param name: Name of the bundle to look for
        @return: The requested bundle, None if not found
        """
        for bundle in self.__bundles.values():
            if bundle_name == bundle.get_symbolic_name():
                # Found !
                return bundle

        # Not found...
        return None


    def get_bundles(self):
        """
        Returns a list of all installed bundles
        """
        return self.__bundles.values()


    def get_property(self, name):
        """
        Retrieves a framework or system property
        
        @param name: The property name
        """
        if name in self.__properties:
            return self.__properties[name]

        return os.getenv(name)


    def get_service(self, bundle, reference):
        """
        Retrieves the service corresponding to the given reference
        
        @param bundle: The bundle requiring the service
        @param reference: A service reference
        @return: The requested service
        @raise BundleException: The service could not be found
        @raise TypeError: The argument is not a ServiceReference object
        """
        if not isinstance(bundle, Bundle):
            raise TypeError("A Bundle object must be given")

        if not isinstance(reference, ServiceReference):
            raise TypeError("A ServiceReference object must be given")

        if reference not in self.__registry:
            raise BundleException("Service not found (reference: %s)" \
                                  % reference)

        # Be sure to have the instance
        service = self.__registry[reference]

        # Indicate the dependency
        bundle.uses_service(reference)
        reference.used_by(bundle)

        return service


    def get_symbolic_name(self):
        """
        Retrieves the framework symbolic name
        
        @return: Always "org.psem2m.pelix"
        """
        return "org.psem2m.pelix"


    def install_bundle(self, location):
        """
        Installs the bundle from the given location
        
        @param location: A bundle location
        @return: The installed bundle ID
        @raise BundleException: Something happened
        """
        # Load the module
        try:
            module = __import__(location)
        except ImportError as ex:
            raise BundleException(ex)

        # Compute the bundle ID
        bundle_id = self.__next_bundle_id
        self.__next_bundle_id += 1

        # Prepare the bundle and its context
        bundle = Bundle(self, bundle_id, module)
        bundle.set_context(BundleContext(self, bundle))

        # Store the bundle
        self.__bundles[bundle_id] = bundle

        return bundle_id


    def register_service(self, bundle, clazz, service, properties, send_event):
        """
        Registers a service and calls the listeners
        
        @param bundle: The bundle registering the service
        @param clazz: Name(s) of the interface(s) implemented by service
        @param properties: Service properties
        @param send_event: If not, doesn't trigger a service registered event
        @return: A ServiceRegistration object
        @raise BundleException: An error occurred while registering the service
        """
        if bundle is None or service is None or not clazz:
            raise BundleException("Invalid registration parameters")

        if not isinstance(properties, dict):
            # Be sure we have a valid dictionary
            properties = {}

        # Prepare the class specification
        if not isinstance(clazz, list):
            if not isinstance(clazz, str):
                # Invalid class name
                raise BundleException("Invalid class name : %s" % clazz)

            else:
                # Make it a list
                clazz = [clazz]

        # Prepare properties
        service_id = self.__next_service_id
        self.__next_service_id += 1

        properties[OBJECTCLASS] = clazz
        properties[SERVICE_ID] = service_id

        # Make the service reference
        ref = ServiceReference(bundle, properties)

        # Store it in global registry
        self.__registry[ref] = service

        # Make the service registration
        registration = ServiceRegistration(self, ref, properties)

        # Store it in the bundle registry
        bundle.store_registration(registration)

        if send_event:
            # Call the listeners
            event = ServiceEvent(ServiceEvent.REGISTERED, ref)
            self.fire_service_event(event)

        return registration


    def remove_bundle_listener(self, listener):
        """
        Unregisters a bundle listener
        
        @param listener: The bundle listener
        """
        _remove_listener(self.__bundle_listeners, listener)


    def remove_service_listener(self, listener):
        """
        Unregisters a service listener
        
        @param listener: The service listener
        """
        _remove_listener(self.__service_listeners, listener)

        if listener in self.__service_listeners_filters:
            del self.__service_listeners_filters[listener]


    def start(self):
        """
        Starts the framework
        
        @raise BundleException: A bundle failed to start
        """
        # Start all registered bundles
        for bundle in self.__bundles.values():
            bundle.start()


    def stop(self):
        """
        Stops the framework
        """

        i = self.__next_bundle_id
        while i > 0:

            bundle = self.__bundles.get(i, None)
            i -= 1

            if bundle is None or bundle.get_state() != Bundle.ACTIVE:
                # Ignore inactive bundle
                continue

            try:
                bundle.stop()

            except:
                # Just log exceptions
                _logger.exception("Error stopping bundle %s" \
                                  % bundle.get_symbolic_name())



    def uninstall(self):
        """
        A framework can't be uninstalled
        
        @raise BundleException: This method must not be called
        """
        raise BundleException("A framework can't be uninstalled")


    def uninstall_bundle(self, bundle):
        """
        Ends the uninstallation of the given bundle (must be called by Bundle)
        
        @param bundle: The bundle to uninstall
        @raise BundleException: Invalid bundle
        """
        if bundle is None:
            # Do nothing
            return

        bundle_id = bundle.get_bundle_id()
        if bundle_id not in self.__bundles:
            raise BundleException("Invalid bundle %s" % bundle)

        # Notify listeners
        self.fire_bundle_event(BundleEvent(BundleEvent.UNINSTALLED, bundle))

        # Remove it from the dictionary
        del self.__bundles[bundle_id]

        # Clean up services from this bundle
        for reference in self.__registry:
            if reference.get_bundle() is bundle:
                # Unregister this remaining service
                self.unregister_service(reference)

            elif bundle in reference.get_using_bundles():
                # Bundle referenced this service, but not anymore
                reference.unused_by(bundle)


    def unregister_service(self, reference):
        """
        Unregisters the given service
        
        @param reference: Reference to the service to unregister
        @raise BundleException: Invalid reference
        """
        if reference not in self.__registry:
            raise BundleException("Invalid service reference")

        # Call the listeners **first**
        event = ServiceEvent(ServiceEvent.UNREGISTERING, reference)
        self.fire_service_event(event)

        # Remove it from the registry
        del self.__registry[reference]


    def update(self):
        """
        Stops and starts the framework
        """
        self.stop()
        self.start()

# ------------------------------------------------------------------------------

class BundleContext:
    """
    Represents a bundle context
    """
    def __init__(self, framework, bundle):
        """
        Sets up the bundle context
        
        @param framework: Hosting framework
        @param bundle: The associated bundle
        """
        self.__bundle = bundle
        self.__framework = framework


    def add_bundle_listener(self, listener):
        """
        Registers a bundle listener
        """
        self.__framework.add_bundle_listener(listener)


    def add_service_listener(self, listener):
        """
        Registers a service listener
        """
        self.__framework.add_service_listener(listener)


    def get_all_service_references(self, clazz, ldap_filter):
        """
        Returns an array of ServiceReference objects.
        The returned array of ServiceReference objects contains services that
        were registered under the specified class and match the specified filter
        expression.
        """
        return self.__framework.find_service_references(clazz, ldap_filter)


    def get_bundle(self, bundle_id=None):
        """
        Retrieves the bundle with the given ID. If no ID is given (None).
        
        @param bundle_id: A bundle ID
        @return: The requested bundle
        @raise BundleException: The given ID is invalid
        """
        if bundle_id is None:
            # Current bundle
            return self.__bundle

        return self.__framework.get_bundle_by_id(bundle_id)


    def get_bundles(self):
        """
        Returns a list of all installed bundles
        """
        return self.__framework.get_bundles()


    def get_property(self, name):
        """
        Returns the value of a property of the framework, else returns the OS
        environment value.
        
        @param name: A property name
        """
        return self.__framework.get_property(name)


    def get_service(self, reference):
        """
        Returns the service described with the given reference
        """
        return self.__framework.get_service(self.__bundle, reference)


    def get_service_reference(self, clazz):
        """
        Returns a ServiceReference object for a service that implements and \
        was registered under the specified class
        
        @param clazz: The class name with which the service was registered.
        @return: A service reference, None if not found
        """
        refs = self.__framework.find_service_references(clazz, None)
        if len(refs) != 0:
            return refs[0]

        return None


    def get_service_references(self, clazz, ldap_filter):
        """
        Returns the service references for services that were registered under
        the specified class by this bundle and matching the given filter
        """
        refs = self.__framework.find_service_references(clazz, None)
        for ref in refs:
            if ref.get_bundle() is not self.__bundle:
                refs.remove(ref)

        return refs


    def install_bundle(self, location):
        """
        Installs the bundle at the given location
        
        @param location: Location of the bundle to install
        @return: The installed bundle ID
        @raise BundleException: An error occurred while installing the bundle
        """
        return self.__framework.install_bundle(location)


    def register_service(self, clazz, service, properties, send_event=True):
        """
        Registers a service
        
        @param clazz: Class or Classes (list) implemented by this service
        @param service: The service instance
        @param properties: The services properties (dictionary)
        @param send_event: If not, doesn't trigger a service registered event
        @return: A ServiceRegistration object
        @raise BundleException: An error occurred while registering the service
        """
        return self.__framework.register_service(self.__bundle, clazz, \
                                                 service, properties, \
                                                 send_event)


    def remove_bundle_listener(self, listener):
        """
        Unregisters a bundle listener
        """
        self.__framework.remove_bundle_listener(listener)


    def remove_service_listener(self, listener):
        """
        Unregisters a service listener
        """
        self.__framework.remove_service_listener(listener)


    def unget_service(self, reference):
        """
        Disables a reference to the service
        """
        # Lose the dependency
        self.__bundle.unuses_service(reference)
        reference.unused_by(self.__bundle)


# ------------------------------------------------------------------------------

class ServiceReference:
    """
    Represents a reference to a service
    """
    def __init__(self, bundle, properties):
        """
        Sets up the service reference
        
        @param bundle: The bundle registering the service
        @param properties: The service properties
        @raise BundleException: The properties doesn't contain mandatory entries
        """
        
        for mandatory in (SERVICE_ID, OBJECTCLASS):
            if mandatory not in properties:
                raise BundleException( \
                            "A Service must at least have a '%s' entry" \
                            % mandatory)

        self.__bundle = bundle
        self.__properties = properties
        self.__using_bundles = []
    
    
    def __str__(self):
        """
        String representation
        """
        return "ServiceReference(%s, %d, %s)" \
                % (self.__properties[SERVICE_ID], \
                   self.__bundle.get_bundle_id(), \
                   self.__properties[OBJECTCLASS])


    def __hash__(self):
        """
        Returns the service hash
        """
        return self.__properties.get(SERVICE_ID, -1)


    def __cmp__(self, other):
        """
        ServiceReference comparison
        
        See : http://www.osgi.org/javadoc/r4v43/org/osgi/framework/ServiceReference.html#compareTo%28java.lang.Object%29
        """
        if not isinstance(other, ServiceReference):
            # Not comparable => "equals"
            return 0

        service_id = int(self.__properties.get(SERVICE_ID, 0))
        other_id = int(other.__properties.get(SERVICE_ID, 0))

        if service_id == other_id:
            # Same ID, same service
            return 0

        service_rank = int(self.__properties.get(SERVICE_RANKING, 65535))
        other_rank = int(other.__properties.get(SERVICE_RANKING, 65535))

        if service_rank == other_rank:
            # Same rank, ID discriminates (greater ID, lesser reference)
            if service_id > other_id:
                return -1
            else:
                return 1

        elif service_rank < other_rank:
            # Lesser rank value, greater reference
            return 1

        else:
            return -1


    def __eq__(self, other):
        """
        Equal to other
        """
        return self.__cmp__(other) == 0


    def __ge__(self, other):
        """
        Greater or equal
        """
        return self.__cmp__(other) >= 0


    def __gt__(self, other):
        """
        Greater than other
        """
        return self.__cmp__(other) > 0


    def __le__(self, other):
        """
        Lesser or equal
        """
        return self.__cmp__(other) <= 0


    def __lt__(self, other):
        """
        Lesser than other
        """
        return self.__cmp__(other) < 0


    def get_bundle(self):
        """
        Retrieves the bundle that registered this service
        """
        return self.__bundle


    def get_using_bundles(self):
        """
        Retrieves the bundles that use this service
        """
        return self.__using_bundles


    def get_properties(self):
        """
        Retrieves a copy of the service properties
        """
        return self.__properties.copy()


    def get_property(self, name):
        """
        Retrieves the property value for the given name
        
        @return: The property value, None if not found
        """
        if name not in self.__properties:
            return None

        return self.__properties[name]


    def get_property_keys(self):
        """
        Returns an array of the keys in the properties of the service
        
        @return: An array of property keys.
        """
        return self.__properties.keys()


    def unused_by(self, bundle):
        """
        Indicates that this reference is not being used anymore by the given 
        bundle
        
        @param bundle: A bundle that used this reference
        """
        if bundle is None or bundle is self.__bundle:
            # Ignore
            return

        if bundle in self.__using_bundles:
            self.__using_bundles.remove(bundle)


    def used_by(self, bundle):
        """
        Indicates that this reference is being used by the given bundle
        
        @param bundle: A bundle using this reference
        """
        if bundle is None or bundle is self.__bundle:
            # Ignore
            return

        if bundle not in self.__using_bundles:
            self.__using_bundles.append(bundle)

# ------------------------------------------------------------------------------

class ServiceRegistration:
    """
    Represents a service registration object
    """
    def __init__(self, framework, reference, properties):
        """
        Sets up the service registration object
        
        @param framework: The host framework
        @param reference: A service reference
        @param properties: A reference to the service properties dictionary
        """
        self.__framework = framework
        self.__reference = reference
        self.__properties = properties


    def get_reference(self):
        """
        Retrieves the reference associated to this registration
        """
        return self.__reference


    def set_properties(self, properties):
        """
        Updates the service properties
        
        @param properties: The new properties
        @raise TypeError: The argument is not a dictionary
        """
        if not isinstance(properties, dict):
            raise TypeError("Waiting for dictionary")

        # Keys that must not be updated
        forbidden_keys = (OBJECTCLASS, SERVICE_ID)

        for forbidden_key in forbidden_keys:
            if forbidden_key in properties:
                del properties[forbidden_key]

        to_delete = []
        for key, value in properties.items():
            if self.__properties.get(key, None) == value:
                # No update
                to_delete.append(key)
        
        for key in to_delete:
            del properties[key]

        if not properties:
            # Nothing to do
            return

        # Update the properties
        previous = self.__properties.copy()
        self.__properties.update(properties)

        # Trigger a new computation in the framework
        self.__framework.fire_service_event(\
                        ServiceEvent(ServiceEvent.MODIFIED, self.__reference, \
                                     previous))


    def unregister(self):
        """
        Unregisters the service
        """
        self.__framework.unregister_service(self.__reference)

# ------------------------------------------------------------------------------

class BundleEvent:
    """
    Represents a bundle event
    """

    INSTALLED = 1
    """The bundle has been installed."""

    RESOLVED = 32
    """The bundle has been resolved."""

    STARTED = 2
    """The bundle has been started."""

    STARTING = 128
    """The bundle is about to be activated."""

    STOPPED = 4
    """The bundle has been stopped."""

    STOPPING = 256
    """The bundle is about to deactivated."""

    UNINSTALLED = 16
    """The bundle has been uninstalled."""

    UNRESOLVED = 64
    """The bundle has been unresolved."""

    UPDATED = 8
    """The bundle has been updated."""


    def __init__(self, kind, bundle):
        """
        Sets up the event
        """
        self.__kind = kind
        self.__bundle = bundle


    def get_bundle(self):
        """
        Retrieves the modified bundle
        """
        return self.__bundle


    def get_kind(self):
        """
        Retrieves the kind of event
        """
        return self.__kind

# ------------------------------------------------------------------------------

class ServiceEvent:
    """
    Represents a service event
    """

    REGISTERED = 1
    """ This service has been registered """

    MODIFIED = 2
    """ The properties of a registered service have been modified """

    UNREGISTERING = 4
    """ This service is in the process of being unregistered """

    MODIFIED_ENDMATCH = 8
    """
    The properties of a registered service have been modified and the new
    properties no longer match the listener's filter
    """

    def __init__(self, kind, reference, previous_properties=None):
        """
        Sets up the event
        
        @param kind: Kind of event
        @param reference: Reference to the modified service
        @param previous_properties: Previous service properties (for MODIFIED
        and MODIFIED_ENDMATCH events)
        """
        self.__kind = kind
        self.__reference = reference

        if previous_properties is not None \
        and not isinstance(previous_properties, dict):
            # Accept None or dict() only
            previous_properties = {}

        self.__previous_properties = previous_properties


    def __str__(self):
        """
        String representation
        """
        return "ServiceEvent(%s, %s)" % (self.__kind, \
                                         str(self.__reference))


    def get_previous_properties(self):
        """
        Previous service properties, meaningless if the the event is not
        MODIFIED nor MODIFIED_ENDMATCH.
        """
        return self.__previous_properties


    def get_service_reference(self):
        """
        Retrieves the service reference
        """
        return self.__reference


    def get_type(self):
        """
        Retrieves the kind of service event
        """
        return self.__kind

# ------------------------------------------------------------------------------

class FrameworkFactory:
    """
    A framework factory
    """

    __singleton = None
    """ The framework singleton """

    @staticmethod
    def get_framework(properties={}):
        """
        If it doesn't exist yet, creates a framework with the given properties,
        else returns the current framework instance.
        
        @return: A Pelix instance
        """
        if FrameworkFactory.__singleton is None:
            FrameworkFactory.__singleton = Framework(properties)

        return FrameworkFactory.__singleton

if __name__ == "__main__":
    FrameworkFactory.get_framework()
