"""
Created on 26 janv. 2012

@author: Thomas Calmant
"""

from psem2m import ldapfilter
from psem2m.component import constants
from psem2m.services import pelix
from psem2m.services.pelix import BundleContext, ServiceEvent
from psem2m.utilities import remove_all_occurrences, SynchronizedClassMethod
import logging
import threading

# ------------------------------------------------------------------------------

# Prepare the module logger
_logger = logging.getLogger("ipopo.core")

# ------------------------------------------------------------------------------

class _Registry:
    """
    The iPOPO Component registry singleton
    """
    # Factories : Name -> Factory class
    factories = {}

    # All instances : Name -> _StoredInstance
    instances = {}

    def __init__(self):
        """
        Constructor that **must never be called**
        """
        raise RuntimeError("The _Registry constructor must never be called")

# ------------------------------------------------------------------------------

class FactoryContext:
    """
    Represents the data stored in a component factory (class)
    """

    # Try to reduce memory footprint (stored instances)
    __slots__ = ('bundle_context', 'callbacks', 'factory_name', 'properties', \
                 'properties_fields', 'provides', 'requirements')

    def __init__(self, bundle_context):
        """
        Sets up the factory context
        """
        assert isinstance(bundle_context, BundleContext)

        self.bundle_context = bundle_context
        self.callbacks = {}
        self.properties = {}
        self.properties_fields = {}
        self.provides = []
        self.requirements = {}


class ComponentContext:
    """
    Represents the data stored in a component instance
    """

    # Try to reduce memory footprint (stored instances)
    __slots__ = ('__factory_context', 'name', 'properties')

    def __init__(self, factory_context, name, properties):
        """
        Sets up the context
        
        @param factory_context: The parent factory context
        @param properties: The component properties
        """
        assert isinstance(factory_context, FactoryContext)
        assert isinstance(properties, dict)

        self.__factory_context = factory_context
        self.name = name

        # Force the instance name property
        properties[constants.IPOPO_INSTANCE_NAME] = name

        self.properties = factory_context.properties.copy()
        self.properties.update(properties)


    def get_bundle_context(self):
        """
        Retrieves the bundle context
        """
        return self.__factory_context.bundle_context


    def get_callback(self, event):
        """
        Retrieves the registered method for the given event. Returns None if not
        found
        """
        return self.__factory_context.callbacks.get(event, None)


    def get_provides(self):
        """
        Retrieves the services that this component provides
        """
        return self.__factory_context.provides


    def get_requirements(self):
        """
        Retrieves the component requirements
        """
        return self.__factory_context.requirements


