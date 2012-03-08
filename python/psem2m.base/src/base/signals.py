#-- Content-Encoding: UTF-8 --
"""
Created on 29 févr. 2012

@author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import fnmatch
import logging
import json
import os
import time
import threading
_logger = logging.getLogger(__name__)

try:
    # Python 3
    import httplib.client as httplib

except ImportError:
    # Python 2
    import httplib

# ------------------------------------------------------------------------------

from psem2m.component.decorators import ComponentFactory, Provides, Requires, \
    Validate, Invalidate, Instantiate

from base.javautils import to_jabsorb, from_jabsorb, JAVA_CLASS

# ------------------------------------------------------------------------------

SPECIAL_INTERNAL_ISOLATES_PREFIX = "org.psem2m.internals.isolates."
SPECIAL_ISOLATE_ID_FORKER = "%s%s" \
                                % (SPECIAL_INTERNAL_ISOLATES_PREFIX, "forker")


@ComponentFactory("IsolateDirectoryFactory")
@Instantiate("SignalDirectory")
@Provides("org.psem2m.IsolateDirectory")
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
        
        :return: All non-internal isolates ID (never null)
        """
        return [isolate_id
                for isolate_id in self.config.keys()
                if self.is_valid_isolate(isolate_id) \
                and not isolate_id.startswith(SPECIAL_INTERNAL_ISOLATES_PREFIX)]


    def _get_all_monitors(self):
        """
        Retrieves the list of all internal isolates, except the forker, testing
        whether the ID starts with SPECIAL_ISOLATE_ID_FORKER or not.
        """
        return [isolate_id
                for isolate_id in self.config.keys()
                if self.is_valid_isolate(isolate_id) \
                and isolate_id.startswith(SPECIAL_INTERNAL_ISOLATES_PREFIX)]



    def get_current_isolate_id(self):
        """
        Retrieves the host isolate ID
        
        :return: The current isolate ID
        """
        return os.getenv("PSEM2M_ISOLATE_ID", "<unknown>")


    def get_isolate(self, isolate_id):
        """
        Retrieves the access string to the given isolate. Returns null if the
        isolate is unknown. Access to the current isolate and the forker can be
        returned by this method.
        
        :param isolate_id: An isolate ID
        :return: The access string to the isolate, or null.
        """
        return self.config.get(isolate_id, None)


    def get_isolates(self, isolates_ids):
        """
        Retrieves the access string of each of the given isolates. Unknown
        isolates are ignored. Returns null if all given isolates are unknown.
        Current isolate and the forker (in PSEM2M) access URLs **must not**
        be returned by this method.

        :param isolates_ids: An array of isolate IDs
        :return: Access strings to the known isolates, null if none is known.
        """
        result = []

        for isolate in isolates_ids:
            if isolate == "*" or isolate == "ALL":
                # Retrieve all isolates at once
                return self.get_isolates(set(self._get_all_isolates())\
                                         .union(self._get_all_monitors()))

            elif isolate == "MONITORS":
                # Retrieve all monitors
                return self.get_isolates(set(self._get_all_monitors()))

            elif isolate == "ISOLATES":
                # Retrieve all non-internal isolates
                return self.get_isolates(set(self._get_all_isolates()))

            else:
                # Standard work
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
        self.config = {
           "isolate-wrapper": "127.0.0.1:10010",
           "isolate-listener": "127.0.0.1:10000",
           "org.psem2m.internals.isolates.monitor-1": "127.0.0.1:9000",
           "org.psem2m.internals.isolates.forker": "127.0.0.1:9001",
                       }

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidation
        """
        self.config.clear()
        self.config = None


# ------------------------------------------------------------------------------

def read_post_body(request_handler):
    """
    Reads the body of a POST request, returns an empty string on error
    """
    try :
        content_len = int(request_handler.headers.getheader('content-length'))
        return request_handler.rfile.read(content_len)

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

        content_type = handler.headers.getheader('content-type')
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
        Handle a received signal
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

        for url in urls:
            try:

                if url == "{local}":
                    self.local_recv.handle_received_signal(name, signal)

                else:
                    # 1 second timeout, to avoid useless waits
                    conn = httplib.HTTPConnection(url, timeout=1)

                    if name[0] == '/':
                        name = name[1:]

                    signal_url = "%s%s" % (SignalReceiver.SERVLET_PATH, name)
                    conn.request("POST", signal_url, json_signal, headers)
                    response = conn.getresponse()
                    if response.status != 200:
                        _logger.warn("Incorrect response for %s : %s %s",
                                     signal_url, response.status,
                                     response.reason)

            except:
                _logger.exception("Error sending signal %s to %s", name, url)


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

        if target == "monitors":
            target = "org.psem2m.internals.isolates.monitor-1"

        elif target == "forker":
            target = "org.psem2m.internals.isolates.forker"

        if not isinstance(target, list):
            target = [target]

        urls = self.directory.get_isolates(target)
        if urls is None:
            _logger.warn("Unknown target(s)")
            return

        self._internal_send(urls, name, data)
