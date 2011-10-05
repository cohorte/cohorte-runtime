'''
Created on 3 oct. 2011

@author: Thomas Calmant
'''

import sys
import xml.dom.minidom
import xml.parsers.expat

class XmlItemParser(object):
    '''
    Reads an XML file to load Item representations
    '''

    # Special entry (can't be found in XML, so OK for us)
    __has_children_entry = "/hasChildren/"

    # Special, to keep track of the name of an element
    __element_name_entry = "/elementName/"

    def __init__(self):
        """
        Sets up members
        """
        self.__root = {}
        self.__element_stack = []
        self.__current_element = None
        self.__node_data = None


    def parse(self, xml_data):
        """
        Parses the given XML string
        
        @param xml_data: An XML string
        @return: The XML content as a dictionary
        """
        parser = xml.parsers.expat.ParserCreate()
        parser.StartElementHandler = self.__start_element
        parser.EndElementHandler = self.__end_element
        parser.CharacterDataHandler = self.__char_data

        # Reset internal members
        self.__root = {}
        self.__element_stack = []
        self.__element_stack.append(self.__root)

        # Parse
        try:
            parser.Parse(xml_data)

        except xml.parsers.expat.ExpatError, ex:
            print >> sys.stderr, "Error reading XML data :", ex

        if XmlItemParser.__has_children_entry in self.__root:
            del self.__root[XmlItemParser.__has_children_entry]

        return self.__root


    def parse_file(self, file_name):
        """
        Parses the given file
        
        @param file_name: The XML file to parse
        @return: The read items
        """
        # Read the file and parse it
        try:
            with open(file_name) as xml_file:
                return self.parse(xml_file.read())

        except IOError:
            print >> sys.stderr, "Error reading", file_name
            return None


    def __start_element(self, name, attributes):
        """
        Beginning of an XML tag
        """
        # New element
        self.__current_element = {XmlItemParser.__element_name_entry: name}
        self.__node_data = None

        # Tell the parent that a child has arrived
        parent = self.__get_element_parent()
        parent[XmlItemParser.__has_children_entry] = True

        # Add the element to the stack
        self.__element_stack.append(self.__current_element)

        # Attributes are considered as nodes
        if attributes:
            for attr in attributes:
                self.__current_element[attr] = attributes[attr]


    def __char_data(self, data):
        """
        XML Text data
        """
        # Set the node content
        if not self.__node_data:
            self.__node_data = data.encode("ascii", "ignore")
        else:
            self.__node_data += data.encode("ascii", "ignore")


    def __end_element(self, name):
        """
        End of an XML tag
        """
        # Remove the current element from the stack
        self.__element_stack.pop()

        # Get the parent node
        parent = self.__get_element_parent()

        # Append child to parent
        if XmlItemParser.__has_children_entry in self.__current_element:

            # XML Element (use a list in case of multiple elements with the
            # same name)
            if name not in parent:
                parent[name] = []

            parent[name].append(self.__current_element)

            # Remove the "hasChildren" entry
            del self.__current_element[XmlItemParser.__has_children_entry]

        else:
            # XML Text Node
            parent[name] = self.__node_data

        # Remove element name
        if XmlItemParser.__element_name_entry in self.__current_element:
            del self.__current_element[XmlItemParser.__element_name_entry]

        # Change current element to the parent
        self.__current_element = parent
        self.__node_data = None


    def __get_element_parent(self):
        """
        Retrieves the previous element in the elements stack, or none
        """
        if len(self.__element_stack) > 0:
            return self.__element_stack[-1]

        return None


# ------------------------------------------------------------------------------

def items_to_xml(items, root_tag="items", element_tag="item"):
    """
    Converts the given items list to an XML file
    """
    if not items:
        return ""

    impl = xml.dom.minidom.getDOMImplementation()
    doc = impl.createDocument(None, root_tag, None)

    for item in items:
        item_node = doc.createElement(element_tag)

        for key in item.keys():
            key_node = doc.createElement(key)
            value_node = doc.createTextNode(str(item[key]))

            key_node.appendChild(value_node)
            item_node.appendChild(key_node)

        doc.documentElement.appendChild(item_node)

    return doc.toxml()
