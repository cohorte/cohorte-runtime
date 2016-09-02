#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Voting engine: Approbation vote

The candidate with the most votes is elected

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 1.1.0

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

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Property

# Voting system
import cohorte.vote

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.vote.SERVICE_VOTE_ENGINE)
@Property('_kind', cohorte.vote.PROP_VOTE_KIND, 'approbation')
@Instantiate('vote-engine-approbation')
class ApprobationEngine(object):
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

    @staticmethod
    def get_options():
        """
        Returns the options available for this engine

        :return: An option -> description dictionary
        """
        return {"votes_per_elector": "Sets the maximum number of votes for an "
                                     "elector. Default: 1",
                "nb_elected": "Number of candidates to elect. Default: 1",
                "penalty": "Penalty implied by a vote against a candidate. "
                           "Default: 0",
                "exclusion": "Number of votes against a candidate before his "
                             "exclusion. Default: 0 (inactive)"}

    @staticmethod
    def analyze(vote_round, ballots, candidates, parameters, vote_bean):
        """
        Analyzes the results of a vote

        :param vote_round: Round number (starts at 1)
        :param ballots: All ballots of the vote
        :param candidates: List of all candidates
        :param parameters: Parameters for the vote engine
        :param vote_bean: A VoteResults bean
        :return: The candidate(s) with the most votes
        """
        # Get the number of votes to take into account
        nb_votes = parameters.get("votes_per_elector", 1)
        nb_elected = parameters.get("nb_elected", 1)
        penalty = parameters.get("penalty", 0)
        penalty_exclusion = parameters.get("exclusion", 0)
        exclusion_value = penalty * (penalty_exclusion + 1)

        # Count the number of votes for each candidate
        results = {}

        nb_penalties = {}
        excluded = []

        for ballot in ballots:
            # Count supported candidates
            for candidate in ballot.get_for()[:nb_votes]:
                results[candidate] = results.get(candidate, 0) + 1

        if penalty > 0 or penalty_exclusion > 0:
            # Exclusion loop
            for ballot in ballots:
                # Count refused candidates
                refused = (candidate
                           for candidate in ballot.get_against()
                           if candidate not in excluded)
                for candidate in refused:
                    # Increase the count of penalties
                    candidate_penalties = results.get(candidate, 0) + 1
                    nb_penalties[candidate] = candidate_penalties

                    if candidate_penalties > penalty_exclusion > 0:
                        # Candidate is excluded !
                        excluded.append(candidate)
                        results[candidate] = exclusion_value

                    # Add the penalty to the votes
                    if penalty > 0:
                        results[candidate] = \
                            results.get(candidate, 0) - penalty

        # Store the results, as it makes a sorted list of tuples:
        # (votes, candidate)
        results = vote_bean.set_results(results)

        if nb_elected == 1:
            # Single result
            return results[0][1]
        else:
            # Return the candidates with the most votes
            return tuple(result[1] for result in results[:nb_elected])
