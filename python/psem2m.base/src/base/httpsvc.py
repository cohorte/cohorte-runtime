#-- Content-Encoding: UTF-8 --
"""
Created on 29 févr. 2012

@author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import threading
import logging
import os
import sys

_logger = logging.getLogger(__name__)

if sys.version_info >= (3, 0):
    # Python 3
    import urllib.parse as urlparse
    from http.server import HTTPServer
    from http.server import BaseHTTPRequestHandler

else:
    # Python 2
    import urlparse
    from BaseHTTPServer import HTTPServer
    from BaseHTTPServer import BaseHTTPRequestHandler

# ------------------------------------------------------------------------------

from psem2m.component.decorators import ComponentFactory, Provides, Validate, \
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
        self.service = http_svc
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

        for path in self.service.servlets:
            if path in parsed_path:
                # Found a corresponding servlet
                servlet = self.service.servlets[path]

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
        self.wfile.write(page)


@ComponentFactory(name="HttpServiceFactory")
@Instantiate("HttpService", {"http.port": int(os.getenv("HTTP_PORT", 10000))})
@Provides(specifications="HttpService")
@Property("port", "http.port", 8080)
class HttpService(object):
    """
    HTTP Service
    """
    def __init__(self):
        """
        Constructor
        """
        self.server = None
        self.servlets = {}
        self.thread = None

    @Validate
    def validate(self, context):
        """
        Component validation
        """
        _logger.info("Starting HTTP server (%d)...", self.port)

        self.server = HTTPServer(('', self.port), \
                                 lambda * x : RequestHandler(self, *x))

        self.thread = threading.Thread(target=self.server.serve_forever)
        self.thread.start()

        _logger.info("HTTP server started (%d)", self.port)

    def register_servlet(self, path, handler):
        """
        Registers a servlet
        """
        self.servlets[path] = handler


    def unregister_servlet(self, handler, path=None):
        """
        Unregisters a servlet
        """
        if path is not None:
            if self.servlets.get(path, None) == handler:
                del self.servlets[path]

        else:
            paths = [path
                     for (path, path_handler) in self.servlets.items()
                     if path_handler == handler]

            for path in paths:
                del self.servlets[path]


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidation
        """
        _logger.info("Shutting down HTTP server (%d)...", self.port)
        # Shutdown connections
        self.server.shutdown()

        # Wait for the thread to stop...
        self.thread.join(2)
        self.thread = None

        # Force the socket to be closed
        self.server.socket.close()
        _logger.info("HTTP server down (%d)", self.port)
