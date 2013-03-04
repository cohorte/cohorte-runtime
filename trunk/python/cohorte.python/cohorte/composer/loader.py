#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: Composition loader

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
import cohorte.monitor

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Provides, Property

# Standard library
import logging
import uuid

# ------------------------------------------------------------------------------

AUTORUN_COMPOSITION = "autorun_conf.js"
""" Auto-run composition file """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-default-checker-factory")
@Provides([cohorte.composer.SERVICE_COMPATIBILITY_CHECKER,
           cohorte.composer.SERVICE_DISTANCE_CALCULATOR])
@Requires('_ratings', cohorte.composer.SERVICE_COMPONENT_RATING)
@Requires('_compatibilities', cohorte.composer.SERVICE_COMPONENT_COMPATIBILITY)
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
            # Same isolate
            return 100

        # Distance by ratings
        ratingA = self._ratings.get(compoA)
        ratingB = self._ratings.get(compoB)
        return abs(ratingA - ratingB)

# ------------------------------------------------------------------------------

@ComponentFactory('cohorte-composer-loader-factory')
@Provides(cohorte.composer.SERVICE_COMPOSITION_LOADER)
@Requires('_parser', cohorte.composer.SERVICE_COMPOSITION_PARSER)
@Requires('_compatibility_checkers',
          cohorte.composer.SERVICE_COMPATIBILITY_CHECKER, aggregate=True)
@Requires('_distance_calculators', cohorte.composer.SERVICE_DISTANCE_CALCULATOR,
          aggregate=True)
@Requires('_status', cohorte.composer.core.SERVICE_STATUS)
@Requires('_monitor', cohorte.monitor.SERVICE_MONITOR)
@Property('_compatibility_threshold', 'threshold.compatibility', 50)
@Property('_distance_threshold', 'threshold.distance', 10)
class CompositionLoader(object):
    """
    Composition loading service.
    Reads the autorun_conf.js file when validated and sets up the parsed
    composition.
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._monitor = None
        self._parser = None
        self._status = None
        self._compatibility_checkers = []
        self._distance_calculators = []

        # Distribution threshold
        self._compatibility_threshold = 50
        self._distance_threshold = 10


    def parse(self, filename):
        """
        Parses a composition file
        
        :param filename: Name of the composition file
        :return: The parsed composition (or None)
        """
        return self._parser.load(filename, 'conf')


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
                        # Too distant components
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
        clusters = [set([component]) for component in components]
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


    def _compute_kind(self, component):
        """
        Computes the kind of a component
        
        :param component: A component bean
        :return: The (language, kind) of configuration needed to start this
                 component
        """
        language = component.language
        if language == "java":
            return ("osgi", language)

        elif language == "python":
            return ("pelix", language)

        else:
            return ("boot", language)


    def instantiate(self, composition):
        """
        Instantiates a composition
        
        :param composition: A composition bean
        """
        # Normalize it
        if not composition.normalize():
            _logger.warning("Composition couldn't be normalized: "
                            "some component might not be validated")

        # 1st distribution: kinds
        kinds = {}
        for component in composition.root.all_components():
            # Store the component
            kind = self._compute_kind(component)
            kinds.setdefault(kind, []).append(component)

        # TODO: compute languages (from language from factory) of kinds[None]

        # 2nd distribution: rules (per language)
        # Name -> (language, factories)
        groups = {}
        group_idx = 0
        for kind in kinds:
            # Distribute the components of the given language
            clusters = self._distribute(kinds[kind])

            for cluster in clusters:
                # Convert clusters into named groups
                group_idx += 1
                name = "{0}-{1}-{2}".format(kind[0], kind[1], group_idx)
                groups[name] = (kind,
                                set([component.factory
                                     for component in cluster]))

        # Store the components in the status
        self._status.add_composition(composition)
        for component in composition.root.all_components():
            # Make an identified instance of the component
            _logger.debug("Requesting... %s", component.name)
            self._status.component_requested(component.copy(uuid.uuid4()))

        # Start new isolates using the monitor
        # -> Events will trigger instantiations
        for name, data in groups.items():
            # Extract kind & language
            kind, language = data[0]
            factories = data[1]

            # TODO: compute the node
            node = None

            # Start the isolate
            self._monitor.start_isolate(name, node, kind, language, 'isolate',
                                        factories=factories)


    def stop(self, uid):
        """
        Stops the composition with the given UID
        
        :param uid: A composition UID
        """
        # TODO: send stop orders
        # TODO: wait for components to stop

        # TODO: clear the status
        pass


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        try:
            composition = self.parse(AUTORUN_COMPOSITION)
            if composition is not None:
                # Composition loaded: instantiate it
                self.instantiate(composition)

        except (ValueError, IOError) as ex:
            _logger.error("Error loading the auto-run composition: %s", ex)
