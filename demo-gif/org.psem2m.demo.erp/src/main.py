#!/usr/bin/python
#-- Content-Encoding: utf-8 --
'''
Created on 3 oct. 2011

@author: Thomas Calmant
'''

import BaseHTTPServer
import itertools
import mimetypes
import os
import random
import sys
import urlparse

import erp
import xml_item_parser

# Load the ERP
ERP_INSTANCE = erp.Erp()
ERP_INSTANCE.load_content(os.getcwd() + os.sep + "data")

# ------------------------------------------------------------------------------

class ErpHttpServer(BaseHTTPServer.BaseHTTPRequestHandler):
    """
    ERP HTTP Server for PSEM2M Demo
    """

    GET_ITEM = "/getItem"
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
        self._handlers["/"] = self.handle_index_page
        self._handlers["/index.html"] = self.handle_index_page
        self._handlers[ErpHttpServer.GET_ITEM] = self.handle_get_item
        self._handlers[ErpHttpServer.GET_ITEMS] = self.handle_get_items
        self._handlers[ErpHttpServer.GET_ITEMS_STOCK] = \
                                                self.handle_get_items_stock
        self._handlers[ErpHttpServer.GET_STATE] = self.handle_get_state
        self._handlers[ErpHttpServer.SET_STATE] = self.handle_set_state

        # Call parent constructor (calls request handling)
        BaseHTTPServer.BaseHTTPRequestHandler.__init__(self, request, \
                                                       client_address, server)


    def __get_all_items(self):
        """
        Retrieves a list containing all items
        """
        # Retrieve all items
        items = []
        for category in ERP_INSTANCE.get_categories():
            items.append(ERP_INSTANCE.get_items(category))

        # Chain all sub-lists
        return itertools.chain.from_iterable(items)

    def handle_get_item(self):
        """
        Retrieves the item corresponding to the given ID (an array of 1 element)
        
        The ID is given in the URL (query string)
        """
        if not ERP_INSTANCE.is_running():
            self.__send_internal_error()
            return

        xmlData = None
        if not "id" in self.parsed_query:
            # Bad request (400)
            code = 400

        else:
            item = ERP_INSTANCE.get_item(self.parsed_query["id"])
            if item:
                # Item found
                code = 200
                xmlData = xml_item_parser.XmlItemOutput().items_to_xml([item])

            else:
                # Item not found
                code = 404

        # Send answer
        self.__send_response(code, xmlData, "text/xml")


    def handle_get_items(self):
        """
        Handles /getItems requests
        """
        if not ERP_INSTANCE.is_running():
            self.__send_internal_error()
            return

        # Only POST command is allowed here
        if self.command != "POST":
            self.send_error(403, \
                            "This URI is accessible only with POST requests")
            self.end_headers()
            return

        # Read the XML data in the POST query
        requested_items = xml_item_parser.XmlItemParser().parse_file(self.rfile)
        if not requested_items:
            self.__send_internal_error()
            return

        if "category" in requested_items:
            # Retrieve a category
            category = self.parsed_query["category"][0]
            items = ERP_INSTANCE.get_items(category)

        else:
            items = self.__get_all_items()


        # Prepare the XML response file
        xmlData = xml_item_parser.XmlItemOutput().items_to_xml(items)

        # Send answer
        self.__send_response(200, xmlData, "text/xml")


    def handle_get_items_stock(self):
        """
        Handles /getItemsStock requests
        """
        if not ERP_INSTANCE.is_running():
            self.__send_internal_error()
            return

        if self.command != "POST":
            self.send_error(403, \
                            "This URI is accessible only with POST requests")
            self.end_headers()
            return

        # Read requested items from POST content
        requested_items = xml_item_parser.XmlItemParser().parse_file(self.rfile)
        if not requested_items:
            self.__send_internal_error()
            return

        # Get the stock for each requested item
        itemsStockDict = dict()
        for item in requested_items:
            if "id" in item:
                itemId = item["id"]
                itemsStockDict[itemId] = ERP_INSTANCE.get_item_stock(itemId)


        # Prepare the XML response file
        xmlData = xml_item_parser.XmlItemOutput().items_to_xml(itemsStockDict)

        # Send answer
        self.__send_response(200, xmlData, "text/xml")


    def handle_get_state(self):
        """
        Handles /getStateERP requests
        """
        if not ERP_INSTANCE.is_running():
            self.__send_internal_error()

        else:
            with open("./html/state_running.html") as f:
                page_content = f.read()

            # Server is running -> 200
            self.__send_response(200, page_content)


    def handle_set_state(self):
        """
        Handles /setStateERP requests
        """

        code = 400
        if "activate" in self.parsed_query:

            activation = str(self.parsed_query["activate"][0]).upper()

            if activation == "ON" or activation == "TRUE":
                ERP_INSTANCE.set_running(True)
                code = 200

            elif activation == "OFF" or activation == "FALSE":
                ERP_INSTANCE.set_running(False)
                code = 200

        # Prepare response content
        with open("./html/code_result.html") as f:
            args = {"code": code}
            page_content = f.read().format(**args)

        # Send the response
        self.__send_response(code, page_content)


    def handle_index_page(self):
        """
        Prepares an information page to show and change the ERP status, and to
        show the items stocks
        """

        # Prepare the format() arguments
        format_args = dict()

        # ERP State
        if ERP_INSTANCE.is_running():
            format_args["erp-running"] = "ON"
        else:
            format_args["erp-running"] = "OFF"

        # Items state
        format_args["items-status"] = self.__prepare_items_rows()

        with open("./html/erp_state.html") as f:
            page_content = f.read().format(**format_args)

        # Send the response
        self.__send_response(200, page_content)


    def __prepare_items_rows(self):
        """
        Prepares HTML table rows representing the items complete status
        """

        # Item map expected keys
        item_keys = ["id", "lib", "price", "stock"]
        item_keys_label = {"id": "ID", "lib": "Name", "price": "Price", \
                     "stock": "Quantity"}


        item_html_row = "\t\t<tr>\n"

        # Table header
        html_content = "\t<thead>\n\t\t<tr>\n"

        for key in item_keys:
            # Write head
            html_content += "\t" * 3 + "<th>\n" + "\t" * 4
            html_content += item_keys_label[key] + "\n" + "\t" * 3 + "</th>\n"

            # Prepare item row model
            item_html_row += "\t" * 3 + "<td>\n" + "\t" * 4
            item_html_row += "{" + key + "}\n" + "\t" * 3 + "</td>\n"


        # End of head
        html_content += "\t\t</tr>\n\t</thead>\n\t<tbody>\n"

        # Prepare items dictionary (items & stock)
        items = self.__get_all_items()

        if items:
            for item in items:
                item["stock"] = ERP_INSTANCE.get_item_stock(item["id"])

                # Write the line
                html_content += item_html_row.format(**item)

        # End of table
        html_content += "\t</tbody>\n"
        return html_content


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
            try:
                with open("./html" + self.parsed_url.path) as requested_file:
                    self.__send_response(200, requested_file.read(), \
                                         mimetypes.guess_type(self.parsed_url.path)[0])

                # File sent, nothing else to do
                return

            except Exception, ex:
                # File doesn't exist
                self.__send_response(404, str(ex))
                return
            # No handler found, use information page
            #self.handle_index_page()


    def do_POST(self):
        """
        Handles POST requests
        """
        # Handled as GET requests
        self.do_GET()


    def __send_response(self, code=200, content=None, content_type="text/html"):
        """
        Sends an HTTP response
        """
        if code < 400:
            self.send_response(code)
        else:
            self.send_error(code)

        if content:
            self.send_header("content-length", len(content))
            self.send_header("content-type", content_type)

        self.end_headers()

        if content:
            self.wfile.write(content)


    def __send_internal_error(self):
        """
        Sends a generic 500 server error message
        """
        with open("./html/internal_error.html") as f:
            page_content = f.read()

        self.__send_response(500, page_content)


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

