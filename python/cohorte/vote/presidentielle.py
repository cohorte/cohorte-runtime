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
import math

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Property

# Voting system
import cohorte.vote
import cohorte.vote.beans as beans

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
@Property('_kind', cohorte.vote.PROP_VOTE_KIND, 'presidentielle')
@Instantiate('vote-engine-presidentielle')
class PresidentielleFrEngine(object):
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
        return {}

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
        # Count the number of votes for each candidate
        results = {}
        for ballot in ballots:
            try:
                candidate = ballot.get_for()[0]
                if candidate in candidates:
                    # In this kind of election, we can't have additional
                    # candidates
                    results[candidate] = results.get(candidate, 0) + 1
            except IndexError:
                # Blank vote
                continue

        # Store the results
        results = vote_bean.set_results(results)

        # Compute the number of votes for absolute majority
        nb_votes = sum(result[0] for result in results)
        majority = math.floor(nb_votes / 2) + 1

        if results[0][0] >= majority:
            # We have a winner
            return results[0][1]
        else:
            # We need a new round, with the two best candidates
            raise beans.NextRound(result[1] for result in results[:2])
