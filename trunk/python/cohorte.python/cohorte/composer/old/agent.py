#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Agent

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE
import cohorte.composer
import cohorte.composer.core.fsm as fsm
import cohorte.signals

# Pelix / iPOPO
from pelix.ipopo.decorators import ComponentFactory, Provides, Requires, \
    Validate, Invalidate, Property, Instantiate
from pelix.ipopo.constants import IPopoEvent
import pelix.ipopo.constants as constants
import pelix.utilities
import pelix.remote

# Standard library
import logging
import threading

# ------------------------------------------------------------------------------

IPOPO_KIND_TO_EVENT = {
    IPopoEvent.INSTANTIATED: fsm.COMPONENT_EVENT_INSTANTIATED,
    IPopoEvent.VALIDATED: fsm.COMPONENT_EVENT_VALIDATED,
    IPopoEvent.INVALIDATED: fsm.COMPONENT_EVENT_INVALIDATED,
    IPopoEvent.KILLED: fsm.COMPONENT_EVENT_GONE,
}
""" Translation of iPOPO events into composer FSM events """

PROP_COMPONENT_UID = 'cohorte.composer.component.uid'
""" Component UID """

PROP_COMPONENT_NAME = 'cohorte.composer.component.name'
""" Component name (redundant with instance.name) """

PROP_COMPOSITE_NAME = 'cohorte.composer.composite.name'
""" Composite containing the component """

PROP_HOST_ISOLATE = "cohorte.composer.isolate"
""" UID of the isolate hosting the component """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-agent-ipopo-factory")
@Provides("cohorte.composer.Agent")
@Property('_export_specs', pelix.remote.PROP_EXPORTED_INTERFACES,
          ["cohorte.composer.Agent"])
