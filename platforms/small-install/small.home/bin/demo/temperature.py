#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
A fake sensor that provides dummy values

Created on 09 juil. 2012

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

import logging
import random
import threading
import time

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

from pelix.ipopo import constants

from pelix.ipopo.decorators import ComponentFactory, Provides, \
    Validate, Invalidate, Property, Requires

# ------------------------------------------------------------------------------ 

@ComponentFactory("demo-temperature-fake-factory")
@Property("_name", constants.IPOPO_INSTANCE_NAME)
@Property("_min", "temper.value.min", 22)
@Property("_max", "temper.value.max", 35)
@Provides("org.psem2m.demo.sensors.ITemper")
class FakeTemp(object):
    """
    Temperature sensor
    """
    def __init__(self):
        """
        Constructor
        """
        self._max = 0
        self._min = 0
        self._name = ""

        self._last_value = 0
        self._lock = threading.Lock()


    def get_unit(self):
        """
        Retrieves the values unit name
        """
        return "°C"


    def get_value(self):
        """
        Retrieves the current value of the sensor
        """
        with self._lock:
            if self._last_value == 0:
                # No value, use random one
                self._last_value = random.randint(self._min, self._max) * 1.0

            else:
                # Compute a delta
                delta = random.random() * 1.5
                add = random.randint(0, 1) == 1

                if add:
                    self._last_value += delta

                else:
                    self._last_value -= delta

            # Normalize value
            if self._last_value < self._min:
                self._last_value = self._min

            elif self._last_value > self._max:
                self._last_value = self._max

            # Return the result
            return self._last_value


    @Validate
    def validate(self, context):
        """
        Validation
        """
        _logger.info("Component %s validated", self._name)


    @Invalidate
    def invalidate(self, context):
        """
        Invalidation
        """
        _logger.info("Component %s invalidated", self._name)


if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    tmp = FakeTemp()
    tmp.validate(None)
    i = 0
    while i < 10:
        i += 1
        _logger.debug("%02d: %.2f %s", i, tmp.get_value(), tmp.get_unit())

    tmp.invalidate(None)
    _logger.debug("DONE")
