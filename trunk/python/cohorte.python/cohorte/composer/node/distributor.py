#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Isolate Distributor

Clusters the components of a composition into groups according to several
criteria.

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

# Vote utility
import cohorte.utils.vote

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_DISTRIBUTOR_ISOLATE)
@Requires('_distance_criteria',
          cohorte.composer.SERVICE_NODE_CRITERION_DISTANCE, aggregate=True)
@Requires('_reliability_criteria',
          cohorte.composer.SERVICE_NODE_CRITERION_RELIABILITY, aggregate=True,
          optional=True)
@Instantiate('cohorte-composer-node-distributor')
class IsolateDistributor(object):
    """
    Clusters components into groups. Each group corresponds to an isolate.
    """
    def __init__(self):
        """
        Sets up members
        """
        # Distance criteria
        self._distance_criteria = []

        # Reliability criteria
        self._reliability_criteria = []


    def _get_matching_isolates(self, component, isolates):
        """
        Returns the isolates that match the given component.

        :param component: Component to check
        :param isolates: Extra available isolates
        :return: A set of isolates that could host the component (can be empty)
        """
        # Filter: component language
        language = component.language
        return {isolate for isolate in isolates
                if isolate.language in (None, language)}


    def distribute(self, components, existing_isolates):
        """
        Computes the distribution of the given components

        :param components: A list of RawComponent beans
        :param existing_isolates: A set of pre-existing eligible isolates
        :return: A set of EligibleIsolate beans
        """
        isolates = set(existing_isolates)

        # Nominate electors
        electors = set(self._distance_criteria)
        electors.update(self._reliability_criteria)

        # Create a vote
        vote = cohorte.utils.vote.MatchVote(electors)

        for component in components:
            # Compute the isolates that could match this component
            # FIXME: hidden due to python/python3 comparison problem
            # matching_isolates = self._get_matching_isolates(component,
            #                                                 isolates)
            matching_isolates = isolates

            # Vote !
            isolate = vote.vote(component, matching_isolates)
            if isolate is None:
                # Vote without result
                isolate = beans.EligibleIsolate()

            # Associate the component to the isolate
            isolate.add_component(component)

            # Isolate rename accepted
            if not isolate.name:
                isolate.accepted_rename()

            # Store the isolate
            isolates.add(isolate)

            # Reset others
            for other_isolate in matching_isolates:
                if other_isolate is not isolate:
                    other_isolate.rejected_rename()

        return isolates
