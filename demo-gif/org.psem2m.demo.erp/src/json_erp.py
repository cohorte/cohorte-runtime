'''
Created on 14 nov. 2011

@author: Thomas Calmant
'''

from jsonrpclib.SimpleJSONRPCServer import SimpleJSONRPCServer, SimpleJSONRPCRequestHandler
from pprint import pformat
import erp
import os
import sys
import logging
import itertools
import mimetypes
import urlparse


#=====================================================================================

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


#=====================================================================================
class ErpUserInterface(SimpleJSONRPCRequestHandler):
    """
    User
    """
    ROOT = "/"
    INDEX = "/index.html"
    UPDATE_STOCK = "/updateStocks"
    GET_STATE = "/getStateERP"
    RESET_STATS = "/resetStats"
    SET_STATE = "/setStateERP"

    def __init__(self, request, client_address, server):

        # Some log...
        logging.info("%s - %s ", "ErpUserInterface", "<init>")

        # Path handled flag
        self._handled = True

        # Parsed query members
        self.parsed_query = None
        self.parsed_url = None

        # Prepare handlers dictionary
        self._handlers = dict()
        self._handlers[ErpUserInterface.ROOT] = self._handle_index_page
        self._handlers[ErpUserInterface.INDEX] = self._handle_index_page
        self._handlers[ErpUserInterface.UPDATE_STOCK] = self._handle_update_stock
        self._handlers[ErpUserInterface.RESET_STATS] = self._handle_reset_stats
        self._handlers[ErpUserInterface.GET_STATE] = self._handle_get_state
        self._handlers[ErpUserInterface.SET_STATE] = self._handle_set_state

        #super
        SimpleJSONRPCRequestHandler.__init__(self, request, client_address, server)

    def do_POST(self):
        """
        Handles POST requests
        """
                # Some log...
        logging.info("%s - %s - %s", "ErpUserInterface", "do_POST", self.path)
        try:
            # Parse URL
            self.parsed_url = urlparse.urlparse(self.path)

            # Find paths  and use the handler
            if self.parsed_url.path == ErpUserInterface.UPDATE_STOCK:

                # Handled as GET requests
                self.do_GET()
            else:

                # lets the parent class to do the job
                SimpleJSONRPCRequestHandler.do_POST(self)

        except Exception, ex:

            logging.error("%s - %s - %s", "ErpUserInterface", "do_POST", pformat(ex))
            # File doesn't exist
            self.__send_response(404, pformat(ex))
            return


    def do_GET(self):
        """
        Handles GET requests
        """
        # Parse URL
        self.parsed_url = urlparse.urlparse(self.path)

        # Read the parameters
        self.parsed_query = urlparse.parse_qs(self.parsed_url.query)

        # Some log...
        logging.info("%s - %s ", self.command, self.parsed_url.path)

        # Find and use the handler
        if self.parsed_url.path in self._handlers:
            # Log handlers events
            self._handlers[self.parsed_url.path]()

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


    def _handle_index_page(self):

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
        format_args["erp-average"] = "%.2f" % erp_stats["average"]

        # Items state
        format_args["items-status-table"] = prepare_items_rows()

        with open("./html/erp_state.html") as html_file:
            page_content = format_text(html_file.read(), format_args)

        # Send the response
        self.__send_response(200, page_content)

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

            # Some log...
            logging.info("set_stat - %s ", activation)

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
            if "reset" in query_dict:
                new_stock = int(query_dict["reset"][0])

                # Some log...
                logging.info("set_stat - %d", new_stock)

                for item_id in query_dict:
                    ERP_INSTANCE.set_item_stock(item_id, new_stock, False)

            else:
                # Some log...
                logging.info("set_stat - %s", "apply changes")
                for item_id in query_dict:
                    ERP_INSTANCE.set_item_stock(item_id, \
                                                query_dict[item_id][0], \
                                                False)

        # Prepare response content
        with open("./html/code_result.html") as html_file:
            format_args = {"code": code}
            page_content = format_text(html_file.read(), format_args)

        # Send the response
        self.__send_response(code, page_content)

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
            logging.debug("code = %d - content :\n%s\n", code, content)

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




