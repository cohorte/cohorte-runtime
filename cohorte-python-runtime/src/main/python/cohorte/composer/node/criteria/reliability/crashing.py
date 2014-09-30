#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Vote according to components stability (crashes vs time)

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

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Invalidate, Validate

# Standard library
import logging
import operator
import time

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.composer.SERVICE_NODE_CRITERION_RELIABILITY)
@Instantiate('cohorte-composer-node-criterion-crash')
class CrashCriterion(object):
    """
    Votes for the isolate that will host a component according to the
    configuration
    """
    def __init__(self):
        """
        Sets up members
        """
        # Component name -> Rating
        self._ratings = {}

        # Component name -> Time of last crash
        self._last_crash = {}

        # Unstable components names
        self._unstable = set()

    def __str__(self):
        """
        String representation
        """
        return "Components reliability rating"

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # TODO: load initial ratings
        self._ratings.clear()

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._ratings.clear()
        self._last_crash.clear()
        self._unstable.clear()

    def _update_rating(self, component, delta):
        """
        Updates the rating of the component with the given delta

        :param component: A component name
        :param delta: Rating modification
        """
        # Normalize the new rating
        new_rating = self._ratings.setdefault(component, 50) + delta
        if new_rating < 0:
            new_rating = 0
        elif new_rating > 100:
            new_rating = 100

        # Store it
        self._ratings[component] = new_rating

        if new_rating < 5:
            # Lower threshold reached: components are incompatible
            self._unstable.add(component)

    def handle_event(self, event):
        """
        Does nothing: this elector only cares about what is written in
        configuration files
        """
        # Get the implicated components
        components = sorted(set(component.name
                                for component in event.components))

        if event.kind == 'timer':
            self.on_timer(components)
        elif event.kind == 'isolate.lost':
            self.on_crash(components)

    def on_crash(self, components):
        """
        An isolate has been lost

        :param components: Names of the components in the crashed isolate
        """
        # Get the time of the crash
        now = time.time()

        # Update their stability ratings
        for name in components:
            if name not in self._unstable:
                # Get the last crash information
                last_crash = self._last_crash.get(name, 0)
                time_since_crash = now - last_crash
                if time_since_crash < 60:
                    # Less than 60s since the last crash
                    self._update_rating(name, -10)

                else:
                    # More than 60s
                    self._update_rating(name, -5)

            # Update the last crash information
            self._last_crash[name] = now

    def on_timer(self, components):
        """
        The timer ticks: some components have been OK before last tick and now

        :param components: Names of the components that well behaved
        """
        # Get the tick time
        now = time.time()

        # Update their stability ratings
        for name in components:
            if name not in self._unstable:
                # Get the last crash information
                last_crash = self._last_crash.get(name, 0)
                time_since_crash = now - last_crash
                if time_since_crash > 120:
                    # More than 120s since the last crash
                    self._update_rating(name, +8)

                elif time_since_crash > 60:
                    # More than 60s since the last crash
                    self._update_rating(name, +4)

                # do nothing the minute right after a crash

    def compute_stats(self, components):
        """
        Computes statistics about the components of an isolate

        :param components: Components already assigned to the isolate
        """
        # Get the components names
        names = set(component.name for component in components)

        # TODO: compute variance too ?

        # Mean rating
        return sum(self._ratings.setdefault(name, 90)
                   for name in names) / len(names)

    def vote(self, candidates, subject, ballot):
        """
        Votes the isolate that matches the best the stability of the given
        component

        :param candidates: Isolates to vote for
        :param subject: The component to place
        :param ballot: The vote ballot
        """
        # Get/Set the rating of the component
        rating = self._ratings.setdefault(subject.name, 50.0)

        # Distance with other components
        distances = []

        for candidate in candidates:
            if candidate.components:
                if len(candidate.components) == 1 \
                        and subject in candidate.components:
                    # Single one in the isolate where we were
                    distances.append((0, candidate))
                elif subject.name in self._unstable:
                    # Don't try to go with other components...
                    ballot.append_against(candidate)
                elif rating > 20:
                    # Only accept to work with other components if the given
                    # one is stable enough (> 20% stability rating)
                    # Compute the mean and variance of the current components
                    # ratings
                    mean = self.compute_stats(candidate.components)
                    distance = abs(mean - rating)
                    if distance < 20:
                        # Prefer small distances
                        distances.append((distance, candidate))

            else:
                # Prefer non-"neutral" isolates
                if not candidate.name:
                    distances.append((20, candidate))

                else:
                    # First component of this isolate
                    distances.append((5, candidate))

        # Sort computed distances (lower is better)
        distances.sort(key=operator.itemgetter(0))

        # Use them as our vote
        ballot.set_for(distance[1] for distance in distances)
        ballot.lock()
