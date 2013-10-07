#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top Composer entry point service

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
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
import cohorte.monitor

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_COMPOSER_TOP)
@Requires('_distributor', cohorte.composer.SERVICE_DISTRIBUTOR_NODE)
@Requires('_status', cohorte.composer.SERVICE_STATUS_TOP)
@Requires('_commander', cohorte.composer.SERVICE_COMMANDER_TOP)
@Requires('_monitor', cohorte.monitor.SERVICE_MONITOR)
@Instantiate('cohorte-composer-top')
class TopComposer(object):
    """
    The Top Composer entry point
    """
    def __init__(self):
        """
        Sets up components
        """
        self._distributor = None
        self._status = None
        self._commander = None


    def start(self, composition):
        """
        Instantiates the given composition

        :param composition: A RawComposition bean
        :return: The UID of the instantiated composition
        """
        # Distribute components
        distribution = self._distributor.distribute(composition)

        # Store the distribution
        uid = self._status.store(distribution)

        # Tell the monitor to start the nodes
        # FIXME: use an intermediate level for nodes (EC2 LoadBalancer-like)
        self._monitor.start_nodes(distribution.keys())

        # Tell the commander to start the instantiation on existing nodes
        self._commander.start(distribution)
        return uid


    def stop(self, uid):
        """
        Stops the given composition

        :param uid: The UID of the composition to stop
        :raise KeyError: Unknown composition
        """
        # Pop the distribution
        distribution = self._status.pop(uid)

        # Tell all node composers to stop their components
        self._commander.stop(distribution)
