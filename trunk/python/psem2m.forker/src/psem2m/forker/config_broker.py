#!/usr/bin/python
#-- Content-Encoding: UTF-8 --
"""
Forker configuration broker.

A simple servlet handling GET and DELETE commands to provide a raw JSON
configuration for the requested isolate, if available.

The stored configurations should be the ones given by a monitor requesting to
start an isolate.

A configuration should be deleted on a request by the isolate itself when it
read it correctly.

:author: Thomas Calmant
"""
import threading

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Instantiate, \
    Requires, Validate, Invalidate, Property, Provides

# ------------------------------------------------------------------------------

import logging
_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

def send_response(handler, code, data, content_type='text/html'):
    """
    Sends a response using the given handler
    
    :param handler: The request handler
    :param code: The response code
    :param data: The response content
    :param content_type: The response content type
    """
    if code < 400:
        handler.send_response(code)

    else:
        handler.send_error(code)

    if content_type is not None:
        handler.send_header('Content-Type', content_type)

    if data is not None:
        handler.send_header('Content-Length', len(data))
    handler.end_headers()

    if data is not None:
        handler.wfile.write(str(data).encode())


def send_error(handler, code=404, title=None, message=None):
    """
    Sends a HTML error page
    
    :param handler: The request handler
    :param code: The response code
    :param title: The error title (page title and h1 title)
    :param message: Small message under the error title (in a paragraph)
    """
    send_response(handler, code, """<html>
<head>
<title>{title}</title>
</head>
<body>
<h1>Error {code}: {title}</h1>
<p>{message}</p>
</body>
</html>
""".format(code=code, title=title, message=message), "text/html")

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-forker-configuration-broker-factory")
@Instantiate("psem2m-forker-configuration-broker")
@Provides("org.psem2m.forker.configuration.store")
@Requires("_directory", "org.psem2m.signals.ISignalDirectory")
@Requires("_http", "HttpService")
@Property("_servlet_path", "servlet.path", "/configuration")
class ConfigBroker(object):
    """
    The configuration broker servlet
    """
    def __init__(self):
        """
        Constructor
        """
        self._http = None
        self._servlet_path = None

        # Configurations : Isolate ID -> JSON string
        self._configurations = {}

        # Configurations lock
        self.__config_lock = threading.Lock()


    def _extract_parameters(self, url):
        """
        Extracts the isolate ID contained in the given URL, if any
        """
        if not url or not url.startswith(self._servlet_path):
            # Invalid URL
            return

        # Remove the servlet prefix
        params = url[len(self._servlet_path):]
        if params[0] == '/':
            params = params[1:]

        # Split the parameters
        parts = params.split('/')
        if not parts or not parts[0]:
            # No parameters
            return

        # Return all found parts
        return parts


    def do_GET(self, handler):
        """
        Handles GET requests
        
        :param handler: The request handler
        """
        params = self._extract_parameters(handler.path)
        if not params:
            send_error(handler, 400, "Missing parameters", "No parameter given")
            return

        cmd = params[0]
        if cmd == "configuration":
            # Configuration
            if len(params) != 2:
                send_error(handler, 400, "Invalid request",
                           "The 'configuration' command needs an Isolate ID")
                return

            isolate_id = params[1]
            if isolate_id:
                with self.__config_lock:
                    json_config = self._configurations.get(isolate_id)

            else:
                json_config = None

            if not json_config:
                # Send a File not found error
                send_error(handler, 404, "Unknown Isolate",
                           "Unknown isolate {id}".format(id=isolate_id))

            else:
                # Send the configuration
                send_response(handler, 200, json_config, 'application/json')

        else:
            send_error(handler, 404, "Unknown command",
                       "Unknown command: {0}".format(cmd))


    def do_DELETE(self, handler):
        """
        Handles DELETE requests
        
        :param handler: The request handler
        """
        params = self._extract_parameters(handler.path)

        if not params or len(params) != 2:
            send_response(handler, 404,
                          '{"result":false, "message":"Isolate ID not found"}',
                          'application/json')

        elif params[0] != "configuration":
            send_response(handler, 404,
                          '{"result":false, "message":"Unknown command"}',
                          'application/json')

        else:
            isolate_id = params[1]
            if self.delete_configuration(isolate_id):
                send_response(handler, 200,
                            '{"result":true,"message":"Configuration deleted"}',
                            'application/json')

            else:
                send_response(handler, 404,
                              '{"result":false, "message":"Unknown isolate"}',
                              'application/json')


    def get_access_url(self):
        """
        Retrieves the URL to access this broker
        
        :return: A string URL to access this broker
        """
        return "http://{host}:{port}{path}".format(
                        host=self._http.get_hostname(),
                        port=self._http.get_port(),
                        path=self._servlet_path)



    def delete_configuration(self, isolate_id):
        """
        Deletes the configuration of the given isolate
        
        :param isolate_id: The isolate to forget
        :return: True if the isolate was known, else False
        """
        if not isolate_id:
            return False

        with self.__config_lock:
            if isolate_id in self._configurations:
                # Found !
                del self._configurations[isolate_id]
                return True

        return False


    def store_configuration(self, isolate_id, json_config):
        """
        Stores the configuration of the given isolate
        
        :param isolate_id: An isolate ID
        :param json_config: The JSON configuration (string) of the given isolate
        :return: False if a parameter is invalid, else True
        """
        if not isolate_id or not json_config:
            # Invalid parameters
            _logger.error("Invalid configuration for isolate %s", isolate_id)
            return False

        with self.__config_lock:
            self._configurations[isolate_id] = json_config

        return True


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._configurations.clear()
        self._http.register_servlet(self._servlet_path, self)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._http.unregister_servlet(self)

        with self.__config_lock:
            self._configurations.clear()
