#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Updates the isolate state directory

:author: Thomas Calmant
:license: Apache Software License 2.0

..

    Copyright 2014 isandlaTech

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

# Python standard library
import json
import logging

# Pelix framework
from pelix.ipopo.decorators import ComponentFactory, Requires, Property, \
    Provides, Invalidate, Validate
from pelix.utilities import to_str
import pelix.http

# Cohorte boot constants
import cohorte.boot.constants as constants

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory("cohorte-forker-state-updater-factory")
@Provides(pelix.http.HTTP_SERVLET)
@Provides('cohorte.forker.state.updater', controller='_svc_flag')
@Property("_servlet_path", pelix.http.HTTP_SERVLET_PATH,
          "/cohorte/state-updater")
@Requires('_state_dir', 'cohorte.forker.state')
class StateUpdater(object):
    """
    The state updater servlet
    """
    def __init__(self):
        """
        Sets up members
        """
        # The service controller
        self._svc_flag = False

        # The path to this servlet
        self._servlet_path = None

        # Servlet access
        self._host = None
        self._port = None

        # The forker isolate state directory
        self._state_dir = None

    def bound_to(self, path, parameters):
        """
        Servlet bound to a HTTP service

        :param path: The path to access the servlet
        :param parameters: The server & servlet parameters
        """
        if path == self._servlet_path:
            # Update our access information
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

    def do_POST(self, request, response):
        """
        Handles a POST request

        :param request: The HTTP request bean
        :param request: The HTTP response handler
        """
        # Parse the request content
        data = json.loads(to_str(request.read_data()))

        # Extract information
        uid = data['uid']
        state = data['state']
        extra = data.get('extra')

        # The response dictionary
        code = 500
        result = {'uid': uid}

        # Change the state
        try:
            self._state_dir.change_state(uid, state)
            code = 200
            result['success'] = True
        except (KeyError, ValueError) as ex:
            code = 500
            result['success'] = False
            result['message'] = str(ex)

        if state == constants.STATE_FAILED:
            # The isolate failed: print an error
            _logger.warning("Isolate '%s' sent a failure status:\n%s",
                            uid, extra)

        # Send the JSON response
        response.send_content(code, json.dumps(result), 'application/json')

    def get_url(self):
        """
        Retrieves the URL to access this servlet

        :return: The URL to access this servlet
        """
        if not self._svc_flag:
            # Service is disabled
            return None

        if not self._host or not self._port:
            # Invalid URL
            return None

        # Send a "localhost" address to avoid an "address not available" error
        # under Windows
        if ':' in self._host:
            # IPv6 host
            host = '[::1]'
        else:
            host = '127.0.0.1'

        return 'http://{host}:{port}{path}'.format(host=host, port=self._port,
                                                   path=self._servlet_path)

    @Validate
    def validate(self, context):
        """
        Component invalidated

        :param context: The bundle context
        """
        _logger.debug("State updater validated")

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated

        :param context: The bundle context
        """
        _logger.debug("State updater invalidated")

        # Reset the service flag
        self._svc_flag = False
