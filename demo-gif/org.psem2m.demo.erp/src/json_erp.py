'''
Created on 14 nov. 2011

@author: Thomas Calmant
'''

from jsonrpclib.SimpleJSONRPCServer import SimpleJSONRPCServer
import erp
import os
import sys

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
        self.erp = erp.Erp()
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

    # Prepare the ERP internals
    erp_proxy = ErpProxy()

    # Prepare the JSON RPC server
    server = SimpleJSONRPCServer(('localhost', 8080))
    server.register_function(erp_proxy.apply_cart, "applyCart")
    server.register_function(erp_proxy.get_item, "getItem")
    server.register_function(erp_proxy.get_items, "getItems")
    server.register_function(erp_proxy.get_items_stock, "getItemsStock")

    server.register_function(erp_proxy.reset_stocks, "reset_stocks")

    try:
        server.serve_forever()

    except KeyboardInterrupt:
        return 0

if __name__ == '__main__':
    sys.exit(main())
