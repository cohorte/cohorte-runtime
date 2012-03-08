#-- Content-Encoding: UTF-8 --
"""
Created on 1 mars 2012

@author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import logging
import threading
import os
_logger = logging.getLogger(__name__)

from jsonrpclib.SimpleJSONRPCServer import SimpleJSONRPCServer
import jsonrpclib

# ------------------------------------------------------------------------------

from psem2m.component.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Instantiate, Property
from base.javautils import to_jabsorb, from_jabsorb, JAVA_CLASS

import psem2m.services.pelix as pelix

# ------------------------------------------------------------------------------

EXPORTED_SERVICE_FILTER = "(|(service.exported.interfaces=*)(service.exported.configs=*))"

ISOLATE_LOST_SIGNAL = "/psem2m/isolate/lost"

BROADCASTER_SIGNAL_NAME_PREFIX = "/psem2m/remote-service-broadcaster"
BROADCASTER_SIGNAL_REQUEST_ENDPOINTS = "%s/request-endpoints" \
                                        % BROADCASTER_SIGNAL_NAME_PREFIX
SIGNAL_REMOTE_EVENT = "%s/remote-event" % BROADCASTER_SIGNAL_NAME_PREFIX
SIGNAL_REQUEST_ENDPOINTS = "%s/request-endpoints" \
                                        % BROADCASTER_SIGNAL_NAME_PREFIX

SERVICE_EXPORTED_PREFIX = "service.exported."
SERVICE_EXPORTED_CONFIGS = "service.exported.configs"
SERVICE_EXPORTED_INTERFACES = "service.exported.interfaces"
SERVICE_IMPORTED = "service.imported"
SERVICE_IMPORTED_CONFIGS = "service.imported.configs"

# ------------------------------------------------------------------------------

@ComponentFactory("ServiceExporterFactory")
@Instantiate("ServiceExporter")
@Requires("sender", "org.psem2m.SignalSender")
@Requires("receiver", "org.psem2m.SignalReceiver")
@Requires("directory", "org.psem2m.IsolateDirectory")
@Property("port", "jsonrpc.port", int(os.getenv("RPC_PORT", 10001)))
class ServiceExporter(object):
    """
    PSEM2M Remote Services exporter
    """
    def __init__(self):
        """
        Constructor
        """
        # Server
        self.server = None
        self.port = 8080
        self._thread = None

        # Bundle context
        self.context = None

        # Signal sender
        self.directory = None
        self.sender = None

        # Exported services
        self._exported_references = []
        self._endpoints = {}
        self._registrations = {}


    def _dispatch(self, method, params):
        """
        Called by (xml|json)rpclib
        """
        found = None
        len_found = 0

        for name in self._endpoints:
            if len(name) > len_found and method.startswith(name + "."):
                # Better matching end point name (longer that previous one)
                found = name
                len_found = len(found)

        if found is None:
            # Method not found
            raise NameError("No end point found for %s" % method)

        # Get the method name. +1 for the trailing dot
        method_name = method[len_found + 1:]

        # Get the service
        svc = self._endpoints[found][1]

        # Convert parameters from Jabsorb
        converted_params = []
        for param in params:
            converted_params.append(from_jabsorb(param))

        result = getattr(svc, method_name)(*converted_params)

        # Transform result to Jabsorb
        return to_jabsorb(result)


    def _export_service(self, reference):
        """
        Exports the given service
        """
        if reference in self._exported_references:
            # Already exported service
            return

        # Compute the end point name
        endpoint_name = reference.get_property("endpoint.name")
        if endpoint_name is None:
            endpoint_name = "service_%d" \
                                % reference.get_property(pelix.SERVICE_ID)

        # Get the service
        try:
            svc = self.context.get_service(reference)
            if svc is None:
                _logger.error("Invalid service for reference %s",
                              str(reference))

        except pelix.BundleException:
            _logger.exception("Error retrieving the service to export")
            return

        # Register the reference and the service
        self._exported_references.append(reference)
        self._endpoints[endpoint_name] = (reference, svc)

        _logger.debug("> Exported: %s (%s)", endpoint_name, str(reference))

        specifications = reference.get_property("objectClass")
        if isinstance(specifications, str):
            specifications = tuple([specifications])
        else:
            specifications = tuple(specifications)

        exported_config = reference.get_property("service.exported.configs")
        if exported_config is None:
            exported_config = "*"

        # Get the service properties
        properties = reference.get_properties()

        # Generate the service ID
        service_id = "%s.%s" % (self.directory.get_current_isolate_id(),
                                properties.get(pelix.SERVICE_ID))

        # Create the registration map
        registration = {
            JAVA_CLASS: "org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration",
            "endpoints": ({
                    JAVA_CLASS: "org.psem2m.isolates.services.remote.beans.EndpointDescription",
                    "endpointName": endpoint_name,
                    "endpointUri": "/",
                    "exportedConfig": exported_config,
                    "host": "localhost",
                    "port": self.port,
                    "protocol": "http"
                },),
            "exportedInterfaces": specifications,
            "hostIsolate": self.directory.get_current_isolate_id(),
            "serviceProperties": properties,
            "serviceId": service_id
        }

        # Store it
        self._registrations[reference] = registration

        # Send registration signal
        remote_event = {
            JAVA_CLASS: "org.psem2m.isolates.services.remote.beans.RemoteServiceEvent",
            "eventType": {
                JAVA_CLASS:"org.psem2m.isolates.services.remote.beans.RemoteServiceEvent$ServiceEventType",
                "enumValue":"REGISTERED"
            },
            "senderHostName": "localhost",
            "serviceRegistration": registration
        }

        self.sender.send_data("*", SIGNAL_REMOTE_EVENT, remote_event)


    def _unexport_service(self, reference):
        """
        Stops the export of the given service
        """
        if reference not in self._exported_references:
            # Unknown reference
            return

        # Remove corresponding end points
        endpoints = [endpoint
                     for endpoint, ref in self._endpoints.items()
                     if ref[0] == reference]

        for endpoint in endpoints:
            del self._endpoints[endpoint]

        # Remove the reference from the list
        self._exported_references.remove(reference)

        # Pop the registration object
        registration = self._registrations.pop(reference)

        # Send signal
        remote_event = {
            JAVA_CLASS: "org.psem2m.isolates.services.remote.beans.RemoteServiceEvent",
            "eventType": {
                JAVA_CLASS:"org.psem2m.isolates.services.remote.beans.RemoteServiceEvent$ServiceEventType",
                "enumValue":"UNREGISTERED"
            },
            "senderHostName": "localhost",
            "serviceRegistration": registration
        }

        self.sender.send_data("*", SIGNAL_REMOTE_EVENT, remote_event)


    def service_changed(self, event):
        """
        Called when a service event is triggered
        """
        kind = event.get_type()
        ref = event.get_service_reference()

        if kind == pelix.ServiceEvent.REGISTERED or \
                (kind == pelix.ServiceEvent.MODIFIED \
                 and ref not in self._exported_references):
            # Matching registering or updated service
            self._export_service(ref)

        elif ref in self._exported_references and\
                (kind == pelix.ServiceEvent.UNREGISTERING or \
                 kind == pelix.ServiceEvent.MODIFIED_ENDMATCH):
            # Service is updated or unregistering
            self._unexport_service(ref)


    def handle_received_signal(self, name, signal_data):
        """
        Called when a remote services signal is received
        """
        if name != BROADCASTER_SIGNAL_REQUEST_ENDPOINTS:
            # Never happens... in theory...
            return

        sender = signal_data["isolateSender"]
        if sender == self.sender.get_current_isolate_id():
            # Ignore local events
            return

        events = [
                  {
                   JAVA_CLASS: "org.psem2m.isolates.services.remote.beans.RemoteServiceEvent",
                   "eventType": {
                                 JAVA_CLASS:"org.psem2m.isolates.services.remote.beans.RemoteServiceEvent$ServiceEventType",
                                 "enumValue":"REGISTERED"
                                 },
                   "senderHostName": "localhost",
                   "serviceRegistration": registration
                   }
                  for registration in self._registrations.values()]

        self.sender.send_data(sender, SIGNAL_REMOTE_EVENT, events)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # Store the bundle context
        self.context = context

        # Set up the JSON-RPC server
        self.server = SimpleJSONRPCServer(("localhost", self.port),
                                          logRequests=False)

        # The service exporter is the only RPC instance
        self.server.register_instance(self)

        # Export existing services
        existing_ref = context.get_all_service_references(None,
                                                        EXPORTED_SERVICE_FILTER)

        if existing_ref is not None:
            for reference in existing_ref:
                self._export_service(reference)

        # Register a service listener, to update the exported services state
        context.add_service_listener(self, EXPORTED_SERVICE_FILTER)

        # Register to the REQUEST_ALL_ENDPOINTS signal
        self.receiver.register_listener(BROADCASTER_SIGNAL_REQUEST_ENDPOINTS,
                                        self)

        # Start the RPC thread
        self.thread = threading.Thread(target=self.server.serve_forever)
        self.thread.start()


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister to the REQUEST_ALL_ENDPOINTS signal
        self.receiver.unregister_listener(BROADCASTER_SIGNAL_REQUEST_ENDPOINTS,
                                          self)

        # Stop the server
        self.server.shutdown()
        self.server.socket.close()

        # Wait a little for the thread
        self.thread.join(1)
        self.thread = None

        # Unregister the service listener
        context.remove_service_listener(self)

        # Remove all exports
        references_copy = self._exported_references[:]
        for reference in references_copy:
            self._unexport_service(reference)

        # Clean up the storage, to be sure of our state
        self._endpoints.clear()
        del self._exported_references[:]

        # Remove the reference to the context
        self.context = None

# ------------------------------------------------------------------------------

def _filter_export_properties(properties):
    """
    Filters imported service properties. Makes a new dictionary

    @param properties: Imported service properties
    @return: A filtered dictionary
    """
    if properties is None:
        return None

    result = {}

    # Remove export properties
    for key, value in properties.items():
        if not key.startswith(SERVICE_EXPORTED_PREFIX):
            result[key] = value

    # Add import properties
    result[SERVICE_IMPORTED] = "True"

    if SERVICE_EXPORTED_CONFIGS in properties:
        result[SERVICE_IMPORTED_CONFIGS] = properties[SERVICE_EXPORTED_CONFIGS]

    return result


class _JSON_proxy(object):
    """
    JSON-RPC proxy
    """
    def __init__(self, endpoint, endpoint_name):
        """
        Constructor
        """
        self.server = jsonrpclib.Server(endpoint)
        self.endpoint = endpoint_name


    def __getattr__(self, name):
        """
        Proxy core
        """

        def wrapped_call(*args, **kwargs):
            """
            Wrapped call
            """
            method_name = "%s.%s" % (self.endpoint, name)
            _logger.debug("Remote call to %s", method_name)

            method = self.server.__getattr__(method_name)
            if args:
                args = [to_jabsorb(arg) for arg in args]

            if kwargs:
                kwargs = dict([(key, to_jabsorb(value))
                               for key, value in kwargs.items()])

            result = method(*args, **kwargs)
            return from_jabsorb(result)

        return wrapped_call


    def __stop__(self):
        """
        Stops the proxy
        """
        self.server = None
        self.endpoint = None


@ComponentFactory("ServiceImporterFactory")
@Instantiate("ServiceImporter")
@Requires("sender", "org.psem2m.SignalSender")
@Requires("receiver", "org.psem2m.SignalReceiver")
class ServiceImporter(object):
    """
    PSEM2M Remote Services importer
    """
    def __init__(self):
        """
        Constructor
        """
        # Dependencies
        self.sender = None
        self.receiver = None

        # Bundle context
        self.context = None

        # Isolate ID -> [Service ID]
        self._services = {}

        # Service ID -> (proxy, reference)
        self._registered_services = {}


    def handle_received_signal(self, name, signal_data):
        """
        Called when a remote services signal is received
        """
        sender = signal_data["isolateSender"]
        if sender == self.sender.get_current_isolate_id():
            # Ignore local events
            return

        # Get the raw signal content
        data = signal_data["signalContent"]

        if name == SIGNAL_REMOTE_EVENT:
            # Remote service event
            if isinstance(data, list):
                # Multiple events in one signal
                for event in data:
                    try:
                        self._handle_remote_event(sender, event)
                    except:
                        _logger.exception("Error reading RemoteServiceEvent")

            else:
                # Single event
                self._handle_remote_event(sender, data)

        elif name == ISOLATE_LOST_SIGNAL and isinstance(data, str):
            # Isolate lost (data : isolate name)
            self._handle_isolate_lost(data)


    def _handle_remote_event(self, sender, remote_event):
        """
        Handle a remote service event.

        @param remote_event: Raw remote service event dictionary
        """
        try:
            event_type = remote_event["eventType"]["enumValue"]
        except:
            _logger.exception("Invalid RemoteEvent object\n%s", remote_event)
            return

        if event_type == "REGISTERED":
            self._import_service(remote_event)

        elif event_type == "UNREGISTERED":
            self._unimport_service(sender,
                            remote_event["serviceRegistration"]["serviceId"])


    def _handle_isolate_lost(self, isolate_name):
        """
        Handle an isolate lost signal

        @param isolate_name: Name of the lost isolate
        """
        services = self._services.get(isolate_name, None)
        if not services:
            # No service registered for this isolate
            return

        services_copy = services[:]
        for service_id in services_copy:
            # Cancel the import
            self._unimport_service(isolate_name, service_id)

        # Clean up the isolate services list
        del services[:]


    def _import_service(self, remote_event):
        """
        Import the given remote service
        """
        remote_reg = remote_event["serviceRegistration"]

        # Store remote service ID
        service_id = remote_reg["serviceId"]
        if service_id in self._registered_services:
            # Already registered service
            _logger.debug("Service ID already registered")
            return

        # TODO: add service filter

        # Select the end point
        endpoints = remote_reg["endpoints"]
        if not endpoints:
            # No end point
            _logger.error("No end point given")
            return

        # TODO: select it upon available proxies
        endpoint = endpoints[0]

        # Extract end point information
        protocol = endpoint.get("protocol", "http")
        host = endpoint.get("host", "localhost")
        port = int(endpoint.get("port", 80))
        uri = endpoint.get("endpointUri", "/")
        endpoint_name = endpoint.get("endpointName", service_id)

        # Create the proxy (select the factory according to export config)
        endpoint_url = "%s://%s:%d%s" % (protocol, host, port, uri)
        proxy = _JSON_proxy(endpoint_url, endpoint_name)
        if proxy is None:
            _logger.error("Remote service proxy not created")
            return

        # Filter properties
        properties = _filter_export_properties(remote_reg["serviceProperties"])

        # Register the service
        try:
            reg = self.context.register_service(
                        remote_reg["exportedInterfaces"], proxy, properties)

        except pelix.BundleException:
            _logger.exception("Error registering the proxy service")
            reg = None

        if reg is None:
            # Error creating the proxy
            proxy.__stop__()
            return

        # Store information
        self._registered_services[service_id] = (proxy, reg)

        isolate_name = remote_reg["hostIsolate"]
        services = self._services.get(isolate_name, None)
        if services is None:
            services = []
            self._services[isolate_name] = services

        services.append(service_id)


    def _unimport_service(self, isolate_name, service_id):
        """
        Cancel the import of the given service
        """
        service = self._registered_services.get(service_id, None)
        if not service:
            _logger.debug("Unknown service - %s", service_id)
            # Unknown service
            return

        # Extract service registration and proxy instance
        proxy, reg = service

        try:
            # Unregister the service from Pelix
            reg.unregister()

        except pelix.BundleException:
            _logger.exception("Error unregistering the imported service %s",
                              service_id)

        # Stop the proxy
        proxy.__stop__()

        # Delete the references to this service
        del self._registered_services[service_id]

        isolate_services = self._services.get(isolate_name, None)
        if isolate_services is not None and service_id in isolate_services:
            isolate_services.remove(service_id)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self.context = context

        # Clean up a little, just in case
        self._services.clear()
        self._registered_services.clear()

        # Register remote services signals
        self.receiver.register_listener(BROADCASTER_SIGNAL_NAME_PREFIX + "/*",
                                        self)
        self.receiver.register_listener(ISOLATE_LOST_SIGNAL, self)

        # Send "request endpoints" signal
        self.sender.send_data("*", BROADCASTER_SIGNAL_REQUEST_ENDPOINTS, None)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister remote services signals
        self.receiver.unregister_listener(BROADCASTER_SIGNAL_NAME_PREFIX + "/*",
                                          self)
        self.receiver.unregister_listener(ISOLATE_LOST_SIGNAL, self)

        # Unregister imported services (in a single loop)
        for imported_service in self._registered_services.values():
            if imported_service is None:
                continue

            proxy, reg = imported_service
            try:
                # Unregister the service
                if reg is not None:
                    reg.unregister()

            except pelix.BundleException:
                _logger.exception("Error unregistering service")

            if proxy is not None:
                # Stop the proxy
                proxy.__stop__()

        # Clean up the collections
        self._services.clear()
        self._registered_services.clear()

        # Forget the context
        self.context = None
