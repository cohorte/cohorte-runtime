#-- Content-Encoding: UTF-8 --
"""
Created on 1 mars 2012

@author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import logging
import threading
_logger = logging.getLogger(__name__)

from jsonrpclib.SimpleJSONRPCServer import SimpleJSONRPCServer

# ------------------------------------------------------------------------------

from psem2m.component.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Instantiate, Property

import psem2m.services.pelix as pelix

# ------------------------------------------------------------------------------

EXPORTED_SERVICE_FILTER = "(|(service.exported.interfaces=*)(service.exported.configs=*))"
JAVA_CLASS = u"javaClass"

# ------------------------------------------------------------------------------

def result_to_jabsorb(result):
    """
    Adds informations for Jabsorb, if needed
    """
    converted_result = {}

    # Map ?
    if isinstance(result, dict):
        converted_result["map"] = {}

        for key in result.keys():
            converted_result["map"][key] = result_to_jabsorb(result[key])

        converted_result[JAVA_CLASS] = "java.util.HashMap"

    # List ?
    elif isinstance(result, list):
        converted_result[JAVA_CLASS] = "java.util.ArrayList"
        converted_result["list"] = []

        for item in result:
            converted_result["list"].append(result_to_jabsorb(item))

    # Other ?
    else:
        converted_result = result

    return converted_result


def request_from_jabsorb(request):
    """
    Removes informations from jabsorb
    """
    if not isinstance(request, dict):
        # Raw element
        return request

    java_class = str(request[JAVA_CLASS])

    # Map ?
    if java_class.endswith("Map"):
        result = {}

        for key in request["map"]:
            result[key] = request_from_jabsorb(request["map"][key])

        return result

    # List ?
    elif java_class.endswith("List"):
        result = []

        for element in request["list"]:
            result.append(request_from_jabsorb(element))

        return result

    # Other ?
    return request

# ------------------------------------------------------------------------------

@ComponentFactory("ServiceExporterFactory")
@Instantiate("ServiceExporter")
@Requires("sender", "org.psem2m.SignalSender")
@Property("port", "jsonrpc.port", 10001)
class ServiceExporter(object):
    """
    PSEM2M Remote Services exporter
    """
    def __init__(self):
        """
        Constructor
        """
        self.server = None
        self.port = 8080
        self.context = None
        self._exported_references = []
        self._endpoints = {}
        self._thread = None


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
            converted_params.append(request_from_jabsorb(param))

        result = getattr(svc, method_name)(*converted_params)

        # Transform result to Jabsorb
        return result_to_jabsorb(result)


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

        _logger.debug("> Exported: %s", endpoint_name)

        # TODO: send signal


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

        # TODO: Send signals



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

        # Start the RPC thread
        self.thread = threading.Thread(target=self.server.serve_forever)
        self.thread.start()


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
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
