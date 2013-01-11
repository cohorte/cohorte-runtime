#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Implementation of COHORTE Signals based on the HTTP protocol

**TODO:**
* Move the SlaveAgent out of this bundle
* Keep-alive/pool connections (for the sender)
* Complete code review
* Avoid using Java compatibility methods
* Use MessagePack instead of JSON+Jabsorb ?

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

# Pelix/iPOPO
from pelix.ipopo.decorators import ComponentFactory, Provides, Requires, \
    Validate, Invalidate, Instantiate, Property
import pelix.framework
import pelix.http

# Python utilities
from base.utils import to_bytes, to_unicode

# Java utilities
from base.javautils import to_jabsorb, from_jabsorb, JAVA_CLASS

# Python standard library
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
    from urllib.parse import quote

else:
    # Python 2
    import httplib
    from urllib import quote

# ------------------------------------------------------------------------------

MODE_ACK = "ack"
MODE_FORGET = "forget"
MODE_SEND = "send"

DEFAULT_MODE = MODE_SEND
HEADER_SIGNAL_MODE = "psem2m-mode"

# ------------------------------------------------------------------------------

CONTENT_TYPE_JSON = "application/json"
""" MIME type: JSON data """

CONTENT_TYPE_URLENCODED = "application/x-www-form-urlencoded"
""" MIME type: URL-Encoded """

DEFAULT_RECEIVER_PATH = "/cohorte-signal-receiver"
""" Default servlet path to register the signals receiver """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

def _make_json_result(code, message="", results=None):
    """
    An utility method to prepare a JSON result string, usable by the
    SignalReceiver
    
    :param code: A HTTP Code
    :param message: An associated message
    """
    return code, json.dumps(to_jabsorb({'code': code,
                                        'message': message,
                                        'results': results}))


