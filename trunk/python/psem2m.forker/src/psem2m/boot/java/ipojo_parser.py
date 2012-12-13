#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Parses the iPOJO entry in the manifest of a bundle

:author: Thomas Calmant
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

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
    def __init__(self):
        """
        Sets up the parser
        """
        self.elements = []


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


    def __parse_elements(self, line):
        """
        Parses the elements in the given line
        
        :param line: A manifest line
        """
        # Loop control
        line_len = len(line)
        idx = 0

        while idx < line_len:
            current = line[idx]
            if current == '$':
                # Beginning of an attribute
                attr, idx = self.__parse_attribute(line, idx)
                self.elements[-1].add_attribute(attr)

            elif current == '}':
                # End of the current element
                if len(self.elements) > 1:
                    # Keep the root
                    # Add the element as a child of the last one
                    last = self.elements.pop()
                    self.elements[-1].add_element(last)

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

                self.elements.append(element)

            idx += 1


    def parse(self, manifest_line):
        """
        Parses the given Manifest line
        """
        # Clear elements
        del self.elements[:]

        # Make the root
        self.elements.append(Element("iPOJO", None))

        # Parse elements
        self.__parse_elements(manifest_line)
        return self.elements[0]
