#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Vote by configuration

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 3.0.0

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
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

# Composer
import cohorte.composer
import cohorte.composer.node.beans as beans

# Vote
import cohorte.vote.beans as vote

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_NODE_CRITERION_DISTANCE)
@Instantiate('cohorte-composer-node-criterion-configuration')
class ConfigurationIsolateCriterion(object):
    """
    Vote for the isolate that will host a component according to the
    configuration
    """
    def __str__(self):
        """
        String representation
        """
        return "Configuration"


    def handle_event(self, event):
        """
        Does nothing: this elector only cares about what is written in
        configuration files
        """
        pass


    def vote(self, candidates, subject, ballot):
        """
        Votes for the isolate(s) with the minimal compatibility distance

        :param candidates: Isolates to vote for
        :param subject: The component to place
        :param ballot: The vote ballot
        """
        # Get the configured isolate
        isolate = subject.isolate
        if not isolate:
            # No forced isolate: blank vote
            return

        found_match = False
        for candidate in candidates:
            if candidate.name == isolate:
                # Found the corresponding isolate
                raise vote.CoupdEtat(candidate)

            elif not candidate.name and candidate.propose_rename(isolate):
                # No name yet, same language and renaming accepted
                ballot.append_for(candidate)
                found_match = True

            else:
                # Wrong isolate: vote against it
                ballot.append_against(candidate)

        if not found_match:
            # Not found, create a new isolate
            # (it will be configured by the node distributor)
            raise vote.CoupdEtat(beans.EligibleIsolate(isolate))
