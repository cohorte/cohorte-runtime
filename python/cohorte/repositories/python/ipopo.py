#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
iPOPO component factories repository

:author: Thomas Calmant
:license: Apache Software License 2.0

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
import ast
import logging
import threading

# Pelix
from pelix.utilities import is_string
from pelix.ipopo.decorators import ComponentFactory, Provides, Invalidate, \
    Property, Requires, Validate

# Repository beans
import cohorte.repositories
from cohorte.repositories.beans import Factory

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


class ComponentFactoryVisitor(ast.NodeVisitor):
    """
    AST visitor to extract imports and version
    """
    # pylint: disable=invalid-name
    def __init__(self):
        """
        Sets up the visitor
        """
        ast.NodeVisitor.__init__(self)
        self.factories = set()
        self.values = {}

    def generic_visit(self, node):
        """
        Custom default visit method that avoids to visit further that the
        module level.
        """
        if type(node) is ast.Module:
            ast.NodeVisitor.generic_visit(self, node)

    def visit_ClassDef(self, node):
        """
        Found a class definition
        """
        for decorator in node.decorator_list:
            try:
                if decorator.func.id != "ComponentFactory":
                    # Not a ComponentFactory decorator
                    continue
            except AttributeError:
                # Not our kind of decorator
                pass
            else:
                name = None
                if decorator.args:
                    # Name: First argument
                    argument = decorator.args[0]
                else:
                    argument = None
                    if hasattr(decorator, 'kwargs'):
                        # Before Python 3.5
                        if decorator.kwargs:
                            argument = decorator.kwargs.get('name')
                    elif hasattr(decorator, 'keywords'):
                        # Python 3.5: kwargs dictionary replaced by a list
                        # of keywords
                        for keyword in decorator.keywords:
                            if keyword.arg == 'name':
                                argument = keyword.value

                    if not argument:
                        # Default name
                        name = "{0}Factory".format(node.name)

                if name is None:
                    if hasattr(argument, 'id'):
                        # Constant
                        try:
                            name = self.values[argument.id]
                        except KeyError:
                            _logger.debug("Factory name '%s' is unknown (%s)",
                                          argument.id, node.name)
                    else:
                        # Literal
                        try:
                            name = ast.literal_eval(argument)
                        except (ValueError, SyntaxError) as ex:
                            _logger.debug(
                                "Invalid factory name for class %s: %s",
                                node.name, ex)

                if name is not None:
                    # Store the factory name
                    self.factories.add(name)

    def visit_Assign(self, node):
        """
        Found an assignment
        """
        field = getattr(node.targets[0], 'id', None)
        if field:
            try:
                value = ast.literal_eval(node.value)
                if is_string(value):
                    self.values[field] = value
            except (ValueError, SyntaxError):
                # Ignore errors
                pass


def _extract_module_factories(filename):
    """
    Extract the version and the imports from the given Python file

    :param filename: Path to the file to parse
    :return: A (version, [imports]) tuple
    :raise ValueError: Unreadable file
    """
    visitor = ComponentFactoryVisitor()
    try:
        with open(filename) as filep:
            source = filep.read()
    except (OSError, IOError) as ex:
        raise ValueError("Error reading {0}: {1}".format(filename, ex))

    try:
        module = ast.parse(source, filename, 'exec')
    except (ValueError, SyntaxError) as ex:
        raise ValueError("Error parsing {0}: {1}".format(filename, ex))

    try:
        visitor.visit(module)
    except Exception as ex:
        raise ValueError("Error visiting {0}: {1}".format(filename, ex))

    return visitor.factories

# ------------------------------------------------------------------------------


@ComponentFactory("cohorte-repository-factories-ipopo-factory")
@Provides(cohorte.repositories.SERVICE_REPOSITORY_FACTORIES,
          controller="_controller")
@Requires('_repositories', cohorte.repositories.SERVICE_REPOSITORY_ARTIFACTS,
          True, False,
          "({0}=python)".format(cohorte.repositories.PROP_REPOSITORY_LANGUAGE))
