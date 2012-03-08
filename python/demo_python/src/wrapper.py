#!/usr/bin/python
#-- Content-encoding: utf-8 --
"""
@author: Thomas Calmant
"""

from psem2m.component.decorators import ComponentFactory, Provides, Requires, \
    Property, Validate, Invalidate
from psem2m.utilities import SynchronizedClassMethod

import ctypes
import logging
import threading
import time

_logger = logging.getLogger(__name__)

@ComponentFactory("Sensor")
@Provides("demo.sensor")
@Requires("listeners", "demo.sensor.listener", True, True)
@Property("lib_name", "lib.name", "./libdemo_python_lib.so")
@Property("filename", "sensor.file", "sensor.txt")
@Property("id", "sensor.id", "<unknown>")
class SensorWrapper(object):
    """
    C library wrapper
    """
    def __init__(self):
        """
        Constructor
        """
        self.thread = None
        self.running = False
        self.lib = None
        self.lib_name = None
        self.id = None
        self.filename = None
        self.listeners = None
        self.old_state = -1
        self.error = 0
        self.lock = threading.RLock()
        _logger.debug("SensorWrapper instantiated")


    @Validate
    @SynchronizedClassMethod('lock')
    def validate(self, context):
        """
        Component validated
        """
        self.old_state = -1
        self.lib = ctypes.cdll.LoadLibrary(self.lib_name)
        self.running = True
        self.thread = threading.Thread(target=self.run)
        self.thread.start()

        _logger.debug("%s: Validated", self.id)



    @Invalidate
    @SynchronizedClassMethod('lock')
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.running = False
        self.thread = None
        self.lib = None
        self.old_state = -1

        _logger.debug("%s: Invalidated", self.id)


    def run(self):
        """
        Thread core
        """
        while self.running:
            with self.lock:
                if self.lib:
                    new_state = self.lib.read_state(self.filename)
                    self.trigger(new_state)
                    time.sleep(.2)


    def trigger(self, new_state):
        """
        Notify state modifications
        """
        _logger.debug("Read state : %d", new_state)

        if new_state < 0:
            if self.error != new_state:
                self.error = new_state
                self.send("Error", new_state, True)

        elif new_state != self.old_state:
            self.error = 0
            self.send("Changed state : %s -> %s" % (self.old_state, new_state),
                      new_state)
            self.old_state = new_state

        else:
            self.error = 0


    @SynchronizedClassMethod('lock')
    def send(self, message, new_state, error=False):
        """
        Sends a message to all listeners
        """
        if not self.listeners:
            _logger.debug("%s: No listeners to notify", self.id)
            return

        for listener in self.listeners:
            if error:
                listener.error(self.id, message, -new_state)
            else:
                listener.notify(self.id, message, new_state, self.old_state)
