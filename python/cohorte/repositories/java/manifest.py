#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Utility module to handle Java Manifest.mf files

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

import contextlib
import shlex
import sys

PYTHON3 = (sys.version_info[0] == 3)
if PYTHON3:
    # Python 3
    import io
    StringIO = io.StringIO
else:
    # Python 2
    import StringIO
    StringIO = StringIO.StringIO

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

# iPOJO components description key
IPOJO_COMPONENTS_KEY = 'iPOJO-Components'

# ------------------------------------------------------------------------------


class Manifest(object):
    """
    Java Manifest parser
    """
    def __init__(self):
        """
        Sets up the parser
        """
        # Manifest entries
        self.entries = {}

        # get() shortcut
        self.get = self.entries.get

    def extract_packages_list(self, manifest_key):
        """
        Retrieves a list of packages and their attributes

        :param manifest_key: Name of the package list in the manifest
        :return: A dictionary: package -> dictionary of attributes
        """
        parsed_list = {}
        packages_list = self.entries.get(manifest_key, '').strip()

        if packages_list:
            # Use shlex to handle quotes
            parser = shlex.shlex(packages_list, posix=True)
            parser.whitespace = ','
            parser.whitespace_split = True

            for package_str in parser:
                # Extract import values
                package_info = package_str.strip().split(';')

                name = package_info[0]
                attributes = {}
                for value in package_info[1:]:
                    if value:
                        attr_name, attr_value = value.split('=', 1)
                        if attr_name[-1] == ':':
                            # Remove the ':' of ':=' in some attributes
                            attr_name = attr_name[:-1].strip()

                        attributes[attr_name] = attr_value.strip()

                parsed_list[name] = attributes

        return parsed_list

    def format(self):
        """
        Formats the entries to be Manifest format compliant
        """
        # First line: Manifest version
        lines = [': '.join(('Manifest-Version',
                            self.entries.get('Manifest-Version', '1.0')))]

        # Sort keys, except the version
        keys = [key.strip() for key in self.entries.keys()
                if key != 'Manifest-Version']
        keys.sort()

        # Wrap values
        for key in keys:
            line = ': '.join((key, self.entries[key].strip()))
            lines.extend(self._wrap_line(line))

        return '\n'.join(lines)

    def parse(self, manifest):
        """
        Parses the given Manifest file content to fill this Manifest
        representation

        :param manifest: The content of a Manifest file
        """
        # Clear current entries
        self.entries.clear()

        if PYTHON3 and not isinstance(manifest, str):
            # Python 3 doesn't like bytes
            manifest = str(manifest, 'UTF-8')

        # Read the manifest, line by line
        with contextlib.closing(StringIO(manifest)) as manifest_io:
            key = None
            for line in manifest_io.readlines():
                if key is not None and line[0] == ' ':
                    # Line continuation
                    self.entries[key] += line.strip()
                else:
                    # Strip the line
                    line = line.strip()
                    if not line:
                        # Empty line
                        key = None
                        continue

                    # We have a key
                    key, value = line.split(':', 1)

                    # Strip values
                    self.entries[key] = value.strip()

    @staticmethod
    def _wrap_line(line):
        """
        Wraps a line, Manifest style

        :param line: The line to wrap
        :return: The wrapped line
        """
        # 70 chars for the first line
        lines = [line[:70]]

        # space + 69 chars for the others
        chunk = line[70:]
        while chunk:
            lines.append(' ' + chunk[:69])
            chunk = chunk[69:]
        return lines
