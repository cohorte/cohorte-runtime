#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Regularly triggers the "timer" event for components that haven't failed since
last check.

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
import logging
import threading

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Invalidate, Validate, Requires

# Composer
import cohorte.composer
import cohorte.composer.node.beans as beans

# ------------------------------------------------------------------------------

# Module version
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

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

    @staticmethod
    def vote(candidates, subject, ballot):
        """
        Does nothing
        """
        pass
