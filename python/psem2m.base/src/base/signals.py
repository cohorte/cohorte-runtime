#-- Content-Encoding: UTF-8 --
"""
Created on 29 févr. 2012

@author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

from psem2m.component.decorators import ComponentFactory, Provides, Requires, \
    Validate, Invalidate, Instantiate

from base.javautils import to_jabsorb, from_jabsorb, JAVA_CLASS

import psem2m.services.pelix as pelix
import psem2m.utilities as utilities

# ------------------------------------------------------------------------------

import fnmatch
import logging
import json
import os
import socket
import time
import threading
import sys

if sys.version_info >= (3, 0):
    # Python 3
    import http.client as httplib

    def _to_string(data, encoding="UTF-8"):
        """
        Converts the given bytes array to a string
        """
        if type(data) is str:
            # Nothing to do
            return data

        return str(data, encoding)

else:
    # Python 2
    import httplib

    def _to_string(data, encoding="UTF-8"):
        """
        Converts the given bytes array to a string
        """
        return data.encode(encoding)

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

SPECIAL_INTERNAL_ISOLATES_PREFIX = "org.psem2m.internals.isolates."
SPECIAL_ISOLATE_ID_FORKER = "%s%s" \
                                % (SPECIAL_INTERNAL_ISOLATES_PREFIX, "forker")

# ------------------------------------------------------------------------------

@ComponentFactory("IsolateDirectoryFactory")
@Instantiate("SignalDirectory")
@Provides("org.psem2m.IsolateDirectory")
@Requires("config", "org.psem2m.isolates.services.conf.ISvcConfig")
class IsolateDirectory(object):
    """
    Isolate directory
    """
    def __init__(self):
        """
        Constructor
        """
        self.config = None


    def is_valid_isolate(self, isolate_id):
        """
        Tests if the given isolate ID can be used in a "getAllXXX" method.
        Returns false if the isolate ID is the current one or the forker one.

        :param isolate_id: The isolate ID
        :return: True if the isolate ID can be used"
        """
        if not isolate_id:
            return None

        return isolate_id != SPECIAL_ISOLATE_ID_FORKER \
            and isolate_id != self.get_current_isolate_id()


    def _get_all_isolates(self):
        """
        Retrieves the list of all non-internal isolates, testing whether the ID
        starts with SPECIAL_ISOLATE_ID_FORKER or not.
        
        :return: All non-internal isolates ID (never None)
        """
        return set([isolate_id
            for isolate_id
            in self.config.get_application().get_isolate_ids()
            if self.is_valid_isolate(isolate_id) \
            and not isolate_id.startswith(SPECIAL_INTERNAL_ISOLATES_PREFIX)])


    def _get_all_monitors(self):
        """
        Retrieves the list of all internal isolates, except the forker, testing
        whether the ID starts with SPECIAL_ISOLATE_ID_FORKER or not.
        """
        return set((isolate_id
                for isolate_id
                in self.config.get_application().get_isolate_ids()
                if self.is_valid_isolate(isolate_id) \
                and isolate_id.startswith(SPECIAL_INTERNAL_ISOLATES_PREFIX)))



    def get_current_isolate_id(self):
        """
        Retrieves the host isolate ID
        
        :return: The current isolate ID
        """
        return os.getenv("PSEM2M_ISOLATE_ID", "<unknown>")


    def get_isolate(self, isolate_id):
        """
        Retrieves the (host, port) access tuple to the given isolate. Returns
        None if the isolate is unknown. Access to the current isolate and the
        forker can be returned by this method.
        
        :param isolate_id: An isolate ID
        :return: The (host, port) tuple to access the isolate, or None
        """
        isolate = self.config.get_application().get_isolate(isolate_id)
        if isolate is not None:
            return isolate.get_access()

        return None


    def get_isolates(self, isolates_ids):
        """
        Retrieves the access string of each of the given isolates.
        The parameter can be an array of isolate IDs or one of :
        
        * "*" or "ALL" : target monitors and isolates
        * "MONITORS" : target monitors only
        * "ISOLATES" : target isolates only
        
        Unknown isolates are ignored.
        Returns None if all given isolates are unknown.
        
        Current isolate and the forker (in PSEM2M) access URLs **must not**
        be returned by this method.

        :param isolates_ids: An array of isolate IDs or a target name
        :return: Access strings to the known isolates, null if none is known.
        """
        if not isolates_ids:
            # Don't work for nothing
            return None

        if utilities.is_string(isolates_ids):
            # Targets
            if isolates_ids == "*" or isolates_ids == "ALL":
                # Retrieve all isolates at once
                return self.get_isolates(set((isolate for isolate
                            in self.config.get_application().get_isolate_ids()
                            if self.is_valid_isolate(isolate))))

            elif isolates_ids == "MONITORS":
                # Retrieve all monitors
                return self.get_isolates(self._get_all_monitors())

            elif isolates_ids == "ISOLATES":
                # Retrieve all non-internal isolates
                return self.get_isolates(self._get_all_isolates())

            # Isolate
            url = self.get_isolate(isolates_ids)
            if url is not None:
                return [url]

            return None

        else:
            result = []

            for isolate in isolates_ids:
                # Standard work
                if self.is_valid_isolate(isolate):
                    url = self.get_isolate(isolate)
                    if url is not None:
                        result.append(url)

            if len(result) == 0:
                return None

            return result


    @Validate
    def validate(self, context):
        """
        Component validation
        """
        # Be sure that the configuration is fresh enough
        self.config.refresh()


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidation
        """
        self.config = None


