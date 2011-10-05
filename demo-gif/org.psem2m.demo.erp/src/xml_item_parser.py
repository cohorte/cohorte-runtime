'''
Created on 3 oct. 2011

@author: Thomas Calmant
'''

import xml.dom.minidom
import xml.parsers.expat

class XmlItemParser(object):
    '''
    Reads an XML file to load Item representations
    '''
    __has_children_entry = "/hasChildren/"

    def parse_file(self, file_name):
        """
        Parses the given file
        
        @param file_name: The XML file to parse
        @return: The read items
        """
        parser = xml.parsers.expat.ParserCreate()
        parser.StartElementHandler = self.__start_element
        parser.EndElementHandler = self.__end_element
        parser.CharacterDataHandler = self.__char_data

        # Reset internal members
        self.__root = dict()
        self.__element_stack = list()
        self.__element_stack.append(self.__root)

        # Parse
        try:
            with open(file_name) as xml_file:
                parser.ParseFile(xml_file)

        except IOError:
            print "Error reading", file_name
            return None

        return self.__root


    def __start_element(self, name, attributes):
        """
        Beginning of an XML tag
        """
        # New element
        self.__current_element = dict()
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
        if XmlItemParser.__has_children_entry in self.__current_element \
            and self.__current_element[XmlItemParser.__has_children_entry]:
            # XML Element (use a list in case of multiple elements with the
            # same name)
            if name not in parent:
                parent[name] = []

            parent[name].append(self.__current_element)

        else:
            # XML Text Node
            parent[name] = self.__node_data

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

class XmlItemOutput(object):
    """
    XML output generator
    """

    def items_to_xml(self, items, root_tag="items", element_tag="item"):
        """
        Converts the given items list to an XML file
        """
        if not items:
            return ""

        impl = xml.dom.minidom.getDOMImplementation()
        doc = impl.createDocument(None, root_tag, None)

        for item in items:
            itemNode = doc.createElement(element_tag)

            for key in item.keys():
                keyNode = doc.createElement(key)
                valueNode = doc.createTextNode(str(item[key]))

                keyNode.appendChild(valueNode)
                itemNode.appendChild(keyNode)

            doc.documentElement.appendChild(itemNode)

        return doc.toxml()
