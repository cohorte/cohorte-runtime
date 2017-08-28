#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Votes according to the composition configuration

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 3.0.0

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
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate

# Composer
import cohorte.composer
import cohorte.composer.node.beans as beans

# Vote
import cohorte.vote.beans as vote

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

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

    @staticmethod
    def handle_event(event):
        """
        Does nothing: this elector only cares about what is written in
        configuration files
        """
        pass

    @staticmethod
    def vote(candidates, subject, ballot):
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