class _StoredInstance:
    """
    Represents a component instance
    """

    # Try to reduce memory footprint (stored instances)
    __slot__ = ('bindings', 'context', 'factory', 'instance', 'name', \
                'registration', 'state', '_lock')

    INVALID = 0
    """ This component has been invalidated """

    VALID = 1
    """ This component has been validated """

    KILLED = 2
    """ This component has been killed """


    def __init__(self, factory_name, context, instance):
        """
        Sets up the instance object
        """
        assert isinstance(context, ComponentContext)

        # The lock
        self._lock = threading.RLock()

        # Component context
        self.context = context

        # The instance name
        self.name = self.context.name

        # Factory name
        self.factory = factory_name

        # Component instance
        self.instance = instance

        # Field -> [Service reference(s)]
        self.bindings = {}

        # The provided service registration
        self.registration = None

        # Set the instance state
        self.state = _StoredInstance.INVALID

        # Register to the service events
        self.context.get_bundle_context().add_service_listener(self)


    def __repr__(self):
        """
        String representation
        """
        return self.__str__()


    def __str__(self):
        """
        String representation
        """
        return "StoredInstance(%s, %d)" % (self.name, self.state)


    @SynchronizedClassMethod('_lock')
    def callback(self, event, *args, **kwargs):
        """
        Calls the registered method in the component for the given event
        
        @param event: An event (IPOPO_CALLBACK_VALIDATE, ...)
        @return: The callback result, or None
        @raise Exception: Something went wrong
        """
        comp_callback = self.context.get_callback(event)
        if not comp_callback:
            # No registered callback
            return None

        # Call it
        return comp_callback(self.instance, *args, **kwargs)


    @SynchronizedClassMethod('_lock')
    def invalidate(self):
        """
        Does the post-invalidation job. Unregisters the provided service(s), if
        any
        """
        if self.state != _StoredInstance.VALID:
            # Instance is not running...
            return

        # Change the state
        self.state = _StoredInstance.INVALID

        if self.registration is not None:
            self.registration.unregister()
            self.registration = None


    @SynchronizedClassMethod('_lock')
    def kill(self):
        """
        This instance is killed : invalidate it if needed, clean up all members
        """
        self.invalidate()

        # Change the state
        self.state = _StoredInstance.KILLED

        # Clean up members
        self.bindings.clear()
        del self.context
        del self.instance


    @SynchronizedClassMethod('_lock')
    def safe_callback(self, event, *args, **kwargs):
        """
        Calls the registered method in the component for the given event,
        ignoring raised exceptions
        
        @param event: An event (IPOPO_CALLBACK_VALIDATE, ...)
        @return: The callback result, or None
        """
        callback = self.context.get_callback(event)
        if not callback:
            # No registered callback
            return None

        try:
            return self.callback(event, *args, **kwargs)

        except:
            _logger.exception("Component '%s' : error calling callback " \
                               "method for event %s" % (self.name, event))
            return None


    @SynchronizedClassMethod('_lock')
    def __set_binding(self, field, requirement, reference):
        """
        Injects the given service into the given field
        
        @param field: The field where the service is injected
        @param requirement: The field requirement description
        @param reference: The injected service reference
        """
        current_value = getattr(self.instance, field, None)

        if requirement.aggregate:
            # Aggregation
            if current_value is not None:
                if not isinstance(current_value, list):
                    # Invalid field content
                    _logger.error("%s : The injected field %s must be a " \
                                  "list, not %s", self.name, field, \
                                  type(current_value).__name__)
                    return

            else:
                # No previous value
                current_value = []

        # Get the service instance
        service = self.context.get_bundle_context().get_service(reference)

        if requirement.aggregate:
            # Append the service to the list and inject the whole list
            current_value.append(service)
            setattr(self.instance, field, current_value)

        else:
            # Inject the service directly in the field
            setattr(self.instance, field, service)

        # Keep track of the bound reference
        if field in self.bindings:
            self.bindings[field].append(reference)

        else:
            # Create the list if the needed
            self.bindings[field] = [reference]

        # Call the component back
        self.safe_callback(constants.IPOPO_CALLBACK_BIND, service)


    @SynchronizedClassMethod('_lock')
    def __set_multiple_binding(self, field, current_value, requirement, \
                               references):
        """
        Injects multiple services in a field in one time. Only works with
        aggregations.
        
        @param field: The field where to inject the services
        @param current_value: Current field value (should be None or a list)
        @param requirement: Dependency description
        @param references: Injected services references (must be a list)
        """
        if not requirement.aggregate:
            # Not an aggregation...
            _logger.error("%s: field '%s' is not an aggregation", \
                          self.name, field)
            return

        if not isinstance(references, list):
            # Bad references
            _logger.error("%s: Invalid references list type %s", \
                          self.name, type(references).__name__)

        if current_value is not None and not isinstance(current_value, list):
            # Injected field as the right type
            _logger.error("%s: field '%s' must be a list", \
                          self.name, field)

            return

        # Special case for a list : we must ignore already injected
        # references
        if field in self.bindings:
            refs = [reference for reference in references \
                    if reference not in self.bindings[field]]

        if not refs:
            # Nothing to add, ignore this field
            return

        # Prepare the injected value
        if current_value is not None:
            injected = current_value

        else:
            injected = []

        # Compute the bound services
        bundle_context = self.context.get_bundle_context()
        bound = [bundle_context.get_service(reference) for reference in refs]

        # Set the field
        setattr(self.instance, field, injected)

        # Add dependency marker
        bindings = self.bindings.get(field, [])
        bindings.extend(refs)

        for service in bound:
            # Inject the service
            injected.append(service)

            # Call Bind
            self.safe_callback(constants.IPOPO_CALLBACK_BIND, service)


    @SynchronizedClassMethod('_lock')
    def __unset_binding(self, field, requirement, reference, service):
        """
        Remove the given service from the given field
        
        @param field: The field where the service is injected
        @param requirement: The field requirement description
        @param reference: The injected service reference
        @param service: The injected service instance
        """
        current_value = getattr(self.instance, field, None)
        if current_value is None:
            # Nothing to do...
            return

        if requirement.aggregate and not isinstance(current_value, list):
            # Aggregation, but invalid field content
            _logger.error("%s : The injected field %s must be a " \
                           "list, not %s", self.name, field, \
                           type(current_value).__name__)
            return

        # Call the component back
        self.safe_callback(constants.IPOPO_CALLBACK_UNBIND, service)

        if requirement.aggregate:
            # Remove the service from the list
            remove_all_occurrences(current_value, service)
            if len(current_value) == 0:
                # Don't keep empty lists
                setattr(self.instance, field, None)

        else:
            # Set single references to None
            setattr(self.instance, field, None)


    @SynchronizedClassMethod('_lock')
    def update_bindings(self):
        """
        Updates the bindings of the given component
        
        @return: True if the component can be validated
        """
        # Get the requirement, or an empty dictionary
        requirements = self.context.get_requirements()
        if not requirements:
            # No requirements : nothing to do
            return True

        all_bound = True
        component = self.instance
        bundle_context = self.context.get_bundle_context()

        for field, requires in requirements.items():
            # For each field
            current_value = getattr(component, field, None)
            if not requires.aggregate and current_value is not None:
                # A dependency is already injected
                _logger.debug("%s: Field '%s' already bound", \
                              self.name, field)
                continue

            # Find possible services (specification test is already in filter
            refs = bundle_context.get_all_service_references(None, \
                                                             requires.filter)

            if not refs:
                if not requires.optional:
                    # Required link not found
                    _logger.debug("%s: Missing requirement for field %s", \
                                  self.name, field)
                    all_bound = False

                continue

            if requires.aggregate:
                # Aggregation
                self.__set_multiple_binding(field, current_value, requires, refs)

            else:
                # Normal field, bind the first reference
                self.__set_binding(field, requires, refs[0])

        return all_bound

    @SynchronizedClassMethod('_lock')
    def update_property(self, name, old_value, new_value):
        """
        Handles a property changed event
        
        @param name: The changed property name
        @param old_value: The previous property value
        @param new_value: The new property value 
        """
        if self.registration is not None:
            # use the registration to trigger the service event
            self.registration.set_properties({name: new_value})


    @SynchronizedClassMethod('_lock')
    def validate(self):
        """
        Ends the component validation, registering services
        
        @raise RuntimeError: You try to awake a dead component
        """
        if self.state == _StoredInstance.VALID:
            # No work to do
            return

        if self.state == _StoredInstance.KILLED:
            raise RuntimeError("%s: Zombies !" % self.context.name)

        bundle_context = self.context.get_bundle_context()
        provides = self.context.get_provides()

        # All good
        self.state = _StoredInstance.VALID

        if not provides:
            # Nothing registered
            self.registration = None

        else:
            self.registration = bundle_context.register_service(\
                                                self.context.get_provides(), \
                                                self.instance, \
                                                self.context.properties.copy(), \
                                                True)


    @SynchronizedClassMethod('_lock')
    def service_changed(self, event):
        """
        Called by Pelix when some service properties changes
        
        @param event: A ServiceEvent object
        """
        kind = event.get_type()
        reference = event.get_service_reference()
        bundle_context = self.context.get_bundle_context()

        if kind == ServiceEvent.REGISTERED:
            # Maybe a new dependency...
            can_validate = (self.state != _StoredInstance.VALID)

            for field, requires in self.context.get_requirements().items():

                if reference in self.bindings.get(field, []):
                    # Reference already known, ignore it
                    continue

                field_value = getattr(self.instance, field, None)

                if not requires.aggregate and field_value is not None:
                    # Field already injected
                    continue

                if requires.matches(reference.get_properties()):
                    # Inject the service
                    self.__set_binding(field, requires, reference)

                elif can_validate and not requires.optional \
                and field_value is None:
                    # Missing a required dependency
                    can_validate = False

            if can_validate:
                # ... even a validating dependency
                self.safe_callback(constants.IPOPO_CALLBACK_VALIDATE)
                self.validate()

        elif kind == ServiceEvent.UNREGISTERING:
            # A dependency may be gone...
            invalidate = False

            for field, binding in self.bindings.items():
                if reference in binding:
                    # We were using this dependency
                    service = bundle_context.get_service(reference)
                    field_value = getattr(self.instance, field)
                    requirement = self.context.get_requirements()[field]

                    if not invalidate and not requirement.optional:
                        if not requirement.aggregate or len(field_value) == 1:
                            # Last reference for a required field : invalidate
                            invalidate = True
                            self.safe_callback(\
                                        constants.IPOPO_CALLBACK_INVALIDATE)

                    # Remove the entry
                    self.__unset_binding(field, requirement, reference, service)

                    # Free the reference to the service
                    bundle_context.unget_service(reference)

            # Finish the invalidation
            if invalidate:
                self.invalidate()

            # Ask for a new chance...
            if self.update_bindings() and invalidate:
                self.safe_callback(constants.IPOPO_CALLBACK_VALIDATE)
                self.validate()


        elif kind == ServiceEvent.MODIFIED:
            # Modified service property
            invalidate = False

            to_remove = []
            for field, binding in self.bindings.items():
                if reference in binding:
                    # We are using this dependency
                    requirement = self.context.get_requirements()[field]

                    if requirement.matches(reference.get_properties()):
                        # The service still corresponds to the field, ignore
                        continue

                    # We lost it... (yeah, same as above, I know)
                    service = bundle_context.get_service(reference)
                    field_value = getattr(self.instance, field)

                    if not invalidate and not requirement.optional:
                        if not requirement.aggregate or len(field_value) == 1:
                            # Last reference for a required field : invalidate
                            invalidate = True
                            self.safe_callback(\
                                        constants.IPOPO_CALLBACK_INVALIDATE)

                    # Remove the entry
                    self.__unset_binding(field, requirement, reference, service)

                    to_remove.append((field, service))

                    # Free the reference to the service
                    bundle_context.unget_service(reference)

            # Finish the removal
            for field, service in to_remove:
                remove_all_occurrences(self.bindings[field], service)
                if len(self.bindings[field]) == 0:
                    # Don't keep empty lists
                    del self.bindings[field]

            # Finish the invalidation
            if invalidate:
                self.invalidate()

            # Ask for a new chance...
            if self.update_bindings() and invalidate:
                self.safe_callback(constants.IPOPO_CALLBACK_VALIDATE)
                self.validate()

