#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Java Manifest.mf utility module

Parses a Manifest file and its iPOJO-Components entry, if present.

**TODO:**
* Enhance API & code
* Convert into a service ?

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

import contextlib
import shlex
import sys

PYTHON3 = (sys.version_info[0] == 3)
if PYTHON3:
    # Python 3
    from io import StringIO

else:
    # Python 2
    from StringIO import StringIO

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

        # iPOJO root element
        self._ipojo = None

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
        # Format values
        lines = []

        # First line: Manifest version
        lines.append(': '.join(('Manifest-Version',
                                self.entries.get('Manifest-Version', '1.0'))))

        # Sort keys, except the version
        keys = [key.strip() for key in self.entries.keys()
                if key != 'Manifest-Version']
        keys.sort()

        # Wrap values
        for key in keys:
            line = ': '.join((key, self.entries[key].strip()))
            lines.extend(self._wrap_line(line))

        return '\n'.join(lines)


    def get_ipojo_root(self):
        """
        Retrieves the iPOJO root description, if any
        
        :return: The iPOJO root Element, or None
        """
        return self._ipojo


    def parse(self, manifest):
        """
        Parses the given Manifest file content to fill this Manifest
        representation

        :param manifest: The content of a Manifest file
        """
        # Clear current entries
        self.entries.clear()
        self._ipojo = None

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

        # Parse the iPOJO Components entry
        ipojo_line = self.entries.get(IPOJO_COMPONENTS_KEY)
        if ipojo_line:
            parser = IPojoMetadataParser()
            self._ipojo = parser.parse(ipojo_line)


    def _wrap_line(self, line):
        """
        Wraps a line, Manifest style

        :param line: The line to wrap
        :return: The wrapped line
        """
        lines = []
        # 70 chars for the first line
        lines.append(line[:70])

        # space + 69 chars for the others
        chunk = line[70:]
        while chunk:
            lines.append(' ' + chunk[:69])
            chunk = chunk[69:]

        return lines

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
        self.value = value.lower()

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
        return "{0}.Attribute('{1}')".format(__name__, self.__str__())

# ------------------------------------------------------------------------------

class IPojoMetadataParser(object):
    """
    iPOJO metadata parser
    """
    def __parse_attribute(self, line, idx):
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


    def parse(self, line):
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
                attr, idx = self.__parse_attribute(line, idx)
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
                    ns, name = qname.split(':', 1)
                    element = Element(name, ns)

                elements.append(element)

            idx += 1

        # Return the root element
        return elements[0]
