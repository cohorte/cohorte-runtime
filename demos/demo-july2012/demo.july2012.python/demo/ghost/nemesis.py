#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Basic crasher

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 0.1

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
__version_info__ = (0, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

# Local
import demo.ghost

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Property, Validate, \
    Invalidate
import pelix.internals.events as events

# Standard library
import logging
import threading

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory('ghost-nemesis-factory')
@Property('_id', 'ghost.id', 'nemesis')
@Property('_nemesis', 'nemesis')
class Nemesis(demo.ghost.Ghost):
    """
    Ghost crasher
    """
    def __init__(self):
        """
        Sets up members
        """
        demo.ghost.Ghost.__init__(self)
        self._context = None
        self._nemesis = None


    def crash(self):
        """
        Crashes after 5 seconds
        """
        threading.Timer(5, super(Nemesis, self).crash).start()


    def handle_order(self, event):
        """
        Handles a ghost order
        """
        parts = event.split(',')
        if parts[0] != 'nemesis':
            return

        try:
            if parts[1] == 'set':
                # Set the nemesis
                self._nemesis = parts[2]
                self._check()
                self._register()

            elif parts[1] == 'check':
                self._check()

        except IndexError:
            # Not enough command
            _logger.warning("Incomplete order: %s", event)
            return


    def service_changed(self, event):
        """
        A service event happened
        """
        if event.get_kind() in (events.ServiceEvent.REGISTERED,
                                events.ServiceEvent.MODIFIED):
            # Service appeared: crash
            _logger.debug("Nemesis appeared (event)")
            self.crash()


    def _check(self):
        """
        One-shot check if the other service is present
        """
        if not self._nemesis:
            # Invalid check
            _logger.debug("No nemesis to check for....")
            return

        if self._context.get_service_reference(self._nemesis):
            # Service found
            _logger.debug("Nemesis found (check)")
            self.crash()


    def _register(self):
        """
        Registers a service event to
        """
        # Unregister current listener
        self._context.remove_service_listener(self)

        if not self._nemesis:
            # No nemesis
            _logger.debug("No nemesis to wait for....")
            return

        # Register as service listener
        self._context.add_service_listener(self, None, self._nemesis)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context
        self._register()
        self._check()


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._context.remove_service_listener(self)
        self._context = None