@Property('_export_name', pelix.remote.PROP_ENDPOINT_NAME, 'composer-agent')
@Requires("_ipopo", constants.IPOPO_SERVICE_SPECIFICATION)
@Requires('_sender', cohorte.SERVICE_SIGNALS_SENDER)
@Instantiate('cohorte-composer-agent-ipopo')
class ComposerAgent(object):
    """
    Python composer agent
    """
    def __init__(self):
        """
        Sets up members
        """
        # Bundle context
        self._context = None

        # Injected services
        self._ipopo = None
        self._sender = None

        # Properties
        self._export_specs = "*"
        self._export_name = "composer-agent"

        # Instantiation lock
        self._lock = threading.RLock()

        # Instantiated components: name -> UID
        self._instances = {}

        # Waiting component: factory -> [(name, properties)]
        self._waiting = {}


    def can_handle_components(self, components):
        """
        Tests which components can be instantiated here.
        
        :param components: A list of component descriptions
        :return: The names of the components that can be instantiated
                 immediately with this agent
        """
        # Isolate identification
        local_uid = self.get_isolate()
        local_name = self._context.get_property(cohorte.PROP_NAME)

        # List of handled components
        handled = []
        for component in components:
            isolate = component.get("isolate", None)
            if isolate and isolate not in (local_uid, local_name):
                # Not configured for this isolate
                continue

            factory = component["type"]
            if self._ipopo.is_registered_factory(factory):
                # Factory available
                handled.append(component["name"])

        return tuple(handled) if handled else None


    def get_factories(self):
        """
        Retrieves a tuple of all factories accessible by this agent
        
        :return: A tuple of factories names
        """
        return tuple(self._ipopo.get_factories())


    def get_isolate(self):
        """
        Retrieves the UID and the name of the isolate hosting this agent
        
        :return: A tuple: (isolate UID, isolate name)
        """
        return self._context.get_property(cohorte.PROP_UID), \
            self._context.get_property(cohorte.PROP_NAME)


    def handle_ipopo_event(self, event):
        """
        Handles an iPOPO event
        
        :param event: An iPOPO event
        """
        kind = event.get_kind()
        name = event.get_component_name()
        factory = event.get_factory_name()

        if kind == IPopoEvent.REGISTERED and factory in self._waiting:
            # Instantiate waiting components
            with self._lock:
                self._instantiation_loop(self._waiting[factory])

        elif name in self._instances and kind in IPOPO_KIND_TO_EVENT:
            # Known component, handled event
            uid = self._instances[name]
            event = IPOPO_KIND_TO_EVENT[kind]

            self._sender.post(cohorte.composer.SIGNAL_AGENT_EVENT,
                              {event: {uid: name}},
                              dir_group=cohorte.signals.GROUP_OTHERS)

            if kind == IPopoEvent.KILLED:
                # Clean up references
                del self._instances[name]


    def _instantiation_loop(self, components):
        """
        Instantiates the given components.
        A component description is 3-tuple: (instance name, factory name,
        properties). 
        
        :param components: A list of component descriptions
        :return: A 3-tuple: instantiated components, already running ones and
                 failed ones.
        """
        if not components:
            # Nothing to do
            return

        # Instantiated components UID -> Name
        success = {}

        # List of already running components
        running = []

        # Component name -> Error message
        errors = {}

        for name, factory, properties in components:
            if name in self._instances:
                # Already instantiated component
                running.append(name)

            else:
                uid = properties[PROP_COMPONENT_UID]
                # Create the instance entry first, as it will be tested
                # while handling iPOPO events
                self._instances[name] = uid

                try:
                    # Instantiate the component
                    self._ipopo.instantiate(factory, name, properties)

                except Exception as ex:
                    # Store the error
                    errors[name] = str(ex)
                    _logger.exception("Error instantiating %s: %s", name, ex)

                    # Clear the instance
                    del self._instances[name]

                else:
                    # Store the instance
                    success[uid] = name

        # Notify the composer
        self._sender.post(cohorte.composer.SIGNAL_AGENT_EVENT,
                          {"instantiated": success, "running": running,
                           "gone": errors},
                          dir_group=cohorte.signals.GROUP_OTHERS)


    def instantiate(self, components, until_possible):
        """
        Instantiates the given components. The result lists only contains
        components names, without compositions names. 
        
        :param components: A list of component descriptions
        :param until_possible: If True, every component whom factory is missing
                               will be kept until its instantiation can be done;
                               else, those components are considered failing.
        """
        to_instantiate = []

        with self._lock:
            # We'll known the composition name while reading components beans
            for component in components:
                # Get the component description
                name = component["name"]
                factory = component["type"]

                # Normalize the instance properties
                properties = component.get("properties", {})
                if not isinstance(properties, dict):
                    # Ensure it is a dictionary
                    properties = {}

                # Set up Composer properties
                properties[PROP_COMPONENT_UID] = component["uid"]
                properties[PROP_COMPONENT_NAME] = name
                properties[PROP_COMPOSITE_NAME] = component["parentName"]
                properties[PROP_HOST_ISOLATE] = self.get_isolate()
                properties[pelix.remote.PROP_EXPORTED_INTERFACES] = "*"

                # Set up fields filters (if any)
                # TODO: convert to LDAP here instead of in the composer
                req_filters = component["fieldsFilters"]
                if req_filters:
                    properties[constants.IPOPO_REQUIRES_FILTERS] = req_filters

                if until_possible \
                and not self._ipopo.is_registered_factory(factory):
                    # Factory is missing, try again later
                    self._waiting.setdefault(factory, []).append((name, factory,
                                                                  properties))

                else:
                    # Add the component to the instantiation loop
                    to_instantiate.append((name, factory, properties))

            # Run the instantiation loop (will notify the composer)
            self._instantiation_loop(to_instantiate)


    def is_running(self, component):
        """
        Tests if the given component is running and returns 0 if the component
        is unknown, 1 if it running, and -1 if a component with the same name
        but a different factory is running. The latter only works if the given
        parameter is a component description.
        
        :param component: A component description or a name
        :return: One of 0, 1, -1
        """
        if pelix.utilities.is_string(component):
            # Got a component name
            name = component
            factory = None

        else:
            # Got a component description
            name = component["name"]
            factory = component["type"]

        try:
            details = self._ipopo.get_instance_details(name)
            if not factory or details["factory"] == factory:
                # Same/no factory
                return 1
            else:
                # Same name, different factories
                return -1

        except ValueError:
            # Unknown/Invalid component name
            return 0



    def kill(self, components):
        """
        Kills the given components
        
        :param components: A list of component descriptions or names
        :return: A tuple (stopped components names, unknown names)
        """
        killed = []
        unknown = []

        for component in components:
            if pelix.utilities.is_string(component):
                # Got a component name
                name = component
            else:
                # Got a component bean
                name = component["name"]

            if name in self._instances \
            and self._ipopo.is_registered_instance(name):
                # Kill the instance
                # References clean up will occur during iPOPO events handling
                self._ipopo.kill(name)
                killed.append(name)

            else:
                # Unknown instance (or not started by this agent)
                unknown.append(name)

        return killed, unknown


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context

        # Ensure the storage is empty
        self._waiting.clear()
        self._instances.clear()

        # Register to iPOPO events
        self._ipopo.add_listener(self)
        _logger.info("Composer agent validated")


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister to events
        self._ipopo.remove_listener(self)

        # Kill active components
        self.kill(self._instances.keys())

        # Clean up
        self._waiting.clear()
        self._instances.clear()
        self._context = None
        _logger.info("Composer agent invalidated")
