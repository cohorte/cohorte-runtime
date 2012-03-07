#-- Content-Encoding: UTF-8 --
"""
Created on 29 févr. 2012

@author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import logging
from psem2m.component.ipopo import IPopoEvent
_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

from psem2m.component.decorators import ComponentFactory, Provides, Requires, \
    Validate, Invalidate, Instantiate
import psem2m.component.constants as constants

# ------------------------------------------------------------------------------

SIGNAL_PREFIX = "/psem2m-composer-agent"
SIGNAL_REQUEST_PREFIX = "%s/request" % SIGNAL_PREFIX
SIGNAL_RESPONSE_PREFIX = "%s/response" % SIGNAL_PREFIX
SIGNAL_FACTORY_PREFIX = "%s/factory-state" % SIGNAL_PREFIX

SIGNAL_REQUEST_PATTERN = "%s/*" % SIGNAL_REQUEST_PREFIX
SIGNAL_CAN_HANDLE_COMPONENTS = "%s/can-handle-components" \
                               % SIGNAL_REQUEST_PREFIX
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

# ------------------------------------------------------------------------------

@ComponentFactory("ComposerAgentFactory")
@Instantiate("ComposerAgent")
@Provides("org.psem2m.composer.Agent")
@Requires("ipopo", constants.IPOPO_SERVICE_SPECIFICATION)
@Requires("directory", "org.psem2m.IsolateDirectory")
@Requires("sender", "org.psem2m.SignalSender")
@Requires("receiver", "org.psem2m.SignalReceiver")
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
        self.sender = None
        self.receiver = None
        self.directory = None
        self.ipopo = None
        self.instances = {}


    def handle_received_signal(self, name, signal_data):
        """
        Called when a composer signal is received
        
        :param name: The signal name
        :param data: The signal data object
        """
        _logger.info("Composer Signal: %s", name)

        sender = signal_data["isolateSender"]
        data = signal_data["signalContent"]

        if name == SIGNAL_CAN_HANDLE_COMPONENTS:
            self.can_handle_components(sender, data)

        elif name == SIGNAL_INSTANTIATE_COMPONENTS:
            self.instantiate_components(sender, data)

        elif name == SIGNAL_STOP_COMPONENTS:
            self.kill_components(sender, data)


    def can_handle_components(self, sender, data):
        """
        Sends a signal to the given isolate to indicate which components can be
        instantiated here.
        
        :param sender: The isolate that sent the request signal
        :param data: An array of ComponentBean objects
        """
        current_isolate = self.directory.get_current_isolate_id()
        handled = []

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

        # Send the result
        if not handled:
            handled = None

        self.sender.send_data(sender, SIGNAL_RESPONSE_HANDLES_COMPONENTS,
                              handled)


    def instantiate_components(self, sender, data):
        """
        Instantiates the given components
        
        :param sender: The isolate requesting the instantiation
        :param data: The signal content
        """
        current_isolate = self.directory.get_current_isolate_id()

        success = []
        failure = []
        composite_name = None

        for component in data:

            if not composite_name:
                # Root name
                composite_name = component.get("rootName", None)

            # Get the name
            name = component["name"]

            # Get the type
            factory = component["type"]

            # Get the properties
            try:
                properties = component["properties"]
            except:
                properties = {}

            if not isinstance(properties, dict):
                properties = {}

            # Set up properties
            properties[ComposerAgent.COMPOSITE_NAME] = component["parentName"]
            properties[ComposerAgent.HOST_ISOLATE] = current_isolate
            properties["service.exported.interfaces"] = "*"

            # Get field filters (if any)
            fields_filters = component["fieldsFilters"]
            if fields_filters:
                properties[constants.IPOPO_REQUIRES_FILTERS] = fields_filters

            try:
                instance = self.ipopo.instantiate(factory, name, properties)

                self.instances[name] = instance
                success.append(name)

            except:
                _logger.exception("EPIC FAIL !")
                failure.append(name)

        result_map = {"instantiated": success, "failed": failure}
        self.sender.send_data(sender, SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS,
                              result_map)


    def kill_components(self, sender, data):
        """
        Kills given components
        
        :param sender: The isolate requesting the destruction of a component
        :param data: The signal content
        """
        killed = []
        unknown = []

        for component in data:
            name = component["name"]

            if self.ipopo.is_registered_instance(name):
                self.ipopo.kill(name)
                killed.append(name)

            else:
                unknown.append(name)

        result_map = {"stopped": killed, "unknown": unknown}
        self.sender.send_data(sender, SIGNAL_RESPONSE_STOP_COMPONENTS,
                              result_map)


    def handle_ipopo_event(self, event):
        """
        Handles an iPOPO event
        
        :param event: An iPOPO event
        """
        kind = event.get_kind()
        factory = event.get_factory_name()

        if kind == IPopoEvent.REGISTERED:
            # Factory registered
            self.sender.send_data("*", SIGNAL_ISOLATE_ADD_FACTORY, (factory,))

        elif kind == IPopoEvent.UNREGISTERED:
            # Factory gone
            self.sender.send_data("*", SIGNAL_ISOLATE_REMOVE_FACTORY,
                                  (factory,))


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        self.instances.clear()
        self.receiver.register_listener(SIGNAL_REQUEST_PATTERN, self)
        self.ipopo.add_listener(self)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        self.ipopo.remove_listener(self)
        self.receiver.unregister_listener(SIGNAL_REQUEST_PATTERN, self)

        # Send a signal to tell others that all our factories are gone
        self.sender.send_data("*", SIGNAL_ISOLATE_FACTORIES_GONE, None)

        # Kill active components
        if len(self.instances) > 0:
            data = []
            for compo_name in self.instances:
                data.append({"name": compo_name})

            self.kill_components("monitors", data)

        self.instances.clear()
