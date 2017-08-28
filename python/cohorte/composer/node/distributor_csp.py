#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Node Composer: Isolate Distributor based on ``ortools``

Clusters the components of a composition into groups according to several
criteria.

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

# Standard library
import logging

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, \
    Instantiate

# OR-Tools Linear solver
from ortools.linear_solver import pywraplp as ortools

# Composer
from cohorte.composer.node.beans import EligibleIsolate
import cohorte.composer

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.composer.SERVICE_DISTRIBUTOR_ISOLATE)
@Instantiate('cohorte-composer-node-distributor')
class IsolateDistributor(object):
    """
    Clusters components into groups. Each group corresponds to an isolate.
    """
    def __init__(self):
        """
        Sets up members
        """
        # Number of calls to this distributor
        self._nb_distribution = 0

        # Names of components considered unstable
        self.__unstable = set()

    def distribute(self, components, existing_isolates):
        """
        Computes the distribution of the given components

        :param components: A list of RawComponent beans
        :param existing_isolates: A set of pre-existing eligible isolates
        :return: A tuple of tuples: updated and new EligibleIsolate beans
        """
        # Prepare the lists of updated and new isolates
        updated_isolates = set()
        new_isolates = set()

        # Create a map name -> isolate bean
        map_isolates = {isolate.name: isolate for isolate in existing_isolates}

        # 1. Predefined host isolates
        reserved_isolates = set()
        remaining = set()
        for component in components:
            if component.isolate:
                isolate_name = component.isolate
                reserved_isolates.add(isolate_name)
                try:
                    # Use existing bean
                    isolate = map_isolates[isolate_name]
                    isolate.add_component(component)
                    updated_isolates.add(isolate)
                except KeyError:
                    # Create a new bean
                    isolate = EligibleIsolate(component.isolate,
                                              component.language,
                                              [component])
                    map_isolates[isolate_name] = isolate
                    new_isolates.add(isolate)
            else:
                # Component must be treated afterwards
                remaining.add(component)

        # Hide reserved isolates
        for isolate_name in reserved_isolates:
            map_isolates.pop(isolate_name)

        # 2. Unstable components must be isolated
        # ... group remaining components by language
        remaining_stable = {}
        for component in remaining:
            if component.name in self.__unstable:
                # Component is known as unstable: isolate it
                isolate = EligibleIsolate(None, component.language,
                                          [component])
                new_isolates.add(isolate)
            else:
                # Store stable component, grouped by language
                remaining_stable.setdefault(component.language, set()) \
                    .add(component)

        for language, components in remaining_stable.items():
            # Gather components according to their compatibility
            updated, added = self.__csp_dist(map_isolates, components,
                                             language)
            updated_isolates.update(updated)
            new_isolates.update(added)

        # Return tuples of updated and new isolates beans
        return tuple(updated_isolates), tuple(new_isolates)

    def __csp_dist(self, map_isolates, components, language):
        """
        Gather components using OR-Tools

        :param map_isolates: A Name -> EligibleIsolate bean map
        :param components: Set of components to gather
        :param language: Implementation language of components
        :return: A tuple: (updated isolates, new isolates)
        """
        # Normalize entries (components and isolates)
        components_names = sorted(component.name for component in components)
        nb_components = len(components_names)
        isolates_names = sorted(map_isolates.keys())

        # Compute boundaries
        max_isolates = max(len(components_names), len(isolates_names)) + 1

        # Prepare the incompatibility matrix
        incompat_matrix = self.__make_incompatibility_matrix(components_names)

        # Prepare the problem solver
        solver = ortools.Solver("Components distribution",
                                ortools.Solver.CBC_MIXED_INTEGER_PROGRAMMING)

        # Declare variables
        # ... component on isolate (Iso_i <=> Iso_i_j = 1)
        iso = {}
        for i, name in enumerate(components_names):
            for j in range(max_isolates):
                iso[i, j] = solver.IntVar(0, 1, "{0} on {1}".format(name, j))

        # ... assigned isolates (for the objective)
        assigned_isolates = [solver.IntVar(0, 1, "Isolate {0}".format(i))
                             for i in range(max_isolates)]

        # ... number of isolates for a component (must be 1)
        nb_component_isolate = [solver.Sum(iso[i, j]
                                           for j in range(max_isolates))
                                for i in range(nb_components)]

        # ... number of components for an isolate
        nb_isolate_components = [solver.Sum(iso[i, j]
                                            for i in range(nb_components))
                                 for j in range(max_isolates)]

        # Constraints:
        # ... 1 isolate per component
        for i in range(nb_components):
            solver.Add(nb_component_isolate[i] == 1)

        # ... assigned isolates values must be updated
        for j in range(max_isolates):
            solver.Add(assigned_isolates[j] >=
                       nb_isolate_components[j] / nb_components)

        # ... Avoid incompatible components on the same isolate
        for i in range(len(incompat_matrix)):
            for j in range(max_isolates):
                # Pair on same isolate: sum = 2
                solver.Add(iso[incompat_matrix[i][0], j] +
                           iso[incompat_matrix[i][1], j] <=
                           assigned_isolates[j])

        # Define the objective: minimize the number of isolates
        nb_assigned_isolates = solver.Sum(assigned_isolates)
        solver.Minimize(nb_assigned_isolates)

        # Solve the problem
        solver.Solve()

        # Print results
        _logger.info("Number of isolates.: %s",
                     int(solver.Objective().Value()))
        _logger.info("Isolates used......: %s",
                     [int(assigned_isolates[i].SolutionValue())
                      for i in range(max_isolates)])

        for i in range(nb_components):
            for j in range(max_isolates):
                if int(iso[i, j].SolutionValue()) == 1:
                    break
            else:
                # No isolate associated ?
                j = None

            _logger.info("Component %s: Isolate %s", components_names[i], j)

        _logger.info("WallTime...: %s", solver.WallTime())
        _logger.info("Iterations.: %s", solver.Iterations())

        # TODO: Prepare result isolates
        updated_isolates = set()
        added_isolates = [EligibleIsolate(None, language, components)]
        return updated_isolates, added_isolates

    @staticmethod
    def __make_incompatibility_matrix(components_names):
        """
        Prepares the incompatibility matrix

        :param components_names: List of components names.
        :return: A sorted incompatibility matrix
        """
        # The incompatibility dictionary: component -> incompatible
        incompat = {'Component-A': ['Nemesis-A'],
                    'Component-B': ['Nemesis-B']}

        # Prepare the matrix (set of pairs)
        incompat_matrix = set()
        for name, incompat_names in incompat.items():
            idx_name = components_names.index(name)
            for incompat_name in incompat_names:
                try:
                    idx_incompat = components_names.index(incompat_name)
                    # Store a sorted tuple (hashable)
                    incompat_matrix.add(tuple(
                        sorted((idx_name, idx_incompat))))
                except ValueError:
                    # An incompatible component is not in the composition
                    pass

        # Return a sorted tuple or sorted tuples
        return tuple(sorted(incompat_matrix))

    @staticmethod
    def handle_event(event):
        """
        Handles a component/composition event

        :param event: The event to handle
        """
        # TODO: notify the crash and incompatibility stores
        pass
