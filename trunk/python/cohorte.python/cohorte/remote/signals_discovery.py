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

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Requires
import pelix.remote
import pelix.remote.beans as beans

# Standard library
import logging

# ------------------------------------------------------------------------------

SIGNALS_NAME_PREFIX = "/cohorte/remote-service-broadcaster"
SIGNALS_MATCH_ALL = "{0}/*".format(SIGNALS_NAME_PREFIX)
SIGNAL_REMOTE_EVENT = "{0}/remote-event".format(SIGNALS_NAME_PREFIX)
SIGNAL_REQUEST_ENDPOINTS = "{0}/request-endpoints".format(SIGNALS_NAME_PREFIX)

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

ENDPOINT_EVENT_TYPE = "org.cohorte.remote.discovery.signals.EEndpointEventType"
EVENT_REGISTERED = {'javaClass': ENDPOINT_EVENT_TYPE, 'enumValue': "REGISTERED"}
EVENT_MODIFIED = {'javaClass': ENDPOINT_EVENT_TYPE, 'enumValue': "MODIFIED"}
EVENT_UNREGISTERED = {'javaClass': ENDPOINT_EVENT_TYPE,
                      'enumValue': "UNREGISTERED"}

class EndpointEventBean(object):
    """
    An endpoint event bean
    """
    javaClass = "org.cohorte.remote.discovery.signals.EndpointEventBean"
    """ Java class (for Jabsorb) """

    def __init__(self, event_type):
        """
        Sets up members
        """
        self.endpoints = tuple()
        self.type = event_type


    def set_endpoints(self, export_endpoints):
        """
        Make Java beans from endpoints
        """
        self.endpoints = tuple(EndpointDescriptionBean(export_endpoint)
                               for export_endpoint in export_endpoints)

class EndpointDescriptionBean(object):
    """
    An endpoint description bean
    """
    javaClass = "org.cohorte.remote.discovery.signals.EndpointDescriptionBean"
    """ Java class (for Jabsorb) """

    def __init__(self, export_endpoint):
        """
        Sets up the bean according to the given ExportEndpoint bean

        :param export_endpoint: An ExportEndpoint bean
        """
        self.uid = export_endpoint.uid
        self.frameworkUid = export_endpoint.framework
        self.name = export_endpoint.name

        # Use tuple to force arrays in the Jabsorb serialization
        self.configurations = tuple(export_endpoint.configurations)
        self.specifications = tuple(export_endpoint.specifications)

        # Store merged properties
        self.properties = export_endpoint.make_import_properties()

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-remote-discovery-signals-factory")
@Provides((pelix.remote.SERVICE_EXPORT_ENDPOINT_LISTENER,
           cohorte.signals.SERVICE_ISOLATE_PRESENCE_LISTENER))
