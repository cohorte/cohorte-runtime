#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Remote Services: JSON-RPC in Jabsorb format

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# JSON-RPC
from jsonrpclib.SimpleJSONRPCServer import SimpleJSONRPCDispatcher, \
    NoMulticallResult
import jsonrpclib.jsonrpc as jsonrpclib

# Cohorte
import cohorte.java.jabsorb as jabsorb

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Requires, Property
import pelix.framework
import pelix.http
import pelix.remote.beans
from pelix.utilities import to_str

# Standard library
import logging
import socket
import threading
import uuid

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

class _JabsorbRpcServlet(SimpleJSONRPCDispatcher):
    """
    A JSON-RPC servlet, replacing the SimpleJSONRPCDispatcher from jsonrpclib,
    converting data from and to Jabsorb format.
    """
    def __init__(self, dispatch_method, encoding=None):
        """
        Sets up the servlet
        """
        SimpleJSONRPCDispatcher.__init__(self, encoding)

        # Register the system.* functions
        self.register_introspection_functions()

        # Make a link to the dispatch method
        self._dispatch_method = dispatch_method


    def _simple_dispatch(self, name, params):
        """
        Dispatch method
        """
        try:
            # Internal method
            return self.funcs[name](*params)

        except KeyError:
            # Other method
            pass

        # Avoid calling this method in the "except" block, as it would be in
        # an exception state (logs will consider the KeyError as a failure)
        return self._dispatch_method(name, params)


    def do_POST(self, request, response):
        """
        Handle a post request

        :param request: The HTTP request bean
        :param request: The HTTP response handler
        """
        # Get the request JSON content
        data = jsonrpclib.loads(to_str(request.read_data()))

        # Convert from Jabsorb
        data = jabsorb.from_jabsorb(data)

        # Dispatch
        try:
            result = self._unmarshaled_dispatch(data, self._simple_dispatch)

        except NoMulticallResult:
            # No result (never happens, but who knows...)
            result = None

        if result is not None:
            # Convert result to Jabsorb
            if 'result' in result:
                result['result'] = jabsorb.to_jabsorb(result['result'])

            # Store JSON
            result = jsonrpclib.jdumps(result)

        else:
            # It was a notification
            result = ''

        # Send the result
        response.send_content(200, result, 'application/json-rpc')

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-jabsorbrpc-exporter-factory")
@Requires('_dispatcher', pelix.remote.SERVICE_DISPATCHER)
@Requires('_http', pelix.http.HTTP_SERVICE)
@Property('_path', pelix.http.HTTP_SERVLET_PATH, '/JSON-RPC')
@Property('_kind', 'endpoints.kind', 'jsonrpc')
class JsonRpcServiceExporter(object):
    """
    JSON-RPC Remote Services exporter
    """
    def __init__(self):
        """
        Sets up the exporter
        """
        # Bundle context
        self._context = None

        # Dispatcher
        self._dispatcher = None
        self._kind = None

        # HTTP Service
        self._http = None
        self._path = None

        # JSON-RPC servlet
        self._servlet = None

        # Exported services: Name -> ExportEndpoint
        self.__endpoints = {}

        # Service Reference -> ExportEndpoint
        self.__registrations = {}


    def _dispatch(self, method, params):
        """
        Called by the JSON-RPC servlet: calls the method of an exported service
        """
        # Get the best matching name
        matching = None
        len_found = 0

        for name in self.__endpoints:
            len_name = len(name)
            if len_name > len_found and method.startswith(name + "."):
                # Better matching end point name (longer that previous one)
                matching = name
                len_found = len_name

        if matching is None:
            # No end point name match
            raise KeyError("No end point found for: {0}".format(method))

        # Extract the method name. (+1 for the trailing dot)
        method_name = method[len_found + 1:]

        # Call the dispatcher
        return self._dispatcher.dispatch(self._kind, matching,
                                         method_name, params)


    def _compute_endpoint_name(self, reference):
        """
        Computes the end point name according to service properties

        :param reference: A ServiceReference object
        :return: The computed end point name
        """
        service_id = reference.get_property(pelix.framework.SERVICE_ID)
        endpoint_name = reference.get_property(pelix.remote.PROP_ENDPOINT_NAME)
        if not endpoint_name:
            endpoint_name = 'service_{0}'.format(service_id)

        return endpoint_name


    def _export_service(self, reference):
        """
        Exports the given service

        :param reference: A ServiceReference object
        """
        # Compute the end point name
        endpoint_name = self._compute_endpoint_name(reference)
        if endpoint_name in self.__endpoints:
            # Already known end point
            _logger.error("Already known end point %s for JSON-RPC (jabsorb)",
                          endpoint_name)
            return

        # Get the service
        try:
            service = self._context.get_service(reference)
            if service is None:
                _logger.error("Invalid service for reference %s",
                              str(reference))

        except pelix.framework.BundleException as ex:
            _logger.error("Error retrieving the service to export: %s", ex)
            return

        try:
            # Create the registration information
            endpoint = pelix.remote.beans.ExportEndpoint(str(uuid.uuid4()),
                                                     self._kind, endpoint_name,
                                                     reference, service,
                                                     self.get_access())
        except ValueError:
            # Invalid end point
            return False

        try:
            # Register the end point
            self._dispatcher.add_endpoint(self._kind, endpoint_name, endpoint)

        except KeyError as ex:
            _logger.error("Error registering end point: %s", ex)

        else:
            # Store informations
            self.__endpoints[endpoint_name] = endpoint
            self.__registrations[reference] = endpoint


    def _update_service(self, reference, old_properties):
        """
        Service properties updated
        """
        # Compute the new end point name
        new_name = self._compute_endpoint_name(reference)

        # Get the end point
        endpoint = self.__registrations[reference]
        if endpoint.name != new_name:
            # Name changed -> re-export the service
            self._unexport_service(reference)
            self._export_service(reference)

        else:
            # Notify the dispatcher
            self._dispatcher.update_endpoint(self._kind, endpoint.name,
                                             endpoint, old_properties)


    def _unexport_service(self, reference):
        """
        Stops the export of the given service

        :param reference: A ServiceReference object
        """
        # Find the corresponding end point
        endpoint = self.__registrations.get(reference)
        if endpoint is not None:
            # Delete the registration
            del self.__registrations[reference]
            del self.__endpoints[endpoint.name]

            # Unregister the service from the dispatcher
            self._dispatcher.remove_endpoint(self._kind, endpoint.name)


    def service_changed(self, event):
        """
        Called when a service event is triggered
        """
        kind = event.get_kind()
        svcref = event.get_service_reference()

        if kind == pelix.framework.ServiceEvent.REGISTERED:
            # Simply export the service
            self._export_service(svcref)

        elif kind == pelix.framework.ServiceEvent.MODIFIED:
            # Matching registering or updated service
            if svcref not in self.__registrations:
                # New match
                self._export_service(svcref)

            else:
                # Properties modification:
                # Re-export if endpoint.name has changed
                self._update_service(svcref, event.get_previous_properties())

        elif svcref in self.__registrations and \
                (kind == pelix.framework.ServiceEvent.UNREGISTERING or \
                 kind == pelix.framework.ServiceEvent.MODIFIED_ENDMATCH):
            # Service is updated or unregistering
            self._unexport_service(svcref)


    def get_access(self):
        """
        Retrieves the URL to access this component
        """
        host, port = self._http.get_access()
        if ':' in host:
            # IPv6 address
            host = '[{0}]'.format(host)

        return "http://{0}:{1}{2}".format(host, port, self._path)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # Store the context
        self._context = context

        # Prepare the service filter
        ldapfilter = '(|(|({0}=jsonrpc)({0}=\*))(&(!({0}=*))({1}=*)))' \
                    .format(pelix.remote.PROP_EXPORTED_CONFIGS,
                            pelix.remote.PROP_EXPORTED_INTERFACES)

        # Export existing services
        existing_ref = self._context.get_all_service_references(None,
                                                                ldapfilter)
        if existing_ref is not None:
            for reference in existing_ref:
                self._export_service(reference)

        # Register a service listener, to update the exported services state
        self._context.add_service_listener(self, ldapfilter)

        # Create/register the servlet
        self._servlet = _JabsorbRpcServlet(self._dispatch)
        self._http.register_servlet(self._path, self._servlet)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Unregister the service listener
        context.remove_service_listener(self)

        # Unregister the servlet
        self._http.unregister(None, self._servlet)

        # Remove all exports
        for reference in list(self.__registrations.keys()):
            self._unexport_service(reference)

        # Clean up the storage
        self.__endpoints.clear()
        self.__registrations.clear()

        # Clean up members
        self._servlet = None
        self._context = None

