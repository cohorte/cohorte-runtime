#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Voting system core service

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
import cohorte.vote.beans as beans

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Requires, \
    Instantiate

# Standard library
import logging

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
        self._engines = None

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
                # Coup d'État !
                _logger.warning("Coup d'état by %s", ex.claimant)
                vote_bean.coup_d_etat = True
                result = ex.claimant
                break

            except beans.NextRound as ex:
                # Another round is necessary
                candidates = ex.candidates
                vote_round += 1
                vote_bean.next_round(candidates)

                _logger.warning("New round required with: %s", candidates)

                if len(candidates) == 1:
                    # Tricky engine...
                    result = candidates[0]
                    break

        # Store the vote results
        vote_bean.set_vote_results(result)
        self._store.store_vote(vote_bean)
        return result