@Requires('_dispatcher', pelix.remote.SERVICE_DISPATCHER)
@Requires("_registry", pelix.remote.SERVICE_REGISTRY)
@Requires("_directory", cohorte.SERVICE_SIGNALS_DIRECTORY)
@Requires("_receiver", cohorte.SERVICE_SIGNALS_RECEIVER)
@Requires("_sender", cohorte.SERVICE_SIGNALS_SENDER)
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

        # End points registry
        self._dispatcher = None
        self._registry = None

        # Signals
        self._directory = None
        self._receiver = None
        self._sender = None


    def _request_endpoints(self, isolate=None):
        """
        Requests the services exported by the given isolate. If isolate is None,
        then the request is sent to all known isolates.

        :param: isolate: An isolate UID (optional)
        """
        # Prepare our event, to be sent with the request
        local_event = self._make_exports_event()

        if not isolate:
            raw_results = self._sender.send(SIGNAL_REQUEST_ENDPOINTS,
                                            local_event,
                                        dir_group=cohorte.signals.GROUP_OTHERS)

        else:
            raw_results = self._sender.send(SIGNAL_REQUEST_ENDPOINTS,
                                            local_event, isolate=isolate)

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
            # Get the first result as the event
            if not isolate_sigresult:
                _logger.warning("Isolate %s returned None", isolate_uid)
                continue

            isolate_results = isolate_sigresult['results']
            if not isolate_results:
                _logger.warning("Isolate %s returned nothing", isolate_uid)
                continue

            event = isolate_results[0]

            # Get the node of the isolate
            node_uid = self._directory.get_isolate_node(isolate_uid)

            # Handle the event
            self.__handle_event(event,
                                self._directory.get_host_for_node(node_uid))


    def _make_exports_event(self):
        """
        Returns a single REGISTERED endpoint event, matching the endpoints
        exported by the local isolate
        """
        # Get the export endpoints
        event = EndpointEventBean(EVENT_REGISTERED)

        event.set_endpoints(self._dispatcher.get_endpoints())
        return event


    def _send_event(self, event):
        """
        Sends an event using Signals.

        :param event: An EndpointEventBean
        """
        self._sender.fire(SIGNAL_REMOTE_EVENT, event,
                          dir_group=cohorte.signals.GROUP_OTHERS)


    def endpoints_added(self, endpoints):
        """
        New endpoints are exported
        """
        # Prepare the event bean
        event = EndpointEventBean(EVENT_REGISTERED)
        event.set_endpoints(endpoints)

        # Send the signal
        self._send_event(event)


    def endpoint_updated(self, endpoint, old_properties):
        """
        An end point is updated
        """
        # Prepare the event bean
        event = EndpointEventBean(EVENT_MODIFIED)
        event.set_endpoints([endpoint])

        # Send the signal
        self._send_event(event)


    def endpoint_removed(self, endpoint):
        """
        An end point has been removed

        :param endpoint: An ExportEndpoint bean
        """
        # Prepare the event bean
        event = EndpointEventBean(EVENT_UNREGISTERED)
        event.set_endpoints([endpoint])

        # Send the signal
        self._send_event(event)


    def __handle_event(self, event, sender_address):
        """
        Hanldles an endpoint event

        :param event: An EndpointEvent bean
        :param sender_address: The address of the event sender
        """
        kind = event.type

        if kind == EVENT_REGISTERED:
            # Registration of endpoints
            self.__register_endpoints(event.endpoints, sender_address)

        elif kind == EVENT_MODIFIED:
            # Single endpoint updated
            endpoint = event.endpoints[0]
            self._registry.update(endpoint.uid, endpoint.properties)

        elif kind == EVENT_UNREGISTERED:
            # Single endpoint unregistered
            endpoint = event.endpoints[0]
            self._registry.remove(endpoint.uid)

        else:
            # Unknown kind of event
            _logger.warning("Unknown RS event type: %s", kind)


    def __register_endpoints(self, endpoints, server):
        """
        Registers the new endpoints
        """
        for endpoint in endpoints:
            # Make the ImportEndpoint bean
            import_endpoint = beans.ImportEndpoint(endpoint['uid'],
                                                   endpoint['frameworkUid'],
                                                   endpoint['configurations'],
                                                   endpoint['name'],
                                                   endpoint['specifications'],
                                                   endpoint['properties'])
            import_endpoint.server = server

            # Notify the import registry
            self._registry.add(import_endpoint)


    def handle_isolate_presence(self, uid, name, node, event):
        """
        Handles an isolate presence event

        :param uid: UID of the isolate
        :param name: Name of the isolate
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
        # Get the sender address
        sender = signal_data['senderAddress']

        if name == SIGNAL_REMOTE_EVENT:
            # Received a remote service event
            data = signal_data["signalContent"]

            # Single event
            self.__handle_event(data, sender)

        elif name == SIGNAL_REQUEST_ENDPOINTS:
            # An isolate requests all of our exported services
            event = signal_data["signalContent"]

            # Register remote endpoints
            self.__handle_event(event, sender)

            # Return our endpoints
            return self._make_exports_event()


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister from signals
        self._receiver.unregister_listener(SIGNALS_MATCH_ALL, self)

        self._context = None
        _logger.info("Signals discovery invalidated")


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context

        # Register to signals
        self._receiver.register_listener(SIGNALS_MATCH_ALL, self)

        # Send a discovery request
        self._request_endpoints()

        _logger.info("Signals discovery validated")
