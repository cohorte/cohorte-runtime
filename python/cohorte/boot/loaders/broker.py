#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Cohorte isolate loader, using a Configuration Broker, i.e. retrieving its
configuration using a HTTP request

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
    
    
Modifications:
    MOD_BD_20150916 retrieving Cohorte Data directory location from broker.
    
    
"""

# Standard library
import json
import logging

try:
    # Python 3
    # pylint: disable=F0401,E0611
    import http.client as httplib
    import urllib.parse as urlparse
except ImportError:
    # Python 2
    # pylint: disable=F0401
    import httplib
    import urlparse

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Provides, Property
from pelix.utilities import to_unicode

# COHORTE constants
import cohorte
import cohorte.boot.loaders.utils as utils

# Herald
import herald

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

ISOLATE_LOADER_FACTORY = 'cohorte-loader-broker-factory'
""" Configuration broker loader factory name """

LOADER_KIND = 'broker'
""" Kind of isolate started with this loader """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory(ISOLATE_LOADER_FACTORY)
@Provides(cohorte.SERVICE_ISOLATE_LOADER)
@Property('_handled_kind', cohorte.SVCPROP_ISOLATE_LOADER_KIND, LOADER_KIND)
@Requires('_config', cohorte.SERVICE_CONFIGURATION_READER)
class BrokerClientLoader(object):
    """
    Isolate loader component, retrieving configuration from a configuration
    broker. Automatically instantiated.
    """
    def __init__(self):
        """
        Sets up members
        """
        # Configuration reader
        self._config = None

        # The loader service reference
        self._loader_ref = None

        # Access to the state updater
        self._updater_access = None

        # Isolate UID
        self._uid = None

        # Bundle context
        self._context = None

        # Framework instance
        self._framework = None

    @staticmethod
    def _grab_broker_configuration(access):
        """
        Retrieves the isolate configuration from the configuration broker, if
        any

        :param access: A (host, HTTP port, servlet path) tuple
        :return: The raw content returned by the broker
        :raise ValueError: Invalid server address or path
        """
        # First request : retrieve the configuration
        try:
            conn = httplib.HTTPConnection(access[0], access[1])
        except httplib.HTTPException as ex:
            raise ValueError("Invalid server address: {0}".format(ex))

        try:
            # Get the configuration
            conn.request("GET", access[2])
            response = conn.getresponse()
            data = to_unicode(response.read())
            if response.status != 200:
                raise ValueError("Bad result from server: {0}"
                                 .format(response.status))

            # Delete it once retrieved
            conn.request("DELETE", access[2])

            # Be nice, even if we don't care of the result
            conn.getresponse().read()

            # Configuration has been correctly read
            return data
        except IOError as ex:
            raise ValueError("Error requesting the broker: {0}".format(ex))
        finally:
            # Close the connection in any case
            conn.close()

    @staticmethod
    def _parse_url(url):
        """
        Extracts information from the given URL and returns a tuple to make
        requests for the given address

        :param url: An URL
        :return: A (host, port, path) tuple to request the configuration
        :raise ValueError: Invalid URL
        """
        if not url:
            # No data
            raise ValueError("Empty URL")

        # Only HTTP is handled
        url = urlparse.urlparse(url)
        if url.scheme != "http":
            raise ValueError("Unknown configuration broker protocol: {0}"
                             .format(url.scheme))

        # The host name (default: localhost)
        if url.hostname:
            host = url.hostname
        else:
            host = "localhost"

        # The access port (default: 8080)
        if url.port:
            port = url.port
        else:
            # Default port
            port = 8080

        return host, port, url.path

    def prepare_state_updater(self, state_updater_url):
        """
        Prepares the access to the state updater

        :param state_updater_url: The URL to the state updater
        """
        # Get the UID if already present
        self._uid = self._context.get_property(cohorte.PROP_UID)

        try:
            # Parse the state updater URL
            self._updater_access = self._parse_url(state_updater_url)
        except ValueError as ex:
            # Ignore errors
            _logger.debug('Invalid status updater URL: %s', ex)
            self._updater_access = None

    def update_state(self, new_state, extra=None):
        """
        Sends the new state to the state updater

        :param new_state: The new state of the isolate
        :param extra: Extra information to send (must be JSON serializable)
        """
        if not self._updater_access:
            # No access, do nothing
            return

        if not self._uid:
            # No UID, updates will be ignored: do nothing
            return

        # First request : retrieve the configuration
        try:
            conn = httplib.HTTPConnection(self._updater_access[0],
                                          self._updater_access[1])
        except httplib.HTTPException as ex:
            raise ValueError("Invalid server address: {0}".format(ex))

        try:
            # Prepare the content
            data = {'uid': self._uid,
                    'state': new_state,
                    'extra': extra}

            # Send the new state
            conn.request("POST", self._updater_access[2],
                         body=json.dumps(data))
            response = conn.getresponse()
            data = response.read()
            if response.status != 200:

                try:
                    message = json.loads(data)['message']
                except (ValueError, KeyError, TypeError):
                    message = "Unknown error"

                _logger.warning("Error updating the isolate status: %s",
                                message)
        except IOError as ex:
            _logger.warning("Error updating the isolate status: %s", ex)
        finally:
            # Close the connection in any case
            conn.close()

    def load(self, configuration):
        """
        Grabs the configuration from the broker and uses the corresponding
        loader (instantiates it if needed).

        The *cohorte.configuration.broker* framework property must be set
        correctly before calling this method, or a KeyError exception will be
        raised.

        :raise KeyError: A mandatory property is missing
        :raise ValueError: Invalid parameter/file encountered
        :raise BundleException: Error installing a bundle
        :raise Exception: Unknown error raised while loading the isolate
        """
        # To simplify...
        context = self._context
        framework = self._framework

        # Get the broker URL
        broker_url = context.get_property(cohorte.PROP_CONFIG_BROKER)
        if not broker_url:
            raise KeyError("No configuration broker URL given")

        # Load it
        broker_access = self._parse_url(broker_url)
        raw_data = self._grab_broker_configuration(broker_access)

        # Parse it
        json_data = json.loads(raw_data)

        # Get the kind of this isolate
        kind = json_data['kind']

        # Store Cohorte isolate and Herald peer properties (same values)
        framework.add_property(cohorte.PROP_UID, json_data['uid'])
        framework.add_property(herald.FWPROP_PEER_UID, json_data['uid'])

        isolate_name = json_data['name']
        framework.add_property(cohorte.PROP_NAME, isolate_name)
        framework.add_property(herald.FWPROP_PEER_NAME, isolate_name)

        node_uid = json_data['node_uid']
        framework.add_property(cohorte.PROP_NODE_UID, node_uid)
        framework.add_property(herald.FWPROP_NODE_UID, node_uid)

        node_name = json_data['node_name']
        framework.add_property(cohorte.PROP_NODE_NAME, node_name)
        framework.add_property(herald.FWPROP_NODE_NAME, node_name)

        node_data_dir = json_data['node_data_dir']
        framework.add_property(cohorte.PROP_NODE_DATA_DIR, node_data_dir)        

        framework.add_property(cohorte.PROP_KIND, kind)
        framework.add_property(herald.FWPROP_PEER_GROUPS,
                               ('all', isolate_name, node_uid, node_name))

        # Get the boot configuration
        if 'boot' in json_data:
            # Use the one in the given configuration
            boot_config = self._config.load_boot_dict(json_data['boot'])
            _logger.debug("Using boot configuration from the broker")
        else:
            # By default, load the boot file for this kind
            boot_config = self._config.load_boot(kind)
            _logger.debug("Using boot configuration from the local files")

        # Let the utility method do its job
        utils.boot_load(context, boot_config)

        # Use a specific loader to continue
        kind_filter = "({0}={1})".format(cohorte.SVCPROP_ISOLATE_LOADER_KIND,
                                         kind)
        svc_ref = context.get_service_reference(cohorte.SERVICE_ISOLATE_LOADER,
                                                kind_filter)
        if svc_ref is None:
            raise ValueError("No isolate loader found for kind '{0}'"
                             .format(kind))

        try:
            # Call the loader
            loader = context.get_service(svc_ref)
            loader.load(json_data)

            # Keep the reference to the loader
            self._loader_ref = svc_ref
        finally:
            # Clean the service usage
            context.unget_service(svc_ref)

    def wait(self):
        """
        Waits for the isolate to stop

        :raise ValueError: No loader used in load()
        :raise BundleException: Error retrieving the loader
        """
        if self._loader_ref is None:
            raise ValueError("Invalid loader reference")

        try:
            # Re-Use the loader
            loader = self._context.get_service(self._loader_ref)
            loader.wait()
        finally:
            # Clean the service usage
            if self._context is not None:
                self._context.unget_service(self._loader_ref)

    @Validate
    def validate(self, context):
        """
        Component validated

        :param context: The bundle context
        """
        # Store the framework access
        self._context = context
        self._framework = context.get_bundle(0)
        self._loader_ref = None

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated

        :param context: The bundle context
        """
        self._uid = None
        self._updater_access = None

        # Clear the framework access
        self._context = None
        self._framework = None
        self._loader_ref = None