# ------------------------------------------------------------------------------

class Requirement:
    """
    Represents a component requirement
    """
    def __init__(self, specification=None, aggregate=False, optional=False, \
                 spec_filter=None):
        """
        Sets up the requirement
        
        @param specification: The requirement specification (can't be None)
        @param aggregate: If true, this requirement represents a list
        @param optional: If true, this requirement is optional
        @param spec_filter: A filter to select dependencies
        
        @raise TypeError: A parameter has an invalid type
        @raise ValueError: An error occurred while parsing the filter
        """
        if isinstance(specification, type):
            specification = specification.__name__

        if not specification:
            raise TypeError("A specification must be given")

        if not isinstance(specification, str):
            raise TypeError("The requirement specification must be a string")

        self.aggregate = aggregate
        self.optional = optional
        self.specification = specification

        # Make the filter, escaping the specification name
        ldap_filter = "(%s=%s)" % (pelix.OBJECTCLASS, \
                                   ldapfilter.escape_LDAP(specification))

        if isinstance(spec_filter, str) and len(spec_filter) > 0:
            # String given
            ldap_filter = ldapfilter.combine_filters([ldap_filter, spec_filter])

        # Parse the filter
        self.filter = ldapfilter.get_ldap_filter(ldap_filter)


    def matches(self, properties):
        """
        Tests if the given _StoredInstance matches this requirement
        
        @param properties: Service properties
        @return: True if the instance matches this requirement
        """
        if properties is None:
            # No properties : invalid service
            return False

        assert(isinstance(properties, dict))

        # Properties filter test
        return self.filter.matches(properties)

