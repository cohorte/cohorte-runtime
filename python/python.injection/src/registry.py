"""
Created on 18 janv. 2012

@author: Thomas Calmant
"""

import logging

from constants import IPOPO_CALLBACK_VALIDATE, IPOPO_REQUIREMENTS, \
    IPOPO_PROVIDES, IPOPO_CALLBACK_INVALIDATE, IPOPO_CALLBACK_BIND, \
    IPOPO_CALLBACK_UNBIND

_logger = logging.getLogger("ipopo.registry")

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


class Instance:
    """
    Represents a component instance
    """
    def __init__(self, name, instance, properties):
        """
        Sets up the instance object
        """
        self.name = name
        self.instance = instance
        self.factory = self.instance._ipopo_factory_name
        self.depends_on = []


class _Registry:
    """
    The iPOPO Component registry singleton
    """
    # Factories
    factories = {}

    # All instances
    instances = {}

    # Instances waiting for validation
    waitings = []

    # Running instances
    running = []

    def __init__(self):
        """
        Constructor that **must never be called**
        """
        raise RuntimeError("The _Registry constructor must never be called")


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

    if factory == None or not isinstance(factory, type):
        raise TypeError("The factory '%s' must be a type" % factory_name)


    if factory_name in _Registry.factories:
        _logger.warning("The factory %s has already been registered (override)",
                        factory_name)

    _Registry.factories[factory_name] = factory
    _logger.info("Factory '%s' registered" % factory_name)


def unregister_factory(factory_name):
    """
    Unregisters the given component factory
    
    @param factory_name: Name of the factory to unregister
    """
    if not factory_name:
        return

    if factory_name in _Registry.factories:
        # TODO: invalidate and delete all components of this factory
        _unregistration_loop(factory_name)
        del _Registry.factories[factory_name]
        _logger.info("Factory '%s' removed" % factory_name)


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
    if factory == None:
        raise TypeError("Null factory registered '%s'" % factory_name)

    # Create component instance
    instance = factory()
    if instance == None:
        raise TypeError("Factory '%s' failed to create '%s'" \
                        % (factory_name, name))

    # Store the instance
    stored_instance = Instance(name, instance, properties)

    # FIXME: Update component properties
    # instance._ipopo_update_properties(properties)

    # Store the instance
    _Registry.instances[name] = stored_instance

    # Add the instance in the waiting queue
    _Registry.waitings.append(stored_instance)

    # Try to validate components
    _try_validation()

    # Second try to bind optional dependencies
    _try_bind_incompletes()

    return instance


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


def _try_bind_incompletes():
    """
    Tries to bind optional dependencies to "incomplete" instances
    """

    for comp_instance in _Registry.running:

        # Get the requirement, or an empty dictionary
        requirements = getattr(comp_instance.instance, IPOPO_REQUIREMENTS, None)
        if not requirements:
            # No requirements : do nothing
            _logger.debug("No requirements")
            continue

        for field, requires in requirements.items():
            # For each field

            if not requires.optional:
                # Not an optional field, don't care
                _logger.debug("Not a optional dependency : %s" % field)
                continue

            current_value = getattr(comp_instance.instance, field, None)
            if not requires.aggregate and current_value is not None:
                # A dependency is already injected
                _logger.debug("Already bound : %s" % field)
                continue

            # Find possible link(s)
            link = _find_requirement(requires)

            if not link:
                _logger.debug("No link found : %s" % field)
                continue

            # Set the field value
            if isinstance(link, list):
                # Aggregation
                if not isinstance(current_value, list):
                    # Injected field as the right type
                    _logger.error("In '%s', field '%s' must be a list", \
                                  comp_instance.name, field)
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
                setattr(comp_instance.instance, field, injected)

                # Add dependency marker
                comp_instance.depends_on.extend(link)

                # Call the bind callback
                for dependency in injected:
                    _callback(comp_instance, IPOPO_CALLBACK_BIND, dependency)

            else:
                # Normal field
                # Set the instance
                setattr(comp_instance.instance, field, link.instance)

                # Add dependency marker
                comp_instance.depends_on.append(link)

                # Call the bind callback
                _callback(comp_instance, IPOPO_CALLBACK_BIND, link.instance)