# ------------------------------------------------------------------------------

class _ServiceCallProxy(object):
    """
    Service call proxy
    """
    def __init__(self, uid, name, url, on_error):
        """
        Sets up the call proxy

        :param uid: End point UID
        :param name: End point name
        :param url: End point URL
        :param on_error: A method to call back in case of socket error
        """
        self.__uid = uid
        self.__name = name
        self.__url = url
        self.__on_error = on_error


    def __getattr__(self, name):
        """
        Prefixes the requested attribute name by the endpoint name
        """
        # Make a proxy for this call
        # This is an ugly trick to handle multithreaded calls, as the underlying
        # proxy re-uses the same connection when possible: sometimes it means
        # sending a request before retrieving a result
        proxy = jsonrpclib.ServerProxy(self.__url)

        def wrapped_call(*args, **kwargs):
            """
            Wrapped call
            """
            # Get the method from the proxy
            method = getattr(proxy, "{0}.{1}".format(self.__name, name))

            # Convert arguments
            args = [jabsorb.to_jabsorb(arg) for arg in args]
            kwargs = dict([(key, jabsorb.to_jabsorb(value))
                               for key, value in kwargs.items()])

            try:
                result = method(*args, **kwargs)
                return jabsorb.from_jabsorb(result)

            except socket.error:
                # In case of transport error, look if the service has gone away
                if self.__on_error is not None:
                    self.__on_error(self.__uid)

                # Let the exception stop the caller
                raise

        return wrapped_call

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-jabsorbrpc-importer-factory")
@Provides(pelix.remote.SERVICE_ENDPOINT_LISTENER)
@Property('_kind', 'endpoints.kind', 'jsonrpc')
@Property('_listener_flag', pelix.remote.PROP_LISTEN_IMPORTED, True)
class JsonRpcServiceImporter(object):
    """
    JSON-RPC Remote Services importer
    """
    def __init__(self):
        """
        Sets up the exporter
        """
        # Bundle context
        self._context = None

        # Component properties
        self._kind = None
        self._listener_flag = True

        # Registered services (end point -> reference)
        self.__registrations = {}
        self.__reg_lock = threading.Lock()


    def endpoint_added(self, endpoint):
        """
        An end point has been imported
        """
        if endpoint.kind not in ('*', self._kind):
            # Not for us
            return

        with self.__reg_lock:
            # Already known end point
            if endpoint.uid in self.__registrations:
                return

            # Register the service
            svc = _ServiceCallProxy(endpoint.uid, endpoint.name, endpoint.url,
                                    self._unregister)
            svc_reg = self._context.register_service(endpoint.specifications,
                                                     svc, endpoint.properties)

            # Store references
            self.__registrations[endpoint.uid] = svc_reg


    def endpoint_updated(self, endpoint, old_properties):
        """
        An end point has been updated
        """
        with self.__reg_lock:
            if endpoint.uid not in self.__registrations:
                # Unknown end point
                return

            # Update service properties
            svc_reg = self.__registrations[endpoint.uid]
            svc_reg.set_properties(endpoint.properties)


    def endpoint_removed(self, endpoint):
        """
        An end point has been removed
        """
        with self.__reg_lock:
            if endpoint.uid in self.__registrations:
                # Unregister the end point
                self._unregister(endpoint.uid)


    def _unregister(self, endpoint_uid):
        """
        Unregisters the service associated to the given UID

        :param endpoint_uid: An end point UID
        :return: True on success, else False
        """
        try:
            # Pop references
            svc_reg = self.__registrations.pop(endpoint_uid)

            # Unregister the service
            svc_reg.unregister()
            return True

        except KeyError:
            _logger.debug("Unknown end point %s", endpoint_uid)
            return False

        except pelix.framework.BundleException as ex:
            _logger.debug("Can't unregister end point %s: %s", endpoint_uid, ex)
            return False


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._context = None
