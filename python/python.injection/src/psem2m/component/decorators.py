#-- Content-Encoding: UTF-8 --
"""
Defines the iPOPO decorators classes and utility methods

@author: Thomas Calmant
"""

import inspect
import logging
import types

from psem2m.component import constants, ipopo
from psem2m.component.ipopo import FactoryContext, ComponentContext
from psem2m.services import pelix

# ------------------------------------------------------------------------------

# Prepare the module logger
_logger = logging.getLogger("ipopo.decorators")

# ------------------------------------------------------------------------------


def _get_factory_context(cls):
    """
    Retrieves the factory context object associated to a factory. Creates it
    if needed
    
    @param cls: The factory class
    @return: The factory class context
    @raise RuntimeException: Not called in a Pelix framework context
    """
    context = getattr(cls, constants.IPOPO_FACTORY_CONTEXT, None)
    if context is None:
        # Get the framework
        framework = pelix.FrameworkFactory.get_framework()

        bundle = framework.get_bundle_by_name(cls.__module__)
        if bundle is None:
            raise RuntimeError("Not in a Pelix framework context (module %s)" \
                               % cls.__module__)

        bundle_context = bundle.get_bundle_context()
        if bundle_context is None:
            raise RuntimeError("Invalid bundle context for bundle %s" \
                               % bundle.get_symbolic_name())

        context = ipopo.FactoryContext(bundle_context)
        setattr(cls, constants.IPOPO_FACTORY_CONTEXT, context)

    return context


def _ipopo_setup_callback(cls, context):
    """
    Sets up the class _callback dictionary
    
    @param cls: The class to handle
    @param context: The factory class context
    """
    assert isinstance(cls, type)
    assert isinstance(context, FactoryContext)

    callbacks = {}
    functions = inspect.getmembers(cls, inspect.isfunction)

    for name, function in functions:

        if not hasattr(function, constants.IPOPO_METHOD_CALLBACKS):
            # No attribute, get the next member
            continue

        method_callbacks = getattr(function, constants.IPOPO_METHOD_CALLBACKS)

        if not isinstance(method_callbacks, list):
            # Invalid content
            _logger.warning("Invalid attribute %s in %s", \
                            constants.IPOPO_METHOD_CALLBACKS, name)
            continue

        # Maybe remove the attribute of the function ?
        delattr(function, constants.IPOPO_METHOD_CALLBACKS)

        # Store the callbacks
        for _callback in method_callbacks:
            if _callback in callbacks:
                _logger.warning("Redefining the _callback %s in '%s'. " \
                                "Previous _callback : '%s'.", \
                                _callback, name, \
                                callbacks[_callback].__name__)

            callbacks[_callback] = function

    # Update the factory context
    context.callbacks.clear()
    context.callbacks.update(callbacks)

# ------------------------------------------------------------------------------

def _append_object_entry(obj, list_name, entry):
    """
    Appends the given entry in the given object list.
    Creates the list field if needed.
    
    @param obj: The object that contains the list
    @param list_name: The name of the list member in *obj*
    @param entry: The entry to be added to the list
    """
    # Get the list
    try:
        obj_list = getattr(obj, list_name)
        if not obj_list:
            # Prepare a new dictionary dictionary
            obj_list = []

    except AttributeError:
        # We'll have to create it
        obj_list = []
        setattr(obj, list_name, obj_list)


    assert(isinstance(obj_list, list))

    # Set up the property, if needed
    if entry not in obj_list:
        obj_list.append(entry)

# ------------------------------------------------------------------------------

def get_field_property_name(component, field):
    """
    Retrieves the name of the property associated to the given field
    
    @param component: A component instance
    @param field: A field name
    @return: The property name, or None
    """
    if component is None or field is None:
        return None

    # Get the factory context
    factory_context = getattr(component, constants.IPOPO_FACTORY_CONTEXT, None)
    if factory_context is None:
        # Can't work
        return

    assert isinstance(factory_context, FactoryContext)

    # Get the name associated to the field
    return factory_context.properties_fields.get(field, None)


