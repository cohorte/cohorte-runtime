#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Node status history

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 3.0.0

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
"""

# Standard library
from pprint import pformat
import logging
import threading
import time

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Invalidate

# Composer
import cohorte.composer

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.composer.SERVICE_HISTORY_NODE)
@Instantiate('cohorte-composer-node-history')
class NodeHistory(object):
    """
    Associates components to their hosting isolate
    """
    def __init__(self):
        """
        Sets up members
        """
        # Storage: time stamp -> distribution
        self._storage = {}
        self._lock = threading.Lock()

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.clear()

    def clear(self):
        """
        Clears the storage
        """
        with self._lock:
            self._storage.clear()

    def keep_recent(self, timestamp):
        """
        Keeps history after the given time stamp, removes other entries

        :param timestamp: Minimal timestamp to be kept
        """
        with self._lock:
            # Sort times
            stamps = sorted(self._storage.keys())
            for stamp in stamps:
                if stamp < timestamp:
                    del self._storage[stamp]

    def items(self):
        """
        Returns a sorted list of (time stamp, {isolate -> [names]}) tuples
        """
        with self._lock:
            return sorted(self._storage.items())

    def store(self, distribution):
        """
        Stores the given isolate distribution

        :param distribution: A isolate -> components names dictionary
        """
        # Store the stamp ASAP
        timestamp = time.time()

        with self._lock:
            # Store our distribution
            self._storage[timestamp] = {
                isolate: tuple(components)
                for isolate, components in distribution.items()}
            _logger.info("Node composer stored in history:\n%s",
                         pformat(self._storage[timestamp]))
