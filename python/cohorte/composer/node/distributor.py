#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Vote-based isolate distributor

Clusters the components of a composition into groups according to several
criteria.

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
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# Composer
import cohorte.composer
import cohorte.composer.node.beans as beans

# Vote service
import cohorte.vote

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
@Provides(cohorte.composer.SERVICE_DISTRIBUTOR_ISOLATE)
@Requires('_distance_criteria',
          cohorte.composer.SERVICE_NODE_CRITERION_DISTANCE, aggregate=True)
@Requires('_reliability_criteria',
          cohorte.composer.SERVICE_NODE_CRITERION_RELIABILITY, aggregate=True,
          optional=True)
@Requires('_vote', cohorte.vote.SERVICE_VOTE_CORE)
@Instantiate('cohorte-composer-node-distributor')
class IsolateDistributor(object):
    """
    Clusters components into isolates using a vote.
    """
    def __init__(self):
        """
        Sets up members
        """
        # Distance criteria
        self._distance_criteria = []

        # Reliability criteria
        self._reliability_criteria = []

        # Vote system
        self._vote = None

        # Distribution loop index
        self._nb_distribution = 0

    @staticmethod
    def _get_matching_isolates(component, isolates):
        """
        Returns the isolates that match the given component.

        :param component: Component to check
        :param isolates: Extra available isolates
        :return: A set of isolates that could host the component (can be empty)
        """
        # Filter: component language
        language = component.language

        # FIXME: ugly trick due to python/python3 comparison problem
        authorized_languages = {None, language}
        if language.startswith('python'):
            authorized_languages.update(('python', 'python3'))

        return [isolate for isolate in isolates
                if isolate.language in authorized_languages]

    def distribute(self, components, existing_isolates):
        """
        Computes the distribution of the given components

        :param components: A list of RawComponent beans
        :param existing_isolates: A set of pre-existing eligible isolates
        :return: A tuple of tuples: updated and new EligibleIsolate beans
        """
        # Nominate electors
        electors = set(self._distance_criteria)
        electors.update(self._reliability_criteria)

        # Growing list of candidates
        all_candidates = set(existing_isolates)

        # Hide components of the vote
        for candidate in all_candidates:
            candidate.hide(components)

        # Prepare the return
        updated_isolates = set()
        new_isolates = set()

        # Prepare parameters of the vote
        # -> -5 votes on "against"
        # -> Remove candidate after 2 "against"
        kind = "approbation"
        parameters = {'penalty': 5, 'exclusion': 2}

        self._nb_distribution += 1
        prefix = "Distribution {0}".format(self._nb_distribution)

        # Sort components by name
        sorted_components = list(components)
        sorted_components.sort(key=operator.attrgetter('name'))

        for component in sorted_components:
            # Compute the isolates that could match this component
            matching_isolates = self._get_matching_isolates(component,
                                                            all_candidates)

            # Sort candidates by name
            matching_isolates = sorted(
                matching_isolates,
                key=lambda iso: iso.name if iso.name is not None else "")

            # Add an empty isolate in the candidates
            neutral = beans.EligibleIsolate()
            matching_isolates.append(neutral)

            for candidate in matching_isolates:
                # Show the component in this vote
                candidate.unhide(component)

            # Vote !
            isolate = self._vote.vote(electors, matching_isolates, component,
                                      "{0}-{1}".format(prefix, component.name),
                                      kind, parameters)
            if isolate is None:
                # Vote without result
                isolate = neutral

            # Associate the component to the isolate
            isolate.add_component(component)

            # Add the new isolate for the next round
            all_candidates.add(isolate)

            # Isolate rename accepted
            if not isolate.name:
                isolate.accepted_rename()

            # Store the isolate
            if isolate is neutral:
                # New isolate
                new_isolates.add(isolate)

            elif isolate not in new_isolates:
                # Old one updated
                updated_isolates.add(isolate)

            # Reset others
            for other_isolate in matching_isolates:
                if other_isolate is not isolate:
                    other_isolate.rejected_rename()

                    # Re-hide component
                    other_isolate.hide((component,))

        return tuple(updated_isolates), tuple(new_isolates)

    def handle_event(self, event):
        """
        Handles a component/composition event

        :param event: The event to handle
        """
        # Nominate electors
        electors = set(self._distance_criteria)
        electors.update(self._reliability_criteria)

        for elector in electors:
            elector.handle_event(event)
