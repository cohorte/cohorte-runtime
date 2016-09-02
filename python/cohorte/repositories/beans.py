#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Classes and constants for the representation of repositories

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
import os

try:
    # Python 3
    # pylint: disable=F0401,E0611
    import urllib.request as urllib
    import urllib.parse as urlparse
except ImportError:
    # Python 2
    # pylint: disable=F0401
    import urlparse
    import urllib

# Pelix utilities
from pelix.utilities import is_string

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------


class Artifact(object):
    """
    Basic representation of an artifact, i.e. a bundle (Java),
    a module (Python), ...
    """
    def __init__(self, language, name, version=None, filename=None):
        """
        Sets up the artifact

        :param language: Language of implementation of the artifact (mandatory)
        :param name: Name of the artifact (will be file name if empty)
        :param version: Version of the artifact (optional)
        :param filename: Path to the artifact file (optional)
        :raise ValueError: Invalid parameter
        """
        # Validate parameters
        if not language:
            raise ValueError("Artifact language can't be empty")

        if not name and not filename:
            raise ValueError("Artifact name and filename can't be both empty")

        # Store values
        self.__language = language.lower()
        self.__name = name or filename
        self.__version = Version(version)
        self.__file = None
        self.file = filename

    def __hash__(self):
        """
        Computes the hash of this object
        """
        return hash(repr(self))

    def __eq__(self, other):
        """
        Tests the equality with another artifact
        """
        if isinstance(other, Artifact):
            # Compare the other artifact
            if self.__file == other.__file:
                # Same file, same artifact
                return True

            # All other attributes must be equal
            return self.__language == other.__language \
                and self.__name == other.__name \
                and self.__version == other.__version
        elif is_string(other):
            # Compare by name
            return self.__name == other
        else:
            # Unknown type
            return NotImplemented

    def __ne__(self, other):
        """
        Tests the inequality with another artifact
        """
        equality = self.__eq__(other)
        if equality is NotImplemented:
            return NotImplemented

        return not equality

    def __lt__(self, other):
        """
        Compares this artifact with another
        """
        if not isinstance(other, Artifact):
            # Not a version
            return NotImplemented

        if self.__name == other.__name:
            # Same name: compare versions
            return self.__version < other.__version

        else:
            # Name order
            return self.__name < other.__name

    def __le__(self, other):
        """
        Compares this artifact with another
        """
        equals = self.__eq__(other)
        if equals is NotImplemented:
            return NotImplemented

        return equals or self.__lt__(other)

    def __gt__(self, other):
        """
        Compares this artifact with another
        """
        if not isinstance(other, Artifact):
            # Not a version
            return NotImplemented

        if self.__name == other.__name:
            # Same name: compare versions
            return self.__version > other.__version
        else:
            # Name order
            return self.__name > other.__name

    def __ge__(self, other):
        """
        Compares this artifact with another
        """
        equals = self.__eq__(other)
        if equals is NotImplemented:
            return NotImplemented

        return equals or self.__gt__(other)

    def __repr__(self):
        """
        String representation of the artifact
        """
        return "Artifact('{art.language}', '{art.name}', " \
               "{art.version!r}, '{art.file}')".format(art=self)

    def __str__(self):
        """
        Human-readable representation
        """
        return "{art.name}-{art.version}".format(art=self)

    @property
    def file(self):
        """
        Path to the artifact file
        """
        return self.__file

    @file.setter
    def file(self, filename):
        """
        Sets the path to the artifact file
        """
        if not filename:
            self.__file = None
        else:
            self.__file = os.path.realpath(filename)

    @property
    def language(self):
        """
        Language of implementation of the artifact (read-only)
        """
        return self.__language

    @property
    def name(self):
        """
        Name of the artifact (read-only)
        """
        return self.__name

    @property
    def version(self):
        """
        Version of the artifact (read-only)
        """
        return self.__version

    @property
    def url(self):
        """
        The URL to the bundle file, if known
        """
        if self.__file:
            return urlparse.urljoin('file:', urllib.pathname2url(self.__file))

# ------------------------------------------------------------------------------


