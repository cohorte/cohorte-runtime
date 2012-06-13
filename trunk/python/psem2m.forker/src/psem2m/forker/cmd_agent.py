#!/usr/bin/python
#-- Content-Encoding: UTF-8 --
"""
Forker command agent.

Transforms controller orders to signals sent to monitors.

:author: Thomas Calmant
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Instantiate, \
    Requires, Validate, Invalidate

# ------------------------------------------------------------------------------

import logging
import psem2m
import os
import json
_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

SIGNAL_FORKER_COMMAND = "/psem2m-forker-control-command"
SIGNAL_STOP_PLATFORM = "/psem2m/platform/stop"

ACCESS_FILE = "forker.access"

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-forker-cmd-agent-factory")
@Instantiate("psem2m-forker-cmd-agent")
@Requires("_sender", "org.psem2m.signals.ISignalBroadcaster")
@Requires("_receiver", "org.psem2m.signals.ISignalReceiver")
class ForkerCmdAgent(object):
    """
    Forker controller command agent
    """
    def __init__(self):
        """
        Constructor
        """
        self._sender = None
        self._receiver = None

        # The PSEM2M base directory
        self.base = None


    def handle_received_signal(self, name, signal_data):
        """
        Called when a remote services signal is received
        
        :param name: Signal name
        :param signal_data: Signal content
        """
        action = signal_data["signalContent"]["cmd"]
        handler = getattr(self, "do_%s" % action, None)
        if handler is None:
            # No handler found
            return None

        # Call the handler
        args = signal_data["signalContent"].get("args", None)
        handler(args)


    def do_stop(self, *args):
        """
        Stop command handler
        
        :param args: Possible arguments
        """
        # Send the stop platform signal to all monitors
        self._sender.send(SIGNAL_STOP_PLATFORM, None, groups=["MONITORS"])


    def _get_access_file(self):
        """
        Retrieves the path to the access file. Creates parent directories if
        needed. Returns None on error.
        
        :return: the path to the access file, or None
        """
        # Prepare the access file path
        access_file = os.path.join(self.base, "var", ACCESS_FILE)

        # Make parent directories if needed
        try:
            parent = os.path.dirname(access_file)
            if not os.path.exists(parent):
                os.makedirs(parent)

        except OSError:
            # Error preparing the path
            return None

        return access_file



    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        # Get the base directory
        self.base = os.getenv(psem2m.PSEM2M_BASE, os.getenv(psem2m.PSEM2M_HOME,
                                                            os.getcwd()))

        self._receiver.register_listener(SIGNAL_FORKER_COMMAND, self)

        # Get access information
        access_info = self._receiver.get_access_info()
        json_access_info = json.dumps({"host": access_info[0],
                                       "port": access_info[1]})

        # Prepare the access file path
        access_file = self._get_access_file()
        if access_file is not None:
            # Write to it
            with open(access_file, "w") as access_fp:
                access_fp.write(json_access_info)
                access_fp.write("\n")


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        self._receiver.unregister_listener(SIGNAL_FORKER_COMMAND, self)

        # Remote the access file
        access_file = self._get_access_file()
        if access_file is not None and os.path.isfile(access_file):
            os.remove(access_file)

        self.base = None
