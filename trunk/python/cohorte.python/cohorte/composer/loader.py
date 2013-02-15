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
import cohorte.composer.core.events as events
import cohorte.monitor

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Provides, Property

# Standard library
import logging

# ------------------------------------------------------------------------------

AUTORUN_COMPOSITION = "autorun_conf.js"
""" Auto-run composition file """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

def __make_kind_filter(kind):
    """
    Makes a filter string to obtain a rule engine for the given kind of event
    """
    return "({0}={1})".format(cohorte.composer.core.PROP_ENGINE_KIND, kind)

# ------------------------------------------------------------------------------

@ComponentFactory('cohorte-composer-loader-factory')
@Provides(cohorte.composer.SERVICE_COMPOSITION_LOADER)
@Requires('_parser', cohorte.composer.SERVICE_COMPOSITION_PARSER)
@Requires('_distributor', cohorte.composer.core.SERVICE_RULE_ENGINE,
          spec_filter=__make_kind_filter("distribution"))
@Requires('_status', cohorte.composer.core.SERVICE_STATUS)
@Requires('_monitor', cohorte.monitor.SERVICE_MONITOR)
@Property('_threshold', 'compatibility.threshold', 10)
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
        self._distributor = None
        self._monitor = None
        self._parser = None
        self._status = None

        # Distribution threshold
        self._threshold = 10


    def parse(self, filename):
        """
        Parses a composition file
        
        :param filename: Name of the composition file
        :return: The parsed composition (or None)
        """
        return self._parser.load(filename)


    def _find_next_subgroup(self, group):
        """
        """
        subgroup = []

        idx = 0
        while idx < len(group):
            if group[idx] < self._threshold:
                subgroup.append(idx)

            idx += 1

        return subgroup


    def instantiate(self, composition):
        """
        Instantiates a composition
        
        :param composition: A composition bean
        """
        # Normalize it
        if not composition.normalize():
            _logger.warning("Composition couldn't be normalized: "
                            "some component might not be validated")

        # TODO: compute languages

        # 1st distribution: languages
        languages = {}
        for component in composition.all_components():
            language = component.language
            if not language:
                # Normalize none/empty string
                language = None

            # Store the component
            languages.setdefault(language, []).append(component)

        # 2nd distribution: rules (per language)
        # Name -> (language, factories)
        groups = {}
        group_idx = 0
        for language in languages:
            # Make the matrix for the components of this language
            size = len(language)
            matrix = [[0] * size] * size

            next_idx = 0
            for component in language:
                # Compute from the following component
                idx = next_idx
                next_idx += 1

                for i in range(next_idx, size):
                    # Check the compatibility between each components
                    event = events.MatchEvent(composition,
                                              component, language[i])

                    # Compatibility level: >= 0
                    compatibility = self._distributor.handle(event)

                    # Store the result
                    matrix[idx][i] = matrix[i][idx] = compatibility

            # Make the groups
            while len(matrix) != 0:
                indexes = self._find_next_subgroup(matrix[0])

                # Extract group components
                group_idx += 1
                name = "{0}-{1}".format(language, group_idx)
                groups[name] = (language,
                                [language[idx].factory for idx in indexes])

                # Remove them from the list and matrix
                indexes.reverse()
                for idx in indexes:
                    # Remove the component from the language distribution
                    del language[idx]

                    # Remove the component from the matrix
                    del matrix[idx]
                    for line in matrix:
                        del line[idx]

        # Store the components in the status
        self._status.add_composition(composition)
        for component in component.all_components():
            # Make an identified instance of the component
            self._status.component_requested(component)


        # Start new isolates using the monitor
        # -> Events will trigger instantiations
        for name, info in groups:
            # TODO: compute kind before (or more nicely)
            language = info[0]
            if language == 'java':
                kind = 'osgi'
            elif language == 'python':
                kind = "pelix"
            else:
                kind = language

            # TODO: compute the node
            node = None

            # Start the isolate
            self._monitor.start_isolate(name, node, kind, language, 'isolate',
                                        factories=info[1])


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
        composition = self.load(AUTORUN_COMPOSITION)
        if composition is not None:
            # Composition loaded: instantiate it
            self.instantiate(composition)

