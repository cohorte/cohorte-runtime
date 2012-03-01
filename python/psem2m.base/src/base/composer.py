#-- Content-Encoding: UTF-8 --
"""
Created on 29 févr. 2012

@author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import logging
_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

from psem2m.component.decorators import ComponentFactory, Provides, Requires, \
    Validate, Invalidate, Instantiate, Property
import psem2m.component.constants

# ------------------------------------------------------------------------------

@ComponentFactory("erp-proxy-json-rpc")
@Instantiate("DummyCompo")
@Provides("org.psem2m.tests.Dummy")
@Property("__export", "service.exported.interfaces", "*")
class Dummy(object):

    def test(self):
        return "Hello, World !"

    def hello(self, name):
        return "Hello, %s !" % name

    @Validate
    def validate(self, context):
        _logger.warn("HERE AT THE WALL ! " * 3)

    @Invalidate
    def invalidate(self, context):
        _logger.warn("GONE")


@ComponentFactory("ComposerAgentFactory")
@Instantiate("ComposerAgent")
@Provides("org.psem2m.composer.Agent")
@Requires("ipopo", psem2m.component.constants.IPOPO_SERVICE_SPECIFICATION)
@Requires("directory", "org.psem2m.IsolateDirectory")
@Requires("sender", "org.psem2m.SignalSender")
@Requires("receiver", "org.psem2m.SignalReceiver")
class ComposerAgent(object):
    """
    Python Composer agent
    """
    SIGNAL_REQUEST_PATTERN = "/psem2m-composer-agent/request/*"
    SIGNAL_CAN_HANDLE_COMPONENTS = "/psem2m-composer-agent/request/can-handle-components"
    SIGNAL_INSTANTIATE_COMPONENTS = "/psem2m-composer-agent/request/instantiate-components"
    SIGNAL_STOP_COMPONENTS = "/psem2m-composer-agent/request/stop-components"

    SIGNAL_RESPONSE_HANDLES_COMPONENTS = "/psem2m-composer-agent/response/can-handle-components"
    SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS = "/psem2m-composer-agent/response/instantiate-components"
    SIGNAL_RESPONSE_STOP_COMPONENTS = "/psem2m-composer-agent/response/stop-components"

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
        """
        _logger.info("Composer Signal: %s", name)

        sender = signal_data["isolateSender"]
        data = signal_data["signalContent"]

        if name == ComposerAgent.SIGNAL_CAN_HANDLE_COMPONENTS:
            self.can_handle_components(sender, data)

        elif name == ComposerAgent.SIGNAL_INSTANTIATE_COMPONENTS:
            self.instantiate_components(sender, data)

        elif name == ComposerAgent.SIGNAL_STOP_COMPONENTS:
            self.kill_components(sender, data)


    def can_handle_components(self, sender, data):
        """
        Sends a signal to the given isolate to indicate which components can be
        instantiated here.
        
        @param data: An array of ComponentBean objects
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
                _logger.debug("%s can be handled here", name)
                handled.append(component)


        # Send the result
        if not handled:
            handled = None

        self.sender.send_data(sender,
                              ComposerAgent.SIGNAL_RESPONSE_HANDLES_COMPONENTS,
                              handled)


    def instantiate_components(self, sender, data):
        """
        Instantiates the given components
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
                properties = component["properties"]["map"]
            except:
                properties = {}

            if not isinstance(properties, dict):
                properties = {}

            # Set up properties
            properties[ComposerAgent.COMPOSITE_NAME] = component["parentName"]
            properties[ComposerAgent.HOST_ISOLATE] = current_isolate
            properties["service.exported.interfaces"] = "*"

            try:
                instance = self.ipopo.instantiate(factory, name, properties)

                self.instances[name] = instance
                success.append(name)

            except:
                _logger.exception("EPIC FAIL !")
                failure.append(name)

        result_map = {
            "javaClass": "java.util.HashMap",
            "map": {
                    "instantiated": success,
                    "failed": failure
                }
            }

        self.sender.send_data(sender,
                        ComposerAgent.SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS,
                        result_map)


    def kill_components(self, sender, data):
        """
        Kills given components
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

        result_map = {
            "javaClass": "java.util.HashMap",
            "map": {
                    "stopped": killed,
                    "unknown": unknown
                }
            }

        self.sender.send_data(sender,
                        ComposerAgent.SIGNAL_RESPONSE_STOP_COMPONENTS,
                        result_map)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self.instances.clear()
        self.receiver.register_listener(ComposerAgent.SIGNAL_REQUEST_PATTERN,
                                        self)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.receiver.unregister_listener(ComposerAgent.SIGNAL_REQUEST_PATTERN,
                                          self)

        # Kill active components
        if len(self.instances) > 0:
            data = []
            for compo_name in self.instances:
                data.append({"name": compo_name})

            self.kill_components("monitors", data)

        self.instances.clear()
