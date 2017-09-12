#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Utility vote handler.

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
import functools
import itertools
import logging

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

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
        super(CoupdEtat, self).__init__(str(self))

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
        self.candidates = candidates or []
        super(NextTurn, self).__init__(str(self))

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

    def __eq__(self, other):
        """
        Equality based on the candidate
        """
        return self.__candidate == other.__candidate

    def __str__(self):
        """
        String representation
        """
        return '{0:s} ({1:d} votes)'.format(self.__candidate, self.__votes)

    __repr__ = __str__

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
        self._electors = frozenset(electors)

    def _compute_majority(self, votes, default=None):
        """
        Returns the candidate with nb_voters+1 votes, or the set of candidates
        for the next turn.

        :param votes: A set of _Vote beans
        :param default: Result if no votes given
        :return: The candidate elected by majority
        :raise NextTurn: A new turn is necessary
        """
        nb_voters = len(self._electors)

        # Absolute majority
        majority = (nb_voters / 2) + 1

        # Sort by number of votes
        results = sorted(votes, reverse=True)
        if results[0].votes >= majority:
            # Elected by majority
            return results[0].candidate

        # Threshold to go on next turn: > 10% of voters
        threshold = (nb_voters / 10) + 1

        def predicate(result):
            """
            Predicate to filter candidates according to their results
            """
            return result.votes >= threshold

        # Call for next turn
        candidates = {result.candidate
                      for result in itertools.takewhile(predicate, results)}

        raise NextTurn(candidates)

    def _compute_results(self, votes, default=None):
        """
        Computes the results of an election

        :param votes: A set of _Vote beans
        :param default: Result if no votes given
        :return: The elected candidate, or a new neutral one
        :raise NextTurn: No candidate with majority
        """
        if not votes:
            # No one elected: force a new isolate
            return default
        elif len(votes) == 1:
            # Only 1 of the candidates has been retained
            _logger.critical("Only 1 of the candidates has been retained")
            return next(iter(votes)).candidate
        else:
            # Compute isolates with majority or raises a NextTurn exception
            return self._compute_majority(votes)

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
        candidates = tuple(_Vote(candidate)
                           for candidate in initial_candidates)
        elected = None
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
                    candidates = tuple(_Vote(candidate)
                                       for candidate in ex.candidates)

        except CoupdEtat as ex:
            # Well, that escalated quickly...
            _logger.critical("Coup d'État for %s", ex.claimant)
            elected = ex.claimant

        if elected is None:
            # Election failed
            return default

        # Return the elected isolate
        return elected
