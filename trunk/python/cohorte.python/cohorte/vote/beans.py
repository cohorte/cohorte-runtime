#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Voting system beans

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
        if candidate not in self.__against and candidate not in self._for:
            self.__for.append(candidate)


    def append_against(self, candidate):
        """
        The elector votes against the given candidate

        :param candidate: One of the candidates
        """
        if candidate not in self.__against and candidate not in self._for:
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

# ------------------------------------------------------------------------------

class SecretBallot(Ballot):
    """
    Secret ballot: no one can tell who voted
    """
    def __init__(self, elector=None):
        """
        Sets up members

        :param elector: Unused optional parameter, to support Ballot constructor
                        signature
        """
        super(SecretBallot, self).__init__(None)