def _try_validation():
    """
    Tries to validate as many components as possible
    """
    at_least_one_validation = True

    # At least one validation has been done, call try_validation again
    while at_least_one_validation:

        at_least_one_validation = False
        validated = []

        for comp_instance in _Registry.waitings:
            # For each comp_instance...
            can_validate = True

            # Get the requirement, or an empty dictionary
            requirements = getattr(comp_instance.instance, \
                                   IPOPO_REQUIREMENTS, {})

            for field, requires in requirements.items():
                # For each field...
                link = _find_requirement(requires)
                if not link:

                    if requires.optional:
                        # Optional dependency, ignore
                        continue

                    # Missing requirement
                    can_validate = False
                    _logger.debug("'%s': no link for %s" \
                                  % (comp_instance.name, field))

                else:
                    # TODO: make a Nullable proxy if link is None

                    # Set the field value
                    if isinstance(link, list):

                        # Prepare the injected value
                        injected = [comp_inst.instance for comp_inst in link]

                        # Set the field
                        setattr(comp_instance.instance, field, injected)

                        # Add dependency marker
                        comp_instance.depends_on.extend(link)

                        # Call the bind callback
                        for dependency in injected:
                            _safe_callback(comp_instance, IPOPO_CALLBACK_BIND, \
                                           dependency)

                    else:
                        # Set the instance
                        setattr(comp_instance.instance, field, link.instance)

                        # Add dependency marker
                        comp_instance.depends_on.append(link)

                        # Call the bind callback
                        _safe_callback(comp_instance, IPOPO_CALLBACK_BIND, \
                                       link.instance)


            if can_validate:
                # Component can be validated
                _logger.info("'%s' can be validated" % comp_instance.name)
                validated.append(comp_instance)


        # Validation loop
        for comp_instance in validated:

            try:
                _callback(comp_instance, IPOPO_CALLBACK_VALIDATE)

            except Exception as ex:
                # Something wrong occurred
                _logger.error("Error validating '%s'" % comp_instance.name, \
                              exc_info=True)
                raise ex

            else:
                # Remove it from the waiting list
                _Registry.waitings.remove(comp_instance)
                _Registry.running.append(comp_instance)
                at_least_one_validation = True


def _find_requirement(requirement):
    """
    Tries to find links for the given requirement
    """
    links = []
    required_spec = requirement.specification
    aggregate = requirement.aggregate

    for running_instance in _Registry.running:

        provides = getattr(running_instance.instance, IPOPO_PROVIDES, [])
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


def _instance_invalidation(comp_instance):
    """
    Sets to None all instances references
    """
    # Get the requirement, or an empty dictionary
    requirements = getattr(comp_instance.instance, IPOPO_REQUIREMENTS, {})

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
    for instance_tuple in to_remove:

        instance = instance_tuple[1]
        to_rebind = []

        for running in _Registry.running:
            # Found a depending component
            if instance in running.depends_on:
                to_rebind.append(running)

        for running in to_rebind:
            # Change the component state in the registry 
            _Registry.running.remove(running)
            _Registry.waitings.append(running)

            # Calls its unbind method
            _safe_callback(running, IPOPO_CALLBACK_UNBIND, instance.instance)


        all_to_rebind.extend(to_rebind)

    # Try to change links
    _try_validation()

    # Invalidate not re-linked elements
    for running in all_to_rebind:
        if running in _Registry.waitings:
            # Not rebound...
            _logger.warning("'%s' not rebound" % running.name)

            # Callback
            _safe_callback(running, IPOPO_CALLBACK_INVALIDATE)

            # Clean up the instance
            _instance_invalidation(running)


    # Call the invalidate method of all instances, 
    # after dependencies invalidation
    for instance_tuple in to_remove:
        _safe_callback(instance_tuple[1], IPOPO_CALLBACK_INVALIDATE)
        del instance_tuple[1].instance
