#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
The isolate loader for PSEM2M Python.

This module shall be installed in a Pelix instance after iPOPO and PSEM2M base
configuration.

:author: Thomas Calmant
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Instantiate, \
    Requires, Validate, Invalidate

import pelix.framework as pelix

from base.utils import to_unicode

# ------------------------------------------------------------------------------

import logging
_logger = logging.getLogger(__name__)

import json
import sys
import time

if sys.version_info[0] == 3:
    # Python 3
    import http.client as httplib
    import urllib.parse as urlparse

else:
    # Python 2
    import httplib
    import urlparse

# ------------------------------------------------------------------------------

ISOLATE_STATE_AGENT_DONE = 10
""" "Agent Done" status value """

ISOLATE_STATUS_CLASS = "org.psem2m.isolates.base.isolates.boot.IsolateStatus"
""" IsolateStatus Java class name """

ISOLATE_STATUS_SIGNAL = "/psem2m/isolate/status"
""" Isolate status PSEM2M signal name """

SPEC_SIGNAL_BROADCASTER = "org.psem2m.signals.ISignalBroadcaster"
""" Signal broadcaster service specification """

# ------------------------------------------------------------------------------

def _setup_broker_url(broker_url, isolate_id):
    """
    Extracts information from the given broker URL and returns a tuple to make
    requests for the given isolate
    
    :param broker_url: The URL to the broker servlet
    :param isolate_id: An isolate ID
    :return: A tuple : (host, port, path)
    """
    if not broker_url:
        return None

    # Only HTTP is handled
    url = urlparse.urlparse(broker_url)
    if url.scheme != "http":
        _logger.warn("Unknown configuration broker protocol: %s",
                     url.scheme)
        return None

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

    # The isolate configuration URL
    if url.path[-1] != '/':
        # Add a trailing slash if needed
        isolate_url = '{base}/{cmd}/{id}'
    else:
        isolate_url = '{base}{cmd}/{id}'

    return (host, port, isolate_url.format(base=url.path, cmd="configuration",
                                           id=isolate_id))

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-isolate-loader-factory")
@Instantiate("psem2m-isolate-loader")
@Requires("_config", "org.psem2m.isolates.services.conf.ISvcConfig")
class IsolateLoader(object):
    """
    Bundle loader for PSEM2M Python isolates
    """
    def __init__(self):
        """
        Constructor
        """
        self._config = None
        self.context = None

        # Installed bundles list
        self._bundles = []


    def _configure(self, isolate_descr):
        """
        Installs the bundles according to the given isolate description.
        
        The description must be an object compatible with
        base.config._IsolateDescription and inner bundles with
        base.config._BundleDescription. 
        
        If an error occurs during a bundle installation, and if the bundle is
        not optional, the isolate is automatically reset.
        
        :param isolate_descr: Description of the current isolate
        :return: True on success, else False
        """
        optionals = {}

        for bundle in isolate_descr.get_bundles():
            # Install the bundle
            try:
                bid = self.context.install_bundle(bundle.name)
                bnd = self.context.get_bundle(bid)

                # Store the installed bundle
                self._bundles.append(bnd)
                optionals[bnd] = bundle.optional

            except pelix.BundleException:
                _logger.exception("Error installing bundle %s",
                                  bundle.get_symbolic_name())

                if not bundle.optional:
                    # Reset isolate on error
                    self.reset()
                    return False

        # Special thing before starting bundle : set up the HTTP port property
        port = isolate_descr.get_port()
        framework = self.context.get_bundle(0)
        framework.add_property("http.port", port)
        framework.add_property("psem2m.isolate.id", isolate_descr.get_id())
        framework.add_property("psem2m.isolate.node", isolate_descr.get_node())

        _logger.debug("HTTP Port set to %d", port)

        # Start bundles
        for bnd in self._bundles:
            try:
                bnd.start()
                _logger.debug("Bundle %s started", bnd)

            except pelix.BundleException:
                _logger.exception("Error starting bundle %s",
                                  bnd.get_symbolic_name())

                if not optionals.get(bnd):
                    # Reset isolate on error
                    self.reset()
                    return False

        return True


    def get_broker_configuration(self, isolate_id):
        """
        Retrieves the isolate configuration from the configuration broker, if
        any
        
        :param isolate_id: The ID of the isolate to configure
        :return: The isolate description, or None
        """
        broker_url = self.context.get_property("psem2m.configuration.broker")
        broker = _setup_broker_url(broker_url, isolate_id)

        if not broker:
            # Unreadable URL
            _logger.debug("Unreadable URL : '%s'", broker_url)
            return None

        # Extract data
        host, port, isolate_url = broker[0:3]

        _logger.debug("Broker : %s", broker)

        # First request : retrieve the configuration
        try:
            conn = httplib.HTTPConnection(host, port)
        except:
            _logger.exception("Error connecting to the broker")
            return None

        try:
            # Get the configuration
            conn.request("GET", isolate_url)
            response = conn.getresponse()
            data = to_unicode(response.read())
            if response.status != 200:
                return None

            # Delete it once retrieved
            conn.request("DELETE", isolate_url)

            # Be nice, even if we don't care of the result
            conn.getresponse().read()

            try:
                isolate_descr = json.loads(data)

            except ValueError:
                _logger.exception("Invalid JSON string :\n%s\n", data)
                return None

        finally:
            # Close the connection in any case
            conn.close()

        # Configuration has been correctly read
        return isolate_descr


    def _find_configuration(self, isolate_id, is_forker=False):
        """
        Tries to find the isolate configuration
        """
        # Try using the broker
        isolate_conf = self.get_broker_configuration(isolate_id)
        if isolate_conf is not None:
            # Parse the configuration
            return self._config.parse_isolate(isolate_conf)

        # Get the isolate from the local configuration
        app = self._config.get_application()
        isolate_descr = app.get_isolate(isolate_id)

        if isolate_descr is None and is_forker:
            # Configuration not found, but we look for a forker
            for isolate in app.get_isolate_ids():
                if isolate.startswith("org.psem2m.internals.isolates.forker"):
                    # Forker configuration found
                    isolate_descr = app.get_isolate(isolate)
                    break

        return isolate_descr


    def send_agent_done(self):
        """
        Tries to send an IsolateStatus signal with the status **AGENT_DONE**.
        
        Simply prints a message if the signal couldn't be sent.
        
        :return: True if the signal was sent, else False
        """
        try:
            # Find the SignalSender service
            ref = self.context.get_service_reference(SPEC_SIGNAL_BROADCASTER)
            if ref is None:
                _logger.warning("No signal broadcaster available")
                return False

            # Get the service instance
            sender = self.context.get_service(ref)
            if sender is None:
                _logger.warning("Invalid signal broadcaster service")
                return False

        except pelix.BundleException as ex:
            _logger.exception("Error grabbing the signal broadcaster: %s", ex)
            return False

        # Prepare the bean
        isolate_id = self._config.get_current_isolate().get_id()
        status_bean = {
                       "javaClass": ISOLATE_STATUS_CLASS,
                       "isolateId": isolate_id,
                       "progress": float(100),
                       "state": ISOLATE_STATE_AGENT_DONE,
                       "statusUID": int(time.time() * 1000),
                       "timestamp": int(time.time() * 1000)
                       }

        # Send the isolate status
        sender.send(ISOLATE_STATUS_SIGNAL, status_bean, dir_group="ALL")

        try:
            # Release the service instance
            self.context.unget_service(ref)

        except pelix.BundleException as ex:
            _logger.exception("Error freeing the signal broadcaster: %s", ex)

        # Done !
        return True


    def setup_isolate(self):
        """
        Configures the current isolate according to the configuration for the
        isolate ID indicated in the framework property ``psem2m.isolate.id``.
        
        :return: True on success, False on error
        """
        is_forker = self.context.get_property("psem2m.forker")
        isolate_id = self.context.get_property("psem2m.isolate.id")
        if isolate_id is None and not is_forker:
            # No isolate ID found and not a forker: do nothing
            return False

        isolate_descr = self._find_configuration(isolate_id, is_forker)
        if isolate_descr is None:
            # No description for this isolate
            _logger.info("No configuration found for '%s'", isolate_id)
            return False

        if not isolate_descr.get_node():
            _logger.warning("No node given for isolate '%s'", isolate_id)
            return False

        # Set the configuration that will be used 
        self._config.set_current_isolate(isolate_descr)

        # Reset isolate
        self.reset()

        # Install required bundles
        return self._configure(isolate_descr)


    def reset(self):
        """
        Uninstalls all bundles installed by this component.
        
        Ignores bundles exceptions.
        """
        for bundle in self._bundles:
            try:
                # Uninstall each bundle
                bundle.uninstall()

            except pelix.BundleException:
                # Only log errors
                _logger.exception("Error uninstalling %s",
                                  bundle.get_symbolic_name())

        # Clean up the list
        del self._bundles[:]


    @Validate
    def validate(self, context):
        """
        Component validation
        
        :param context: The bundle context
        """
        self.context = context

        if not self.setup_isolate():
            # An error occurred, stop the framework
            _logger.error("An error occurred starting the bundle: Abandon.")

            # FIXME: Pelix should stop there
            raise pelix.FrameworkException("Loader had to stop the framework",
                                           True)

        else:
            # Send the "agent done" isolate status signal
            self.send_agent_done()


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidation
        
        :param context: The bundle context
        """
        # Uninstall bundles
        self.reset()
        self.context = None
