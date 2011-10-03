#!/usr/bin/python
#-- Content-Encoding: utf-8 --
'''
Created on 3 oct. 2011

@author: Thomas Calmant
'''

import BaseHTTPServer
import os
import random
import sys
import urlparse

import xml_item_parser as item_parser

# ------------------------------------------------------------------------------

class Erp(object):
    """
    Describes the ERP state
    """
    def __init__(self):
        """
        Prepares the ERP members
        """
        self.__running = True
        
        self.__categories = dict()
        self.__items = dict()
        self.__stocks = dict()
        
        self.load_content("/home/tcalmant/Bureau")
        
    
    def load_content(self, source_folder):
        """
        Loads the ERP data from the given file
        """
        self.__categories = dict()
        self.__items = dict()
        self.__stocks = dict()
        
        folder_content = os.listdir(source_folder)
        
        for member in folder_content:
            if member.endswith(".xml"):
                
                category = member[:-len(".xml")]
                source_file = source_folder + os.sep + member
                
                parser = item_parser.XmlItemParser()
                items = parser.parse_file(source_file)
                
                if not items:
                    # No items in the file
                    continue
                
                self.__store_items(category, items)
    
    
    def __store_items(self, category, items):
        """
        Stores the parsed items in the ERP
        """
        self.__categories[category] = []
        
        for item in items:
            item_id = item["id"]
            
            self.__categories[category].append(item_id)
            self.__items[item_id] = item
            self.__stocks[item_id] = random.randint(0, 220)
        
        
    def get_categories(self):
        """
        Retrieves the list of all known categories
        
        @return: All known categories
        """
        return self.__categories.keys()
        
        
    def get_items(self, category):
        """
        Retrieves the items of the given category, None if the category is
        unknown
        
        @param category: The category to list
        @return: A list of items, or None
        """
        if category not in self.__categories:
            # No category found
            return
        
        items = []
        
        for item_id in self.__categories[category]:
            # Store each element in a list
            if item_id in self.__items:
                items.append(self.__items[item_id])

        return items


    def get_item_stock(self, item_id):
        """
        Retrieves the availability of the given item (int)
        
        @param item_id: ID of the item
        @return: The available stock for the given item, -1 if the item is
        unknown
        """
        if item_id not in self.__stocks:
            return -1
        
        return int(self.__stocks[item_id])


    def is_running(self):
        """
        Returns the running state of the  ERP
        """
        return self.__running
    
    
    def set_running(self, running):
        """
        Sets the state of the ERP (True or False)
        """
        self.__running = running



ERP_INSTANCE = Erp()

# ------------------------------------------------------------------------------

class ErpHttpServer(BaseHTTPServer.BaseHTTPRequestHandler):
    """
    ERP HTTP Server for PSEM2M Demo
    """
    
    GET_ITEMS = "/getItems"
    GET_ITEMS_STOCK = "/getItemsStock"
    GET_STATE = "/getStateERP"
    SET_STATE = "/setStateERP"
    
    def __init__(self, request, client_address, server):
        """
        Constructor
        """
        # Prepare handlers dictionary
        self._handlers = dict()
        self._handlers[ErpHttpServer.GET_ITEMS] = self.handle_get_items
        self._handlers[ErpHttpServer.GET_ITEMS_STOCK] = \
                                                self.handle_get_items_stock
        self._handlers[ErpHttpServer.GET_STATE] = self.handle_get_state
        self._handlers[ErpHttpServer.SET_STATE] = self.handle_set_state
        
        # Call parent constructor (calls request handling)
        BaseHTTPServer.BaseHTTPRequestHandler.__init__(self, request, \
                                                       client_address, server)
    
    
    def send_internal_error(self):
        """
        Returns a generic 500 server error message
        """
        self.send_error(500)
        self.end_headers()
        self.wfile.write("""<html>
<head>
<title>Error 500</title>
</head>
<body>
<h1>Error 500 - Internal Server Error</h1>
<p>The ERP is currently unavailable</p>
</body>
</html>
""")
        
        
    def handle_get_items(self):
        """
        Handles /getItems requests
        """
        if not ERP_INSTANCE.is_running():
            self.send_internal_error()
            return
        
        # Send answer
        self.send_response(200)
        self.end_headers()
        
        if "category" in self.parsed_query:
            # Retrieve a category
            category = self.parsed_query["category"]
            items = ERP_INSTANCE.get_items(category)
            
        else:
            # Retrieve all items
            items = []
            for category in ERP_INSTANCE.get_categories():
                items.append(ERP_INSTANCE.get_items(category))
        
        self.wfile.write("Items : " + str(items))

    
    def handle_get_items_stock(self):
        """
        Handles /getItemsStock requests
        """
        if not ERP_INSTANCE.is_running():
            self.send_internal_error()
            return
        
        self.send_response(200)
        self.end_headers()
    
    
    def handle_get_state(self):
        """
        Handles /getStateERP requests
        """
        if not ERP_INSTANCE.is_running():
            self.send_internal_error()
            
        else:
            # Server is running -> 200
            self.send_response(200)
            self.end_headers()
        
        
    def handle_set_state(self):
        """
        Handles /setStateERP requests
        """
        
        if "activate" in self.parsed_query:
            
            activation = str(self.parsed_query["activate"][0]).upper()
            
            if activation == "ON" or activation == "TRUE":
                ERP_INSTANCE.set_running(True)
                self.send_response(200)
                
            elif activation == "OFF" or activation == "FALSE":
                ERP_INSTANCE.set_running(False)
                self.send_response(200)
                
            else:
                # Unknown action, bad request
                self.send_response(400)
        
        else:
            # Error 400 - Bad Request
            self.send_response(400)
        
        # End of treatment
        self.end_headers()
    
    
    def do_GET(self):
        """
        Handles GET requests
        """
        
        # Parse URL
        self.parsed_url = urlparse.urlparse(self.path)
        
        # Read the parameters
        self.parsed_query = urlparse.parse_qs(self.parsed_url.query)
        
        # Find and use the handler
        if self.parsed_url.path in self._handlers:
            self._handlers[self.parsed_url.path]()
        
        else:
            # No handler found
            self.send_error(404)
            self.end_headers()
            self.wfile.write("No handler found for path : " + \
                             self.parsed_url.path)
        

    def do_POST(self):
        """
        Handles POST requests
        """
        # Handled as GET requests
        self.do_GET()

# ------------------------------------------------------------------------------

def main():
    """
    Script entry point
    """
    random.seed()
    
    port = 8080
    if len(sys.argv) > 1:
        try:
            port = int(sys.argv[1])
        except Exception, ex:
            print >> sys.stderr, "Error reading port number :", ex
                
    server = BaseHTTPServer.HTTPServer(('', port), ErpHttpServer)
    
    print "Serving..."
    try:
        server.serve_forever()
        
    except KeyboardInterrupt:
        print "Interruption caught."
        
    print "Server stopped."

# ------------------------------------------------------------------------------

if __name__ == "__main__":
    main()
        
