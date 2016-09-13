#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Core of the COHORTE Forker, in the Forker/Monitor/NodeComposer process

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 1.1.0

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
    MOD_BD_20150916 Adding framework property 'cohorte.node.data.dir' to 
                    isolate properties.
        
"""

# Standard library
import logging
import threading
import uuid

# Pelix framework
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Provides, RequiresMap, BindField
import pelix.http
import pelix.threadpool

# COHORTE modules
import cohorte
import cohorte.forker
import cohorte.monitor

# Herald
import herald
from herald.beans import Message

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.1"

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory('cohorte-forker-basic-factory')
@Provides(cohorte.SERVICE_FORKER, '_controller')
@Provides(cohorte.forker.SERVICE_WATCHER_LISTENER)
@Requires('_directory', herald.SERVICE_DIRECTORY)
@Requires('_herald', herald.SERVICE_HERALD)
@Requires('_http', pelix.http.HTTP_SERVICE)
@Requires('_state_dir', 'cohorte.forker.state')
@Requires('_state_updater', 'cohorte.forker.state.updater')
@RequiresMap('_starters', cohorte.forker.SERVICE_STARTER,
             cohorte.forker.PROP_STARTER_KINDS)
class ForkerBasic(object):
    """
    The forker core component
    """
    def __init__(self):
        """
        Constructor
        """
        # Injected services
        self._directory = None
        self._herald = None
        self._http = None
        self._state_dir = None
        self._state_updater = None
        self._starters = {}

        # Service controller (delayed)
        self._controller = False
        self._timer = None

        # Bundle context
        self._context = None

        # Platform is not yet stopped
        self._platform_stopping = False
        self._sent_stopping = False

        # Node name and UID
        self._node_name = None
        self._node_uid = None

        # Node Data directory location 
        self._node_data_dir = None

        # Isolate ID -> associated starter
        self._isolates = {}

        # Loop control of thread watching isolates
        self._watchers_running = False

        # Small lock for starting isolates asynchronously
        self.__lock = threading.Lock()

    @BindField('_starters')
    def _bind_starter(self, field, svc, svc_ref):
        """
        A new isolate starter is bound. Delays the forker service registration
        """
        if self._timer is not None:
            self._timer.cancel()

        # Set a new timer
        self._timer = threading.Timer(.5, self.__provide)
        self._timer.start()

    def __provide(self):
        """
        Sets the service controller to True
        """
        self._controller = True

    def start_isolate(self, isolate_config):
        """
        Starts the given isolate description.

        The result can be one of :

        * 0: SUCCESS, the isolate has been successfully started
        * 1: ALREADY_RUNNING, the isolate is already running
        * 2: RUNNER_EXCEPTION, an error occurred starting the isolate process
        * 3: INVALID_PARAMETER, a parameter is missing or invalid

        :param isolate_config: The isolate configuration dictionary
        :return: The result code
        """
        # -- Normalize configuration --
        # Name is mandatory
        name = isolate_config.get('name')
        if not name:
            _logger.error("Isolate doesn't have a name")
            return 3

        # Compute the isolate UID
        uid = isolate_config.get('uid')
        if not uid:
            # UID is missing, generate one and update the configuration
            uid = isolate_config['uid'] = str(uuid.uuid4())

        elif self.ping(uid) == 0:
            # Isolate is already running
            _logger.error("UID '%s' is already running", uid)
            return 1

        # FIXME: remove compatibility mode ASAP
        kind = isolate_config.get('kind', 'cohorte-compat')
        if not kind:
            # Kind is mandatory
            raise ValueError("No kind of isolate given")

        all_props = []
        if 'boot' in isolate_config and 'properties' in isolate_config['boot']:
            # ... in boot properties
            all_props.append(isolate_config['boot']['properties'])

        # ... in isolate properties
        all_props.append(isolate_config.setdefault('properties', {}))

        # Store the dumper port property (HTTP service port)
        dumper_port = self._http.get_access()[1]
        for props in all_props:
            props[cohorte.PROP_DUMPER_PORT] = dumper_port

        # Force node name and UID
        isolate_config['node_uid'] = self._node_uid
        isolate_config['node_name'] = self._node_name
        isolate_config['node_data_dir'] = self._node_data_dir
        
        # Tell the state directory to prepare an entry
        self._state_dir.prepare_isolate(uid)

        # -- Start the isolate --
        starter = None
        for kinds, starter in self._starters.items():
            if kind in kinds:
                break
        else:
            raise KeyError("Unhandled kind of isolate: {0}".format(kind))

        # Tell the state directory to prepare an entry
        self._state_dir.prepare_isolate(uid)

        with self.__lock:
            try:
                # Start the isolate
                uses_state = starter.start(isolate_config,
                                           self._state_updater.get_url())
            except (ValueError, OSError):
                # Clear the state (error must be logged by the starter)
                self._state_dir.clear_isolate(uid)
                return 2

        if uses_state:
            # Wait for the isolate to come up
            try:
                # Wait for it to be loaded (30 seconds max)
                _logger.debug('Waiting for %s to come up', uid)
                self._state_dir.wait_for(uid, 30)
            except KeyError:
                _logger.error("Isolate %s (%s) stopped before the forker "
                              "could wait for it", uid, name)
            except ValueError as ex:
                # Timeout reached or isolate lost
                _logger.error("Error waiting for isolate %s (%s) to be loaded:"
                              " %s", uid, name, ex)

                # Forget the isolate
                self._state_dir.clear_isolate(uid)

                # Kill the isolate
                starter.terminate(uid)
                return 2
        else:
            # No need to wait for the isolate
            self._state_dir.clear_isolate(uid)

        # Link UID and starter
        self._isolates[uid] = starter

        # No error
        return 0

    def ping(self, uid):
        """
        Tests the given isolate state.

        The result can be one of :

        * 0: ALIVE, the isolate is running
        * 1: DEAD, the isolate is not running (or unknown)
        * 2: STUCK, the isolate is running but doesn't answer to the forker
          ping request. (*not yet implemented*)

        :param uid: The UID of the isolate to test
        :return: The isolate process state
        """
        try:
            starter = self._isolates[uid]
        except KeyError:
            # Unknown UID -> dead
            return 1

        if starter.ping(uid):
            return 0
        else:
            return 1

    def stop_isolate(self, uid, timeout=3):
        """
        Kills the process with the given isolate ID. Does nothing the if the
        isolate ID is unknown.

        :param uid: The UID of the isolate to stop
        :param timeout: Maximum time to wait before killing the isolate process
                        (in seconds)
        :raise KeyError: Unknown isolate
        """
        # Find the starter handling the isolate and tell it to stop it
        starter = self._isolates.pop(uid)

        if not self._platform_stopping:
            # Send a signal to all other isolates
            self._herald.fire_group(
                'all', Message(cohorte.monitor.SIGNAL_ISOLATE_LOST, uid))

        # Stop the isolate
        starter.stop(uid)

        # Remove references to the isolate
        self._state_dir.clear_isolate(uid)

    def set_platform_stopping(self):
        """
        Sets the forker in platform-stopping mode: no more isolate will be
        spawn
        """
        self._platform_stopping = True

    def is_alive(self):
        """
        Tests if the forker is still usable

        :return: True if the forker is usable, False if it is shutting down
        """
        return self._watchers_running \
            and not self._sent_stopping \
            and not self._platform_stopping

    def handle_lost_isolate(self, uid, name):
        """
        Handles the loss of an isolate.
        If the isolate is a monitor, it must be restarted immediately.
        If not, a lost isolate signal is sent to monitors.

        :param uid: The UID of the lost isolate
        :param name: Name of the lost isolate
        """
        # Locally unregister the isolate
        self._directory.unregister(uid)

        try:
            starter = self._isolates.pop(uid)
        except KeyError:
            # Already stopped
            return

        _logger.warning("Lost isolate %s", uid)

        # FIXME: handle loss of predefined isolates

        # Clear isolate status
        self._state_dir.clear_isolate(uid)

        # Tell the starter to remove references to this isolate
        starter.terminate(uid)

        if not self._platform_stopping:
            # Send a signal to all other isolates
            self._herald.fire_group(
                'all', Message(cohorte.monitor.SIGNAL_ISOLATE_LOST, uid))

    def _send_stopping(self):
        """
        Sends the "forker stopping" signal to others
        """
        if self._sent_stopping:
            # Do not send this signal twice
            return

        _logger.info("Sending 'forker stopping' signal.")
        content = {'uid': self._context.get_property(cohorte.PROP_UID),
                   'node': self._context.get_property(cohorte.PROP_NODE_UID),
                   'isolates': list(self._isolates.keys())}

        self._herald.fire_group(
            'all', Message(cohorte.forker.SIGNAL_FORKER_STOPPING, content))

        # Flag up
        self._sent_stopping = True

    def _stop_isolates(self, max_threads=5, stop_timeout=5,
                       total_timeout=None):
        """
        Stops/kills all isolates started by this forker.
        Uses a task pool to parallelize isolate stopping.

        :param max_threads: Maximum number of threads to use to stop isolates
        :param stop_timeout: Maximum time to wait for an isolate to stop
                            (in seconds)
        :param total_timeout: Maximum time to wait for all isolates to be
                              stopped (in seconds)
        """
        # Create a task pool
        nb_threads = min(len(self._isolates), max_threads)
        if nb_threads > 0:
            pool = pelix.threadpool.ThreadPool(nb_threads,
                                               logname="forker-core-killer")
            for uid, starter in self._isolates.items():
                pool.enqueue(starter.stop, uid)

            # Run the pool
            pool.start()
            pool.join(total_timeout)
            pool.stop()

    def framework_stopping(self):
        """
        Called by the Pelix framework when it is about to stop
        """
        # Flags down
        self._watchers_running = False
        self._platform_stopping = True

        # Send the "forker stopping" signal
        self._send_stopping()

    @Invalidate
    def _invalidate(self, context):
        """
        Component invalidated

        :param context: The bundle context
        """
        # De-activate watchers
        self._watchers_running = False
        self._platform_stopping = True

        # Kill the timer
        if self._timer is not None:
            self._timer.cancel()
            self._timer = None

        try:
            # Send the "forker stopping" signal
            self._send_stopping()

            # Try to (nicely stop isolates)
            self._stop_isolates()
        except:
            pass
        finally:
            # Unregister from the framework
            # (if we weren't stopped by the framework)
            context.remove_framework_stop_listener(self)

            # Clean up
            self._isolates.clear()
            self._context = None
            self._node_name = None
            self._node_uid = None
            self._node_data_dir = None
            _logger.info("Forker invalidated")

    @Validate
    def _validate(self, context):
        """
        Component validated

        :param context: The bundle context
        """
        # Store the bundle context
        self._context = context

        # Get node information
        self._node_name = context.get_property(cohorte.PROP_NODE_NAME)
        self._node_uid = context.get_property(cohorte.PROP_NODE_UID)
        self._node_data_dir = context.get_property(cohorte.PROP_NODE_DATA_DIR)
        
        # Activate watchers
        self._sent_stopping = False
        self._watchers_running = True
        self._platform_stopping = False

        # Register as a framework listener
        context.add_framework_stop_listener(self)