class Factory(object):
    """
    Represents a component factory
    """
    def __init__(self, name, language, model, artifact):
        """
        Sets up the component factory

        :param name: Name of the component factory
        :param language: Language of implementation of the factory
        :param model: The component model that can handle the factory
        :param artifact: Artifact that provides this factory
        :raise ValueError: Invalid parameters
        """
        if not name:
            raise ValueError("Empty factory name")

        if not language:
            raise ValueError("No language given")

        if not model:
            raise ValueError("No component model given")

        if artifact is None:
            raise ValueError("Invalid artifact")

        # Read-only members
        self.__name = name
        self.__language = language
        self.__model = model
        self.__artifact = artifact

        # Provided services
        self.provides = set()

    def __repr__(self):
        """
        String representation of the factory
        """
        return "Factory('{fact.name}', '{fact.language}', " \
               "'{fact.model}', {fact.artifact!r})".format(fact=self)

    def __str__(self):
        """
        Human-readable representation
        """
        return "{fact.name} ({fact.model})".format(fact=self)

    @property
    def artifact(self):
        """
        Artifact that provides the factory (read-only)
        """
        return self.__artifact

    @property
    def language(self):
        """
        Component factory implementation language (read-only)
        """
        return self.__language

    @property
    def model(self):
        """
        Component model (read-only)
        """
        return self.__model

    @property
    def name(self):
        """
        Component factory name (read-only)
        """
        return self.__name

# ------------------------------------------------------------------------------


