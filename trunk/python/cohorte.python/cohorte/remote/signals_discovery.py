#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Remote Services: Discovery based on Signals

TODO:
* handle new/lost isolates
* update signals names

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# Cohorte
import cohorte
import cohorte.java.jabsorb as jabsorb

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Requires, Property
import pelix.framework
import pelix.remote

# Standard library
import logging
import sys

if sys.version_info[0] == 3:
    from urllib.parse import urlparse

else:
    from urlparse import urlparse

# ------------------------------------------------------------------------------

JAVA_BEANS_PACKAGE = "org.psem2m.isolates.services.remote.beans"
JAVA_ENDPOINT_DESCRIPTION = "{0}.EndpointDescription".format(JAVA_BEANS_PACKAGE)
JAVA_REMOTE_SERVICE_EVENT = "{0}.RemoteServiceEvent".format(JAVA_BEANS_PACKAGE)
JAVA_SERVICE_EVENT_TYPE = "{0}$ServiceEventType" \
                                            .format(JAVA_REMOTE_SERVICE_EVENT)
JAVA_REMOTE_SERVICE_REGISTRATION = "{0}.RemoteServiceRegistration" \
                                            .format(JAVA_BEANS_PACKAGE)

BROADCASTER_SIGNAL_NAME_PREFIX = "/psem2m/remote-service-broadcaster"
BROADCASTER_SIGNALS_PATTERN = "{0}/*".format(BROADCASTER_SIGNAL_NAME_PREFIX)
BROADCASTER_SIGNAL_REQUEST_ENDPOINTS = "{0}/request-endpoints" \
                                        .format(BROADCASTER_SIGNAL_NAME_PREFIX)
SIGNAL_REMOTE_EVENT = "{0}/remote-event".format(BROADCASTER_SIGNAL_NAME_PREFIX)
SIGNAL_REQUEST_ENDPOINTS = "{0}/request-endpoints" \
                                        .format(BROADCASTER_SIGNAL_NAME_PREFIX)

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

REGISTERED = "REGISTERED"
MODIFIED = "MODIFIED"
UNREGISTERED = "UNREGISTERED"

class _RemoteEvent(object):
    """
    Represents a remote service event Java bean
    """
    javaClass = JAVA_REMOTE_SERVICE_EVENT
    """ Java class (for Jabsorb) """

    def __init__(self, kind, registration):
        """
        Sets up the bean
        """
        if kind not in (REGISTERED, MODIFIED, UNREGISTERED):
            raise ValueError("Invalid RemoteEvent kind: %s", kind)

        self.eventType = {jabsorb.JAVA_CLASS: JAVA_SERVICE_EVENT_TYPE,
                          "enumValue": kind}
        self.serviceRegistration = registration


class _Registration(object):
    """
    Represents a registration Java bean
    """
    javaClass = JAVA_REMOTE_SERVICE_REGISTRATION
    """ Java class (for Jabsorb) """

    def __init__(self, specifications, isolate_uid, service_id, properties,
                 endpoints=None):
        """
        Sets up the bean
        """
        if endpoints:
            self.endpoints = tuple(endpoint for endpoint in endpoints
                                   if endpoint is not None)

        else:
            self.endpoints = ()

        self.exportedInterfaces = specifications
        self.hostIsolate = isolate_uid
        self.serviceProperties = properties
        self.serviceId = service_id


    def add_endpoint(self, endpoint):
        """
        Adds an end point to the registration
        """
        if endpoint is not None and endpoint not in self.endpoints:
            endpoints = list(self.endpoints)
            endpoints.append(endpoint)
            self.endpoints = tuple(endpoints)


