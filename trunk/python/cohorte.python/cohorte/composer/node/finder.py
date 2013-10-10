#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
The Node Composer

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

# Cohorte
import cohorte.composer
import cohorte.repositories

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

# ------------------------------------------------------------------------------

@ComponentFactory()
@Provides(cohorte.composer.SERVICE_COMPONENT_FINDER)
@Requires('_repositories', cohorte.repositories.SERVICE_REPOSITORY_FACTORIES,
          aggregate=True)
@Instantiate('cohorte-composer-node-finder')
class ComponentFinder(object):
    """
    Looks for the source bundle of components
    """
    def __init__(self):
        """
        Sets up members
        """
        self._repositories = []


    def normalize(self, component):
        """
        Adds missing information and corrects others in the given component.
        Component bean is modified in-place.

        :param component: A RawComponent bean
        :return: The Bundle bean providing the component
        :raise ValueError: Component factory not available
        """
        # Filter repositories
        if component.language is not None:
            # Language given, filter repositories
            repositories = {repository for repository in self._repositories
                            if repository.get_language() == component.language}

        else:
            repositories = set(self._repositories)

        for repository in repositories:
            try:
                # Get the first found bundle
                bundle = repository.find_factory(component.factory,
                                                 component.bundle_name,
                                                 component.bundle_version)[0]
                break

            except KeyError:
                # Not in this repository
                pass

        else:
            # Factory not found
            raise ValueError("Component factory not found: {0}" \
                             .format(component.name))

        # Copy bundle information
        component.language = bundle.language
        component.bundle_name = bundle.name
        component.bundle_version = str(bundle.version)

        return bundle
