"""
Created on 18 janv. 2012

@author: Thomas Calmant
"""

import logging

import constants

# ------------------------------------------------------------------------------

_logger = logging.getLogger("ipopo.registry")

# ------------------------------------------------------------------------------

class _Registry:
    """
    The iPOPO Component registry singleton
    """
    # Factories : Name -> Factory class
    factories = {}

    # All instances : Name -> StoredInstance
    instances = {}

    # Instances waiting for validation (StoredInstance objects)
    waitings = []

    # Running instances (StoredInstance objects)
    running = []

    def __init__(self):
        """
        Constructor that **must never be called**
        """
        raise RuntimeError("The _Registry constructor must never be called")

# ------------------------------------------------------------------------------

class Requirement:
    """
    Represents a component requirement
    """
    def __init__(self, specification="", aggregate=False, optional=False, \
                 spec_filter=None):
        """
        Sets up the requirement
        """
        self.aggregate = aggregate
        self.specification = specification
        self.optional = optional
        self.filter = spec_filter

# ------------------------------------------------------------------------------

class StoredInstance:
    """
    Represents a component instance
    """
    def __init__(self, factory_name, name, instance):
        """
        Sets up the instance object
        """
        # Instance name
        self.name = name

        # Factory name
        self.factory = factory_name

        # Component instance
        self.instance = instance

        # Other instances this component depends on
        self.depends_on = []

        # Other instances this component depends on, optionally
        self.depends_on_optional = []

        # Other instances bound to this one
        self.used_by = []


    def get_properties(self):
        """
        Retrieves the component properties values
        
        @return: The component properties, or an empty dictionary
        """
        properties = getattr(self.instance, constants.IPOPO_PROPERTIES, {})
        if isinstance(properties, dict):
            return properties

        # Never return None
        return dict()


    def get_requirements(self):
        """
        Retrieves the requirements dictionary, or an empty dictionary
        
        @return: The requirements dictionary
        """
        requirements = getattr(self.instance, constants.IPOPO_REQUIREMENTS, {})

        if requirements is None:
            # Member forced to None, correct it
            return dict()

        assert(isinstance(requirements, dict))
        return requirements


    def update_bindings(self):
        """
        Updates the bindings of the given component
        
        @return: True if the component can be validated
        """
        # Get the requirement, or an empty dictionary
        component = self.instance
        requirements = self.get_requirements()
        if not requirements:
            # No requirements : do nothing
            return True

        all_bound = True

        for field, requires in requirements.items():
            # For each field

            current_value = getattr(component, field, None)
            if not requires.aggregate and current_value is not None:
                # A dependency is already injected
                _logger.debug("%s: Field '%s' already bound", self.name, field)
                continue

            # Find possible link(s)
            link = _find_requirement(requires)

            if not link:
                _logger.debug("%s: No link found : %s", self.name, field)

                if not requires.optional:
                    # Required link not found
                    all_bound = False

                continue

            # Set the field value
            if isinstance(link, list):
                # Aggregation
                if not isinstance(current_value, list):
                    # Injected field as the right type
                    _logger.error("%s: field '%s' must be a list", self.name, \
                                  field)

                    all_bound = False
                    continue

                # Special case for a list : we must remove already injected
                # references
                for element in link:
                    if element.instance in current_value:
                        link.remove(element)

                if not link:
                    # Nothing to add, ignore this field
                    continue

                # Prepare the injected value
                injected = [comp_inst.instance for comp_inst in link]

                # Set the field
                setattr(component, field, injected)

                # Add dependency marker
                self.set_dependency(link, requires.optional)

                # Call the bind callback
                for dependency in injected:
                    _callback(self, constants.IPOPO_CALLBACK_BIND, dependency)

            else:
                # Normal field
                # Set the instance
                setattr(component, field, link.instance)

                # Add dependency marker
                self.set_dependency(link, requires.optional)

                # Call the bind callback
                _callback(self, constants.IPOPO_CALLBACK_BIND, link.instance)

        return all_bound


    def set_dependency(self, dependency, optional=False):
        """
        Sets this instance to depend on the given one
        
        @param dependency: An instance this one depends on
        @param optional: Flag set up if the dependency is optional
        """
        if isinstance(dependency, StoredInstance):

                # Self depends on dependency
            if optional:
                if dependency not in self.depends_on_optional:
                    self.depends_on_optional.append(dependency)
            else:
                if dependency not in self.depends_on:
                    self.depends_on.append(dependency)

            if self not in dependency.used_by:
                # Dependency is used by self
                dependency.used_by.append(self)

        elif isinstance(dependency, list):
            # Multiple dependencies to handle
            for dependency_instance in dependency:
                self.set_dependency(dependency_instance)


    def unset_dependency(self, dependency):
        """
        Opposite of set_dependency()
        
        @param dependency: An instance this one doesn't depend on anymore
        """
        if isinstance(dependency, StoredInstance):
            # Remove self and dependency from lists
            remove_all_occurrences(self.depends_on, dependency)
            remove_all_occurrences(self.depends_on_optional, dependency)
            remove_all_occurrences(dependency.used_by, self)

            # Clean up fields
            self.__clean_up_dependency(dependency)

        elif isinstance(dependency, list):
            # Multiple dependencies to handle
            for dependency_instance in dependency:
                self.unset_dependency(dependency_instance)


    def __clean_up_dependency(self, dependency):
        """
        Removes the given dependency from all instance fields
        
        @param dependency: An instance this one doesn't depend on anymore
        """
        dependency_instance = dependency.instance

        for field, requires in self.get_requirements().items():
            value = getattr(self.instance, field, None)

            if value is None:
                # Useless field
                continue

            elif value is dependency_instance:
                # Direct value, set it to None
                setattr(self.instance, field, None)

            elif requires.aggregate and isinstance(value, list) \
            and dependency_instance in value:
                # Aggregation
                remove_all_occurrences(value, dependency_instance)


    def update_properties(self, properties):
        """
        Updates the component properties
        """
        if not isinstance(properties, dict):
            # Be sure we get a dictionary
            properties = {}

        # Always indicate the instance name
        properties[constants.IPOPO_INSTANCE_NAME] = self.name

        # Update component dictionary
        self.instance._ipopo_update_properties(properties)

