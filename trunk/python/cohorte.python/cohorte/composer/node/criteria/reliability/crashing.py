#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Vote by reliability

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
        # Factory name -> Rating
        self._ratings = {}


    def __str__(self):
        """
        String representation
        """
        return "Crashing"


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


    def handle_event(self, event):
        """
        Does nothing: this elector only cares about what is written in
        configuration files
        """
        if event.kind not in ('isolate.lost', 'timer'):
            # Ignore other messages
            return

        # Get the implicated factories
        factories = set(component.factory for component in event.components)

        if event.good:
            # Timer event
            delta = 2
        else:
            # Isolate lost
            delta = -5

        # Update their ratings
        for factory in factories:
            new_rating = self._ratings.get(factory, 50.0) + delta
            # Normalize the new rating
            if new_rating < 0:
                new_rating = 0

            elif new_rating > 100:
                new_rating = 100

            self._ratings[factory] = new_rating

            _logger.warning("Updating factory rating: %s -> %s",
                            factory, self._ratings[factory])


    def compute_stats(self, components):
        """
        Computes statistics about the components of an isolate

        :param components: Components already assigned to the isolate
        """
        # Get the components factories
        factories = set(component.factory for component in components)

        # TODO: compute variance too ?

        # Mean rating
        return sum(self._ratings.setdefault(factory, 50.0)
                   for factory in factories) / len(factories)


    def vote(self, candidates, subject, ballot):
        """
        Votes the isolate that matches the best the stability of the given
        component

        :param candidates: Isolates to vote for
        :param subject: The component to place
        :param ballot: The vote ballot
        """
        # Get/Set the rating of the component's factory
        rating = self._ratings.setdefault(subject.factory, 50.0)

        # Distance with other components
        distances = []

        for candidate in candidates:
            if rating > 10 and candidate.components:
                # Only accept to work with other components if the given one
                # is stable enough (> 10% stability rating)
                # Compute the mean and variance of the current components
                # ratings
                mean = self.compute_stats(candidate.components)
                distance = abs(mean - rating)
                if distance < 10:
                    # Prefer small distances
                    distances.append((distance, candidate))

            else:
                # First component of this isolate ?
                distances.append((0, candidate))

        # Sort computed distances (lower is better)
        distances.sort(key=operator.itemgetter(0))

        # Use them as our vote
        ballot.set_for(distance[1] for distance in distances)
        ballot.lock()
