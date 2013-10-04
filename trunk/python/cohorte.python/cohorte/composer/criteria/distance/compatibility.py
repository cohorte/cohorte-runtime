#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top Composer: Compatibility distance criterion

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

INCOMPATIBLE = 1

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_CRITERION_DISTANCE)
@Requires('_compatibility_checkers',
          cohorte.composer.SERVICE_COMPATIBILITY_CHECKER, aggregate=True)
@Requires('_distance_calculators', cohorte.composer.SERVICE_DISTANCE_CALCULATOR,
          aggregate=True)
@Instantiate('cohorte-composer-criterion-distance-compatibility')
class CompatibilityCriterion(object):
    """
    Groups components by compatibility rating
    """
    def __init__(self):
        """
        Sets up members
        """
        self._compatibility_checkers = None
        self._distance_calculators = None


    def _clusters_distance(self, clusterA, clusterB):
        """
        Computes the "distance" between two clusters.
        Returns None if the clusters can't be bound together.

        :param clusterA: A cluster
        :param clusterB: Another cluster
        :return: The computed distance or None
        """
        # Maximum distance found
        max_distance = 0

        for componentA in clusterA:
            for componentB in clusterB:
                # Check the minimum compatibility level
                min_compat = None
                for checker in self._compatibility_checkers:
                    compat = checker.get_compatibility(componentA, componentB)
                    if compat is None:
                        # Incompatible components
                        return None

                    elif min_compat is None or compat < min_compat:
                        # Update the minimal compatibility level
                        min_compat = compat

                    if min_compat == INCOMPATIBLE:
                        # Too low compatibility level: no binding authorized
                        return None

                # Check the maximum distance
                for calculator in self._distance_calculators:
                    distance = calculator.get_distance(componentA, componentB)
                    if distance is None:
                        # Can't compute a distance
                        return None

                    max_distance = max(distance, max_distance)
                    if max_distance == INCOMPATIBLE:
                        # Two components are too distant, therefore the clusters
                        # are too
                        return None

        return max_distance


    def _find_closest_clusters(self, clusters):
        """
        Finds the closest pair of clusters

        :param clusters: A list of clusters
        :return: The closest pair (tuple), or None
        """
        min_couple = None
        min_distance = None

        i = 0
        for clusterA in clusters:
            i += 1
            for clusterB in clusters[i:]:
                distance = self._clusters_distance(clusterA, clusterB)
                if (distance is not None and
                    (min_distance is None or distance < min_distance)):
                    # Found sufficiently close clusters
                    min_couple = (clusterA, clusterB)
                    min_distance = distance

        return min_couple


    def group(self, components, groups):
        """
        Groups components according to their implementation language

        :param components: List of components to group
        :param groups: Dictionary of current groups
        :return: A tuple:

                 * Dictionary of grouped components (group -> components)
                 * List of components that haven't been grouped
        """
        # 1. Start by assigning each item to a cluster
        clusters = [{component} for component in components]
        count = len(clusters)

        while count > 1:
            # 2. Find the closest pair of clusters
            pair = self._find_closest_clusters(clusters)
            if not pair:
                # No more pair available
                break

            # Merge'em in A (and remove B)
            clusterA, clusterB = pair
            clusterA.update(clusterB)
            clusters.remove(clusterB)
            count -= 1

        return (dict(("compatibility-{0}".format(idx), cluster)
                      for idx, cluster in enumerate(clusters)), [])