# ------------------------------------------------------------------------------

def register_factory(factory_name, factory):
    """
    Registers a component factory
    
    @param factory_name: The name of the factory
    @param factory: The factory class object
    @raise ValueError: The factory name is invalid
    @raise TypeError: The factory object is invalid
    """
    if not factory_name or not isinstance(factory_name, str):
        raise ValueError("Factory name must be a non-empty string")

    if factory is None or not isinstance(factory, type):
        raise TypeError("The factory '%s' must be a type" % factory_name)


    if factory_name in _Registry.factories:
        _logger.warning("The factory %s has already been registered",
                        factory_name)

    _Registry.factories[factory_name] = factory
    _logger.info("Factory '%s' registered", factory_name)


def unregister_factory(factory_name):
    """
    Unregisters the given component factory
    
    @param factory_name: Name of the factory to unregister
    """
    if not factory_name:
        return

    if factory_name in _Registry.factories:
        # Invalidate and delete all components of this factory
        _unregistration_loop(factory_name)
        del _Registry.factories[factory_name]
        _logger.info("Factory '%s' removed", factory_name)

# ------------------------------------------------------------------------------

def instantiate(factory_name, name, properties={}):
    """
    Instantiates a component from the given factory, with the given name
    
    @param factory_name: Name of the component factory
    @param name: Name of the instance to be started
    @return: The component instance
    @raise TypeError: The given factory is unknown
    @raise ValueError: The given name or factory name is invalid
    @raise Exception: Something wrong occurred in the factory
    """
    # Test parameters
    if not factory_name:
        raise ValueError("Invalid factory name")

    if factory_name not in _Registry.factories:
        raise TypeError("Unknown factory '%s'" % factory_name)

    if not name:
        raise ValueError("Invalid component name")

    if name in _Registry.instances:
        raise ValueError("'%s' is an already running instance name" % name)

    factory = _Registry.factories[factory_name]
    if factory is None:
        raise TypeError("Null factory registered '%s'" % factory_name)

    # Get the factory context
    factory_context = getattr(factory, constants.IPOPO_FACTORY_CONTEXT, None)
    if factory_context is None:
        raise TypeError("Factory context missing in '%s'" % factory_name)

    # Create component instance
    instance = factory()
    if instance is None:
        raise TypeError("Factory '%s' failed to create '%s'" \
                        % (factory_name, name))

    # Normalize given properties
    if properties is None or not isinstance(properties, dict):
        properties = {}

    # Set the instance context
    component_context = ComponentContext(factory_context, name, properties)
    setattr(instance, constants.IPOPO_COMPONENT_CONTEXT, component_context)

    # Prepare the stored instance
    stored_instance = _StoredInstance(factory_name, component_context, instance)

    # Store the instance
    _Registry.instances[name] = stored_instance

    # Try to validate it
    if stored_instance.update_bindings():
        try:
            stored_instance.callback(constants.IPOPO_CALLBACK_VALIDATE)

            # End the validation on success...
            stored_instance.validate()

        except Exception:
            # Log the error
            _logger.exception("Error validating '%s'", stored_instance.name)

    return instance


