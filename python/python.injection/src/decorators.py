#-- Content-Encoding: UTF-8 --
"""
Defines the iPOPO decorators classes and utility methods

@author: Thomas Calmant
"""

import inspect
import logging
import types

import constants
import registry

# ------------------------------------------------------------------------------

# Prepare the module logger
_logger = logging.getLogger("ipopo.decorators")

# ------------------------------------------------------------------------------

def _ipopo_setup_callback(cls):
    """
    Sets up the class _callback dictionary
    
    @param cls: The class to handle
    """
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

    # Set the class attribute
    setattr(cls, constants.IPOPO_METHOD_CALLBACKS, callbacks)

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


def _put_object_entry(obj, dict_name, entry, value):
    """
    Puts an entry in an object dictionary
    Creates the dictionary field if needed.

    @param obj: The object that contains the dictionary
    @param dict_name: The name of the dictionary member in *obj*
    @param entry: The name of the entry to add
    @param value: The value of the entry
    """
    # Get the dictionary
    try:
        dictionary = getattr(obj, dict_name)
        if not dictionary:
            # Prepare a new dictionary dictionary
            dictionary = {}

    except AttributeError:
        # We'll have to create it
        dictionary = {}
        setattr(obj, dict_name, dictionary)

    assert(isinstance(dictionary, dict))

    # Set up the property
    dictionary[entry] = value

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

    # Get fields
    fields = getattr(component, constants.IPOPO_PROPERTIES_FIELDS, None)
    if not isinstance(fields, dict):
        return None

    if field not in fields:
        # Unknown field
        return None

    # Get properties
    properties = getattr(component, constants.IPOPO_PROPERTIES, None)
    if not isinstance(properties, dict):
        return None

    return fields[field]


def _get_field_property(component, field):
    """
    Retrieves the value of the field property
    
    @param component: A component instance
    @param field: A field name
    @return: The property value, or None
    """
    if component is None or field is None:
        return None

    # Get properties
    properties = getattr(component, constants.IPOPO_PROPERTIES, None)
    if not isinstance(properties, dict):
        return None

    property_name = get_field_property_name(component, field)
    if property_name is None or property_name not in properties:
        # Unknown property
        return None

    return properties[property_name]


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

    # Get fields
    fields = getattr(component, constants.IPOPO_PROPERTIES_FIELDS, None)
    if not isinstance(fields, dict):
        return

    if field not in fields:
        # Unknown field
        return

    # Get properties
    properties = getattr(component, constants.IPOPO_PROPERTIES, None)
    if not isinstance(properties, dict):
        return

    # Set the property value
    properties[fields[field]] = value


def _ipopo_update_properties(self, new_values={}):
    """
    Sets the properties fields values
    
    @param new_values: New properties values, in case of a value update
    """
    properties = getattr(self, constants.IPOPO_PROPERTIES, None)
    if not properties:
        # Nothing to do
        _logger.debug("No properties for %s", self)
        return

    # Update instance properties
    if isinstance(new_values, dict):
        for key, value in new_values.items():
            # Update or insert properties
            properties[key] = value


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
        old_value = _get_field_property(self, field)
        name = get_field_property_name(self, field)
        _set_field_property(self, field, new_value)

        # Trigger an update event
        registry.handle_property_changed(self, name, old_value, new_value)

    return property(get_value, set_value)


def read_only_property(value):
    """
    Makes a read-only property that always returns the given value
    """
    return property(lambda cls: value)

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

        # Read only property (factory name)
        factory_class._ipopo_factory_name = read_only_property(self.__factory_name)

        # Add iPOPO properties methods
        factory_class._ipopo_update_properties = _ipopo_update_properties

        # Callbacks
        _ipopo_setup_callback(factory_class)

        # Register the factory class
        registry.register_factory(self.__factory_name, factory_class)
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

        # Set up the property in the class
        _put_object_entry(clazz, constants.IPOPO_PROPERTIES, self.__name, \
                          self.__value)

        # Associate the field to the property name
        _put_object_entry(clazz, constants.IPOPO_PROPERTIES_FIELDS, \
                          self.__field, self.__name)

        # Add the field to the class
        setattr(clazz, self.__field, \
                _ipopo_field_property(self.__field, self.__name, self.__value))
        return clazz

# ------------------------------------------------------------------------------

class Provides:
    """
    @Provides decorator
    
    Defines an interface exported by a component.
    """
    def __init__(self, specification=None):
        """
        Sets up the specification
        
        @param specification: The provided interface name (can't be empty)
        @raise ValueError: If the name if None or empty
        """
        if not specification:
            raise ValueError("Provided interface name can't be empty")

        self.__specification = specification


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

        # Set up the property in the class
        _append_object_entry(clazz, constants.IPOPO_PROVIDES, \
                             self.__specification)
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
        """
        self.__field = field
        self.__requirement = registry.Requirement(specification, aggregate, \
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
        _put_object_entry(clazz, constants.IPOPO_REQUIREMENTS, self.__field, \
                          self.__requirement)
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