def _get_field_property(component, field):
    """
    Retrieves the value of the field property
    
    @param component: A component instance
    @param field: A field name
    @return: The property value, or None
    """
    if component is None or field is None:
        return None

    # Also get the component context
    component_context = getattr(component, constants.IPOPO_COMPONENT_CONTEXT, \
                                None)
    if component_context is None:
        # Can't work
        return

    assert isinstance(component_context, ComponentContext)

    # Get the property name
    property_name = get_field_property_name(component, field)

    # Retrieve the property value
    value = component_context.properties.get(property_name, None)
    return value


def _set_field_property(component, field, value):
    """
    Sets the property value associated to the given field. Does nothing if the
    field is unknown.
    
    @param component: A component instance
    @param field: A field name
    @param value: The new property value
    """
    if component is None or field is None:
        return

    # Get the property name
    property_name = get_field_property_name(component, field)
    if not property_name:
        # Invalid property name
        return

    # Also get the component context
    component_context = getattr(component, constants.IPOPO_COMPONENT_CONTEXT, \
                                None)
    if component_context is None:
        # Can't work
        return

    assert isinstance(component_context, ComponentContext)

    # Set the property value
    component_context.properties[property_name] = value


def _ipopo_field_property(field, name, value):
    """
    Sets up an iPOPO field property, using Python property() capabilities
    """

    def get_value(self):
        """
        Retrieves the property value, from the iPOPO dictionaries
        
        @return: The property value
        """
        return _get_field_property(self, field)


    def set_value(self, new_value):
        """
        Sets the property value and trigger an update event
        
        @param new_valuie: The new property value
        """
        name = get_field_property_name(self, field)
        old_value = _get_field_property(self, field)
        _set_field_property(self, field, new_value)

        # Trigger an update event
        ipopo.handle_property_changed(self, name, old_value, new_value)

        return new_value

    return property(get_value, set_value)

# ------------------------------------------------------------------------------

class ComponentFactory:
    """
    Decorator that sets up a component factory class
    """
    def __init__(self, name=""):
        """
        Sets up the decorator

        @param name: Name of the component factory
        """
        self.__factory_name = name

    def __call__(self, factory_class):
        """
        Sets up and registers the factory class

        @param factory_class: The decorated class
        """
        if not isinstance(factory_class, type):
            raise TypeError("@ComponentFactory can decorate only classes, " \
                            "not '%s'" % type(factory_class).__name__)

        # Get the factory context
        context = _get_factory_context(factory_class)

        # Find callbacks
        _ipopo_setup_callback(factory_class, context)

        # Add the component context field (set it to None)
        setattr(factory_class, constants.IPOPO_COMPONENT_CONTEXT, None)

        # Register the factory class
        ipopo.register_factory(self.__factory_name, factory_class)

        return factory_class

# ------------------------------------------------------------------------------

class Property:
    """
    @Property decorator
    
    Defines a component property.
    """
    def __init__(self, field=None, name=None, value=None):
        """
        Sets up the property
        
        @param field: The property field in the class (can't be None nor empty)
        @param name: The property name (if None, this will be the field name)
        @param value: The property value
        @raise ValueError: If The name if None or empty
        """

        if not field:
            raise ValueError("@Property with name '%s' : field name missing" \
                             % name)

        if not name:
            # No name given : use the field name
            name = field

        self.__field = field
        self.__name = name
        self.__value = value


    def __call__(self, clazz):
        """
        Adds the property to the class iPOPO properties field.
        Creates the field if needed.
        
        @param clazz: The decorated class
        @raise TypeError: If *clazz* is not a type
        """
        if not isinstance(clazz, type):
            raise TypeError("@Property can decorate only classes, not '%s'" \
                            % type(clazz).__name__)

        # Get the factory context
        context = _get_factory_context(clazz)

        # Set up the property in the class
        context.properties[self.__name] = self.__value

        # Associate the field to the property name
        context.properties_fields[self.__field] = self.__name

        # Add the field to the class -> it becomes a property
        setattr(clazz, self.__field, \
                _ipopo_field_property(self.__field, self.__name, self.__value))

        return clazz

# ------------------------------------------------------------------------------

