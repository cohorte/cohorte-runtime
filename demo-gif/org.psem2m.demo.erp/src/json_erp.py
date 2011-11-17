'''
Created on 14 nov. 2011

@author: Thomas Calmant
'''

from jsonrpclib.SimpleJSONRPCServer import SimpleJSONRPCServer
from pprint import pformat
import erp
import os
import sys

class ErpProxy(object):
    """
    ERP Proxy, for Jabsorb
    """

    JAVA_CLASS = u"javaClass"
    """
    Java class map key
    """

    def __init__(self):
        """
        Constructor
        """
        self.erp = erp.Erp()
        self.erp.load_content(os.getcwd() + os.sep + "data")


    def apply_cart(self, cart_map):
        """
        Calls applyCart for Jabsorb
        """
        # Compute parameters
        cart_id = cart_map["cartId"]
        cart_content = cart_map["lines"]

        # Call the ERP
        erp_result = self.erp.apply_cart(cart_id, cart_content)
        print "ERP Result :", pformat(erp_result)

        return self.result_to_jabsorb(erp_result)


    def get_item(self, item_id):
        """
        Calls getItem for Jabsorb
        """
        # Call the ERP
        erp_result = self.erp.get_item(item_id)

        return self.result_to_jabsorb(erp_result)


    def get_items(self, category, count, randomize, base_id):
        """
        Calls getItems for Jabsorb
        """
        # Call the ERP
        erp_result = self.erp.get_items(category, count, randomize, base_id)

        return self.result_to_jabsorb(erp_result)


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

        return self.result_to_jabsorb(result)


    def result_to_jabsorb(self, erp_result):
        """
        Adds informations for Jabsorb, if needed
        """
        result = {}

        # Map ?
        if isinstance(erp_result, dict):
            result[ErpProxy.JAVA_CLASS] = "java.util.HashMap"
            result["map"] = {}

            for key in erp_result.keys():
                if key != ErpProxy.JAVA_CLASS:
                    result["map"][key] = self.result_to_jabsorb(erp_result[key])

        # List ?
        elif isinstance(erp_result, list):
            result[ErpProxy.JAVA_CLASS] = "java.util.ArrayList"
            result["list"] = []

            for item in erp_result:
                result["list"].append(self.result_to_jabsorb(item))

        # Other ?
        else:
            result = erp_result

        return result


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

    try:
        server.serve_forever()

    except KeyboardInterrupt:
        return 0

if __name__ == '__main__':
    sys.exit(main())