#===============================================================================

JAVA_CLASS = u"javaClass"

def result_to_jabsorb(erp_result):
    """
    Adds informations for Jabsorb, if needed
    """
    result = {}

    # Map ?
    if isinstance(erp_result, dict):
        result["map"] = {}

        for key in erp_result.keys():
            result["map"][key] = result_to_jabsorb(erp_result[key])

        result[JAVA_CLASS] = "java.util.HashMap"

    # List ?
    elif isinstance(erp_result, list):
        result[JAVA_CLASS] = "java.util.ArrayList"
        result["list"] = []

        for item in erp_result:
            result["list"].append(result_to_jabsorb(item))

    # Other ?
    else:
        result = erp_result

    return result


def request_from_jabsorb(request):
    """
    Removes informations from jabsorb
    """
    if not isinstance(request, dict):
        # Raw element
        return request

    java_class = str(request[JAVA_CLASS])

    # Map ?
    if java_class.endswith("Map"):
        result = {}

        for key in request["map"]:
            result[key] = request_from_jabsorb(request["map"][key])

        return result

    # List ?
    elif java_class.endswith("List"):
        result = []

        for element in request["list"]:
            result.append(request_from_jabsorb(element))

        return result

    # Other ?
    return request


class ErpProxy(object):
    """
    ERP Proxy, for Jabsorb
    """

    def __init__(self):
        """
        Constructor
        """
        self.erp = ERP_INSTANCE
        self.erp.load_content(os.getcwd() + os.sep + "data")

    def reset_stocks(self):
        """
        Resets all stocks to 100
        """
        self.erp.reset_stocks(100)
        return True


    def apply_cart(self, cart_map):
        """
        Calls applyCart for Jabsorb
        """
        cart_map = request_from_jabsorb(cart_map)

        # Compute parameters
        cart_id = cart_map["id"]
        cart_content = cart_map["lines"]

        # Call the ERP
        erp_result = self.erp.apply_cart(cart_id, cart_content)

        # Convert the result bean to a map
        result_map = {"code": erp_result.code, "message": erp_result.message,
                      "reason": erp_result.reason}

        return result_to_jabsorb(result_map)


    def get_item(self, item_id):
        """
        Calls getItem for Jabsorb
        """
        # Call the ERP
        erp_result = self.erp.get_item(item_id)

        return result_to_jabsorb(erp_result)


    def get_items(self, category, count, randomize, base_id):
        """
        Calls getItems for Jabsorb
        """
        # Call the ERP
        erp_result = self.erp.get_items(category, count, randomize, base_id)

        return result_to_jabsorb(erp_result)


    def get_items_stock(self, items_ids):
        """
        Calls erp.get_item_stock, for each given item
        """
        result = []

        # Virtual method, call the ERP for each item
        for item_id in items_ids:
            stock = self.erp.get_item_stock(item_id)
            if stock is not None:
                item_result = {}
                item_result["id"] = item_id
                item_result["stock"] = stock

                result.append(item_result)

        return result_to_jabsorb(result)



def main():
    """
    Entry point
    """
    wHttpPort = 8080

    # Some log...
    logging.info("%s - %s - listen %d", "main", "<start>", wHttpPort)

    # Prepare the ERP internals
    erp_proxy = ErpProxy()

    # Prepare the JSON RPC server
    server = SimpleJSONRPCServer(('localhost', wHttpPort), ErpUserInterface, True)

    server.register_function(erp_proxy.apply_cart, "applyCart")
    server.register_function(erp_proxy.get_item, "getItem")
    server.register_function(erp_proxy.get_items, "getItems")
    server.register_function(erp_proxy.get_items_stock, "getItemsStock")

    server.register_function(erp_proxy.reset_stocks, "reset_stocks")

    try:
        server.serve_forever()

    except KeyboardInterrupt:
        return 0


#=====================================================================================
logging.basicConfig(level=logging.INFO)

ERP_INSTANCE = erp.Erp()
ERP_INSTANCE.load_content(os.getcwd() + os.sep + "data")

if __name__ == '__main__':
    sys.exit(main())
