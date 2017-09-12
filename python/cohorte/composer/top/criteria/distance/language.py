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

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# Composer
import cohorte.composer
import cohorte.repositories

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.composer.SERVICE_TOP_CRITERION_DISTANCE)
@Requires('_repositories', cohorte.repositories.SERVICE_REPOSITORY_FACTORIES,
          aggregate=True)
@Instantiate('cohorte-composer-criterion-distance-language')
class LanguageCriterion(object):
    """
    Groups components by language
    """
    def __init__(self):
        """
        Sets up members
        """
        self._repositories = []

    @staticmethod
    def group(components, groups):
        """
        Groups components according to their implementation language

        :param components: List of components to group
        :param groups: Dictionary of current groups
        :return: A tuple:

                 * Dictionary of grouped components (group -> components)
                 * List of components that haven't been grouped
        """
        return {}, components