class _EndpointDescription(object):
    """
    Represents an end point Java bean
    """
    javaClass = JAVA_ENDPOINT_DESCRIPTION
    """ Java class (for Jabsorb) """

    def __init__(self, name, uri, exported_config, node, port, protocol):
        """
        Sets up the bean
        """
        self.endpointName = name
        self.endpointUri = uri
        self.exportedConfig = exported_config
        self.node = node
        self.port = port
        self.protocol = protocol

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-remote-discovery-signals-factory")
@Provides(pelix.remote.SERVICE_ENDPOINT_LISTENER)
@Requires("_dispatcher", pelix.remote.SERVICE_DISPATCHER)
@Requires("_registry", pelix.remote.SERVICE_REGISTRY)
@Requires("_directory", cohorte.SERVICE_SIGNALS_DIRECTORY)
@Requires("_receiver", cohorte.SERVICE_SIGNALS_RECEIVER)
@Requires("_sender", cohorte.SERVICE_SIGNALS_SENDER)
@Property("_listener_flag", pelix.remote.PROP_LISTEN_EXPORTED, True)
class SignalsDiscovery(object):
    """
    Remote services discovery and notification using COHORTE Signals
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Bundle context
        self._context = None

        # Java Registrations
        self._registrations = {}

        # End point listener flag
        self._listener_flag = True

        # End points registry
        self._dispatcher = None
        self._registry = None

        # Signals
        self._directory = None
        self._receiver = None
        self._sender = None



    def _get_endpoints(self):
        """
        Prepares a tuple of registration events for all known end points
        """
        # Get end points
        events = tuple(_RemoteEvent(REGISTERED, registration)
                       for registration in self._registrations.values())

        if events:
            # Return None instead of an empty tuple
            return events


    def _send_remote_event(self, event_type, registration):
        """
        Sends a RemoteServiceEvent Java bean to all isolates
        
        :param event_type: The event type string (one of REGISTERED, MODIFIED,
                           UNREGISTERED)
        :param registration: The RemoteServiceRegistration associated to the
                             event
        """
        remote_event = _RemoteEvent(event_type, registration)
        self._sender.post(SIGNAL_REMOTE_EVENT, remote_event, dir_group="OTHERS")


    def endpoint_added(self, endpoint):
        """
        A new service is exported
        """
        # Extract information
        specifications = endpoint.reference.get_property(\
                                                    pelix.framework.OBJECTCLASS)
        uid = self._context.get_property(cohorte.PROP_UID)
        node = self._context.get_property(cohorte.PROP_NODE)

        # Parse access URL
        access = urlparse(endpoint.url)

        # Prepare an end point description
        java_endpoint = _EndpointDescription(endpoint.name,
                                             access.path,
                                             endpoint.kind,
                                             node, access.port,
                                             access.scheme or "http")

        # Make a registration bean
        registration = _Registration(specifications, uid,
                                     endpoint.name,
                                     endpoint.reference.get_properties(),
                                     [java_endpoint])

        # Store it
        self._registrations[endpoint.reference] = registration

        # Send the signal
        self._send_remote_event(REGISTERED, registration)


    def endpoint_updated(self, endpoint, old_properties):
        """
        An end point is updated
        """
        # Get the registration bean
        registration = self._registrations.get(endpoint.reference)
        if registration is None:
            # Unknown reference
            return

        # Update the bean
        registration.serviceProperties = endpoint.reference.get_properties()

        # Send the signal
        self._send_remote_event(MODIFIED, registration)


    def endpoint_removed(self, endpoint):
        """
        An end point is removed
        """
        if endpoint.reference not in self._registrations:
            # Unknown reference
            return

        # Pop the registration bean
        registration = self._registrations.pop(endpoint.reference)

        # Send the signal
        self._send_remote_event(UNREGISTERED, registration)


    def handle_received_signal(self, name, signal_data):
        """
        Called when a remote services signal is received
        """
        if name == BROADCASTER_SIGNAL_REQUEST_ENDPOINTS:
            # An isolate requests all of our exported services
            return self._get_endpoints()

        elif name == SIGNAL_REMOTE_EVENT:
            # Received a remote service event
            sender = signal_data["senderUID"]
            data = jabsorb.from_jabsorb(signal_data["signalContent"])

            if isinstance(data, list):
                # Multiple events in one signal
                for event in data:
                    try:
                        self._handle_remote_event(sender, event)

                    except Exception as ex:
                        _logger.exception("Error reading event %s: %s",
                                          event, ex)

            else:
                # Single event
                self._handle_remote_event(sender, data)


    def _filter_properties(self, properties):
        """
        Filters imported service properties. Makes a new dictionary
    
        :param properties: Imported service properties
        :return: A filtered dictionary
        """
        if properties is None:
            # Minimal import properties
            return {pelix.remote.PROP_IMPORTED: True}

        # Modified keys
        prop_filter = {pelix.remote.PROP_EXPORTED_CONFIGS:
                        pelix.remote.PROP_IMPORTED_CONFIGS,
                       pelix.remote.PROP_EXPORTED_INTERFACES:
                        pelix.remote.PROP_IMPORTED_INTERFACES}

        # Prepare the new dictionary
        result = {}
        for key, value in properties.items():
            if key in prop_filter:
                # Key renamed
                key = prop_filter[key]

            if key is not None:
                # Only accept valid keys
                result[key] = value

        # Add the import flag
        result[pelix.remote.PROP_IMPORTED] = True
        return result


    def _handle_remote_event(self, sender, remote_event):
        """
        Handles a remote service event.

        :param sender: UID of the isolate sending the event
        :param remote_event: Raw remote service event dictionary
        """
        try:
            # Compute the event type
            event_type = remote_event["eventType"]["enumValue"]

        except KeyError as ex:
            _logger.error("Invalid RemoteEvent object: %s", ex)
            return

        # Get the registration bean (as a dict)
        registration = remote_event["serviceRegistration"]

        # Get the first end point
        endpoint = registration["endpoints"][0]

        # Prepare the end point description
        name = endpoint["endpointName"]
        uid = "{0}@{1}".format(name, registration["hostIsolate"])
        kind = endpoint["exportedConfig"]
        specs = registration["exportedInterfaces"]

        # Filter the properties (replace exports by imports)
        properties = self._filter_properties(registration["serviceProperties"])

        # Prepare the access URL
        host = self._directory.get_host_for_node(endpoint["node"])
        if ':' in host:
            # IPv6 handling
            host = "[{0}]".format(host)

        url = "{0}://{1}:{2}{3}".format(endpoint["protocol"], host,
                                        endpoint["port"],
                                        endpoint["endpointUri"])

        rs_endpoint = pelix.remote.ImportEndpoint(uid, kind, name, url,
                                                  specs, properties)

        if event_type == REGISTERED:
            # New service
            self._registry.add(rs_endpoint)

        elif event_type == MODIFIED:
            # Update (without previous value)
            self._registry.update(rs_endpoint, None)

        elif event_type == UNREGISTERED:
            # Removed
            self._registry.remove(rs_endpoint)


    def _request_endpoints(self, isolate=None):
        """
        Requests the services exported by the given isolate. If isolate is None,
        then the request is sent to all known isolates.
        
        :param: isolate: An isolate UID (optional)
        """
        if not isolate:
            raw_results = self._sender.send(\
                                        BROADCASTER_SIGNAL_REQUEST_ENDPOINTS,
                                        None, dir_group="OTHERS")

        else:
            raw_results = self._sender.send(\
                                        BROADCASTER_SIGNAL_REQUEST_ENDPOINTS,
                                        None, isolate=isolate)

        if raw_results is None:
            # Nothing to do
            return

        else:
            # Extract information
            sig_results = raw_results[0]
            if not sig_results:
                # Nothing to do...
                return

        for isolate_uid, isolate_sigresult in sig_results.items():
            for result in isolate_sigresult['results']:
                if isinstance(result, list):
                    for event in result:
                        # Handle each event
                        self._handle_remote_event(isolate_uid, event)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister from signals
        self._receiver.unregister_listener(BROADCASTER_SIGNALS_PATTERN, self)

        self._context = None
        _logger.info("Signals discovery invalidated")


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context

        # Register to signals
        self._receiver.register_listener(BROADCASTER_SIGNALS_PATTERN, self)

        # Send a discovery request
        self._request_endpoints()

        _logger.info("Signals discovery validated")