@Property('_model', cohorte.repositories.PROP_FACTORY_MODEL, "ipopo")
@Property('_language', cohorte.repositories.PROP_REPOSITORY_LANGUAGE, "python")
class IPopoRepository(object):
    """
    Represents a repository
    """
    def __init__(self):
        """
        Sets up the repository
        """
        # Properties
        self._model = 'ipopo'
        self._language = 'python'

        # Service controller
        self._controller = False

        # Injected service
        self._repositories = []

        # Name -> [Factories]
        self._factories = {}

        # Artifact -> [Factories]
        self._artifacts = {}

        # Some locking
        self.__lock = threading.RLock()

    def __contains__(self, item):
        """
        Tests if the given item is in the repository

        :param item: Item to be tested
        :return: True if the item is in the repository
        """
        if isinstance(item, Factory):
            # Test artifact model
            if item.model != self._model:
                return False

            # Test if the name is in the factories
            return item.name in self._factories

        elif item in self._factories:
            # Item matches a factory name
            return True

        # No match
        return False

    def __len__(self):
        """
        Length of a repository <=> number of individual factories
        """
        return sum((len(factories) for factories in self._factories.values()))

    def add_artifact(self, artifact):
        """
        Adds the factories provided by the given artifact

        :param artifact: A Python Module artifact
        :raise ValueError: Unreadable file
        """
        with self.__lock:
            # Extract factories
            names = _extract_module_factories(artifact.file)
            artifact_list = self._artifacts.setdefault(artifact, [])
            for name in names:
                # Make the bean
                factory = Factory(name, self._language, self._model, artifact)

                # Factory
                factory_list = self._factories.setdefault(name, [])
                if factory not in factory_list:
                    factory_list.append(factory)

                # Artifact
                if factory not in artifact_list:
                    artifact_list.append(factory)

    def clear(self):
        """
        Clears the repository content
        """
        with self.__lock:
            self._artifacts.clear()
            self._factories.clear()

    def find_factories(self, factories):
        """
        Returns the list of artifacts that provides the given factories

        :param factories: A list of iPOPO factory names
        :return: A tuple ({Name -> [Artifacts]}, [Not found factories])
        """
        with self.__lock:
            factories_set = set(factories)
            resolution = {}
            unresolved = set()

            if not factories:
                # Nothing to do...
                return resolution, factories_set

            for name in factories_set:
                try:
                    # Get the list of factories for this name
                    factories = self._factories[name]
                    providers = resolution.setdefault(name, [])
                    providers.extend(factory.artifact for factory in factories)
                except KeyError:
                    # Factory name not found
                    unresolved.add(name)

            # Sort the artifacts
            for artifacts in resolution.values():
                artifacts.sort(reverse=True)

            return resolution, unresolved

    def find_factory(self, factory, artifact_name=None, artifact_version=None):
        """
        Find the artifacts that provides the given factory, filtered by name
        and version.

        :return: The list of artifacts providing the factory, sorted by name
                 and version
        :raise KeyError: Unknown factory
        """
        with self.__lock:
            # Copy the list of artifacts for this factory
            artifacts = [factory.artifact
                         for factory in self._factories[factory]]

            if artifact_name is not None:
                # Artifact must be selected
                # Prepare the version bean
                version = cohorte.repositories.beans.Version(artifact_version)

                # Filter results
                artifacts = [artifact for artifact in artifacts
                             if artifact.name == artifact_name and
                             version.matches(artifact.version)]

                if not artifacts:
                    # No match found
                    raise KeyError("No matching artifact for {0} -> {1} {2}"
                                   .format(factory, artifact_name, version))

            # Sort results
            artifacts.sort(reverse=True)
            return artifacts

    def get_language(self):
        """
        Retrieves the language of the artifacts stored in this repository
        """
        return self._language

    def get_model(self):
        """
        Retrieves the component model that can handle the factories of this
        repository
        """
        return self._model

    def load_repositories(self):
        """
        Loads the factories according to the repositories
        """
        with self.__lock:
            if not self._repositories:
                # No repository
                return

            # Walk through artifacts
            for repository in self._repositories:
                for artifact in repository.walk():
                    try:
                        self.add_artifact(artifact)
                    except ValueError as ex:
                        # Log the exception instead of stopping here
                        _logger.warning("Error reading artifact: %s",
                                        ex, exc_info=True)

    def __initial_loading(self):
        """
        Initial repository loading
        """
        self.load_repositories()
        self._controller = True

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._controller = False

        # Load repositories in another thread
        threading.Thread(target=self.__initial_loading,
                         name="iPOPO-repository-loader").start()

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.clear()
