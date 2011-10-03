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

    def __init__(self):
        '''
        Sets up members
        '''
        self.__current_item = None
        self.__current_attribute = None
        self.__items = []
        
    
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
        self.__current_item = None
        self.__current_attribute = None
        self.__items = []
        
        # Parse
        try:
            with open(file_name) as xml_file:
                parser.ParseFile(xml_file)
        except IOError:
            print "Error reading", file_name
            return None

        return self.__items


    def __start_element(self, name, attributes):
        """
        Beginning of an XML tag
        """
        
        if name == "item":
            # New item
            self.__current_item = dict()
            
            if attributes:
                for attr in attributes:
                    self.__current_item[attr] = attributes[attr]
            
        elif self.__current_item:
            # Currently reading an item
            self.__current_attribute = name
            self.__current_item[name] = ""
        
        # Do nothing else

    
    def __char_data(self, data):
        """
        XML Text data
        """
        if self.__current_item and self.__current_attribute:
            # Set the node content
            self.__current_item[self.__current_attribute] += data

    
    def __end_element(self, name):
        """
        End of an XML tag
        """
        if name == self.__current_attribute:
            self.__current_attribute = None
            
        elif name == "item":
            self.__items.append(self.__current_item)
            self.__current_item = None

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
