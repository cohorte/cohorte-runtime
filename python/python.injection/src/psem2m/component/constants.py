#-- Content-Encoding: UTF-8 --
"""
Defines some iPOPO constants

@author: Thomas Calmant
@copyright: Copyright 2012, isandlaTech
@license: GPLv3
@version: 0.2
@status: Alpha

    This file is part of iPOPO.

    iPOPO is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    iPOPO is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with iPOPO. If not, see <http://www.gnu.org/licenses/>.
"""

# ------------------------------------------------------------------------------

# iPOPO service
IPOPO_SERVICE_SPECIFICATION = "psem2m.component.ipopo"

class IIPopoService:
    """
    Defines the iPOPO service interface
    """
    def instantiate(self, factory_name, name, properties={}):
        """
        Instantiates a component from the given factory, with the given name
        
        @param factory_name: Name of the component factory
        @param name: Name of the instance to be started
        @return: The component instance
        @raise TypeError: The given factory is unknown
        @raise ValueError: The given name or factory name is invalid, or an
        instance with the given name already exists
        @raise Exception: Something wrong occurred in the factory
        """
        raise NotImplementedError


    def invalidate(self, name):
        """
        Invalidates the given component
        
        @param name: Name of the component to invalidate
        @raise ValueError: Invalid component name
        """
        raise NotImplementedError


    def kill(self, name):
        """
        Kills the given component
        
        @param name: Name of the component to kill
        @raise ValueError: Invalid component name
        """
        raise NotImplementedError

# ------------------------------------------------------------------------------

# Injected class fields
IPOPO_METHOD_CALLBACKS = "_ipopo_callbacks"
IPOPO_FACTORY_CONTEXT = "__ipopo_factory_context__"
IPOPO_FACTORY_CONTEXT_DATA = "__ipopo_factory_context_data__"
IPOPO_INSTANCES = "__ipopo_instances__"

# Method called by the injected property (must be injected in the instance)
IPOPO_PROPERTY_GETTER = "_ipopo_property_getter"
IPOPO_PROPERTY_SETTER = "_ipopo_property_setter"

# ------------------------------------------------------------------------------

# Callbacks
IPOPO_CALLBACK_BIND = "BIND"
IPOPO_CALLBACK_UNBIND = "UNBIND"
IPOPO_CALLBACK_VALIDATE = "VALIDATE"
IPOPO_CALLBACK_INVALIDATE = "INVALIDATE"

# Properties
IPOPO_INSTANCE_NAME = "instance.name"
IPOPO_REQUIRES_FILTERS = "requires.filters"

# ------------------------------------------------------------------------------

def get_ipopo_svc_ref(bundle_context):
    """
    Retrieves a tuple containing the service reference to iPOPO and the service
    itself
    
    @param bundle_context: The calling bundle context
    @return: The reference to the iPOPO service and the service itself,
    None if not available
    """
    ref = bundle_context.get_service_reference(IPOPO_SERVICE_SPECIFICATION)
    if ref is None:
        return None

    svc = bundle_context.get_service(ref)
    if svc is None:
        return None

    return (ref, svc)
