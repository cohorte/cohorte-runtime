#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Python modules repository utility module

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# Repository beans
import cohorte.repositories
from cohorte.repositories.beans import Factory

# Pelix
from pelix.utilities import is_string
from pelix.ipopo.decorators import ComponentFactory, Provides, Invalidate, \
    Property, Requires

# Standard library
import ast
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

class ComponentFactoryVisitor(ast.NodeVisitor):
    """
    AST visitor to extract imports and version
    """
    def __init__(self):
        """
        Sets up the visitor
        """
        ast.NodeVisitor.__init__(self)
        self.factories = set()
        self.values = {}


    def visit_ClassDef(self, node):
        """
        Found a class definition
        """
        for decorator in node.decorator_list:
            if decorator.func.id == "ComponentFactory":
                argument = decorator.args[0]
                if hasattr(argument, 'id'):
                    # Constant
                    try:
                        self.factories.add(self.values[argument.id])

                    except KeyError:
                        _logger.warning("Factory name '%s' is unknown (%s)",
                                        argument.id, node.name)

                else:
                    # Literal
                    try:
                        self.factories.add(ast.literal_eval(argument))

                    except (ValueError, SyntaxError) as ex:
                        _logger.warning("Invalid factory name for class %s: %s",
                                        node.name, ex)


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

    visitor.visit(module)
    return visitor.factories

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-repository-factories-python")
@Provides(cohorte.repositories.SERVICE_REPOSITORY_FACTORIES)
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

        # Injected service
        self._repositories = []

        # Name -> [Factories]
        self._factories = {}

        # Artifact -> [Factories]
        self._artifacts = {}


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


    def add_artifact(self, artifact):
        """
        Adds the factories provided by the given artifact
        
        :param artifact: A Python Module artifact
        :raise ValueError: Unreadable file
        """
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


    def load_repositories(self):
        """
        Loads the factories according to the repositories
        """
        if not self._repositories:
            # No repository
            return

        # Walk through artifacts
        for repository in self._repositories:
            for artifact in repository.walk():
                self.add_artifact(artifact)


    def clear(self):
        """
        Clears the repository content
        """
        self._artifacts.clear()
        self._factories.clear()


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.clear()