# ------------------------------------------------------------------------------

def read_post_body(request_handler):
    """
    Reads the body of a POST request, returns an empty string on error
    """
    try :
        content_len = int(request_handler.headers.get('content-length'))
        return _to_string(request_handler.rfile.read(content_len))

    except:
        _logger.exception("Error reading POST body")

    return ""

@ComponentFactory("SignalReceiverFactory")
@Instantiate("SignalReceiver")
@Provides("org.psem2m.SignalReceiver")
@Requires("http", "HttpService")
class SignalReceiver(object):
    """
    PSEM2M Signals receiver
    """
    SERVLET_PATH = "/psem2m-signal-receiver/"

    def __init__(self):
        """
        Constructor
        """
        self.http = None
        self._listeners = {}


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._listeners.clear()
        self.http.register_servlet(SignalReceiver.SERVLET_PATH, self)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.http.unregister_servlet(self)
        self._listeners.clear()


    def do_POST(self, handler):
        """
        Called when a POST request has been received
        """
        # Read the request
        signal_name = handler.path[len(SignalReceiver.SERVLET_PATH) - 1:]

        content_type = handler.headers.get('content-type')
        if content_type not in (None, 'application/json',
                                'application/x-www-form-urlencoded'):
            handler.send_response(500)

        else:
            # Handle it
            try:
                signal_data = from_jabsorb(json.loads(read_post_body(handler)))
                self.handle_received_signal(signal_name, signal_data)

            except:
                # Error
                _logger.exception("Error reading signal %s", signal_name)
                handler.send_response(500)

            else:
                # OK
                handler.send_response(200)

        # Send headers
        handler.send_header('Content-type', 'application/x-www-form-urlencoded')
        handler.send_header('Content-length', '0')
        handler.end_headers()


    def handle_received_signal(self, name, data):
        """
        Handles a received signal
        """
        for pattern in self._listeners:
            if fnmatch.fnmatch(name, pattern):
                # Signal name matches the pattern
                listeners = self._listeners[pattern]
                for listener in listeners:
                    try:
                        # Notify the listener
                        listener.handle_received_signal(name, data)

                    except:
                        _logger.exception("Error notifying a signal listener")


    def register_listener(self, signal_pattern, listener):
        """
        Registers a signal listener
        """
        if not signal_pattern or listener is None:
            # Invalid listener
            return

        listeners = self._listeners.get(signal_pattern, None)
        if listeners is None:
            listeners = []
            self._listeners[signal_pattern] = listeners

        listeners.append(listener)


    def unregister_listener(self, signal_pattern, listener):
        """
        Unregisters a listener
        """
        if not signal_pattern or listener is None:
            # Invalid listener
            return

        listeners = self._listeners.get(signal_pattern, None)
        if listeners is not None:
            try:
                listeners.remove(listener)

            except ValueError:
                # Listener not found
                pass


