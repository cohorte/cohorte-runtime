#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Created on 29 févr. 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import logging
import socket
import sys
import threading

_logger = logging.getLogger(__name__)

if sys.version_info[0] == 3:
    # Python 3
    import urllib.parse as urlparse
    from http.server import HTTPServer
    from http.server import BaseHTTPRequestHandler
    from socketserver import ThreadingMixIn

else:
    # Python 2
    import urlparse
    from BaseHTTPServer import HTTPServer
    from BaseHTTPServer import BaseHTTPRequestHandler
    from SocketServer import ThreadingMixIn

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Property, Instantiate

# ------------------------------------------------------------------------------

class RequestHandler(BaseHTTPRequestHandler):
    """
    HTTP Service request handler
    """
    def __init__(self, http_svc, *args, **kwargs):
        """
        Constructor
        """
        self._service = http_svc
        BaseHTTPRequestHandler.__init__(self, *args, **kwargs)


    def __getattr__(self, name):
        """
        Get attribute
        """
        if not name.startswith("do_"):
            # Not a request handling
            return object.__getattr__(self, name)

        # Parse the URL
        parsed_url = urlparse.urlparse(self.path)
        parsed_path = parsed_url.path
        if parsed_path[0] != '/':
            parsed_path = "/%s" % parsed_path

        servlet = self._service.get_servlet(parsed_path)
        if servlet is not None:
            # Found a corresponding servlet
            if hasattr(servlet, name):
                # Create a wrapper to pass the handler to the servlet
                def wrapper():
                    return getattr(servlet, name)(self)

                # Return it
                return wrapper

        # Return the super implementation if needed
        return self.send_default_response


    def log_error(self, message, *args):
        """
        Log server error
        """
        _logger.warning(message, *args)


    def log_message(self, *args, **kwargs):
        """
        Shut down server logging
        """
        pass


    def send_default_response(self):
        """
        Default response sent when no servlet is found
        """
        page = """<html>
<head>
</head>
<body>
<h1>No servlet associated to this path</h1>
<p>Path: <pre>%s</pre></p>
</body>
</html>""" % self.path

        # Send headers
        self.send_response(404)
        self.send_header("content-type", "text/html")
        self.send_header("content-length", len(page))
        self.end_headers()

        # Send content
        self.wfile.write(page.encode())

# ------------------------------------------------------------------------------

class _HttpServerFamily(ThreadingMixIn, HTTPServer):
    """
    A small modification to have a threaded HTTP Server with a custom address
    family
    
    Inspired from:
    http://www.arcfn.com/2011/02/ipv6-web-serving-with-arc-or-python.html
    """
    def __init__(self, address_family, server_address, RequestHandlerClass):
        """
        Proxy constructor
        """
        # Change the address family before the socket is created
        self.address_family = address_family

        # Special case: IPv6
        ipv6 = (address_family == socket.AF_INET6)

        # Set up the server, socket, ... but don't bind immediately in IPv6
        HTTPServer.__init__(self, server_address, RequestHandlerClass, not ipv6)

        if ipv6:
            # Explicitly ask to be accessible both by IPv4 and IPv6
            self.socket.setsockopt(socket.IPPROTO_IPV6, socket.IPV6_V6ONLY, 0)

            # Bind & accept
            self.server_bind()
            self.server_activate()


@ComponentFactory(name="HttpServiceFactory")
@Instantiate("HttpService")
@Provides(specifications="HttpService")
@Property("_port", "http.port", 8080)
class HttpService(object):
    """
    HTTP Service
    """
    def __init__(self):
        """
        Constructor
        """
        self._port = 8080

        # Servlets registries lock
        self._lock = threading.Lock()
        self._servlets = {}

        self._server = None
        self._thread = None


    def get_hostname(self):
        """
        Retrieves the server host name
        
        :return: The host name
        """
        return socket.gethostname()


    def get_port(self):
        """
        Retrieves the port that this server listens to
        
        :return: The port this server listens to
        """
        return self._port


    def get_servlet(self, path):
        """
        Retrieves the servlet matching the given path
        
        :param path: A request URI
        :return: The associated servlet or None
        """
        if not path:
            # No path, nothing to return
            _logger.debug("No path given")
            return None

        # Use lower case for comparison
        path = path.lower()

        with self._lock:
            for servlet_path, handler in self._servlets.items():
                if path.startswith(servlet_path):
                    # Found a corresponding servlet
                    return handler

        # Nothing found
        return None


    def register_servlet(self, path, handler):
        """
        Registers a servlet
        
        :param path: Prefix that must match a URI
        :param handler: The request handler
        :return: True if the servlet has been registered, False if one is only
                 registered for this exact path
        :raise ValueError: Invalid path or handler
        """
        if handler is None:
            raise ValueError("Invalid handler")

        if not path:
            raise ValueError("No path given to register the servlet")

        # Use lower-case paths
        path = path.lower()

        with self._lock:
            self._servlets[path] = handler


    def unregister_servlet(self, handler, path=None):
        """
        Unregisters a servlet
        
        :param handler: The handler to unregister
        :param path: If given, the handler is unregistered for this path only.
        """
        with self._lock:
            if path:
                # Use lower-case paths
                path = path.lower()

                if self._servlets.get(path, None) == handler:
                    del self._servlets[path]

            else:
                paths = [path
                         for (path, path_handler) in self._servlets.items()
                         if path_handler == handler]

                for path in paths:
                    del self._servlets[path]


    @Validate
    def validate(self, context):
        """
        Component validation
        """
        _logger.info("Starting HTTP server (%d)...", self._port)

        handler = lambda *x : RequestHandler(self, *x)

        try:
            # Try with IPv6
            self._server = _HttpServerFamily(socket.AF_INET6,
                                             ('::', self._port),
                                             handler)

        except:
            # Fall back on IPv4
            _logger.exception("IPv6 seems to be unsupported. "\
                              "Falling back to IPv4")

            self._server = _HttpServerFamily(socket.AF_INET,
                                             ('0.0.0.0', self._port),
                                             handler)

        self._thread = threading.Thread(target=self._server.serve_forever)
        self._thread.daemon = True
        self._thread.start()

        _logger.info("HTTP server started (%d)", self._port)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidation
        """
        _logger.info("Shutting down HTTP server (%d)...", self._port)
        # Shutdown server
        self._server.shutdown()

        # Wait for the thread to stop...
        _logger.info("Waiting HTTP server (%d) thread to stop...", self._port)
        self._thread.join(2)
        self._thread = None

        # Force the socket to be closed
        try:
            self._server.socket.close()

        except:
            # The socket should already have been closed, so ignore errors
            pass

        _logger.info("HTTP server down (%d)", self._port)
