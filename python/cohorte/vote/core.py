#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Voting system core service

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
from pelix.ipopo.decorators import ComponentFactory, Provides, Requires, \
    Instantiate

# Voting system
import cohorte.vote
import cohorte.vote.beans as beans

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Module version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.vote.SERVICE_VOTE_CORE)
@Requires('_store', cohorte.vote.SERVICE_VOTE_STORE)
@Requires('_engines', cohorte.vote.SERVICE_VOTE_ENGINE,
          aggregate=True, optional=False)
@Instantiate('vote-core')
class VoteCore(object):
    """
    Voting system core service
    """
    def __init__(self):
        """
        Sets up members
        """
        # Vote engines
        self._engines = []

        # Vote results storage
        self._store = None

        # Votes counter
        self._nb_votes = 0

    def get_kinds(self):
        """
        Returns the list of supported kinds of vote
        """
        return [engine.get_kind() for engine in self._engines]

    def vote(self, electors, candidates, subject=None, name=None,
             kind=None, parameters=None):
        """
        Runs a vote for the given

        :param electors: List of electors
        :param candidates: List of candidates
        :param subject: Subject of the vote (optional)
        :param name: Name of the vote
        :param kind: Kind of vote
        :param parameters: Parameters for the vote engine
        :return: The result of the election (kind-dependent)
        :raise NameError: Unknown kind of vote
        """
        # 1. Select the engine
        if kind is None:
            if not self._engines:
                # No engine available
                raise NameError("No engine available")

            # Use the first engine
            engine = self._engines[0]
            kind = engine.get_kind()
        else:
            # Engine given
            for engine in self._engines:
                if engine.get_kind() == kind:
                    break
            else:
                raise NameError("Unknown kind of vote: {0}".format(kind))

        # 2. Normalize parameters
        if not isinstance(parameters, dict):
            # No valid parameters given
            parameters = {}
        else:
            parameters = parameters.copy()

        if not name:
            # Generate a vote name
            self._nb_votes += 1
            name = "Vote {0} ({1})".format(self._nb_votes, kind)

        # Do not try to shortcut the vote if there is only one candidate:
        # it is possible that an elector has to be notified of the votes

        # Prepare the results bean
        vote_bean = beans.VoteResults(name, kind, candidates, electors,
                                      subject, parameters)

        # Vote until we have a result
        vote_round = 1
        result = None
        while True:
            try:
                # 3. Vote
                ballots = []
                for elector in electors:
                    ballot = beans.Ballot(elector)

                    # TODO: add a "last resort" candidate
                    # (if no candidate works)

                    elector.vote(tuple(candidates), subject, ballot)
                    ballots.append(ballot)

                # Store the ballots of this round
                vote_bean.set_ballots(ballots)

                # 4. Analyze votes
                result = engine.analyze(vote_round, ballots, tuple(candidates),
                                        parameters, vote_bean)
                break
            except beans.CoupdEtat as ex:
                # Putch = Coup d'etat !
                _logger.debug("A putch is perpetrated by [%s]", ex.claimant)
                vote_bean.coup_d_etat = True
                result = ex.claimant
                break
            except beans.NextRound as ex:
                # Another round is necessary
                candidates = ex.candidates
                vote_round += 1
                vote_bean.next_round(candidates)

                if len(candidates) == 1:
                    # Tricky engine...
                    result = candidates[0]
                    break
                else:
                    _logger.debug("New round required with: %s", candidates)

        # Store the vote results
        vote_bean.set_vote_results(result)
        self._store.store_vote(vote_bean)
        return result
