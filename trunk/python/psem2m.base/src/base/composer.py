#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Created on 29 févr. 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import logging
import threading

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

from pelix.ipopo.core import IPopoEvent
from pelix.ipopo.decorators import ComponentFactory, Provides, Requires, \
    Validate, Invalidate, Instantiate
import pelix.ipopo.constants as constants
import pelix.utilities

# ------------------------------------------------------------------------------

SIGNAL_PREFIX = "/psem2m-composer-agent"
SIGNAL_REQUEST_PREFIX = "%s/request" % SIGNAL_PREFIX
SIGNAL_RESPONSE_PREFIX = "%s/response" % SIGNAL_PREFIX
SIGNAL_FACTORY_PREFIX = "%s/factory-state" % SIGNAL_PREFIX

SIGNAL_REQUEST_PATTERN = "%s/*" % SIGNAL_REQUEST_PREFIX
SIGNAL_FACTORY_PATTERN = "%s/*" % SIGNAL_FACTORY_PREFIX

SIGNAL_CAN_HANDLE_COMPONENTS = "%s/can-handle-components" \
                                % SIGNAL_REQUEST_PREFIX
SIGNAL_COMPONENT_CHANGED = "%s/component-changed" % SIGNAL_PREFIX
SIGNAL_INSTANTIATE_COMPONENTS = "%s/instantiate-components" \
                                % SIGNAL_REQUEST_PREFIX
SIGNAL_STOP_COMPONENTS = "%s/stop-components" % SIGNAL_REQUEST_PREFIX

SIGNAL_RESPONSE_HANDLES_COMPONENTS = "%s/can-handle-components" \
                                     % SIGNAL_RESPONSE_PREFIX
SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS = "%s/instantiate-components" \
                                         % SIGNAL_RESPONSE_PREFIX
SIGNAL_RESPONSE_STOP_COMPONENTS = "%s/stop-components" % SIGNAL_RESPONSE_PREFIX

SIGNAL_ISOLATE_ADD_FACTORY = "%s/added" % SIGNAL_FACTORY_PREFIX
SIGNAL_ISOLATE_REMOVE_FACTORY = "%s/removed" % SIGNAL_FACTORY_PREFIX
SIGNAL_ISOLATE_FACTORIES_GONE = "%s/all-gone" % SIGNAL_FACTORY_PREFIX
SIGNAL_ISOLATE_FACTORIES_DUMP = "%s/dump" % SIGNAL_FACTORY_PREFIX

# See the Java enumeration: org.psem2m.composer.EComponentState
ECOMPONENTSTATE_COMPLETE = {"javaClass": "org.psem2m.composer.EComponentState",
                            "enumValue": "COMPLETE"}
ECOMPONENTSTATE_REMOVED = {"javaClass": "org.psem2m.composer.EComponentState",
                            "enumValue": "REMOVED"}

# ------------------------------------------------------------------------------

