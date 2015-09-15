#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Votes according to components compatibility, i.e. if they never crashed when
in the same isolate

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
import itertools
import logging
import operator
import time

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Invalidate, Validate, Requires

# Composer
import cohorte.composer

# ------------------------------------------------------------------------------

# Module version
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

DEFAULT_RATING = 92
"""
Rating to use if not yet known.
92 because between 90 "empty isolate" and 95 "previous isolate"
"""

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.composer.SERVICE_NODE_CRITERION_DISTANCE)
@Requires('_status', cohorte.composer.SERVICE_STATUS_NODE)
@Instantiate('cohorte-composer-node-criterion-compatibility')
class CompatibilityCriterion(object):
    """
    Votes for the isolate that will host a component according to a
    compatibility rating between components. This evolves upon time and crashes
    """
    def __init__(self):
        """
        Sets up members
        """
        # sorted(Component name, Component Name) -> Rating
        self._ratings = {}

        # sorted(Component name, Component Name) -> Time of last crash
        self._last_crash = {}

        # Set of sorted(Component name, Component Name)
        self._incompatible = set()

        # Injected
        self._status = None

    def __str__(self):
        """
        String representation
        """
        return "Components compatibility rating"

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # TODO: load initial ratings from a file/db...
        self._ratings.clear()

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._ratings.clear()
        self._last_crash.clear()
        self._incompatible.clear()

    def _update_rating(self, pair, delta):
        """
        Updates the rating of the pair with the given delta

        :param pair: A sorted pair of names
        :param delta: Rating modification
        """
        # Normalize the new rating
        new_rating = self._ratings.setdefault(pair, DEFAULT_RATING) + delta
        if new_rating < 0:
            new_rating = 0
        elif new_rating > 100:
            new_rating = 100

        # Store it
        self._ratings[pair] = new_rating

        if new_rating < 5:
            # Lower threshold reached: components are incompatible
            _logger.debug("Pair %s is now incompatible", pair)
            self._incompatible.add(pair)

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

        # Update their compatibility ratings
        for pair in itertools.combinations(components, 2):
            # Get the last crash information
            last_crash = self._last_crash.get(pair, 0)

            if now - last_crash < 60:
                # Less than 60s since last crash
                delta = -5
            else:
                # More than 60s...
                delta = -1

            # Check if components are on the same isolate
                if self._status.neighbours(pair):
                    delta *= 3

            self._update_rating(pair, delta)

            # Update the last crash information
            self._last_crash[pair] = now

    def on_timer(self, components):
        """
        The timer ticks: some components have been OK before last tick and now

        :param components: Names of the components that well behaved
        """
        # Get the tick time
        now = time.time()

        # Update their compatibility ratings
        for pair in itertools.combinations(components, 2):
            if pair not in self._incompatible:
                # Get the last crash information
                last_crash = self._last_crash.get(pair, 0)
                if now - last_crash > 60:
                    # More than 60s since the last crash
                    delta = +5
                else:
                    # Less than 60s...
                    delta = +1

                # Check if components are on the same isolate
                if self._status.neighbours(pair):
                    delta *= 2

                self._update_rating(pair, delta)

    def vote(self, candidates, subject, ballot):
        """
        Votes for the isolate(s) with the minimal compatibility distance

        :param candidates: Isolates to vote for
        :param subject: The component to place
        :param ballot: The vote ballot
        """
        name = subject.name
        compatibilities = []
        neutral = None

        for candidate in candidates:
            # Analyze each candidate
            components = candidate.components
            if not components:
                # Avoid the "neutral" isolate
                if not candidate.name:
                    neutral = candidate
                    continue

                # No components, we're OK with it
                _logger.info("No components on %s, we're OK with it",
                             candidate)
                # Any other empty isolate
                compatibilities.append((90, candidate))

            else:
                # Get all factories on the isolate
                pairs = set(tuple(sorted((name, component.name)))
                            for component in components)

                # Remove identity
                pairs.discard((name, name))

                if pairs:
                    # Compute the worst compatibility rating on this isolate
                    min_compatibility = min(self._ratings.get(pair,
                                                              DEFAULT_RATING)
                                            for pair in pairs)
                    compatibilities.append((min_compatibility, candidate))

                elif subject in candidate.components:
                    # Isolate where the component already is
                    _logger.info("Previous isolate for component found !")
                    compatibilities.append((95, candidate))

        # Sort results (greater is better)
        compatibilities.sort(key=operator.itemgetter(0), reverse=True)

        # Vote
        for compatibility, candidate in compatibilities:
            if compatibility >= 50:
                # >= 50% of compatibility: OK
                ballot.append_for(candidate)

            elif compatibility < 30:
                # < 30% of compatibility: Reject
                ballot.append_against(candidate)

            # else: blank vote

        if not ballot.get_for() and neutral is not None:
            # We voted for no one: vote for neutral
            ballot.append_for(neutral)

        # Lock our vote
        ballot.lock()