class Provides:
    """
    @Provides decorator
    
    Defines an interface exported by a component.
    """
    def __init__(self, specifications=None):
        """
        Sets up the specifications
        
        @param specifications: A list of provided interface(s) name(s)
        (can't be empty)
        @raise ValueError: If the name if None or empty
        """
        if not specifications:
            raise ValueError("Provided interface name can't be empty")

        if isinstance(specifications, type):
            self.__specifications = [specifications.__name__]

        elif isinstance(specifications, str):
            self.__specifications = [specifications]

        elif not isinstance(specifications, list):
            raise ValueError("Unhandled @Provides specifications type : %s" \
                             % type(specifications).__name__)

        else:
            self.__specification = specifications


    def __call__(self, clazz):
        """
        Adds the interface to the class iPOPO field.
        Creates the field if needed.
        
        @param clazz: The decorated class
        @raise TypeError: If *clazz* is not a type
        """

        if not isinstance(clazz, type):
            raise TypeError("@Provides can decorate only classes, not '%s'" \
                            % type(clazz).__name__)

        # Get the factory context
        context = _get_factory_context(clazz)

        for spec in self.__specifications:
            if spec not in context.provides:
                # Avoid duplicates
                context.provides.append(spec)

        return clazz

# ------------------------------------------------------------------------------

class Requires:
    """
    @Requires decorator
    
    Defines a required component
    """
    def __init__(self, field="", specification="", aggregate=False, \
                 optional=False, spec_filter=None):
        """
        Sets up the requirement
        
        @param field: The injected field
        @param specification: The injected service specification
        @param aggregate: If true, injects a list
        @param optional: If true, this injection is optional
        @param spec_filter: An LDAP query to filter injected services upon their
        properties
        @raise TypeError: A parameter has an invalid type
        @raise ValueError: An error occurred while parsing the filter
        """
        self.__field = field
        self.__requirement = ipopo.Requirement(specification, aggregate, \
                                               optional, spec_filter)

    def __call__(self, clazz):
        """
        Adds the requirement to the class iPOPO field
        
        @param clazz: The decorated class
        @raise TypeError: If *clazz* is not a type
        """
        if not isinstance(clazz, type):
            raise TypeError("@Provides can decorate only classes, not '%s'" \
                            % type(clazz).__name__)

        # Set up the property in the class
        context = _get_factory_context(clazz)
        context.requirements[self.__field] = self.__requirement

        return clazz

# ------------------------------------------------------------------------------

def Bind(method):
    """
    @Bind decorator
    
    Called when a component is bound to a dependency
    
    @param method: The decorated method
    @raise TypeError: The decorated element is not a function
    """
    if type(method) is not types.FunctionType:
        raise TypeError("@Bind can only be applied on functions")

    _append_object_entry(method, constants.IPOPO_METHOD_CALLBACKS, \
                         constants.IPOPO_CALLBACK_BIND)
    return method


def Unbind(method):
    """
    @Unbind decorator
    
    Called when a component dependency is unbound
    
    @param method: The decorated method
    @raise TypeError: The decorated element is not a function
    """
    if type(method) is not types.FunctionType:
        raise TypeError("@Unbind can only be applied on functions")

    _append_object_entry(method, constants.IPOPO_METHOD_CALLBACKS, \
                         constants.IPOPO_CALLBACK_UNBIND)
    return method


def Validate(method):
    """
    @Validate decorator
    
    Called when a component becomes valid, i.e. if all of its required
    dependencies has been injected
    
    @param method: The decorated method
    @raise TypeError: The decorated element is not a function
    """
    if type(method) is not types.FunctionType:
        raise TypeError("@Validate can only be applied on functions")

    _append_object_entry(method, constants.IPOPO_METHOD_CALLBACKS, \
                         constants.IPOPO_CALLBACK_VALIDATE)
    return method


def Invalidate(method):
    """
    @Validate decorator
    
    Called when a component becomes invalid, i.e. if one of its required
    dependencies disappeared
    
    @param method: The decorated method
    @raise TypeError: The decorated element is not a function
    """

    if type(method) is not types.FunctionType:
        raise TypeError("@Invalidate can only be applied on functions")

    _append_object_entry(method, constants.IPOPO_METHOD_CALLBACKS, \
                         constants.IPOPO_CALLBACK_INVALIDATE)
    return method
