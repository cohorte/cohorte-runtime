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
import cohorte.utils.vote as vote

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
    def vote(self, component, eligibles):
        """
        Prepares a coup d'État if the isolate that must host the given component
        has been forced in the configuration

        :param component: A Component bean
        :param eligibles: A set of Isolate beans
        """
        if not component.isolate:
            # No isolate configured: blank vote
            return

        isolate = component.isolate
        language = component.language

        for eligible in eligibles:
            candidate = eligible.candidate

            if candidate.name == isolate:
                # Same name
                if candidate.language not in (None, language):
                    # Incompatible language ?
                    # FIXME: an exception should be raised here (like before)
                    # => Simple warning due to python/python3 naming
                    _logger.warning("Possible language incompatibility for " \
                                    "%s (%s) on isolate %s (%s)",
                                    component.name, language,
                                    isolate, candidate.language)

                # Found the corresponding isolate
                raise vote.CoupdEtat(candidate)

            elif candidate.name is None \
            and candidate.language in (None, language) \
            and candidate.propose_rename(isolate):
                # No name yet, same language and renaming accepted
                eligible.vote()

        else:
            # Not found, create a new isolate
            # (it will be configured by the node distributor)
            raise vote.CoupdEtat(beans.EligibleIsolate(component.isolate))
