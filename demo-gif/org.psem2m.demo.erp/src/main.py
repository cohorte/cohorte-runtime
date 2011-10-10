#!/usr/bin/python
#-- Content-Encoding: utf-8 --
'''
Created on 3 oct. 2011

@author: Thomas Calmant
'''

import BaseHTTPServer
import itertools
import logging
import mimetypes
import os
import random
import sys
import urlparse

import erp
import xml_item_parser

# ------------------------------------------------------------------------------

# Load the ERP
ERP_INSTANCE = erp.Erp()
ERP_INSTANCE.load_content(os.getcwd() + os.sep + "data")

def format_text(text, params=dict()):
    """
    Formats the given string, replacing {toto} by params["toto"], based
    on params keys.
    
    @param text: Text to format
    @param params: Format parameters
    @return: The formatted text
    """
    if not params or not text:
        # Empty parameters
        return text

    for key in params:
        text = text.replace("{" + str(key) + "}", str(params[key]))

    return text


def get_all_items():
    """
    Retrieves a list containing all items in the ERP
    """
    # Retrieve all items
    items = []
    for category in ERP_INSTANCE.get_categories(False):
        items.append(ERP_INSTANCE.get_items(category, update_stats=False))

    # Chain all sub-lists
    return itertools.chain.from_iterable(items)


