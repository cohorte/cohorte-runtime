#!/usr/bin/python
#-- Content-Encoding: UTF-8 --
"""
Forker heart beat.

Sends heart beat signals to the monitors to signal the forker presence.

:author: Thomas Calmant
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Instantiate, \
    Requires, Validate, Invalidate

# ------------------------------------------------------------------------------

import logging
_logger = logging.getLogger(__name__)

import threading

# ------------------------------------------------------------------------------

# The heartbeat signal
SIGNAL_HEARTBEAT = "/psem2m/internals/forkers/heart-beat"

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-forker-heart-factory")
@Instantiate("psem2m-forker-heart")
# Just to have the same life cycle than the forker...
@Requires("_forker", "org.psem2m.isolates.services.forker.IForker")
# To retrieve access information
@Requires("_http", "HttpService")
@Requires("_sender", "org.psem2m.signals.ISignalBroadcaster")
class Heart(object):
    """
    The heart beat sender
    """
    def __init__(self):
        """
        Constructor
        """
        self._forker = None
        self._http = None
        self._sender = None

        self._context = None
        self._event = None
        self._thread = None


    def _run(self):
        """
        Heart beat sender
        """
        # Prepare the access information according to the HTTP service
        server_info = {"node": self._context.get_property('psem2m.isolate.node'),
                       "port": self._http.get_port()
                       }

        while not self._event.is_set():
            # Send the heart beat
            self._sender.send(SIGNAL_HEARTBEAT, server_info,
                              groups=["MONITORS"])

            # Wait 3 seconds before next loop
            self._event.wait(3)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context

        # Prepare the thread controls
        self._event = threading.Event()

        self._thread = threading.Thread(target=self._run, name="HeartBeart")
        self._thread.start()


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Get out of the waiting condition
        self._event.set()

        # Wait for the thread to stop
        self._thread.join(1)

        # Clean up
        self._context = None
        self._event = None
        self._thread = None
