#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top Composer: Node Distributor

Clusters the components of a composition into groups according to several
criteria.

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

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# Composer
import cohorte.composer

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.composer.SERVICE_DISTRIBUTOR_NODE)
@Requires('_distance_criteria',
          cohorte.composer.SERVICE_TOP_CRITERION_DISTANCE, aggregate=True)
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
