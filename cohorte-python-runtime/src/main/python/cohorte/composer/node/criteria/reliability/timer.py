#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Regularly triggers the "timer" event for components that haven't
failed since last check.

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
import cohorte.composer.node.beans as beans

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Invalidate, Validate, Requires

# Standard library
import logging
import threading

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory()
@Requires('_distributor', cohorte.composer.SERVICE_DISTRIBUTOR_ISOLATE)
@Requires('_status', cohorte.composer.SERVICE_STATUS_NODE)
@Provides(cohorte.composer.SERVICE_NODE_CRITERION_RELIABILITY)
@Instantiate('cohorte-composer-node-timer')
class TimerUpdater(object):
    """
    Posts a Node event after a time without error
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._distributor = None
        self._status = None

        # Timer time (45s)
        self._timer_delta = 45

        # The timer
        self._timer = None

        # The set of factories that crashed while ticking
        self._erroneous = set()

        # A lock
        self._lock = threading.Lock()

    def __str__(self):
        """
        String representation
        """
        return "Timer"

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # Setup the timer
        self.__start_timer()

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._timer.cancel()
        self._timer = None

    def __start_timer(self):
        """
        Starts the timer
        """
        self._timer = threading.Timer(self._timer_delta, self.send_event)
        self._timer.start()

    def send_event(self):
        """
        Called by the timer object
        """
        with self._lock:
            events = []
            for name in self._status.get_isolates():
                # Filter components: keep those with factories that didn't
                # fail since the last tick
                components = [component for component
                              in self._status.get_components_for_isolate(name)
                              if component.factory not in self._erroneous]

                if not components:
                    # No "good" component for this isolate
                    continue

                # Prepare an event
                event = beans.Event(name, "timer", True)
                event.components = components
                events.append(event)

            # Clean up
            self._erroneous.clear()

        # Call events outside the lock
        for event in events:
            self._distributor.handle_event(event)

        # Prepare next call
        self.__start_timer()

    def handle_event(self, event):
        """
        Does nothing: this elector only cares about what is written in
        configuration files
        """
        if event.kind == 'timer':
            # Avoid loop back...
            return

        if not event.good:
            with self._lock:
                # Get the implicated factories
                self._erroneous.update(component.factory
                                       for component in event.components)

    def vote(self, candidates, subject, ballot):
        """
        Does nothing
        """
        pass
