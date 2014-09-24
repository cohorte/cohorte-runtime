#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Voting system implementation

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

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

SERVICE_VOTE_CORE = 'cohorte.vote.core'
"""
Specification of the core election service, providing:

* get_kinds() -> tuple(str)

  Returns the list of supported kinds of votes

* vote(candidates: [object], kind: str, params: dict)

  Runs an election of the given kind, with given parameters
"""

SERVICE_VOTE_ENGINE = 'cohorte.vote.engine'
"""
Specification of a vote engine, providing:

* get_kind() -> str

  Returns the kind of vote this engine implements

* get_options() -> dict(str: str)

  Returns a dictionary description all the options supported by this engine
  (option -> description)

* analyze(ballots: tuple(Ballot), params: dict) -> Kind- and
  parameters-dependent result

  Analyzes the ballots of a vote and returns a kind- and parameters-dependent
  result. Raises a NextTurn exception if it requires a new turn.
"""

SERVICE_VOTE_CARTOONIST = 'cohorte.vote.cartoonist'
"""
Specification of a chart cartoonist
"""

SERVICE_VOTE_STORE = 'cohorte.vote.store'
"""
Storage for votes (debug, ...)
"""

# ------------------------------------------------------------------------------

PROP_VOTE_KIND = 'vote.kind'
"""
Kind of vote provided by the vote engine
"""
