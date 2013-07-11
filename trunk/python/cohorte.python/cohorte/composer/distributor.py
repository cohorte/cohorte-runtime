#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: Component distributor

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE
import cohorte.composer.core
import cohorte.composer.core.fsm as fsm
import cohorte.monitor
import cohorte.repositories

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Property, \
    Provides, Instantiate

# Standard library
import logging
import uuid

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-default-checker-factory")
@Provides([cohorte.composer.SERVICE_COMPATIBILITY_CHECKER,
           cohorte.composer.SERVICE_DISTANCE_CALCULATOR])
@Requires('_ratings', cohorte.composer.SERVICE_COMPONENT_RATING)
@Requires('_compatibilities', cohorte.composer.SERVICE_COMPONENT_COMPATIBILITY)
@Instantiate("cohorte-composer-default-checker")
class DefaultDistanceAndCompatibility(object):
    """
    Component providing a default computation of components distance and
    compatibility.
    """
    def __init__(self):
        """
        Sets up the component
        """
        self._compatibilities = None
        self._ratings = None


    def get_compatibility(self, compoA, compoB):
        """
        Checks the compatibility of two components using a compatibility matrix
        
        :param compoA: A component
        :param compoB: Another component
        :return: A compatibility level (0-100 or None)
        """
        return self._compatibilities.get(compoA, compoB)


    def get_distance(self, compoA, compoB):
        """
        Computes the distance between two components according to their
        configuration and ratings.
        
        :param compoA: A component
        :param compoB: Another component
        :return: A distance (0-100 or None)
        """
        # Components are too distant if they are configured to be in different
        # isolates or nodes
        for entry in ('isolate', 'node'):
            entryA = getattr(compoA, entry)
            entryB = getattr(compoB, entry)

            if entryA and entryB and entryA != entryB:
                # Different isolate or node
                return None

        if compoA.isolate and compoA.isolate == compoB.isolate:
            # Same isolate (distance 0)
            return 0

        # Distance by ratings
        ratingA = self._ratings.get(compoA.factory)
        ratingB = self._ratings.get(compoB.factory)
        return abs(ratingA - ratingB)


# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-core-distributor-factory")
@Provides(cohorte.composer.SERVICE_DISTRIBUTOR)
@Requires('_monitor', cohorte.monitor.SERVICE_MONITOR)
@Requires('_status', cohorte.composer.core.SERVICE_STATUS)
@Requires('_agents', 'cohorte.composer.Agent', aggregate=True, optional=True)
@Requires('_compatibility_checkers',
          cohorte.composer.SERVICE_COMPATIBILITY_CHECKER, aggregate=True)
@Requires('_distance_calculators', cohorte.composer.SERVICE_DISTANCE_CALCULATOR,
          aggregate=True)
@Requires('_repositories', cohorte.repositories.SERVICE_REPOSITORY_FACTORIES,
          aggregate=True)