# ------------------------------------------------------------------------------

def remove_all_occurrences(sequence, item):
    """
    Removes all occurrences of item in the given sequence
    
    @param sequence: The items list
    @param item: The item to be removed
    """
    if sequence is None:
        return

    while item in sequence:
        sequence.remove(item)

# ------------------------------------------------------------------------------

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

    # TODO: test all filters again



# ------------------------------------------------------------------------------

def register_factory(factory_name, factory):
    """
    Registers a component factory
    
    @param factory_name: The name of the factory
    @param factory: The factory class object
    @raise ValueError: The factory name is invalid
    @raise TypeError: The factory object is invalid
    """
    if not factory_name:
        raise ValueError("Factory factory_name can't be null")

    if factory is None or not isinstance(factory, type):
        raise TypeError("The factory '%s' must be a type" % factory_name)


    if factory_name in _Registry.factories:
        _logger.warning("The factory %s has already been registered (override)",
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

    if not name:
        raise ValueError("Invalid component name")

    if name in _Registry.instances:
        raise ValueError("'%s' is an already running instance name" % name)

    if factory_name not in _Registry.factories:
        raise TypeError("Unknown factory '%s'" % factory_name)

    factory = _Registry.factories[factory_name]
    if factory is None:
        raise TypeError("Null factory registered '%s'" % factory_name)

    # Create component instance
    instance = factory()
    if instance is None:
        raise TypeError("Factory '%s' failed to create '%s'" \
                        % (factory_name, name))

    # Store the instance
    stored_instance = StoredInstance(factory_name, name, instance)
    stored_instance.update_properties(properties)

    # Store the instance
    _Registry.instances[name] = stored_instance

    # Add the instance in the waiting queue
    _Registry.waitings.append(stored_instance)

    # Try to validate components
    _try_validation()

    # Second try to bind optional dependencies
    _try_new_bindings()

    return instance

# ------------------------------------------------------------------------------

def get_instance_by_name(name):
    """
    Retrieves the component instance with the given name, if any
    
    @param name: Name of the component instance
    @return: The component instance, None if not found
    """
    try:
        return _Registry.instances[name].instance

    except KeyError:
        # Not found
        return None


def get_stored_instance(component_instance):
    """
    Retrieves the StoredInstance corresponding to the given component instance
    
    @param component_instance: A component instance
    @return: The corresponding StoredInstance object, None if not found
    """
    if component_instance is None:
        # Invalid parameter
        return None

    for stored_instance in _Registry.instances.values():
        if stored_instance.instance is component_instance:
            return stored_instance

    # Not found
    return None

# ------------------------------------------------------------------------------

def _try_new_bindings():
    """
    Tries to bind new dependencies to running instances
    """
    for stored_instance in _Registry.running:
        stored_instance.update_bindings()


def _try_validation():
    """
    Tries to validate as many components as possible
    """
    at_least_one_validation = True

    # At least one validation has been done, call try_validation again
    while at_least_one_validation:

        at_least_one_validation = False
        validated = []

        for stored_instance in _Registry.waitings:
            if stored_instance.update_bindings():
                # Component can be validated
                _logger.info("%s can be validated" % stored_instance.name)
                validated.append(stored_instance)


        # Validation loop
        for stored_instance in validated:

            try:
                _callback(stored_instance, constants.IPOPO_CALLBACK_VALIDATE)

            except Exception:
                # Something wrong occurred
                _logger.error("Error validating '%s'", stored_instance.name, \
                              exc_info=True)

            else:
                # Remove it from the waiting list
                _Registry.waitings.remove(stored_instance)
                _Registry.running.append(stored_instance)
                at_least_one_validation = True


def _find_requirement(requirement):
    """
    Tries to find links for the given requirement
    """
    links = []
    required_spec = requirement.specification
    aggregate = requirement.aggregate

    for running_instance in _Registry.running:

        provides = getattr(running_instance.instance, \
                           constants.IPOPO_PROVIDES, [])
        if required_spec in provides:
            # Required specification found
            # TODO: test filter
            if not aggregate:
                # Single instance required
                return running_instance

            else:
                links.append(running_instance)

    if not links:
        # Don't return an empty list
        return None

    return links

# ------------------------------------------------------------------------------

def _callback(comp_instance, event, *args, **kwargs):
    """
    Calls the component callback method
    """
    component = comp_instance.instance
    callbacks = component._ipopo_callbacks
    if event in callbacks:
        callbacks[event](component, *args, **kwargs)


def _safe_callback(comp_instance, event, *args, **kwargs):
    """
    Calls the component callback method and logs raised exceptions
    """
    try:
        _callback(comp_instance, event, *args, **kwargs)

    except Exception:
        _logger.exception("Component '%s' : error calling callback method for" \
                          " event %s" % (comp_instance.name, event))

# ------------------------------------------------------------------------------

def _instance_invalidation(comp_instance):
    """
    Sets to None all instances references
    """
    # Get the requirement, or an empty dictionary
    requirements = getattr(comp_instance.instance, \
                           constants.IPOPO_REQUIREMENTS, {})

    dependencies = set(comp_instance.depends_on) ^ set(comp_instance.depends_on_optional)


    for dependency in dependencies:
        # Call unbind()
        _safe_callback(comp_instance, constants.IPOPO_CALLBACK_UNBIND,
                       dependency.instance)

        # Remove the dependency
        comp_instance.unset_dependency(dependency)

    # Set all fields to None
    for field in requirements:
        setattr(comp_instance.instance, field, None)


def _unregistration_loop(factory_name):
    """
    Invalidates all instances of the given factory
    """
    to_remove = [(name, instance) \
                 for (name, instance) in _Registry.instances.items() \
                 if instance.factory == factory_name]

    # Remove instances from the registry: avoids dependencies update to link
    # against a component from this factory again.
    for name, instance in to_remove:

        if name in _Registry.instances:
            del _Registry.instances[name]

        for storage in (_Registry.running, _Registry.waitings):
            if instance in storage:
                storage.remove(instance)

    # Update/Invalidate dependencies
    all_to_rebind = []
    all_to_revalidate = []

    for instance_tuple in to_remove:

        instance = instance_tuple[1]
        to_rebind = []
        to_revalidate = []

        for running in _Registry.running:

            if running in to_revalidate or running in all_to_revalidate:
                # Already invalidated
                continue

            # Found a depending component
            if instance in running.depends_on:
                # Non optional dependency : invalidate the component
                _safe_callback(running, constants.IPOPO_CALLBACK_INVALIDATE)

                # Calls unbind() and unsert_dependency() methods
                _instance_invalidation(running)

                # Prepare it to be revalidated
                to_revalidate.append(running)

            elif instance in running.depends_on_optional:
                # Calls its unbind method
                _safe_callback(running, constants.IPOPO_CALLBACK_UNBIND, \
                               instance.instance)

                # Remove the dependency
                running.unset_dependency(instance)

                # Prepare it to be rebound
                if running not in to_rebind:
                    to_rebind.append(running)

        for running in to_revalidate:
            # Change the component state in the registry 
            _Registry.running.remove(running)
            _Registry.waitings.append(running)

            # Make the instance appear only once
            if running in to_rebind or running in all_to_rebind:
                remove_all_occurrences(to_rebind, running)

        all_to_rebind.extend(to_rebind)
        all_to_revalidate.extend(to_revalidate)

    # Try to change links
    _try_validation()

    # Try to make more bindings
    _try_new_bindings()

    # Invalidate not re-linked elements
    for running in all_to_revalidate:
        if running in _Registry.waitings:
            # Not rebound...
            _logger.warning("'%s' not rebound" % running.name)

            # Callback & clean up
            _safe_callback(running, constants.IPOPO_CALLBACK_INVALIDATE)
            _instance_invalidation(running)


    # Call the invalidate method of all instances, 
    # after dependencies invalidation
    for instance_tuple in to_remove:
        _safe_callback(instance_tuple[1], constants.IPOPO_CALLBACK_INVALIDATE)
        del instance_tuple[1].instance
