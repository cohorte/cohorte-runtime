#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
iPOJO component factories repository

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
import logging
import threading

# Pelix
from pelix.ipopo.decorators import ComponentFactory, Provides, Invalidate, \
    Property, Requires, Validate

# Repository beans
import cohorte.repositories
from cohorte.repositories.beans import Factory

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

# iPOJO components description key
IPOJO_COMPONENTS_KEY = 'iPOJO-Components'

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


class Element(object):
    """
    iPOJO Metadata element
    """
    def __init__(self, name, namespace):
        """
        Sets up the element
        """
        # Name -> Attribute
        self.attributes = {}

        # Name -> [Elements]
        self.elements = {}

        # Element name
        self.name = name.lower()

        # Element name space
        if namespace:
            namespace = namespace.lower()
        self.namespace = namespace

    def __str__(self):
        """
        String representation
        """
        return self.to_pretty_string("", False)

    __repr__ = __str__

    def add_attribute(self, attribute):
        """
        Sets the value of an attribute of this element
        """
        self.attributes[attribute.get_qname()] = attribute

    def add_element(self, element):
        """
        Adds a child element
        """
        self.elements.setdefault(element.get_qname(), []).append(element)

    def get_attribute(self, name, namespace=None):
        """
        Retrieves the value of the given attribute, or None
        """
        if not namespace:
            qname = name
        else:
            qname = ':'.join((namespace, name))

        return self.attributes.get(qname, None)

    def get_elements(self, name, namespace=None):
        """
        Retrieves all elements with the given name or an empty array
        """
        if not namespace:
            qname = name
        else:
            qname = ':'.join((namespace, name))
        return self.elements.get(qname, [])

    def get_qname(self):
        """
        Returns the qualified name of the Element
        """
        if not self.namespace:
            return self.name
        return ':'.join((self.namespace, self.name))

    def to_pretty_string(self, prefix="", return_list=False, lines=None):
        """
        Pretty string representation
        """
        if prefix is None:
            prefix = ""

        if lines is None:
            lines = []

        lines.append('{0}{1} {{'.format(prefix, self.get_qname()))

        sub_prefix = prefix + "   "
        for attribute in self.attributes.values():
            lines.append("{0}{1}".format(sub_prefix, str(attribute)))

        for elements_list in self.elements.values():
            for element in elements_list:
                element.to_pretty_string(sub_prefix, True, lines)

        lines.append('{0}}}'.format(prefix))

        if return_list:
            return lines
        else:
            return '\n'.join(lines)

# ------------------------------------------------------------------------------


class Attribute(object):
    """
    Attribute of an iPOJO element
    """
    def __init__(self, name, value, namespace=None):
        """
        Sets up the attribute
        """
        self.name = name.lower()
        self.value = value

        if namespace:
            namespace = namespace.lower()
        self.namespace = namespace

    def get_qname(self):
        """
        Returns the qualified name of the Element
        """
        if not self.namespace:
            return self.name

        return ':'.join((self.namespace, self.name))

    def __str__(self):
        """
        String representation
        """
        return '${0}="{1}"'.format(self.get_qname(), self.value)

    def __repr__(self):
        """
        Object representation string
        """
        return "Attribute('{0}')".format(self.__str__())

# ------------------------------------------------------------------------------


def _parse_attribute(line, idx):
    """
    Parses an Element attribute

    :param line: The whole Manifest line
    :param idx: Index of the '$' starting the attribute name in the line
    :return: An (Attribute, idx) tuple, where idx is the index of final '"'
             of the attribute in the line.
    """
    # Beginning of an attribute
    att_name = ""
    att_value = ""
    att_ns = None

    idx += 1
    current = line[idx]
    while current != '=':
        if current == ':':
            att_ns = att_name
            att_name = ""

        else:
            att_name += current

        idx += 1
        current = line[idx]

    # Skip '="'
    idx += 2

    current = line[idx]
    while current != '"':
        att_value += current
        idx += 1
        current = line[idx]

    # Convert to an attribute object
    return Attribute(att_name, att_value, att_ns), idx


