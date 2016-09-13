#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Voting system beans

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
import operator

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------


class NextRound(Exception):
    """
    A new round is necessary for this vote
    """
    def __init__(self, candidates):
        """
        Prepares the exception content
        """
        super(NextRound, self).__init__("A new round is required")
        self.__candidates = tuple(candidates)

    @property
    def candidates(self):
        """
        Returns the candidates for the next round
        """
        return self.__candidates


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
        return "Coup d'État by {0}.".format(self.claimant)

# ------------------------------------------------------------------------------


class Ballot(object):
    """
    Represents a ballot in a vote
    """
    def __init__(self, elector):
        """
        Sets up members

        :param elector: The elector that filled this ballot
        """
        self.__elector = elector
        self.__for = []
        self.__against = []

    def lock(self):
        """
        Locks the ballot, so that it can't be modified anymore
        """
        self.__for = tuple(self.__for)
        self.__against = tuple(self.__against)

    def get_elector(self):
        """
        Returns the elector associated to this vote

        :return: The elector (name or instance)
        """
        return self.__elector

    def get_for(self):
        """
        Returns the candidates the elector voted for
        """
        return tuple(self.__for)

    def get_against(self):
        """
        Returns the candidates the elector voted against (black vote)
        """
        return tuple(self.__against)

    def append_for(self, candidate):
        """
        The elector votes for the given candidate

        :param candidate: One of the candidates
        """
        if candidate not in self.__against and candidate not in self.__for:
            self.__for.append(candidate)

    def append_against(self, candidate):
        """
        The elector votes against the given candidate

        :param candidate: One of the candidates
        """
        if candidate not in self.__against and candidate not in self.__for:
            self.__against.append(candidate)

    def set_for(self, candidates):
        """
        Sets the candidates the elector votes for at once
        (replaces the current list)

        :param candidates: An iterable of candidates
        """
        self.__for = list(candidates)

    def set_against(self, candidates):
        """
        Sets the candidates the elector votes for at once
        (replaces the current list)

        :param candidates: An iterable of candidates
        """
        self.__against = list(candidates)


class SecretBallot(Ballot):
    """
    Secret ballot: no one can tell who voted
    """
    def __init__(self, elector=None):
        """
        Sets up members

        :param elector: Unused optional parameter, to support Ballot
                        constructor signature
        """
        super(SecretBallot, self).__init__(None)

# ------------------------------------------------------------------------------


class VoteResults(object):
    """
    Stores the content of a vote, i.e. what to draw. Filled by the vote core
    and the vote engine.
    """
    def __init__(self, name, kind, candidates, electors,
                 subject=None, parameters=None):
        """
        Name/identifier of the vote
        """
        # Vote information
        self.name = name
        self.kind = kind
        self.candidates = tuple(candidates)
        self.electors = tuple(electors)
        self.subject = subject
        self.parameters = parameters.copy() if parameters else {}

        # Final results of the vote
        self.coup_d_etat = False
        self.results = tuple()

        # Round data
        self.round = {}
        self.rounds = []

        # Prepare the first round
        self.next_round()

    def __str__(self):
        """
        String representation
        """
        text = "Vote {0}, of kind {1}, in {2} rounds".format(
            self.name, self.kind, len(self.round))

        if self.subject:
            text = "{0}, about {1}".format(text, self.subject)

        if self.coup_d_etat:
            text = "{0} (Coup d'État !)".format(text)

        return text

    def next_round(self, candidates=None):
        """
        Prepare for next round

        :param candidates: Candidates for the next round
        """
        if candidates:
            candidates = tuple(candidates)

        # New round
        self.round = {'candidates': candidates or self.candidates,
                      'ballots': {},
                      'results': {},
                      'extra': []}
        self.rounds.append(self.round)

    def set_ballots(self, ballots):
        """
        Add raw ballots informations

        :param ballots: Ballots of the round
        """
        results = self.round['ballots'] = {}
        secret_id = 1

        for ballot in ballots:
            elector = ballot.get_elector()
            if elector is None:
                name = "<Secret-{0}>".format(secret_id)
                secret_id += 1
            else:
                name = str(elector)

            results[name] = {'for': ballot.get_for(),
                             'against': ballot.get_against()}

    def set_results(self, results):
        """
        Intermediate results computed by the vote engine: scoring has been
        applied for all candidates

        :param results: A Candidate -> Score dictionary
        :return: A sorted list of tuple (score, candidate)
        """
        # Make a sorted list of tuples: (votes, candidate)
        results = sorted(((item[1], item[0]) for item in results.items()),
                         key=operator.itemgetter(0), reverse=True)

        self.round['results'] = tuple(results)
        return results

    def set_vote_results(self, results):
        """
        Final results of the vote as seen by the core service after the engine
        finished its analysis.

        :param results: A list of elected candidates
        """
        if not isinstance(results, (list, tuple, set, frozenset)):
            # Single result
            self.results = tuple([results])
        else:
            # List of results
            self.results = tuple(results)

    def add_extra(self, title, values, kind='bar'):
        """
        Adds an extra information

        :param title: Title of the extra information
        :param values: A X -> Y dictionary
        :param kind: Kind of chart to draw
        """
        extra = {'title': title,
                 'kind': kind,
                 'values': values.copy()}
        self.round['extra'].append(extra)
