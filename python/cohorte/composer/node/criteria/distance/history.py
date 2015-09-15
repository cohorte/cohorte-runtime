#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Gathers components according to their history.
This algorithm is a test: it can be a memory hog

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
import operator

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
        if len(components) == 1:
            # Do not forbid a group of 1 component
            # (or it would never be instantiated again)
            return

        # Consolidate history
        crash = frozenset(components)
        to_remove = []
        for old_crash in self._crashes:
            if crash.issubset(old_crash):
                to_remove.append(old_crash)

        for old_crash in to_remove:
            self._crashes.remove(old_crash)

        # Store the isolate composition at the time of crash
        self._crashes.add(tuple(sorted(components)))

        _logger.info("%d crash(es) in history:\n%s", len(self._crashes),
                     '\n'.join('- ' + ', '.join(crash)
                               for crash in self._crashes))

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
            components = sorted(component.name
                                for component in candidate.components)
            if not components and not candidate.name:
                # Found the neutral isolate (do not add it to 'all_components')
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
        sorted_candidates.sort(key=lambda x: (-x[0], x[1].name))

        # Compute candidate preference (empty or OK)
        for _, candidate in sorted_candidates:
            # Analyze each candidate
            components = all_components[candidate]
            if not components:
                # No components, we're OK with it
                preference.append((0, candidate))

            else:
                # Ensure that the content of this isolate won't be a known
                # crashing solution
                future_content = set(components)
                future_content.add(component_name)

                for crash in self._crashes:
                    if future_content.issuperset(crash):
                        # Solution is (a superset of) a crashing solution
                        _logger.info(
                            "Known bad solution for %s:\n%s\ndue to:\n%s",
                            component_name,
                            ', '.join(name for name in sorted(future_content)),
                            ', '.join(name for name in sorted(crash)))
                        ballot.append_against(candidate)
                        break
                else:
                    # Not a crashing solution
                    preference.append((len(components), candidate))

        # TODO: tweak vote preferences to reduce the number of moves

        if preference:
            # Sort results (greater is better: it gathers components)
            preference.sort(key=operator.itemgetter(0), reverse=True)
            _logger.info("Vote preference for %s: %s",
                         component_name, ', '.join(item[1].name or "Neutral"
                                                   for item in preference))

            # Vote
            for _, candidate in preference:
                ballot.append_for(candidate)

        elif neutral_candidate is not None:
            # We voted for no one: vote for neutral
            _logger.info("Using neutral candidate for %s", component_name)
            ballot.append_for(neutral_candidate)

        # Lock our vote
        ballot.lock()
