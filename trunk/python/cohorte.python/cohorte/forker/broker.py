#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Forker configuration broker.

A simple servlet handling GET and DELETE commands to provide a raw JSON
configuration for the requested isolate, if available.

The stored configurations should be the ones given by a monitor requesting to
start an isolate.

A configuration should be deleted on a request by the isolate itself when it
read it correctly.

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE constants
import cohorte

# Pelix framework
from pelix.ipopo.decorators import ComponentFactory, Invalidate, Property, \
    Provides
import pelix.http

# Python standard library
import json
import logging
import threading

# ------------------------------------------------------------------------------

MIME_TYPE_JSON = 'application/json'
""" JSON data MIME type """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-forker-broker-factory")
@Provides(cohorte.SERVICE_CONFIGURATION_BROKER, controller='_svc_flag')
@Provides(pelix.http.HTTP_SERVLET)
@Property("_servlet_path", pelix.http.HTTP_SERVLET_PATH, "/cohorte/broker")
class ConfigBroker(object):
    """
    The configuration broker servlet
    """
    def __init__(self):
        """
        Sets up members
        """
        # The broker flag
        self._svc_flag = False

        # The path to this servlet
        self._servlet_path = None

        # Servlet access
        self._host = None
        self._port = None

        # Configurations : Isolate UID -> JSON string
        self._configurations = {}

        # Configurations lock
        self.__config_lock = threading.Lock()


    def bound_to(self, path, parameters):
        """
        Servlet bound to a HTTP service

        :param path: The path to access the servlet
        :param parameters: The server & servlet parameters
        """
        if path == self._servlet_path:
            # Update our access informations
            self._host = parameters['http.address']
            self._port = int(parameters['http.port'])

            # Register our service
            self._svc_flag = True

        else:
            _logger.warning("Bound to a HTTP service with a different path."
                            "Ignore.")


    def unbound_from(self, path, parameters):
        """
        Servlet unbound from a HTTP service

        :param path: The path to access the servlet
        :param parameters: The server & servlet parameters
        """
        if path == self._servlet_path:
            # Unregister our service
            self._svc_flag = False

            # Clear our access information
            self._host = None
            self._port = None


    def do_GET(self, request, response):
        """
        Handles GET requests

        :param request: The HTTP request bean
        :param request: The HTTP response handler
        """
        # Get the isolate UID (last part of the request path)
        uid = request.get_path().split('/')[-1]

        with self.__config_lock:
            # Get the associated configuration
            json_config = self._configurations.get(uid)

        if json_config:
            # Send the found configuration
            response.send_content(200, json_config, MIME_TYPE_JSON)

        else:
            # Unknown isolate
            error = {'uid': uid,
                     'result': False,
                     'message': "Unknown isolate UID"}

            response.send_content(404, json.dumps(error), MIME_TYPE_JSON)


    def do_DELETE(self, request, response):
        """
        Handles DELETE requests

        :param request: The HTTP request bean
        :param request: The HTTP response handler
        """
        # Get the isolate UID (last part of the request path)
        uid = request.get_path().split('/')[-1]

        code = 500
        result = {'uid': uid}

        if self.delete_configuration(uid):
            # Success
            code = 200
            result['result'] = True
            result['message'] = "Configuration deleted"

        else:
            # Error
            code = 404
            result['result'] = False
            result['message'] = "Unknown isolate UID"

        response.send_content(code, json.dumps(result), MIME_TYPE_JSON)


    def delete_configuration(self, uid):
        """
        Deletes the configuration of the given isolate

        :param uid: An isolate UID
        :return: True if the isolate was known, else False
        """
        with self.__config_lock:
            if uid in self._configurations:
                # Found !
                del self._configurations[uid]
                return True

        return False


    def store_configuration(self, uid, dict_config):
        """
        Stores the configuration of the given isolate

        :param uid: An isolate UID
        :param dict_config: The configuration dictionary of the given isolate
        :return: The URL to access this configuration
        :raise ValueError: Invalid parameter
        """
        if not uid or not dict_config:
            # Invalid parameters
            raise ValueError("Can't store an invalid configuration")

        with self.__config_lock:
            # Store the configuration as a JSON string
            self._configurations[uid] = json.dumps(dict_config)

        # Special IPv6 format
        if ':' in self._host:
            host = '[{0}]'.format(self._host)

        else:
            host = self._host

        return 'http://{host}:{port}{path}/{uid}'.format(uid=uid,
                                                     host=host, port=self._port,
                                                     path=self._servlet_path)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated

        :param context: The bundle context
        """
        # Reset the service flag
        self._svc_flag = False

        with self.__config_lock:
            self._configurations.clear()
