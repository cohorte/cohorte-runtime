#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Created on 11 juin 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Provides, Requires, \
    Validate, Invalidate, Instantiate

from base.javautils import to_jabsorb, from_jabsorb, JAVA_CLASS

import pelix.framework as pelix

# ------------------------------------------------------------------------------

import fnmatch
import logging
import json
import socket
import time
import threading
import sys

if sys.version_info[0] == 3:
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

MODE_ACK = "ack"
MODE_FORGET = "forget"
MODE_SEND = "send"

DEFAULT_MODE = MODE_SEND

# ------------------------------------------------------------------------------

def _make_json_result(code, message):
    """
    An utility method to prepare a JSON result string, usable by the
    SignalReceiver
    
    :param code: A HTTP Code
    :param message: An associated message
    """
    return code, json.dumps({'code': code, 'message': message})


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
        self._listeners_lock = threading.RLock()


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        with self._listeners_lock:
            self._listeners.clear()

        self.http.register_servlet(SignalReceiver.SERVLET_PATH, self)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.http.unregister_servlet(self)

        with self._listeners_lock:
            self._listeners.clear()


    def do_POST(self, handler):
        """
        Called when a POST request has been received
        """
        # Read the request
        signal_name = handler.path[len(SignalReceiver.SERVLET_PATH) - 1:]

        # Default code and content
        code = 501
        content = ""

        content_type = handler.headers.get('content-type')
        if content_type not in (None, 'application/json',
                                'application/x-www-form-urlencoded'):
            # Unknown content type
            code, content = _make_json_result(500, "Unknown content type")

        else:
            # Get the signal mode, or the default one
            mode = handler.headers.get('psem2m-mode', DEFAULT_MODE)

            try:
                # Decode the JSON content
                signal_data = from_jabsorb(json.loads(read_post_body(handler)))

                # Handle the signal
                code, content = self.handle_received_signal(signal_name,
                                                            signal_data,
                                                            mode)
            except:
                # Error
                _logger.exception("Error reading signal %s", signal_name)
                code, content = _make_json_result(500, "Error parsing signal")

        # Convert content (Python 3)
        content = _to_string(content)

        if code != 200:
            # Something wrong occurred
            _logger.debug("Signal: %s - Result: %s", signal_name, content)

        # Send headers
        handler.send_response(code)
        handler.send_header('Content-type', 'application/json')
        handler.send_header('Content-length', len(content))
        handler.end_headers()

        if content:
            # Write result
            handler.wfile.write(content)


    def get_access_info(self):
        """
        Retrieves the (host, port) tuple to access this signal receiver.
        
        WARNING: The host might often be "localhost"
        
        :return: An (host, port) tuple
        """
        return (self.http.get_hostname(), self.http.get_port())


    def handle_received_signal(self, name, data, mode):
        """
        Handles a received signal
        
        :param name: Signal name
        :param data: Complete signal data (meta-data + content)
        :param mode: Request mode
        :return: A tuple : (HTTP code, JSON response content)
        """
        if mode == MODE_SEND:
            # Standard mode
            result = to_jabsorb(self._notify_listeners(name, data))
            return 200, json.dumps(result)

        elif mode == MODE_FORGET:
            # Signal v1 mode
            # Fire and forget mode : the client doesn't want a result
            # -> Start a thread to notify listener and return immediately
            threading.Thread(target=self._notify_listeners,
                             args=(name, data)).start()

            # Success, no result
            return _make_json_result(200, "Signal thread started")

        elif mode == MODE_ACK:
            with self._listeners_lock:
                # Test if at least listener will be notified
                for pattern in self._listeners:
                    if fnmatch.fnmatch(name, pattern):
                        # Found one !
                        result = _make_json_result(200,
                                                 "At least one listener found")
                        break
                else:
                    # No match found
                    result = _make_json_result(404, "No listener found")

            # Start the treatment in another thread
            threading.Thread(target=self._notify_listeners,
                             args=(name, data)).start()

            return result

        # Unknown mode (not implemented error)
        return _make_json_result(501, "Unknown mode %s" % mode)


    def _notify_listeners(self, name, data):
        """
        Notifies all signal listeners that matches the given signal name
        
        :param name: A signal name
        :param data: Complete signal data (meta-data + content)
        :return: The result of all 
        """
        results = []

        with self._listeners_lock:
            # Still use a copy of the listeners, as one may unregister itself
            for pattern in self._listeners.copy():
                if fnmatch.fnmatch(name, pattern):
                    # Signal name matches the pattern
                    listeners = self._listeners[pattern][:]
                    for listener in listeners:
                        try:
                            # Notify the listener
                            result = listener.handle_received_signal(name, data)
                            if result:
                                # Store the result
                                results.append(result)

                        except:
                            _logger.exception("Error notifying a listener")

        return results


    def register_listener(self, signal_pattern, listener):
        """
        Registers a signal listener
        """
        if not signal_pattern or listener is None:
            # Invalid listener
            return

        with self._listeners_lock:
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

        with self._listeners_lock:
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
@Requires("directories", "org.psem2m.IsolateDirectory", aggregate=True)
@Requires("local_recv", "org.psem2m.SignalReceiver")
class SignalSender(object):
    """
    PSEM2M Signals sender implementation
    """
    def __init__(self):
        """
        Constructor
        """
        self.directories = []


    def _internal_send(self, urls, name, data):
        """
        Sends the data
        """
        signal = {
            JAVA_CLASS: "org.psem2m.remotes.signals.http.HttpSignalData",
            "isolateSender": self.directories[0].get_current_isolate_id(),
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
        return self.directories[0].get_current_isolate_id()


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

        all_urls = []

        for directory in self.directories:
            urls = directory.get_isolates(target)
            if urls is not None:
                all_urls.extend(urls)

        if len(all_urls) > 0:
            # Remove duplicates
            all_urls = set(all_urls)
            self._internal_send(all_urls, name, data)

        else:
            _logger.warn("Unknown target(s) - '%s'", target)

# ------------------------------------------------------------------------------

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