def parse_ipojo_line(line):
    """
    Parses the elements in the given line

    :param line: A manifest line
    :return: The root iPOJO Element
    """
    # Loop control
    line_len = len(line)
    idx = 0

    # Elements list, with the root element
    elements = [Element("iPOJO", None)]

    while idx < line_len:
        current = line[idx]
        if current == '$':
            # Beginning of an attribute
            attr, idx = _parse_attribute(line, idx)
            elements[-1].add_attribute(attr)

        elif current == '}':
            # End of the current element
            if len(elements) > 1:
                # Keep the root
                # Add the element as a child of the last one
                last = elements.pop()
                elements[-1].add_element(last)

        elif current != ' ':
            # (Spaces are ignored)
            # Default case
            qname = []

            while current != ' ' and current != '{':
                # Get the qualified name
                qname.append(current)
                idx += 1
                current = line[idx]

            # Convert the name into a string
            qname = ''.join(qname)

            current = line[idx + 1]
            while current == ' ' or current == '{':
                # Skip spaces and '{'
                # Look one step ahead, as idx will be incremented before
                # the next iteration
                idx += 1
                current = line[idx + 1]

            if ':' not in qname:
                element = Element(qname, None)

            else:
                namespace, name = qname.split(':', 1)
                element = Element(name, namespace)

            elements.append(element)

        idx += 1

    # Return the root element
    return elements[0]

# ------------------------------------------------------------------------------


@ComponentFactory("cohorte-repository-factories-ipojo-factory")
@Provides(cohorte.repositories.SERVICE_REPOSITORY_FACTORIES,
          controller="_controller")
@Requires('_repositories', cohorte.repositories.SERVICE_REPOSITORY_ARTIFACTS,
          True, False,
          "({0}=java)".format(cohorte.repositories.PROP_REPOSITORY_LANGUAGE))
@Property('_model', cohorte.repositories.PROP_FACTORY_MODEL, "ipojo")
@Property('_language', cohorte.repositories.PROP_REPOSITORY_LANGUAGE, "java")
class IPojoRepository(object):
    """
    Represents a repository
    """
    def __init__(self):
        """
        Sets up the repository
        """
        # Properties
        self._model = 'ipojo'
        self._language = 'java'

        # Service controller
        self._controller = False

        # Injected service
        self._repositories = []

        # Name -> [Factories]
        self._factories = {}

        # Artifact -> [Factories]
        self._artifacts = {}

        # Thread safety
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

    @staticmethod
    def _extract_bundle_factories(artifact):
        """
        Extracts the iPOJO factories definitions from the given artifact

        :param artifact: A Java Bundle artifact
        :return: A list of factories or None
        """
        # Get the parsed manifest
        if not hasattr(artifact, 'get_manifest'):
            # TODO: read the manifest from artifact.file
            manifest = None

        else:
            manifest = artifact.get_manifest()

        if not manifest:
            # No manifest in this bundle
            return

        # Get the iPOJO entry & parse it
        line = manifest.get(IPOJO_COMPONENTS_KEY)
        if not line:
            # No iPOJO entry
            return

        root = parse_ipojo_line(line)
        if not root:
            # Invalid iPOJO entry
            return

        # Extract all factories
        factories = set()
        for component in root.get_elements('component'):
            name_attr = component.get_attribute('name')
            if name_attr and name_attr.value not in factories:
                # New factory found
                factories.add(name_attr.value)

        return factories

    def add_artifact(self, artifact):
        """
        Adds the factories provided by the given artifact

        :param artifact: A Java Bundle artifact
        :raise ValueError: Unreadable file
        """
        with self.__lock:
            # Extract factories
            names = self._extract_bundle_factories(artifact)
            if not names:
                # No factory in this artifact
                return

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

        :param factories: A list of iPOJO factory names
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
                    self.add_artifact(artifact)

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
                         name="iPOJO-repository-loader").start()

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.clear()
