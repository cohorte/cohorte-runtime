#-- Content-Encoding: UTF-8 --
"""
Created on 29 févr. 2012

@author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import fnmatch
import logging
import json
import time
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

# ------------------------------------------------------------------------------

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


    def get_current_isolate_id(self):
        return "isolate-python"


    def get_isolate(self, isolate_id):
        return self.config.get(isolate_id, None)


    def get_isolates(self, isolates_ids):
        result = []

        for isolate in isolates_ids:
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
           self.get_current_isolate_id(): "{local}",
           "org.psem2m.internals.isolates.monitor-1": "localhost:9000",
           "org.psem2m.internals.isolates.forker": "localhost:9001",
           "isolate-dataserver": "localhost:9210",
           "isolate-cache": "localhost:9211",
           "isolate-erpproxy": "localhost:9212",
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
            handler.end_headers()
            return

        # Handle it
        try:
            signal_data = json.loads(read_post_body(handler))
            self.handle_received_signal(signal_name, signal_data)

        except:
            # Error
            _logger.error("Error reading signal %s", signal_name)
            handler.send_response(500)

        else:
            # OK
            handler.send_response(200)

        # Send headers
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
            "javaClass": "org.psem2m.remotes.signals.http.HttpSignalData",
            "isolateSender": self.directory.get_current_isolate_id(),
            "senderHostName": "localhost",
            "signalContent": data,
            "timestamp": int(time.time() * 1000)
            }

        json_signal = json.dumps(signal)
        headers = {"Content-Type": "application/json"}

        _logger.warning("OUTPUT :\n%s\n\n", json_signal)

        for url in urls:
            try:

                if url == "{local}":
                    self.local_recv.handle_signal(signal)

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
                _logger.exception("Error sending signal")


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