@Property('_compatibility_threshold', 'threshold.compatibility', 50)
@Property('_distance_threshold', 'threshold.distance', 10)
@Instantiate('cohorte-composer-distributor')
class Distributor(object):
    """
    Clusters/Distributes components according to their ratings.
    Calls the monitor to create required isolates.
    Sends orders to detected agents.
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._monitor = None
        self._status = None
        self._agents = []
        self._compatibility_checkers = []
        self._distance_calculators = []
        self._repositories = []

        # Distribution threshold
        self._compatibility_threshold = 50
        self._distance_threshold = 10


    def instantiate(self, composition):
        """
        Instantiates a composition
        
        :param composition: A composition bean
        """
        # Normalize it
        if not composition.normalize():
            _logger.warning("Composition couldn't be normalized: "
                            "some components might not be validated")

        # Distribute & instantiate components
        components = list(composition.root.all_components())
        self.__instantiate_components(components)


    def redistribute(self, components):
        """
        Redistributes a subset of a composition
        
        :param components: The list of components to re-instantiate
        """
        self.__instantiate_components(components)


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

                    if min_compat < self._compatibility_threshold:
                        # Too low compatibility level: no binding authorized
                        return None

                # Check the maximum distance
                for calculator in self._distance_calculators:
                    distance = calculator.get_distance(componentA, componentB)
                    if distance is None:
                        # Can't compute a distance
                        return None

                    max_distance = max(distance, max_distance)
                    if max_distance > self._distance_threshold:
                        # Two components are too distant, therefore the clusters
                        # are too
                        return None

        return max_distance


    def _compute_kind(self, language):
        """
        Retrieves the kind of isolate associated to a component implementation
        language.
        
        :param language: An implementation language
        :return: The (kind, language) tuple needed to start this component
        """
        if language == "java":
            return ("osgi", language)

        elif language == "python":
            return ("pelix", language)

        else:
            return ("boot", language)


    def _distribute(self, components, nb_clusters=1):
        """
        Clustering algorithm, based on:
        
        "Hierarchical Clustering Schemes"
        Stephen C. Johnson (Bell Telephone Laboratories, 1967)
        Psychometrika, 2:241-254
    
        :param components: Components to distribute
        :param nb_clusters: Minimum number of clusters to obtain
        :return: The computed clusters (list of sets)
        :raise ValueError: Invalid parameter
        """
        if not components:
            return []

        if nb_clusters < 1:
            raise ValueError("Invalid number of clusters: {0}" \
                             .format(nb_clusters))


        # 1. Start by assigning each item to a cluster
        clusters = [{component} for component in components]
        count = len(clusters)

        while count > nb_clusters:
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

        return clusters


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


    def __instantiate_components(self, components):
        """
        Instantiates a set of components.
        Those components must have been normalized using the normalize() method
        of their composition (or root composite)
        
        :param components: A list of components
        """
        # 1st distribution: kinds (and artifacts handling)

        # (kind, language) -> [components]
        kinds = {}

        # Factory name -> Artifact
        artifacts = {}

        factories = set(component.factory for component in components)
        for repository in self._repositories:
            # Get the language of the repository
            language = repository.get_language()

            # Compute its kind
            kind = self._compute_kind(language)

            # Find the factories that are in this repository
            found, unresolved = repository.find_factories(factories)

            # Get the list of components for the found factories
            kinds[kind] = [component for component in components
                           if component.factory in found]

            # Add the factory artifact
            artifacts.update((factory, found[factory][0]) for factory in found)

            # Loop on unresolved factories
            factories = unresolved

        if factories:
            # Still some unresolved factories
            _logger.warning("Some factories have not been resolved: %s",
                            factories)

        # 2nd distribution: rules (per language)
        # Name -> (language, factories)
        groups = {}

        # Component -> Group name
        components_group = {}

        group_idx = 0
        for kind in kinds:
            # Distribute the components of the given language
            clusters = self._distribute(kinds[kind])

            for cluster in clusters:
                # Convert clusters into named groups
                for component in cluster:
                    if component.isolate:
                        # A component has an isolate name, use it
                        name = component.isolate
                        break
                else:
                    # Generate a name
                    group_idx += 1
                    name = "{0}-{1}-{2}".format(kind[0], kind[1], group_idx)

                group_factories = set()
                for component in cluster:
                    # Keep track of the factories needed in this group
                    group_factories.add(component.factory)

                    # Store the assigned isolate for the component
                    components_group[component] = name

                # Store the group pre-configuration
                groups[name] = (kind, group_factories)

        # Store the components in the status
        for component in components:
            # Make an identified instance of the component
            live_component = component.copy(str(uuid.uuid4()))

            # Add the new component to the status
            self._status.component_requested(live_component)
            self._status.component_event(live_component.uid,
                                         fsm.COMPONENT_EVENT_ASSIGNED)

            # Update the isolate information
            live_component.isolate = components_group.pop(component)

        # Start new isolates using the monitor
        # -> Events will trigger instantiations
        for name, data in groups.items():
            # Extract kind & language
            kind, language = data[0]
            factories = data[1]

            # TODO: compute the node
            node = None

            # Generate the isolate UID
            uid = str(uuid.uuid4())

            # Start the isolate
            self._monitor.start_isolate(name, node, kind, language, 'isolate',
                                        [artifacts[factory]
                                         for factory in factories], uid)
