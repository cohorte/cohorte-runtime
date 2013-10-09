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
          cohorte.composer.SERVICE_NODE_CRITERION_RELIABILITY, aggregate=True)
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


    def _get_matching_isolates(self, component, additional_isolates):
        """
        Gets the isolates that match the given component. Looks in known
        isolates (starting/running) and in the given isolates.

        :param component: Component to check
        :param additional_isolates: Extra available isolates
        """
        all_isolates = set(additional_isolates)

        # TODO: add known isolates

        # Filter: component language
        language = component.language
        return {isolate for isolate in all_isolates
                if isolate.language in (None, language)}


    def distribute(self, components):
        """
        Computes the distribution of the given components

        :param components: A list of RawComponent beans
        :return: A list of Isolate beans
        """
        isolates = set()

        # Nominate electors
        electors = set(self._distance_criteria)
        electors.update(self._reliability_criteria)

        # Create a vote
        vote = cohorte.utils.vote.MatchVote(electors)

        for component in components:
            # Compute the isolates that could match this component
            matching_isolates = self._get_matching_isolates(component,
                                                            isolates)

            # Vote !
            isolate = vote.vote(component, matching_isolates)
            if isolate is None:
                # Vote without result
                isolate = beans.Isolate()

            # Associate the component to the isolate
            isolate.add_component(component)

            # Store the isolate
            isolates.add(isolate)

        return isolates
