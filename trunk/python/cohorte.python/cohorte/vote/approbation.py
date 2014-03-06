#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Voting engine: Approbation vote

The candidate with the most votes is elected

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 1.0.0

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

# Voting system
import cohorte.vote

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Property

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.vote.SERVICE_VOTE_ENGINE)
@Property('_kind', cohorte.vote.PROP_VOTE_KIND, 'approbation')
@Instantiate('vote-engine-approbation')
class ApprobationEngine(cohorte.vote.AbstractEngine):
    """
    Voting system core service
    """
    def __init__(self):
        """
        Sets up members
        """
        # Supported kind of vote
        self._kind = None


    def get_kind(self):
        """
        Returns supported kind of vote
        """
        return self._kind


    def get_options(self):
        """
        Returns the options available for this engine

        :return: An option -> description dictionary
        """
        return {"votes_per_elector": "Sets the maximum number of votes for an "
                                     "elector. Default: 1",
                "nb_elected": "Number of candidates to elect. Default: 1"}


    def analyze(self, vote_round, ballots, candidates, parameters):
        """
        Analyzes the results of a vote

        :param vote_round: Round number (starts at 1)
        :param ballots: All ballots of the vote
        :param candidates: List of all candidates
        :param parameters: Parameters for the vote engine
        :return: The candidate(s) with the most votes
        """
        # Debug mode flag
        debug_mode = parameters.get('debug', False)

        # Get the number of votes to take into account
        nb_votes = parameters.get("votes_per_elector", 1)
        nb_elected = parameters.get("nb_elected", 1)

        # Count the number of votes for each candidate
        results = {}
        for ballot in ballots:
            accepted = ballot.get_for()[:nb_votes]
            for candidate in accepted:
                results[candidate] = results.get(candidate, 0) + 1

        # Make a sorted list of tuples: (votes, candidate)
        results = sorted(((item[1], item[0]) for item in results.items()),
                         reverse=True)

        if debug_mode:
            self.draw_results(vote_round, ballots, candidates, results)

        if nb_elected == 1:
            # Single result
            return results[0][1]
        else:
            # Return the candidates with the most votes
            return tuple(result[1] for result in results[:nb_elected])