def invalidate(name):
    """
    Invalidates the given component
    
    @param name: Name of the component to invalidate
    @raise ValueError: Invalid component name
    """
    stored_instance = __pop_instance(name)

    # Call the invalidate method (if any), ignoring errors
    stored_instance.safe_callback(constants.IPOPO_CALLBACK_INVALIDATE)

    # Finish the invalidation (unregister services may trigger events)
    stored_instance.invalidate()


def kill(name):
    """
    Kills the given component
    
    @param name: Name of the component to kill
    @raise ValueError: Invalid component name
    """
    stored_instance = __pop_instance(name)

    # Call the invalidate method (if any), ignoring errors
    stored_instance.safe_callback(constants.IPOPO_CALLBACK_INVALIDATE)

    # Kill it
    stored_instance.kill()


def __pop_instance(name):
    """
    Retrieves the _StoredInstance object with the given name and removes it
    from all lists
    
    @param name: Name of the component to pop
    @return: The popped instance
    @raise ValueError: Invalid component name 
    """
    if not name:
        raise ValueError("Invalid component name")

    if name not in _Registry.instances:
        raise ValueError("Unknown instance name '%s'" % name)

    # Get the stored instance
    stored_instance = _Registry.instances[name]

    # Remove it from lists
    del _Registry.instances[name]

    # Return it
    return stored_instance

# ------------------------------------------------------------------------------

def get_stored_instance(component_instance):
    """
    Retrieves the _StoredInstance corresponding to the given component instance
    
    @param component_instance: A component instance
    @return: The corresponding _StoredInstance object, None if not found
    """
    if component_instance is None:
        # Invalid parameter
        return None

    for stored_instance in _Registry.instances.values():
        if stored_instance.instance is component_instance:
            return stored_instance

    # Not found
    return None


def handle_property_changed(changed_component, name, old_value, new_value):
    """
    Handles a property changed event
    
    @param changed_component: The modified component
    @param name: The changed property name
    @param old_value: The previous property value
    @param new_value: The new property value 
    """
    # Get a reference to the changed component
    stored_instance = get_stored_instance(changed_component)

    if not stored_instance:
        # Not of out business...
        return

    stored_instance.update_property(name, old_value, new_value)


# ------------------------------------------------------------------------------

def _unregistration_loop(factory_name):
    """
    Invalidates all instances of the given factory
    
    Optimized method : avoid to try to revalidate or rebind dependent components
    while the factory components are deleted 
    """
    # Compute the list of instances to remove
    to_remove = [name \
                 for (name, stored_instance) in _Registry.instances.items() \
                 if stored_instance.factory == factory_name]

    if not to_remove:
        # Nothing to do
        return

    # Remove instances from the registry: avoids dependencies update to link
    # against a component from this factory again.
    for name in to_remove:
        kill(name)