@ComponentFactory('cohorte-signals-receiver-http-factory')
@Provides(cohorte.SERVICE_SIGNALS_RECEIVER)
@Provides(pelix.http.HTTP_SERVLET)
@Property('_path', pelix.http.HTTP_SERVLET_PATH, DEFAULT_RECEIVER_PATH)
class SignalReceiver(object):
    """
    COHORTE HTTP Signals receiver servlet
    """
    def __init__(self):
        """
        Constructor
        """
        # Servlet path
        self._path = DEFAULT_RECEIVER_PATH

        # Bundle context
        self._context = None

        # Signals listeners
        self._listeners = {}
        self._listeners_lock = threading.RLock()


    def do_GET(self, request, response):
        """
        Handles a GET request
        
        :param request: The HTTP request bean
        :param request: The HTTP response handler
        """
        # Basic properties
        values = {'path': quote(self._path),
                  'nb_listeners': len(self._listeners),
                  'clt_addr': request.get_client_address()}

        # Isolate information
        values['uuid'] = self._context.get_property(cohorte.PROP_UID)
        values['name'] = self._context.get_property(cohorte.PROP_NAME)
        values['node'] = self._context.get_property(cohorte.PROP_NODE)
        values['home'] = self._context.get_property(cohorte.PROP_HOME)
        values['base'] = self._context.get_property(cohorte.PROP_BASE)

        # Generate the page
        content = """<html>
<head>
<title>COHORTE Signals Receiver</title>
</head>
<body>
<h1>COHORTE Signals Receiver</h1>
<h2>Servlet informations:</h2>
<ul>
    <li>Path: {path}</li>
    <li>Number of listeners: {nb_listeners}</li>
</ul>
<h2>Isolate informations:</h2>
<ul>
    <li>UUID: {uuid}</li>
    <li>Name: {name}</li>
    <li>Node: {node}</li>
    <li>Home: {home}</li>
    <li>Base: {base}</li>
</ul>
<h2>Client informations:</h2>
<ul>
    <li>Address: {clt_addr[0]}</li>
    <li>Source port: {clt_addr[1]}</li>
</ul>
</body>
</html>""".format(**values)

        # Send the result
        response.send_content(200, content)


    def do_POST(self, request, response):
        """
        Handles a GET request
        
        :param request: The HTTP request bean
        :param request: The HTTP response handler
        """
        # Default code and content
        code = 501
        content = ""

        # Read the request
        signal_name = request.get_path()[len(self._path):]
        if not signal_name:
            code, content = _make_json_result(404, "No signal name in URI")

        else:
            content_type = request.get_header('content-type')
            if content_type not in (None, CONTENT_TYPE_JSON,
                                    CONTENT_TYPE_URLENCODED):
                # Unknown content type
                code, content = _make_json_result(500, "Unknown content type")

            else:
                # Get the signal mode, or the default one
                mode = request.get_header(HEADER_SIGNAL_MODE, DEFAULT_MODE)

                try:
                    # Decode the JSON content
                    signal_data = from_jabsorb(json.loads(request.read_data()))

                    if signal_data:
                        # Complete the signal information: client IP
                        signal_data["senderAddress"] = \
                                                request.get_client_address()[0]

                    # Handle the signal
                    code, content = self.handle_received_signal(signal_name,
                                                              signal_data, mode)
                except Exception as ex:
                    # Error
                    _logger.exception("Error reading signal '%s': %s",
                                      signal_name, ex)
                    code, content = _make_json_result(500,
                                                      "Error parsing signal")

        if code != 200:
            # Something wrong occurred
            _logger.debug("Signal: %s - Result: %s", signal_name, content)

        if content:
            # Convert content (Python 3)
            content = to_bytes(content)

        # Send headers
        response.send_content(code, content, CONTENT_TYPE_JSON)


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
            return self._notify_listeners(name, data)

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
                # Test if at least one listener will be notified
                for pattern in self._listeners:
                    if fnmatch.fnmatch(name, pattern):
                        # Found one !
                        result = _make_json_result(200,
                                                  "At least one listener found")
                        break
                else:
                    # No match found, return immediately
                    return _make_json_result(404, "No listener found")

            # Start the treatment in another thread, if necessary
            threading.Thread(target=self._notify_listeners,
                             args=(name, data)).start()
            return result

        # Unknown mode (not implemented error)
        return _make_json_result(501, "Unknown mode %s" % mode)


    def register_listener(self, signal_pattern, listener):
        """
        Registers a signal listener
        """
        if not signal_pattern or listener is None:
            # Invalid listener
            return

        with self._listeners_lock:
            listeners = self._listeners.setdefault(signal_pattern, [])
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


    def _notify_listeners(self, name, data):
        """
        Notifies all signal listeners that matches the given signal name
        
        :param name: A signal name
        :param data: Complete signal data (meta-data + content)
        :return: The result of all 
        """
        results = []
        listeners = []

        with self._listeners_lock:
            # Grab all registered listeners
            for pattern in self._listeners:
                if fnmatch.fnmatch(name, pattern):
                    # Signal name matches the pattern
                    for listener in self._listeners[pattern]:
                        if listener not in listeners:
                            listeners.append(listener)

        # Out-of-lock notification loop
        for listener in listeners:
            try:
                # Notify the listener
                result = listener.handle_received_signal(name, data)
                if result is not None:
                    # Store the result
                    results.append(result)

            except Exception as ex:
                _logger.exception("Error notifying a listener: %s", ex)

        return _make_json_result(200, results=results)


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        # Store the framework access
        self._context = context
        _logger.info("SignalReceiver servlet Ready")


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        # Clear the framework access
        self._context = None
        _logger.info("SignalReceiver servlet Gone")


