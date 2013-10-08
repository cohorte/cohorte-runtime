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
import cohorte.repositories

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# Standard library
import itertools

# ------------------------------------------------------------------------------

class Isolate(object):
    """
    Represents an isolate to be instantiated
    """
    def __init__(self):
        """
        Sets up members
        """
        self.name = None
        self.language = None
        self.components = set()


    def __str__(self):
        """
        String representation
        """
        if not self.language:
            return "NeutralIsolate"

        return "Isolate({0}, {1}, {2})".format(self.name, self.language,
                                               len(self.components))

# ------------------------------------------------------------------------------


class CoupdEtat(Exception):
    """
    Election result is forced
    """
    def __init__(self, claimant):
        """
        Sets up members

        :param claimant: The candidate that claims to be elected
        """
        # Do keep a None here
        self.claimant = claimant


    def __str__(self):
        """
        String representation
        """
        return "Coup d'État by {0:s}.".format(self.claimant)


class NextTurn(Exception):
    """
    Needs a new turn of vote
    """
    def __init__(self, candidates=None):
        """
        Sets up members

        :param candidates: Candidates for the next vote
        """
        # Do not keep a None here
        if candidates is None:
            candidates = []

        self.candidates = candidates


    def __str__(self):
        """
        String representation
        """
        return 'Next turn with {0}.'\
            .format(', '.join(str(candidate) for candidate in self.candidates))

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_DISTRIBUTOR_ISOLATE)
@Requires('_distance_criteria', cohorte.composer.SERVICE_CRITERION_DISTANCE,
          aggregate=True)
@Requires('_reliability_criteria',
          cohorte.composer.SERVICE_CRITERION_RELIABILITY, aggregate=True)
@Requires('_repositories', cohorte.repositories.SERVICE_REPOSITORY_FACTORIES,
          aggregate=True)
@Instantiate('cohorte-composer-node-distributor')
class IsolateDistributor(object):
    """
    Clusters components into groups. Each group corresponds to an isolate.
    """
    def __init__(self):
        """
        Sets up members
        """
        # Bundle repositories
        self._repositories = []

        # Distance criteria
        self._distance_criteria = []

        # Reliability criteria
        self._reliability_criteria = []


    def _compute_majority(self, votes, nb_voters):
        """
        Returns the candidate with nb_voters+1 votes, or the set of candidates
        for the next turn.

        :param votes: A {candidate -> votes} dictionary
        :param nb_voters: Number of voters
        :return:
        :raise NextTurn: A new turn is necessary
        """
        # Absolute majority
        majority = (nb_voters / 2) + 1

        # Sort by number of votes
        results = sorted(((nb_votes, candidate)
                          for candidate, nb_votes in votes.items()),
                         reversed=True)

        if results[0][0] >= majority:
            # Elected by majority
            return results[0][1]

        # Threshold to go on next turn: > 10% of voters
        threshold = (nb_voters / 10) + 1
        def predicate(result):
            return result[0] >= threshold

        # Call for next turn
        candidates = set(result[1]
                         for result in itertools.takewhile(predicate, results))
        raise NextTurn(candidates)


    def _compute_results(self, votes, nb_voters, default=None):
        """
        Computes the results of an election

        :param votes: A {candidate -> votes} dictionary
        :param nb_voters: Number of voters
        :param default: Result if no votes given
        :return: The elected candidate, or a new neutral one
        :raise NextTurn: No candidate with majority
        """
        if not votes:
            # No one elected: force a new isolate
            return default

        elif len(votes) == 1:
            # Only 1 of the candidates has been retained
            return next(iter(votes))

        else:
            # Compute isolates with majority or raises a NextTurn exception
            return self._compute_majority(votes, nb_voters)


    def vote(self, component, electors, initial_candidates,
             default=None, max_turns=3):
        """
        Votes for one of the given candidates or creates a new one

        :param component: Subject of election
        :param electors: Objects with a vote() method
        :param initial_candidates: Initial candidates of the election
        :param default: Candidate to use if no one has been elected
        :param max_turns: Maximum number of turns
        :return: The elected candidate
        """
        candidates = dict((candidate, 0)
                          for candidate in initial_candidates)

        try:
            for _ in range(max_turns):
                for elector in electors:
                    # Vote
                    elector.vote(component, candidates)

                    # Check if candidates are still available
                    if not candidates:
                        # Request a new loop with a new candidate
                        break

                try:
                    # Get the results
                    elected = self._compute_results(candidates)
                    break

                except NextTurn as ex:
                    # Still not decided
                    candidates = ex.candidates

        except CoupdEtat as ex:
            # Well, that escalated quickly...
            elected = ex.claimant

        if elected is None:
            # Election failed
            return default

        # Return the elected isolate
        return elected


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

        for component in components:
            # Compute the isolates that could match this component
            matching_isolates = self._get_matching_isolates(component,
                                                            isolates)

            # Vote !
            isolate = self.vote(component, electors,
                                matching_isolates, Isolate())

            # Associate the component to the isolate
            isolate.components.add(component)

            # Store the isolate
            isolates.add(isolate)

        return isolates