# ------------------------------------------------------------------------------

@ComponentFactory("SignalSenderFactory")
@Instantiate("SignalSender")
@Provides("org.psem2m.SignalSender")
@Requires("directory", "org.psem2m.IsolateDirectory")
@Requires("local_recv", "org.psem2m.SignalReceiver")
class SignalSender(object):
    """
    PSEM2M Signals sender implementation
    """
    def __init__(self):
        """
        Constructor
        """
        self.directory = None


    def _internal_send(self, urls, name, data):
        """
        Sends the data
        """
        signal = {
            JAVA_CLASS: "org.psem2m.remotes.signals.http.HttpSignalData",
            "isolateSender": self.directory.get_current_isolate_id(),
            "senderHostName": "localhost",
            "signalContent": data,
            "timestamp": int(time.time() * 1000)
            }


        # Make a JSON form of a Jabsorb signal content
        json_signal = json.dumps(to_jabsorb(signal))

        # Start a new thread to send the signal
        thread = threading.Thread(target=self.__threaded_send,
                                  args=(urls, name, signal, json_signal))
        thread.start()


    def __threaded_send(self, urls, name, signal, json_signal):
        """
        Sends the data, in a separate thread
        """
        headers = {"Content-Type": "application/json"}

        for access in urls:

            if not access:
                continue

            host, port = access

            try:
                if host == "{local}":
                    _logger.debug("Local signal: %s", name)
                    self.local_recv.handle_received_signal(name, signal)

                else:
                    # 1 second timeout, to avoid useless waits
                    conn = httplib.HTTPConnection(host, port, timeout=1)

                    if name[0] == '/':
                        name = name[1:]

                    signal_url = "%s%s" % (SignalReceiver.SERVLET_PATH, name)

                    conn.request("POST", signal_url, json_signal, headers)
                    response = conn.getresponse()
                    if response.status != 200:
                        _logger.warn("Incorrect response for %s : %s %s",
                                     signal_url, response.status,
                                     response.reason)

            except socket.error as ex:
                # Socket error
                _logger.error("Error sending signal %s to %s : %s", name,
                              access, str(ex))

            except:
                # Other error...
                _logger.exception("Error sending signal %s to %s", name, access)


    def get_current_isolate_id(self):
        """
        Retrieves the current isolate ID
        """
        return self.directory.get_current_isolate_id()


    def send_data(self, target, name, data):
        """
        Sends a signal to the given target
        
        @param target: Target isolate
        @param name: Signal name
        @param data: Signal content
        """
        if target is None:
            _logger.warn("No target given")
            return

        urls = self.directory.get_isolates(target)
        if urls is None:
            _logger.warn("Unknown target(s) - '%s'", target)
            return

        self._internal_send(urls, name, data)


@ComponentFactory("SlaveAgentFactory")
@Instantiate("SlaveAgent")
@Requires("_receiver", "org.psem2m.SignalReceiver")
class MiniSlaveAgent(object):
    """
    Mini slave agent implementation : stops the framework when the stop signal
    has been received
    """
    ISOLATE_STOP_SIGNAL = "/psem2m/isolate/stop"

    def __init__(self):
        """
        Constructor
        """
        self._receiver = None
        self.context = None


    def handle_received_signal(self, name, data):
        """
        Handles a received signal
        """
        try:
            self.context.get_bundle(0).stop()
        except pelix.BundleException:
            _logger.exception("Error stopping the framework")


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        self._receiver.register_listener(MiniSlaveAgent.ISOLATE_STOP_SIGNAL,
                                         self)
        self.context = context


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        self._receiver.unregister_listener(MiniSlaveAgent.ISOLATE_STOP_SIGNAL,
                                           self)
        self.context = None
