#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Remote Services: Discovery based on Signals

:author: Thomas Calmant
:copyright: Copyright 2014, isandlaTech
:license: Apache License 2.0
:version: 1.1
:status: Beta

..

    Copyright 2013 isandlaTech

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Module version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

# Cohorte
import cohorte.signals
import cohorte.java.jabsorb as jabsorb

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Requires, Property
from pelix.utilities import is_string
import pelix.remote.beans

# Standard library
import logging

# ------------------------------------------------------------------------------

JAVA_BEANS_PACKAGE = "org.cohorte.remote.beans"
JAVA_ENDPOINT_DESCRIPTION = "{0}.EndpointDescription".format(JAVA_BEANS_PACKAGE)
JAVA_REMOTE_SERVICE_EVENT = "{0}.RemoteServiceEvent".format(JAVA_BEANS_PACKAGE)
JAVA_SERVICE_EVENT_TYPE = "{0}$ServiceEventType" \
                                            .format(JAVA_REMOTE_SERVICE_EVENT)
JAVA_REMOTE_SERVICE_REGISTRATION = "{0}.RemoteServiceRegistration" \
                                            .format(JAVA_BEANS_PACKAGE)

# ------------------------------------------------------------------------------

BROADCASTER_SIGNAL_NAME_PREFIX = "/cohorte/remote-service-broadcaster"
BROADCASTER_SIGNALS_PATTERN = "{0}/*".format(BROADCASTER_SIGNAL_NAME_PREFIX)
BROADCASTER_SIGNAL_REQUEST_ENDPOINTS = "{0}/request-endpoints" \
                                        .format(BROADCASTER_SIGNAL_NAME_PREFIX)
SIGNAL_REMOTE_EVENT = "{0}/remote-event".format(BROADCASTER_SIGNAL_NAME_PREFIX)
SIGNAL_REQUEST_ENDPOINTS = "{0}/request-endpoints" \
                                        .format(BROADCASTER_SIGNAL_NAME_PREFIX)

# ------------------------------------------------------------------------------

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
            self.endpoints = tuple()

        self.exportedInterfaces = tuple(specifications)
        self.hostIsolate = isolate_uid
        self.serviceProperties = properties
        self.serviceId = service_id


    def add_endpoint(self, endpoint):
        """
        Adds an end point to the registration
        """
        if endpoint is not None and endpoint not in self.endpoints:
            self.endpoints = self.endpoints + tuple([endpoint])


class _EndpointDescription(object):
    """
    Represents an end point Java bean
    """
    javaClass = JAVA_ENDPOINT_DESCRIPTION
    """ Java class (for Jabsorb) """

    def __init__(self, uid, name, exported_configs, node_uid):
        """
        Sets up the bean
        """
        self.endpointUid = uid
        self.endpointName = name
        self.exportedConfigs = tuple(exported_configs)
        self.node = node_uid

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-remote-discovery-signals-factory")
@Provides([pelix.remote.SERVICE_ENDPOINT_LISTENER,
           cohorte.signals.SERVICE_ISOLATE_PRESENCE_LISTENER])
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
        self._sender.post(SIGNAL_REMOTE_EVENT, remote_event,
                          dir_group=cohorte.signals.GROUP_OTHERS)


    def endpoints_added(self, endpoints):
        """
        New endpoints are exported
        """
        for endpoint in endpoints:
            self.endpoint_added(endpoint)


    def endpoint_added(self, endpoint):
        """
        A new service is exported
        """
        # Alias...
        svc_ref = endpoint.reference

        # Convert the specifications list into a set
        specifications = set(endpoint.specifications)

        # Add the synonyms
        synonyms = svc_ref.get_property(cohorte.SVCPROP_SYNONYM_INTERFACES)
        if synonyms:
            if is_string(synonyms):
                # Single synonym
                specifications.add(synonyms)

            else:
                # Iterable
                specifications.update(synonyms)

        # Get isolate information
        isolate_uid = self._context.get_property(cohorte.PROP_UID)
        node_uid = self._context.get_property(cohorte.PROP_NODE_UID)

        # Prepare an end point description
        java_endpoint = _EndpointDescription(endpoint.uid,
                                             endpoint.name,
                                             endpoint.configurations,
                                             node_uid)

        # Make a registration bean
        registration = _Registration(specifications, isolate_uid,
                                     endpoint.name, endpoint.get_properties(),
                                     [java_endpoint])

        # Store it
        self._registrations[svc_ref] = registration

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


    def handle_isolate_presence(self, uid, node, event):
        """
        Handles an isolate presence event

        :param uid: UID of the isolate
        :param node: Node of the isolate
        :param event: Kind of event
        """
        if event == cohorte.signals.ISOLATE_REGISTERED:
            # New isolate detected
            self._request_endpoints(uid)

        elif event == cohorte.signals.ISOLATE_UNREGISTERED:
            # Isolate lost
            self._registry.lost_framework(uid)


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
        endpoints = registration["endpoints"]
        if not endpoints:
            _logger.error("RemoteServiceEvent without end point:\n%s",
                          remote_event)
            return

        endpoint = endpoints[0]

        # Prepare the end point description
        uid = endpoint["endpointUid"]
        name = endpoint["endpointName"]
        framework = registration["hostIsolate"]
        kinds = endpoint["exportedConfigs"]
        specifications = registration["exportedInterfaces"]

        # Filter the properties (replace exports by imports)
        properties = self._filter_properties(registration["serviceProperties"])

        # ... add the source isolate information
        properties["service.imported.from"] = registration["hostIsolate"]

        # Create the bean
        rs_endpoint = pelix.remote.beans.ImportEndpoint(uid, framework, kinds,
                                                        name, specifications,
                                                        properties)

        if event_type == REGISTERED:
            # New service
            self._registry.add(rs_endpoint)

        elif event_type == MODIFIED:
            # Update (without previous value)
            self._registry.update(rs_endpoint, properties)

        elif event_type == UNREGISTERED:
            # Removed
            self._registry.remove(uid)


    def _request_endpoints(self, isolate=None):
        """
        Requests the services exported by the given isolate. If isolate is None,
        then the request is sent to all known isolates.

        :param: isolate: An isolate UID (optional)
        """
        if not isolate:
            raw_results = self._sender.send(\
                                        BROADCASTER_SIGNAL_REQUEST_ENDPOINTS,
                                        None,
                                        dir_group=cohorte.signals.GROUP_OTHERS)

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
