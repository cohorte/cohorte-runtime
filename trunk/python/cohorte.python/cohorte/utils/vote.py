#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Utility vote handler.

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
__version_info__ = (1, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

# Standard library
import functools
import itertools

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

@functools.total_ordering
class _Vote(object):
    """
    Associates a candidates to votes
    """
    def __init__(self, candidate):
        """
        Sets up members

        :param candidate: The candidate associated to the vote
        """
        self.__candidate = candidate
        self.__votes = 0


    def __hash__(self):
        """
        Vote hash is the one if the candidate
        """
        return hash(self.__candidate)


    def __str__(self):
        """
        String representation
        """
        return '{0:s} ({1:d} votes)'.format(self.__candidate, self.__votes)


    def __eq__(self, other):
        """
        Equality based on the candidate
        """
        return self.__candidate == other.__candidate


    def __lt__(self, other):
        """
        Lesser than other if less votes or "lesser" string representation
        """
        if self.__votes == other.__votes:
            return str(self.__candidate) < str(other.__candidate)

        return self.__votes < other.__votes


    @property
    def candidate(self):
        """
        The candidate associated to the vote
        """
        return self.__candidate


    @property
    def votes(self):
        """
        The number of votes for this candidate
        """
        return self.__votes


    def reset(self):
        """
        Resets the number of votes
        """
        self.__votes = 0


    def vote(self):
        """
        Vote for this candidate
        """
        self.__votes += 1

# ------------------------------------------------------------------------------

class MatchVote(object):
    """
    Election of the candidate matching the given element
    """
    def __init__(self, electors):
        """
        Sets up members

        :param electors: Electors for this vote
        """
        self._electors = electors


    def _compute_majority(self, votes, nb_voters, default=None):
        """
        Returns the candidate with nb_voters+1 votes, or the set of candidates
        for the next turn.

        :param votes: A set of _Vote beans
        :param nb_voters: Number of voters
        :param default: Result if no votes given
        :return: The candidate elected by majority
        :raise NextTurn: A new turn is necessary
        """
        # Absolute majority
        majority = (nb_voters / 2) + 1

        # Sort by number of votes
        results = sorted(votes, reversed=True)

        if results[0].votes >= majority:
            # Elected by majority
            return results[0].candidate

        # Threshold to go on next turn: > 10% of voters
        threshold = (nb_voters / 10) + 1
        def predicate(result):
            return result.votes >= threshold

        # Call for next turn
        candidates = {result.candidate
                      for result in itertools.takewhile(predicate, results)}

        raise NextTurn(candidates)


    def _compute_results(self, votes, nb_voters, default=None):
        """
        Computes the results of an election

        :param votes: A set of _Vote beans
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
            return next(iter(votes)).candidate

        else:
            # Compute isolates with majority or raises a NextTurn exception
            return self._compute_majority(votes, nb_voters)


    def vote(self, subject, initial_candidates, default=None, max_turns=3):
        """
        Votes for one of the given candidates or creates a new one

        :param subject: Subject of election
        :param initial_candidates: Initial candidates of the election
        :param default: Candidate to return if no one has been elected
        :param max_turns: Maximum number of turns
        :return: The elected candidate
        """
        # Candidate −> Votes
        candidates = frozenset(_Vote(candidate)
                               for candidate in initial_candidates)

        try:
            for _ in range(max_turns):
                try:
                    for elector in self._electors:
                        # Let each elector vote
                        elector.vote(subject, candidates)

                    # Get the results
                    elected = self._compute_results(candidates, default)
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
