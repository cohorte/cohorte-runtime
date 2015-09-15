#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Associates each component to a language and to the bundle that provides its
factory

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

# Standard library
import logging

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate, BindField, UnbindField

# Cohorte
import cohorte.composer
import cohorte.repositories

# ------------------------------------------------------------------------------

# Module version
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

REQUIRED_REPOSITORIES = frozenset(('ipopo', 'ipojo'))
""" Kind of repositories required before providing the service """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.composer.SERVICE_COMPONENT_FINDER, controller="_controller")
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
        # Injected repositories
        self._repositories = []

        # Kinds of injected repositories
        self.__kinds = set()

        # Service controller
        self._controller = False

    @BindField('_repositories')
    def _bind_repository(self, field, svc, svc_ref):
        """
        A repository has been bound. Starts the timer to provide the service
        when most of repositories have been bound.
        """
        kind = svc_ref.get_property(cohorte.repositories.PROP_FACTORY_MODEL)
        self.__kinds.add(kind)

        # Check if we can provide the service,
        # i.e. if all repositories have been loaded
        self._controller = REQUIRED_REPOSITORIES.issubset(self.__kinds)

    @UnbindField
    def _unbind_repository(self, _, svc, svc_ref):
        """
        A repository has been unbound
        """
        kind = svc_ref.get_property(cohorte.repositories.PROP_FACTORY_MODEL)
        try:
            self.__kinds.remove(kind)
        except KeyError:
            # Unknown kind
            pass
        else:
            # Check if we need to stop providing the service
            self._controller = REQUIRED_REPOSITORIES.issubset(self.__kinds)

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
                bundle = repository.find_factory(
                    component.factory, component.bundle_name,
                    component.bundle_version)[0]
            except KeyError:
                # Not in this repository
                pass
            else:
                # Found
                break
        else:
            # Factory not found
            raise ValueError("Component factory not found: {0}"
                             .format(component.name))

        # Copy bundle information
        component.language = bundle.language
        component.bundle_name = bundle.name
        component.bundle_version = str(bundle.version)
        return bundle