def prepare_items_rows():
    """
    Prepares HTML table rows representing the items complete status
    """

    # Item map expected keys
    item_keys = ["id", "lib", "price", "stock"]
    item_keys_label = {"id": "ID", "lib": "Name", "price": "Price", \
                 "stock": "Quantity"}


    item_html_row = "\t\t<tr>\n"

    # Table header
    html_content = '<table class="sortable">\n\t<thead>\n\t\t<tr>\n'

    for key in item_keys:
        # Write head
        html_content += "\t" * 3 + "<th>\n" + "\t" * 4
        html_content += item_keys_label[key] + "\n" + "\t" * 3 + "</th>\n"

        # Prepare item row model
        item_html_row += "\t" * 3 + "<td>\n" + "\t" * 4

        if key == "stock":
            item_html_row += '<input type="text" name="{id}" value={stock} />'

        else:
            item_html_row += "{" + key + "}"

        item_html_row += "\n" + "\t" * 3 + "</td>\n"


    # End of head
    html_content += "\t\t</tr>\n\t</thead>\n\t<tbody>\n"

    # Prepare items dictionary (items & stock)
    items = get_all_items()

    if items:
        for item in items:
            item["stock"] = ERP_INSTANCE.get_item_stock(item["id"], False)

            # Write the line
            html_content += format_text(item_html_row, item)

    # End of table
    html_content += "\t</tbody>\n</table>"
    return html_content

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
    RESET_STATS = "/resetStats"
    UPDATE_STOCK = "/updateStocks"

    def __init__(self, request, client_address, server):
        """
        Constructor
        """
        # Parsed query members
        self.parsed_query = None
        self.parsed_url = None

        # Prepare handlers dictionary
        self._handlers = dict()
        self._handlers["/"] = self._handle_index_page
        self._handlers["/index.html"] = self._handle_index_page
        self._handlers[ErpHttpServer.GET_ITEM] = self._handle_get_item
        self._handlers[ErpHttpServer.GET_ITEMS] = self._handle_get_items
        self._handlers[ErpHttpServer.GET_ITEMS_STOCK] = \
                                                self._handle_get_items_stock
        self._handlers[ErpHttpServer.GET_STATE] = self._handle_get_state
        self._handlers[ErpHttpServer.SET_STATE] = self._handle_set_state
        self._handlers[ErpHttpServer.RESET_STATS] = self._handle_reset_stats
        self._handlers[ErpHttpServer.UPDATE_STOCK] = self._handle_update_stock

        # Path handled flag
        self._handled = False

        # Call parent constructor (calls request handling)
        BaseHTTPServer.BaseHTTPRequestHandler.__init__(self, request, \
                                                       client_address, server)


    def _handle_get_item(self):
        """
        Retrieves the item corresponding to the given ID (an array of 1 element)
        
        The ID is given in the URL (query string)
        """
        if not ERP_INSTANCE.is_running():
            self.__send_internal_error()
            return

        xml_data = None
        if not "id" in self.parsed_query:
            # Bad request (400)
            code = 400

        else:
            # Call the ERP
            item = ERP_INSTANCE.get_item(self.parsed_query["id"][0])

            if item:
                # Item found
                code = 200
                xml_data = xml_item_parser.items_to_xml([item])

            else:
                # Item not found
                code = 404

        # Send answer
        self.__send_response(code, xml_data, "text/xml")


    def _handle_get_items(self):
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
        post_body = self.__read_post_body()
        xml_nodes = xml_item_parser.XmlItemParser().parse(post_body)
        if not xml_nodes:
            self.__send_internal_error()
            return

        if "criteria" not in xml_nodes or len(xml_nodes["criteria"]) == 0:
            self.__send_response(400, "Invalid Request", "text/plain")
            return

        # Read the request
        criteria = xml_nodes["criteria"][0]

        try:
            category = criteria["category"]
            base_id = criteria["baseId"]
            count = int(criteria["count"])
            randomize = (criteria["randomize"].lower() == "true")

        except Exception, ex:
            # An error occurred while reading the request
            self.send_response(400, "Invalid request content\n" + str(ex), \
                               "text/plain")

        # Call the ERP
        items = ERP_INSTANCE.get_items(category, count, randomize, base_id)

        if items == None:
            self.__send_internal_error()

        # Prepare the XML response file
        xml_data = xml_item_parser.items_to_xml(items)

        # Send answer
        self.__send_response(200, xml_data, "text/xml")


    def _handle_get_items_stock(self):
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
        post_body = self.__read_post_body()
        xml_nodes = xml_item_parser.XmlItemParser().parse(post_body)
        if not xml_nodes or "items" not in xml_nodes:
            self.__send_internal_error()
            return

        # Get the stock for each requested item
        requested_items = xml_nodes["items"][0]
        result_items = []

        if "item" not in requested_items:
            self.__send_internal_error()
            return

        for item in requested_items["item"]:
            try:
                item_stock_dict = {}
                item_id = item["id"]
                item_stock_dict["id"] = item_id
                item_stock_dict["stock"] = ERP_INSTANCE.get_item_stock(item_id)

                result_items.append(item_stock_dict)

            except KeyError, ex:
                print >> sys.stderr, "Error looking for node :", ex

        # Prepare the XML response file
        xml_data = xml_item_parser.items_to_xml(result_items)

        # Send answer
        self.__send_response(200, xml_data, "text/xml")


    def _handle_get_state(self):
        """
        Handles /getStateERP requests
        """
        if not ERP_INSTANCE.is_running():
            self.__send_internal_error()

        else:
            with open("./html/state_running.html") as html_file:
                page_content = html_file.read()

            # Server is running -> 200
            self.__send_response(200, page_content)


    def _handle_set_state(self):
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
        with open("./html/code_result.html") as html_file:
            format_args = {"code": code}
            page_content = format_text(html_file.read(), format_args)

        # Send the response
        self.__send_response(code, page_content)


    def _handle_index_page(self):
        """
        Prepares an information page to show and change the ERP status, and to
        show the items stocks
        """
        # Don't log what we send from here...
        self._handled = False

        # Prepare the format() arguments
        format_args = dict()

        # ERP State
        if ERP_INSTANCE.is_running():
            format_args["erp-running"] = "ON"
        else:
            format_args["erp-running"] = "OFF"

        # Number of requests handled
        erp_stats = ERP_INSTANCE.get_stats()
        format_args["erp-requests"] = erp_stats["requests"]
        format_args["erp-time"] = erp_stats["time"]
        format_args["erp-average"] = erp_stats["average"]

        # Items state
        format_args["items-status-table"] = prepare_items_rows()

        with open("./html/erp_state.html") as html_file:
            page_content = format_text(html_file.read(), format_args)

        # Send the response
        self.__send_response(200, page_content)


    def _handle_reset_stats(self):
        """
        Resets the ERP statistics
        """
        ERP_INSTANCE.reset_stats()

        # Prepare response content
        with open("./html/code_result.html") as html_file:
            format_args = {"code": 200}
            page_content = format_text(html_file.read(), format_args)

        # Send the response
        self.__send_response(200, page_content)


    def _handle_update_stock(self):
        """
        Updates the ERP items stocks according to the given POST data.
        
        The POST data is encoded with application/x-www-form-urlencoded
        """

        # Convert the given data into a Python dictionary
        post_body = self.__read_post_body()
        query_dict = urlparse.parse_qs(post_body)
        code = 200

        if not query_dict:
            code = 400

        else:
            for item_id in query_dict:
                ERP_INSTANCE.set_item_stock(item_id, query_dict[item_id][0], \
                                            False)

        # Prepare response content
        with open("./html/code_result.html") as html_file:
            format_args = {"code": code}
            page_content = format_text(html_file.read(), format_args)

        # Send the response
        self.__send_response(code, page_content)


    def do_GET(self):
        """
        Handles GET requests
        """
        # Parse URL
        self.parsed_url = urlparse.urlparse(self.path)

        # Read the parameters
        self.parsed_query = urlparse.parse_qs(self.parsed_url.query)

        # Some log...
        logging.debug("%s - %s - %s", self.command, self.parsed_url.path, \
                      self.parsed_query)

        # Find and use the handler
        if self.parsed_url.path in self._handlers:
            # Log handlers events
            self._handled = True

            try:
                self._handlers[self.parsed_url.path]()

            except Exception, ex:
                self.__send_response(500, str(ex), "text/plain")

        else:
            # No handle found => Try with a real file
            try:
                with open("./html" + self.parsed_url.path) as requested_file:
                    mime = mimetypes.guess_type(self.parsed_url.path)
                    if not mime:
                        mime = ["text/plain"]

                    self.__send_response(200, requested_file.read(), \
                                mime[0])

                # File sent, nothing else to do
                return

            except Exception, ex:
                # File doesn't exist
                self.__send_response(404, str(ex))
                return


    def do_POST(self):
        """
        Handles POST requests
        """
        # Handled as GET requests
        self.do_GET()


    def log_message(self, msg_format, *args):
        """
        Override the Handler logging system
        """
        if self._handled:
            logging.info(msg_format, *args)


    def __read_post_body(self):
        """
        Reads the body of a POST request, returns an empty string on error
        """
        try :
            content_len = int(self.headers.getheader('content-length'))
            post_body = self.rfile.read(content_len)

            if self._handled:
                logging.debug("POST BODY :\n%s", post_body)

            return post_body

        except Exception, ex:
            print >> sys.stderr, "Error reading POST body :", ex
            return ""


    def __send_response(self, code=200, content=None, content_type="text/html"):
        """
        Sends an HTTP response
        """

        if self._handled:
            # Log handler results
            logging.debug("code = %d - content :\n%s", code, content)

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
        with open("./html/internal_error.html") as html_file:
            page_content = html_file.read()

        self.__send_response(500, page_content)


# ------------------------------------------------------------------------------

def main():
    """
    Script entry point
    """

    # Default values
    port = 8080
    logfile = "./log.txt"
    loglevel = logging.DEBUG

    # Read the the arguments
    try:
        port = int(sys.argv[1])
        logfile = sys.argv[2]
        loglevel = getattr(logging, sys.argv[3].upper(), logging.DEBUG)

    except IndexError:
        # Ignore index error (use defaults)
        pass

    except ValueError, ex:
        print >> sys.stderr, "Error reading port :", ex

    # Prepare the "random" module
    random.seed()

    # Prepare the "logging" module
    logging.basicConfig(filename=logfile, level=loglevel, \
                        format='%(asctime)s %(message)s')

    # Bind the server
    server = BaseHTTPServer.HTTPServer(('', port), ErpHttpServer)

    # Run, Forest !
    print "Serving..."
    try:
        server.serve_forever()

    except KeyboardInterrupt:
        print "Interruption caught."

    print "Server stopped."

# ------------------------------------------------------------------------------

if __name__ == "__main__":
    main()

