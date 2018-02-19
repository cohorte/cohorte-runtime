#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top Composer: Group by configuration

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
import cohorte
import cohorte.composer
import cohorte.version
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate


# Composer
# ------------------------------------------------------------------------------
# Bundle version
__version__ = cohorte.version.__version__

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.composer.SERVICE_TOP_CRITERION_DISTANCE)
@Requires('_configuration', cohorte.SERVICE_CONFIGURATION_READER)
@Instantiate('cohorte-composer-criterion-distance-configuration')
class ConfigurationCriterion(object):
    """
    Groups components by configuration
    """
    def __init__(self):
        """
        Sets up members
        """
        self._configuration = None

    def _get_isolate_node(self, isolate_name):
        """
        Reads the configuration of the given isolate and returns the specified
        node name, or None

        :param isolate_name: Name of an isolate
        :return: A node name or None
        """
        try:
            # Read the configuration, without logging file errors
            config = self._configuration.read("{0}.js".format(isolate_name),
                                              False)
           
            # Return the indicated node
            return config.get('node')

        except Exception:
            # Ignore I/O error: the isolate has no specific configuration   
            try:
                config = self._configuration.read("isolate_{0}.js".format(isolate_name),
                                                  False)
                # Return the indicated node
                return config.get('node') if not config else None
            except:
                pass

    def group(self, components, groups):
        """
        Groups components according to their implementation language

        :param components: List of components to group
        :param groups: Dictionary of current groups
        :return: A tuple:

                 * Dictionary of grouped components (group -> components)
                 * List of components that haven't been grouped
        """
        nodes = {}
        isolate_nodes = {}
        remaining = set(components)

        for component in components:
            node = None
            if component.node:
                # Explicit node
                node = component.node

            elif component.isolate:
                # Explicit isolate
                try:
                    node = isolate_nodes[component.isolate]

                except KeyError:
                    # Look for the node associated to the isolate
                    node = self._get_isolate_node(component.isolate)

                    # Store the information
                    isolate_nodes[component.isolate] = node

            if node:
                # Found a node
                nodes.setdefault(node, set()).add(component)
                remaining.remove(component)

        # Return the result
        return nodes, remaining
