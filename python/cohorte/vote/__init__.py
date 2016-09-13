#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Voting system implementation

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