@ComponentFactory("ComposerAgentFactory")
@Instantiate("ComposerAgent")
@Provides("org.psem2m.composer.Agent")
@Requires("ipopo", constants.IPOPO_SERVICE_SPECIFICATION)
@Requires("directory", "org.psem2m.signals.ISignalDirectory")
@Requires("sender", "org.psem2m.signals.ISignalBroadcaster")
@Requires("receiver", "org.psem2m.signals.ISignalReceiver")
class ComposerAgent(object):
    """
    Python Composer agent
    """
    COMPOSITE_NAME = "org.psem2m.composer.composite.name"
    HOST_ISOLATE = "org.psem2m.composer.isolate"

    def __init__(self):
        """
        Constructor
        """
        # Dependencies
        self.sender = None
        self.receiver = None
        self.directory = None
        self.ipopo = None

        # Instantiated components (name -> instance)
        self.instances = {}

        # Protection
        self._lock = threading.RLock()


    def handle_received_signal(self, name, signal_data):
        """
        Called when a composer signal is received
        
        :param name: The signal name
        :param data: The signal data object
        """
        _logger.info("Composer Signal: %s", name)

        sender = signal_data["senderId"]
        data = signal_data["signalContent"]

        if name == SIGNAL_CAN_HANDLE_COMPONENTS:
            self.can_handle_components(sender, data)

        elif name == SIGNAL_INSTANTIATE_COMPONENTS:
            self.instantiate_components(sender, data)

        elif name == SIGNAL_STOP_COMPONENTS:
            self.kill_components(sender, data)

        elif name == SIGNAL_ISOLATE_FACTORIES_DUMP:
            # This signal needs a result: an array with all known factories
            return tuple(self.ipopo.get_factories())


    def can_handle_components(self, sender, data):
        """
        Sends a signal to the given isolate to indicate which components can be
        instantiated here.
        
        :param sender: The isolate that sent the request signal
        :param data: An array of ComponentBean objects
        """
        with self._lock:
            current_isolate = self.directory.get_isolate_id()
            handled = []
            running = []

            # Find all instantiable components
            for component in data:
                name = component["name"]
                host = component.get("isolate", None)
                if host and host != current_isolate:
                    continue

                factory = component["type"]
                if self.ipopo.is_registered_factory(factory):
                    _logger.debug("%s can be handled here (%s)", name, factory)
                    handled.append(component)

                if name in self.instances:
                    # Component already running
                    running.append(component)

        # Send the result
        if not handled:
            handled = None
        else:
            handled = tuple(handled)

        if not running:
            running = None
        else:
            running = tuple(running)

        self.sender.fire(SIGNAL_RESPONSE_HANDLES_COMPONENTS,
                         {"handled": handled, "running": running},
                         isolate=sender)


    def __fire_instantiation_result(self, composet_name, component_name,
                                    success, composer_id):
        """
        Prepares and fires an instantiation result signal, for one component
        
        :param composet_name: The name of the instantiating composition
        :param component_name: The name of the instantiated/failed component
        :param success: True if the component has been successfully instantiated
        :param composer_id: ID of the Composer Core isolate
        """
        if success:
            instantiated = [component_name]
            failed = None

        else:
            instantiated = None
            failed = [component_name]

        self.sender.fire(SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS,
                         {'composite': composet_name,
                          'instantiated': instantiated,
                          'failed': failed},
                         isolate=composer_id)


    def instantiate_components(self, sender, data):
        """
        Instantiates the given components
        
        :param sender: The isolate requesting the instantiation
        :param data: The signal content
        """
        with self._lock:
            current_isolate = self.directory.get_isolate_id()

            # We'll known the composition name while reading components beans
            composite_name = None
            for component in data:
                if not composite_name:
                    # Find the root name
                    composite_name = component.get("rootName", None)

                # Get the instance name
                name = component["name"]

                if name in self.instances:
                    # Already known isolate, consider a success (send a signal)
                    self.__fire_instantiation_result(composite_name, name,
                                                     True, sender)
                    _logger.warning("Already instantiated component: %s", name)
                    continue

                # Get the component type
                factory = component["type"]

                # Get the instance properties
                properties = component.get("properties", {})
                if not isinstance(properties, dict):
                    # Ensure it is a dictionary
                    properties = {}

                # Set up PSEM2M Composer properties
                properties[ComposerAgent.COMPOSITE_NAME] = \
                                                        component["parentName"]
                properties[ComposerAgent.HOST_ISOLATE] = current_isolate
                properties["service.exported.interfaces"] = "*"

                # Set up fields filters (if any)
                fields_filters = component["fieldsFilters"]
                if fields_filters:
                    properties[constants.IPOPO_REQUIRES_FILTERS] = \
                                                                fields_filters

                try:
                    # Instantiate and store the component
                    instance = self.ipopo.instantiate(factory, name, properties)
                    self.instances[name] = instance

                    # Send a success signal
                    self.__fire_instantiation_result(composite_name, name,
                                                     True, sender)

                except Exception as ex:
                    _logger.exception("Composer agent Failed ! %s", ex)
                    # Send a failure signal
                    self.__fire_instantiation_result(composite_name, name,
                                                     False, sender)


    def kill_components(self, sender, data):
        """
        Kills given components
        
        :param sender: The isolate requesting the destruction of a component
        :param data: The signal content
        """
        with self._lock:
            killed = []
            unknown = []

            for component in data:
                if pelix.utilities.is_string(component):
                    # Got a component name
                    name = component
                else:
                    # Got a component bean
                    name = component["name"]

                if self.ipopo.is_registered_instance(name):
                    self.ipopo.kill(name)
                    killed.append(name)

                else:
                    unknown.append(name)

            result_map = {"stopped": killed, "unknown": unknown}
            self.sender.fire(SIGNAL_RESPONSE_STOP_COMPONENTS, result_map,
                             isolate=sender)


    def handle_ipopo_event(self, event):
        """
        Handles an iPOPO event
        
        :param event: An iPOPO event
        """
        with self._lock:
            kind = event.get_kind()
            component = event.get_component_name()
            factory = event.get_factory_name()

            if kind == IPopoEvent.REGISTERED:
                # Factory registered
                self.sender.fire(SIGNAL_ISOLATE_ADD_FACTORY, (factory,),
                                 dir_group="ALL")

            elif kind == IPopoEvent.UNREGISTERED:
                # Factory gone
                self.sender.fire(SIGNAL_ISOLATE_REMOVE_FACTORY, (factory,),
                                 dir_group="ALL")

            elif kind in (IPopoEvent.VALIDATED, IPopoEvent.KILLED) \
            and component in self.instances:
                # Instantiated component state changed
                if kind == IPopoEvent.VALIDATED:
                    state = ECOMPONENTSTATE_COMPLETE

                else:
                    # Clean the agent dictionary
                    del self.instances[component]
                    state = ECOMPONENTSTATE_REMOVED

                # Send the signal
                change_dict = {"name": component,
                               "state": state}

                self.sender.fire(SIGNAL_COMPONENT_CHANGED, change_dict,
                                 dir_group="ALL")


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        self.instances.clear()
        self.receiver.register_listener(SIGNAL_REQUEST_PATTERN, self)
        self.receiver.register_listener(SIGNAL_FACTORY_PATTERN, self)

        # Register to iPOPO events
        self.ipopo.add_listener(self)

        # Send registered iPOPO factories to other isolates
        # Directory might be empty at first time, but not on component reload
        self.sender.fire(SIGNAL_ISOLATE_ADD_FACTORY,
                        tuple(self.ipopo.get_factories()),
                        dir_group="ALL")


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        # Unregister to events
        self.ipopo.remove_listener(self)
        self.receiver.unregister_listener(SIGNAL_REQUEST_PATTERN, self)
        self.receiver.unregister_listener(SIGNAL_FACTORY_PATTERN, self)

        # Send a signal to tell others that all our factories are gone
        self.sender.fire(SIGNAL_ISOLATE_FACTORIES_GONE, None,
                         dir_group="ALL")

        with self._lock:
            # Kill active components
            if len(self.instances) > 0:
                data = []
                for compo_name in self.instances:
                    data.append({"name": compo_name})

                self.kill_components("monitors", data)

            self.instances.clear()