class Version(object):
    """
    Represents a version (OSGi style)
    """
    def __init__(self, version_str=None):
        """
        Parses the given version string, if given
        """
        self.version = None
        self.qualifier = ''
        if version_str is not None:
            self.parse(version_str)

    def __str__(self):
        """
        String representation
        """
        if self.version is None:
            return '0.0.0'

        version = '.'.join((str(version) for version in self.version))
        if self.qualifier:
            version = '{0}-{1}'.format(version, self.qualifier)

        return version

    def __repr__(self):
        """
        Object string representation
        """
        return "Version('{0}')".format(self.__str__())

    def __normalize_cmp(self, other):
        """
        Returns a version of both local and other version with the same tuple
        """
        local_version = self.version
        other_version = other.version

        if local_version is None or other_version is None:
            # Do nothing if one of the version is None
            return local_version or (0, 0, 0), other_version or (0, 0, 0)

        local_len = len(self.version)
        other_len = len(other.version)

        # Add the missing length
        if local_len < other_len:
            local_version = list(self.version)
            local_version.extend([0] * (other_len - local_len))
            local_version = tuple(local_version)

        elif local_len > other_len:
            other_version = list(other.version)
            other_version.extend([0] * (local_len - other_len))
            other_version = tuple(other_version)

        return local_version, other_version

    def __lt__(self, other):
        """
        Compares this version with another
        """
        if not isinstance(other, Version):
            # Not a version
            return NotImplemented

        local_version, other_version = self.__normalize_cmp(other)
        if local_version == other_version:
            return self.qualifier < other.qualifier

        return local_version < other_version

    def __le__(self, other):
        """
        Compares this version with another
        """
        equals = self.__eq__(other)
        if equals is NotImplemented:
            return NotImplemented

        return equals or self.__lt__(other)

    def __eq__(self, other):
        """
        Compares this version with another
        """
        if not isinstance(other, Version):
            # Not a version
            return NotImplemented

        local_version, other_version = self.__normalize_cmp(other)
        return local_version == other_version \
            and self.qualifier == other.qualifier

    def __ne__(self, other):
        """
        Compares this version with another
        """
        equality = self.__eq__(other)
        if equality is NotImplemented:
            return NotImplemented

        return not equality

    def __gt__(self, other):
        """
        Compares this version with another
        """
        if not isinstance(other, Version):
            # Not a version
            return NotImplemented

        local_version, other_version = self.__normalize_cmp(other)
        if local_version == other_version:
            return self.qualifier > other.qualifier

        return local_version > other_version

    def __ge__(self, other):
        """
        Compares this version with another
        """
        equals = self.__eq__(other)
        if equals is NotImplemented:
            return NotImplemented

        return equals or self.__gt__(other)

    def __add__(self, other):
        """
        Adds a version tuple or object. Trims or enlarges the current version
        tuple to the size of the given one.
        """
        if not isinstance(other, Version):
            other = Version(other)

        if other is None:
            return

        # Compute lengths
        local_len = len(self.version)
        other_len = len(other.version)

        i = 0
        new_version = []
        while i < other_len:
            if i < local_len:
                local_part = self.version[i]
            else:
                local_part = None

            if i < other_len:
                other_part = other.version[i]
                if local_part is not None:
                    # Increment version number
                    new_version.append(local_part + other_part)
                else:
                    # All current parts added
                    new_version.append(other_part)
            else:
                # All other parts added
                new_version.append(local_part)

            i += 1

        while len(new_version) < local_len:
            # Fill with zeros, to match the previous size
            new_version.append(0)

        # Add the qualifier, if any
        qualifier = other.qualifier or self.qualifier
        if qualifier:
            new_version.append(qualifier)

        return Version(new_version)

    def matches(self, other):
        """
        Tests if this version matches the given one
        """
        if other is None:
            # None matches everything
            return True

        if self.version is None:
            # No version matches any version
            return True

        if is_string(other):
            # The given string can be either a version or a range
            other = other.strip()
            if other[0] == '[':
                # Range comparison
                inclusive = (other[-1] == ']')
                versions = other[1:-1].split(',')

                # Convert boundaries
                minimum = Version(versions[0])
                maximum = Version(versions[1])
            else:
                minimum = Version(other)
                # Allow binding up to the next major version (excluded)
                maximum = Version(other) + (1,)
                inclusive = False

        if isinstance(other, Version):
            # Compared to another version
            if other.version is None:
                # Other matches any version
                return True
            else:
                # Allow binding up to the next major version (excluded)
                minimum = other
                maximum = other + (1,)
                inclusive = False

        if minimum is not None and self < minimum:
            # We're under the minimal version
            return False
        elif maximum is not None and self > maximum:
            # We're above the maximal version
            return False
        elif not inclusive and self == maximum:
            # We're at the upper boundary
            return False
        else:
            # Range tests passed
            return True

    def parse(self, version_str):
        """
        Parses the given version string
        """
        # Reset values
        self.version = None
        self.qualifier = ''
        if not version_str:
            # Nothing to do
            return

        if isinstance(version_str, Version):
            # Direct copy
            self.version = version_str.version
            self.qualifier = version_str.qualifier
            return

        if isinstance(version_str, (tuple, list)):
            # We have a raw version tuple
            if isinstance(version_str[-1], str):
                # ... with a qualifier
                self.version = tuple(version_str[:-1])
                self.qualifier = version_str[-1]
            else:
                # ... without it
                self.version = tuple(version_str)
        else:
            # String version
            version_str = str(version_str)

            # Separate pars
            version = version_str.split('.')
            if '-' in version[-1]:
                # Qualifier in the last element
                last_part, self.qualifier = version[-1].split('-')
                version[-1] = last_part
            else:
                try:
                    # Try a conversion of the last part
                    int(version[-1])
                except ValueError:
                    # Last part is not an integer, so it a qualifier
                    self.qualifier = version[-1]
                    version = version[:-1]

            self.version = version

        # Normalize the version
        in_qualifier = False
        version = []
        qualifier = []
        for part in self.version:
            if in_qualifier:
                qualifier.append(part)
            else:
                # Not yet in the qualifier
                try:
                    version.append(int(part))
                except ValueError:
                    # Integer conversion error -> begin qualifier mode
                    in_qualifier = True
                    qualifier.append(part)

        # Don't forget the current qualifier
        if self.qualifier:
            qualifier.append(self.qualifier)

        # Update members
        self.version = tuple(version)
        self.qualifier = '.'.join(qualifier)

        if not any(self.version):
            # Version is only zeros (0.0.0)
            self.version = None
            self.qualifier = ''
