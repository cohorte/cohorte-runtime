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
    Invalidate, Validate, Requires
import pelix.shell

# Standard library
import itertools
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_NODE_CRITERION_DISTANCE)
@Requires('_utils', pelix.shell.SERVICE_SHELL_UTILS)
@Instantiate('cohorte-composer-node-criterion-compatibility')
class CompatibilityCriterion(object):
    """
    Votes for the isolate that will host a component according to the
    configuration
    """
    def __init__(self):
        """
        Sets up members
        """
        # sorted(Factory name, Factory Name) -> Rating
        self._ratings = {}

        # Inject Shell utilities
        self._utils = None


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


    def print_matrix(self):
        """
        Prints a matrix of the factories ratings
        """
        all_factories = sorted(set(pair[0] for pair in self._ratings.keys()))

        matrix = []

        for factory_a in all_factories:
            line = [factory_a]
            for factory_b in all_factories:
                line.append(self._ratings.get((factory_a, factory_b), ""))

            matrix.append(line)

        _logger.debug('\n' + self._utils.make_table([''] + all_factories,
                                                    matrix))


    def handle_event(self, event):
        """
        Does nothing: this elector only cares about what is written in
        configuration files
        """
        if event.kind not in ('isolate.lost', 'timer'):
            # Ignore other messages
            return

        # Get the implicated factories
        factories = sorted(set(component.factory
                               for component in event.components))

        if event.good:
            # Timer event
            delta = 2
        else:
            # Isolate lost
            delta = -5

        # Update their compatibility ratings
        for pair in itertools.combinations(factories, 2):
            self._ratings[pair] = self._ratings.get(pair, 50.0) + delta

        self.print_matrix()


    def vote(self, component, eligibles):
        """
        Votes for the isolate with the best minimal compatibility

        :param component: A Component bean
        :param eligibles: A set of Isolate beans
        """
        factory = component.factory
        compatibilities = []
        has_voted = False

        for eligible in eligibles:
            components = eligible.candidate.components
            if not components:
                # No components, we're OK with it
                _logger.warning("No components, we're OK with it")
                eligible.vote()
                has_voted = True

            # Get all factories on the isolate
            pairs = set(tuple(sorted((factory, component.factory)))
                        for component in components)

            # Remove identity
            pairs.discard((factory, factory))

            if pairs:
                # Compute the worst compatibility rating on this isolate
                min_compatibility = min(self._ratings.get(pair, 50.0)
                                        for pair in pairs)
                compatibilities.append((min_compatibility, eligible))

            else:
                # No other factory: vote for it
                eligible.vote()
                has_voted = True

        if not compatibilities:
            # No isolate found
            _logger.warning("No isolate found")
            return

        if has_voted:
            _logger.debug("Already voted...")
            return

        compatibilities.sort(reverse=True)
        best = compatibilities[0]
        if best[0] > 50:
            # Best compatibility is good enough
            _logger.warning("Voting for the best: %s", best)
            best[1].vote()

        else:
            _logger.warning("No vote: compatibility is too low: %s",
                            compatibilities)
