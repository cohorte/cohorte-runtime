#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Node status history

:author: Thomas Calmant
:copyright: Copyright 2014, isandlaTech
:license: GPLv3
:version: 3.0.0

..

    This file is part of Cohorte.

    Cohorte is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cohorte is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cohorte. If not, see <http://www.gnu.org/licenses/>.
"""

# Module version
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

# Composer
import cohorte.composer

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Invalidate

# Standard library
import logging
import time

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
        self._storage.clear()


    def keep_recent(self, timestamp):
        """
        Keeps history after the given time stamp, removes other entries

        :param timestamp: Minimal timestamp to be kept
        """
        # Sort times
        stamps = sorted(self._storage.keys())
        for stamp in stamps:
            if stamp < timestamp:
                del self._storage[stamp]


    def items(self):
        """
        Returns a sorted list of (time stamp, {isolate -> [names]}) tuples
        """
        return sorted(self._storage.items())


    def store(self, distribution):
        """
        Stores the given isolate distribution

        :param distribution: A isolate -> components names dictionary
        """
        self._storage[time.time()] = distribution.copy()
