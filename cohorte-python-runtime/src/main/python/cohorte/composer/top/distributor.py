#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top Composer: Node Distributor

Clusters the components of a composition into groups according to several
criteria.

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

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_DISTRIBUTOR_NODE)
@Requires('_distance_criteria', cohorte.composer.SERVICE_TOP_CRITERION_DISTANCE,
          aggregate=True)
@Instantiate('cohorte-composer-top-distributor')
class NodeDistributor(object):
    """
    Clusters components into groups. Each group corresponds to a node.
    """
    def __init__(self):
        """
        Sets up members
        """
        # Distance criteria
        self._distance_criteria = []


    def distribute(self, composition):
        """
        Computes the distribution of the components of the given composition

        :param composition: A RawComposition bean
        :return: A dictionary: Node name -> set(RawComponent)
        """
        return self.redistribute(composition.all_components())


    def redistribute(self, components):
        """
        Computes the distribution of the given components

        :param components: A list of RawComponent beans
        :return: A dictionary: Node name -> set(RawComponent)
        """
        groups = {}

        not_grouped = list(components)
        for criterion in self._distance_criteria[:]:
            # Group components
            grouped, not_grouped = criterion.group(not_grouped, groups)

            # Update the distribution
            for group, group_components in grouped.items():
                groups.setdefault(group, set()).update(group_components)

        if not_grouped:
            # Some components have not been grouped: use the "undefined" group
            groups.setdefault(None, set()).update(not_grouped)

        return groups
