#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Python implementation of the PSEM2M Signals based on the HTTP protocol

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

MODE_ACK = "ack"
MODE_FORGET = "forget"
MODE_SEND = "send"

DEFAULT_MODE = MODE_SEND
HEADER_SIGNAL_MODE = "psem2m-mode"

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

@ComponentFactory("psem2m-signals-receiver-factory")
@Instantiate("psem2m-signals-receiver")
@Provides("org.psem2m.signals.ISignalReceiver")
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

        # Java API compliance
        self.getAccessInfo = self.get_access_info
        self.registerListener = self.register_listener
        self.unregisterListener = self.unregister_listener


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
            mode = handler.headers.get(HEADER_SIGNAL_MODE, DEFAULT_MODE)

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


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.http.unregister_servlet(self)

        with self._listeners_lock:
            self._listeners.clear()


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


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        with self._listeners_lock:
            self._listeners.clear()

        self.http.register_servlet(SignalReceiver.SERVLET_PATH, self)


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

        return _make_json_result(200, results)

# ------------------------------------------------------------------------------

class FutureResult(object):
    """
    An object to wait for the result of a threaded execution
    """
    def __init__(self, exception_handler=None):
        """
        Sets up the FutureResult object
        
        The given exception handler must be a callable accepting one argument,
        the raised exception. It will be called if the job execution raises an
        error.
        
        :param exception_handler: An exception handling method
        """
        self._event = threading.Event()
        self._handler = exception_handler
        self._result = None


    def done(self):
        """
        Returns True if the job has finished, else False
        """
        return self._event.is_set()


    def execute(self, method, *args, **kwargs):
        """
        Executes the given method in a new thread
        
        :param method: The method to execute
        :param args: The arguments of the method to execute
        """
        if self.done():
            # Reset if necessary
            self.reset()

        # Start the job thread
        threading.Thread(target=self.__internal_execute,
                         args=args, kwargs=kwargs).start()


    def reset(self):
        """
        Resets the object for re-use
        """
        self._result = None
        self._event.clear()


    def result(self, timeout=None):
        """
        Waits up to timeout for the result the threaded job.
        
        :param timeout: The maximum time to wait for a result (in seconds)
        :raise OSError: The timeout raised before the job finished
        """
        if self._event.wait(timeout):
            return self._result

        raise OSError("Timeout raised")


    def __internal_execute(self, method, *args, **kwargs):
        """
        Executes the given method
        
        :param method: A callable object 
        """
        try:
            # Do the job...
            self._result = method(*args, **kwargs)

        except BaseException as ex:
            # Call an exception handler, if any
            if self._handler:
                self._handler(ex)

            else:
                _logger.exception("Error executing a threaded job")

        # Result stored or exception handled, we are ready
        self._event.set()

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-signals-sender-factory")
@Instantiate("psem2m-signals-sender")
@Provides("org.psem2m.signals.ISignalBroadcaster")
@Requires("_directories", "org.psem2m.signals.ISignalDirectory", aggregate=True)
@Requires("_local_recv", "org.psem2m.signals.ISignalReceiver")
class SignalSender(object):
    """
    PSEM2M Signals sender implementation
    """
    def __init__(self):
        """
        Constructor
        """
        self._directories = []
        self._local_recv = None

        # Bundle context
        self._context = None

        # The sending thread pool
        self._send_pool = None

        # Java API compatibility
        self.getCurrentIsolateId = self.get_current_isolate_id
        self.sendTo = self.send_to
        self.fireGroup = lambda s, c, g : self.fire(s, c, groups=g)
        self.postGroup = lambda s, c, g : self.post(s, c, groups=g)
        self.sendGroup = lambda s, c, g : self.send(s, c, groups=g)


    def fire(self, signal, content, isolate=None, isolates=None, groups=None):
        """
        Sends a signal to the given target, without waiting for the result.
        Returns the list of successfully reached isolates, which may not have
        a listener for this signal.
        
        :param signal: The signal name
        :param content: The signal content
        :param isolate: The ID of the target isolate
        :param isolates: A list of isolate IDs
        :param groups: A list of isolates groups names
        :return: The list of reached isolates, None if there is no isolate to
                 send the signal to.
        """
        # Send the signal
        result = self.__common_handling(signal, content, isolate, isolates,
                                        groups, MODE_FORGET)

        if result is None:
            # Unknown targets
            return None

        # Only return reached isolates
        return result[0].keys()


    def get_current_isolate_id(self):
        """
        Retrieves the current isolate ID
        """
        return self.context.get_property("psem2m.isolate.id")


    def post(self, signal, content, isolate=None, isolates=None, groups=None):
        """
        Sends a signal to the given target in a different thread.
        See send(signal, content, isolate, isolates, groups) for more details.
        
        The result is a future object, allowing to wait for and to retrieve the
        result of the signal.
        
        :param signal: The signal name
        :param content: The signal content
        :param isolate: The ID of the target isolate
        :param isolates: A list of isolate IDs
        :param groups: A list of isolates groups names
        :return: A FutureResult object
        """
        future = FutureResult()
        future.execute(self.__common_handling, signal, content, isolate,
                       isolates, groups, MODE_SEND)

        return future


    def send(self, signal, content, isolate=None, isolates=None, groups=None):
        """
        Sends a signal to the given target.
        
        The target can be either the ID of an isolate, a list of group of
        isolates or a list of isolates.
        
        The result is a map, with an entry for each reached isolate.
        The associated result can be empty.
        
        :param signal: The signal name
        :param content: The signal content
        :param isolate: The ID of the target isolate
        :param isolates: A list of isolate IDs
        :param groups: A list of isolates groups names
        :return: A (map Isolate ID -> results array, failed isolates) tuple,
                 None if there is no isolate to send the signal to.
        """
        # Standard behavior
        return self.__common_handling(signal, content, isolate, isolates,
                                      groups, MODE_SEND)


    def send_to(self, signal, content, host, port):
        """
        Sends a signal to the given end point
        
        :param signal: Signal name
        :param content: Signal content
        :param host: Target host name or IP
        :param port: Target port
        :return: The signal result (None or a the listeners results array)
        :raise IOError: Error sending the signal
        """
        complete_content = self._make_content(content)

        try:
            code, result = self.__internal_send((host, port), signal,
                                                complete_content, MODE_SEND)

            if code == 200:
                # OK
                return result

            else:
                # Not OK, return None
                _logger.warning("Signal %s to (%s, %d) result code : %d",
                                signal, host, port, code)
                return None

        except Exception as ex:
            raise IOError("Error sending signal %s to (%s, %s) : %s",
                          signal, host, port, ex)


    def _get_groups_accesses(self, *groups):
        """
        Retrieves an array of (host, port) tuples to access the isolates of the
        given groups
        
        :param groups: A list of group names
        :return: An isolate ID -> (host, port) map, empty no isolate is known
        """
        if not groups:
            # Nothing to do
            return None

        # Use a set, as an isolate may be part of many groups
        accesses = {}

        for group in groups:
            # Compute the isolates of the group according to all directories
            for directory in self._directories:
                group_accesses = directory.get_group_accesses(group)
                if group_accesses is not None:
                    # Expend the group accesses
                    accesses.update(group_accesses)

        return accesses


    def _get_isolates_accesses(self, *isolates):
        """
        Retrieves an array of (host, port) tuples to access the given isolates.
        The result contains only known isolates. Unknown ones are ignored.
        
        :param isolates: A list of isolate IDs
        :return: An isolate ID -> (host, port) map, empty no isolate is known
        """
        # Isolate ID -> (host, port) map
        result = {}

        if not isolates:
            # Nothing to do
            return result

        for isolate_id in isolates:
            for directory in self._directories:
                access = directory.get_isolate(isolate_id)
                if access is not None:
                    # Isolate found !
                    result[isolate_id] = access
                    break

        return result


    def _make_content(self, content):
        """
        Builds the signal complete content (meta-data + content)
        
        :param content: The signal content
        :return: The JSON form of the complete signal
        """
        signal_content = {
            # We need that to talk to Java isolates
            JAVA_CLASS: "org.psem2m.signals.impl.SignalData",
            "isolateId": self.context.get_property('psem2m.isolate.id'),
            # FIXME: set up the node name
            "isolateNode": self.context.get_property('psem2m.isolate.node'),
            "timestamp": int(time.time() * 1000),
            "signalContent": content
            }

        # Make a JSON form of a Jabsorb signal content
        return json.dumps(to_jabsorb(signal_content))


    def __common_handling(self, signal, content, isolate, isolates, groups,
                          mode):
        """
        All multiple targets methods shares the same code : compute accesses,
        use the loop and handle the results
        
        :param signal: The signal name
        :param content: The signal content
        :param isolate: The ID of the target isolate
        :param isolates: A list of isolate IDs
        :param groups: A list of isolates groups names
        :return: A (map Isolate ID -> results array, failed isolates) tuple,
                 None if there is no isolate to send the signal to.
        """
        accesses = {}

        # Compute accesses...
        if isolate:
            accesses.update(self._get_isolates_accesses(isolate))

        if isolates:
            accesses.update(self._get_isolates_accesses(isolates))

        if groups:
            accesses.update(self._get_groups_accesses(groups))

        if not accesses:
            # No isolates to access
            return None

        # Prepare the signal content
        complete_content = self._make_content(content)

        # Send signals
        return self.__loop_send(accesses, signal, complete_content, mode)


    def __internal_send(self, access, signal, content, mode):
        """
        Sends the signal to the given access
        
        :param access: A (host, port) tuple
        :param signal: The name of the signal
        :param content: The complete signal content (meta-data + content)
        :param mode: The signal sending mode
        :return: A (code, response) tuple
        :raise ValueError: Invalid access or signal name
        :raise: Exception raised sending the signal
        """
        if not signal:
            raise ValueError("A signal must have a name")

        if access == "{local}":
            # Special case : local signals don't have to go through the network
            return self._local_recv.handle_received_signal(signal, content)

        try:
            host, port = access

        except (TypeError, ValueError):
            raise ValueError("Invalid access tuple : '%s'" % access)

        # Prepare the signal URL
        if signal[0] == '/':
            signal = signal[1:]
        signal_url = "%s%s" % (SignalReceiver.SERVLET_PATH, signal)

        conn = httplib.HTTPConnection(host, port)
        try:
            # Open a new HTTP Connection
            conn.connect()

            # Send the request
            headers = {"Content-Type": "application/json",
                       HEADER_SIGNAL_MODE: mode}

            conn.request("POST", signal_url, content, headers)

            # Read the response
            response = conn.getresponse()
            if response.status != 200:
                # Not a full success
                _logger.warn("Incorrect response for %s : %s %s",
                             signal_url, response.status,
                             response.reason)

            result = response.read()
            if result:
                try:
                    # Try to convert the result from JSON
                    result = from_jabsorb(json.loads(result))

                except:
                    # Unreadable reponse
                    _logger.debug("Couldn't read response: '%s'", result)
                    result = None
            else:
                # Be sure to have a None value
                result = None

            return (response.status, result)

        finally:
            # Be nice...
            conn.close()


    def __loop_send(self, accesses, signal, content, mode):
        """
        Sends the signal to all given accesses.
        
        :param accesses: A Isolate ID -> (host, port) map
        :param signal: The signal name
        :param content: The complete signal content
        :param mode: The signal request mode
        :return: A (Isolate ID -> response map, failed isolates list) tuple
        """
        results = {}
        failed = []

        for isolate_id, access in accesses.items():
            try:
                results[isolate_id] = self.__internal_send(access, signal,
                                                           content, mode)

            except socket.error as ex:
                # Socket error
                _logger.error("Error sending signal %s to %s : %s",
                              signal, access, ex)
                failed.append(isolate_id)

            except:
                # Other error...
                _logger.exception("Error sending signal %s to %s",
                                  signal, access)
                failed.append(isolate_id)

        return (results, failed)

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-slave-agent-factory")
@Instantiate("psem2m-slave-agent")
@Requires("_receiver", "org.psem2m.signals.ISignalReceiver")
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
        self._context = None


    def handle_received_signal(self, name, data):
        """
        Handles a received signal
        """
        try:
            self._context.get_bundle(0).stop()
        except pelix.BundleException:
            _logger.exception("Error stopping the framework")


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        self._receiver.unregister_listener(MiniSlaveAgent.ISOLATE_STOP_SIGNAL,
                                           self)
        self._context = None


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        self._receiver.register_listener(MiniSlaveAgent.ISOLATE_STOP_SIGNAL,
                                         self)
        self._context = context