# ------------------------------------------------------------------------------
# ------------------------------------------------------------------------------
# ------------------------------------------------------------------------------
# ------------------------------------------------------------------------------
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
@Provides(cohorte.SERVICE_SIGNALS_SENDER)
# TODO: @Requires("_directories", "org.psem2m.signals.ISignalDirectory", aggregate=True)
@Requires("_local_recv", cohorte.SERVICE_SIGNALS_RECEIVER)
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


    def fire(self, signal, content, isolate=None, isolates=None,
             dir_group=None, groups=None, excluded=None):
        """
        Sends a signal to the given target, without waiting for the result.
        Returns the list of successfully reached isolates, which may not have
        a listener for this signal.
        
        :param signal: The signal name
        :param content: The signal content
        :param isolate: The ID of the target isolate
        :param isolates: A list of isolate IDs
        :param dir_group: The name of a group computed by the directory
        :param groups: A list of isolates groups names
        :param excluded: Excluded isolates (only when using groups)
        :return: The list of reached isolates, None if there is no isolate to
                 send the signal to.
        """
        # Send the signal
        result = self.__common_handling(signal, content, isolate, isolates,
                                        dir_group, groups, excluded,
                                        MODE_FORGET)

        if result is None:
            # Unknown targets
            return None

        # Only return reached isolates
        return result[0].keys()


    def fire_to(self, signal, content, host, port):
        """
        Sends a signal to the given end point
        
        :param signal: Signal name
        :param content: Signal content
        :param host: Target host name or IP
        :param port: Target port
        :return: True if the isolate has been reached
        :raise Exception: Error sending the signal
        """
        complete_content = self._make_content(content)

        try:
            result = self.__internal_send((host, port), signal,
                                          complete_content, MODE_FORGET)

            if result is None:
                # No result...
                _logger.warning("No result for signal %s to (%s, %d)",
                                signal, host, port)
                return False

            return True

        except Exception as ex:
            _logger.exception("Error sending signal %s to (%s, %s) : %s",
                              signal, host, port, ex)
            raise


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._context = None
        _logger.info("SignalSender Gone")


    def post(self, signal, content, isolate=None, isolates=None,
             dir_group=None, groups=None, excluded=None):
        """
        Sends a signal to the given target in a different thread.
        See send(signal, content, isolate, isolates, groups) for more details.
        
        The result is a future object, allowing to wait for and to retrieve the
        result of the signal.
        
        :param signal: The signal name
        :param content: The signal content
        :param isolate: The ID of the target isolate
        :param isolates: A list of isolate IDs
        :param dir_group: The name of a group computed by the directory
        :param groups: A list of isolates groups names
        :param excluded: Excluded isolates (only when using groups)
        :return: A FutureResult object
        """
        future = FutureResult()
        future.execute(self.send, signal, content, isolate, isolates,
                       dir_group, groups, excluded)
        return future


    def post_to(self, signal, content, host, port):
        """
        Sends a signal to the given end point
        
        :param signal: Signal name
        :param content: Signal content
        :param host: Target host name or IP
        :param port: Target port
        :return: The signal result (None or a listeners results array)
        :raise Exception: Error sending the signal
        """
        future = FutureResult()
        future.execute(self.send_to, signal, content, host, port)
        return future


    def send(self, signal, content, isolate=None, isolates=None,
             dir_group=None, groups=None, excluded=None):
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
        :param dir_group: The name of a group computed by the directory
        :param groups: A list of isolates groups names
        :param excluded: Excluded isolates (only when using groups)
        :return: A (map Isolate ID -> results array, failed isolates) tuple,
                 None if there is no isolate to send the signal to.
        """
        # Standard behavior
        return self.__common_handling(signal, content, isolate, isolates,
                                      dir_group, groups, excluded, MODE_SEND)


    def send_to(self, signal, content, host, port):
        """
        Sends a signal to the given end point
        
        :param signal: Signal name
        :param content: Signal content
        :param host: Target host name or IP
        :param port: Target port
        :return: The signal result (None or a listeners results array)
        :raise Exception: Error sending the signal
        """
        complete_content = self._make_content(content)

        try:
            result = self.__internal_send((host, port), signal,
                                          complete_content, MODE_SEND)

            if result is None:
                # No result...
                _logger.warning("No result for signal %s to (%s, %d)",
                                signal, host, port)

            return result

        except Exception as ex:
            _logger.exception("Error sending signal %s to (%s, %s) : %s",
                              signal, host, port, ex)
            raise


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context
        _logger.info("SignalSender Ready")


    def _get_directory_group_accesses(self, dir_group):
        """
        Retrieves a map of (host, port) couples to access the isolates of the
        group computed by the directory
        
        :param groups: A list of group names
        :return: An isolate ID -> (host, port) map, empty no isolate is known 
        """
        # Isolate ID -> (host, port) map
        accesses = {}

        if not dir_group:
            # Nothing to do
            return accesses

        for directory in self._directories:
            group_accesses = directory.get_computed_group_accesses(dir_group)
            if group_accesses is not None:
                # Expend the group accesses
                accesses.update(group_accesses)

        return accesses


    def _get_groups_accesses(self, groups):
        """
        Retrieves a map of (host, port) couples to access the isolates of the
        given groups
        
        :param groups: A list of group names
        :return: An isolate ID -> (host, port) map, empty no isolate is known
        """
        # Isolate ID -> (host, port) map
        accesses = {}

        if not groups:
            # Nothing to do
            return accesses

        for group in groups:
            # Compute the isolates of the group according to all directories
            for directory in self._directories:
                group_accesses = directory.get_group_accesses(group)
                if group_accesses is not None:
                    # Expend the group accesses
                    accesses.update(group_accesses)

        return accesses


    def _get_isolates_accesses(self, isolates):
        """
        Retrieves a map of (host, port) couples to access the given isolates.
        The result contains only known isolates. Unknown ones are ignored.
        
        :param isolates: A list of isolate IDs
        :return: An isolate ID -> (host, port) map, empty no isolate is known
        """
        # Isolate ID -> (host, port) map
        accesses = {}

        if not isolates:
            # Nothing to do
            return accesses

        for isolate_id in isolates:
            for directory in self._directories:
                access = directory.get_isolate_access(isolate_id)
                if access is not None:
                    # Isolate found !
                    accesses[isolate_id] = access
                    break

        return accesses


    def _make_content(self, content):
        """
        Builds the signal complete content (meta-data + content)
        
        :param content: The signal content
        :return: The JSON form of the complete signal
        """
        signal_content = {
            # We need that to talk to Java isolates
            JAVA_CLASS: "org.psem2m.signals.SignalData",
            "senderId": self._context.get_property('psem2m.isolate.id'),
            # Set up the node name
            "senderNode": self._context.get_property('psem2m.isolate.node'),
            "timestamp": int(time.time() * 1000),
            "signalContent": content
            }

        # Make a JSON form of a Jabsorb signal content
        return json.dumps(to_jabsorb(signal_content))


    def __common_handling(self, signal, content, isolate, isolates,
                          dir_group, groups, excluded, mode):
        """
        All multiple targets methods shares the same code : compute accesses,
        use the loop and handle the results
        
        :param signal: The signal name
        :param content: The signal content
        :param isolate: The ID of the target isolate
        :param isolates: A list of isolate IDs
        :param dir_group: The name of a group computed by the directory
        :param groups: A list of isolates groups names
        :param excluded: Excluded isolates (only when using groups)
        :param mode: Signal sending mode
        :return: A (map Isolate ID -> results array, failed isolates) tuple,
                 None if there is no isolate to send the signal to.
        """
        accesses = {}

        # Compute accesses...
        if isolate:
            accesses.update(self._get_isolates_accesses([isolate]))

        if isolates:
            accesses.update(self._get_isolates_accesses(isolates))

        if groups:
            accesses.update(self._get_groups_accesses(groups))

        if dir_group:
            accesses.update(self._get_directory_group_accesses(dir_group))

        if excluded:
            for excluded_isolate in excluded:
                # Remove excluded isolates, if any
                if excluded_isolate in accesses:
                    del accesses[excluded_isolate]

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
        
        **TODO:**
        * Use a pool of connections instead creating a new one each time
        
        :param access: A (host, port) tuple
        :param signal: The name of the signal
        :param content: The complete signal content (meta-data + content)
        :param mode: The signal sending mode
        :return: The isolate response or None
        :raise ValueError: Invalid access or signal name
        :raise: Exception raised sending the signal
        """
        if not signal:
            raise ValueError("A signal must have a name")

        try:
            host, port = access

        except (TypeError, ValueError):
            raise ValueError("Invalid access tuple : '%s'" % access)

        if host == None:
            # Special case : local signals don't have to go through the network
            return self._local_recv.handle_received_signal(signal, content)

        # Prepare the signal URL
        if signal[0] == '/':
            signal = signal[1:]
        signal_url = "%s%s" % (SignalReceiver.SERVLET_PATH, signal)

        conn = httplib.HTTPConnection(host, port, timeout=3)
        try:
            # Open a new HTTP Connection
            conn.connect()

            # Send the request
            headers = {"Content-Type": CONTENT_TYPE_JSON,
                       HEADER_SIGNAL_MODE: mode}

            conn.request("POST", signal_url, content, headers)

            # Read the response
            try:
                response = conn.getresponse()

            except Exception as ex:
                _logger.error("Error while reading HTTP response: %s", ex)
                return None

            if response.status != 200:
                # Not a full success
                _logger.warn("Incorrect response for %s : %s %s",
                             signal_url, response.status,
                             response.reason)

            result = response.read()

            # Verify content type
            content_type = response.getheader("Content-Type")
            json_result = (content_type == CONTENT_TYPE_JSON)
            if content_type and not json_result:
                # Content-Type given but not handled
                _logger.debug("Received something that is not JSON: %s",
                              content_type)

            if result and json_result:
                try:
                    # Try to convert the result from JSON
                    result = from_jabsorb(json.loads(to_unicode(result)))

                except Exception as ex:
                    # Unreadable response
                    _logger.error("Couldn't read response: %s'", ex)
                    result = None
            else:
                # Be sure to have a None value
                result = None

            return result

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
    
    **TODO:**
    * Shouldn't be there
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
        except pelix.framework.BundleException:
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
