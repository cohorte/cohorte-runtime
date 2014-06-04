#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Gathers components according to their history
This algorithm is a test: it can be a memory hog

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

# Standard library
from pprint import pformat
import logging
import operator

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_NODE_CRITERION_DISTANCE)
@Requires('_status', cohorte.composer.SERVICE_STATUS_NODE)
@Instantiate('cohorte-composer-node-criterion-compatibility')
class HistoryCriterion(object):
    """
    Gathers components which never crashed when they were in the same isolate
    """
    def __init__(self):
        """
        Sets up members
        """
        # A set of tuples: each tuple contains the components which were in an
        # isolate that crashed
        self._crashes = set()

        # Injected
        self._status = None


    def __str__(self):
        """
        String representation
        """
        return "Components gathering based on history"


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # TODO: load previous crashes from a file/db...
        self._crashes.clear()


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # TODO: store crashes to a file/db...
        self._crashes.clear()


    def handle_event(self, event):
        """
        Does nothing: this elector only cares about what is written in
        configuration files
        """
        # Get the implicated components
        components = set(component.name for component in event.components)
        if event.kind == 'isolate.lost':
            self.on_crash(components)


    def on_crash(self, components):
        """
        An isolate has been lost

        :param components: Names of the components in the crashed isolate
        """
        # Store the isolate composition at the time of crash
        self._crashes.add(frozenset(components))

        # TODO: consolidate history
        _logger.critical("%d crashes in history", len(self._crashes))
        _logger.critical("Known crashes:\n%s", pformat(self._crashes))


    def vote(self, candidates, subject, ballot):
        """
        Votes for the isolate(s) with the minimal compatibility distance

        :param candidates: Isolates to vote for
        :param subject: The component to place
        :param ballot: The vote ballot
        """
        # Subject component name
        component_name = subject.name

        # Preference for candidate: (number of components, candidate)
        preference = []

        # Neutral isolate (last resort)
        neutral_candidate = None

        # Prepare a dictionary: candidate -> components
        all_components = {}
        for candidate in candidates:
            components = [component.name for component in candidate.components]
            if not components and not candidate.name:
                # Found the neutral isolate
                neutral_candidate = candidate

            else:
                if component_name in components:
                    # Found the isolate where the isolate already is
                    components.remove(component_name)

                # Store information
                all_components[candidate] = components

        # Sort candidates by number of components already there
        sorted_candidates = [(len(content), candidate)
                             for candidate, content in all_components.items()]
        sorted_candidates.sort(key=operator.itemgetter(0), reverse=True)

        # Compute candidate preference (empty or OK)
        for _, candidate in sorted_candidates:
            # Analyze each candidate
            components = all_components[candidate]
            if not components:
                # No components, we're OK with it
                _logger.info("No components on %s, we're OK with it",
                             candidate)
                # Any other empty isolate
                preference.append((0, candidate))

            else:
                # Ensure that the content of this isolate won't be a known
                # crashing solution
                future_content = set(components)
                future_content.add(component_name)

                for crash in self._crashes:
                    if future_content.issuperset(crash):
                        # Solution is (a superset of) a crashing solution
                        _logger.warning("Known bad solution: %s",
                                        future_content)
                        ballot.append_against(candidate)
                        break
                else:
                    # Not a crashing solution
                    preference.append((len(components), candidate))

        if preference:
            # Sort results (greater is better: it gathers components)
            preference.sort(key=operator.itemgetter(0), reverse=True)
            _logger.info("Vote preferences for %s: %s",
                         component_name, preference)

            # Vote
            for _, candidate in preference:
                ballot.append_for(candidate)

        elif neutral_candidate is not None:
            # We voted for no one: vote for neutral
            _logger.warning("No good candidate for %s", component_name)
            ballot.append_for(neutral_candidate)

        # Lock our vote
        ballot.lock()
