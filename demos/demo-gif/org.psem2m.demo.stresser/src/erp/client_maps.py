'''
Created on 13 oct. 2011

@author: Thomas Calmant
'''

import jsonrpclib

JAVA_CLASS = u"javaClass"

def to_jabsorb(erp_result):
    """
    Adds informations for Jabsorb, if needed
    """
    result = {}

    # Map ?
    if isinstance(erp_result, dict):
        result["map"] = {}

        for key in erp_result.keys():
            result["map"][key] = to_jabsorb(erp_result[key])

        result[JAVA_CLASS] = "java.util.HashMap"

    # List ?
    elif isinstance(erp_result, list):
        result[JAVA_CLASS] = "java.util.ArrayList"
        result["list"] = []

        for item in erp_result:
            result["list"].append(to_jabsorb(item))

    # Other ?
    else:
        result = erp_result

    return result


def from_jabsorb(request):
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
            result[key] = from_jabsorb(request["map"][key])

        return result

    # List ?
    elif java_class.endswith("List"):
        result = []

        for element in request["list"]:
            result.append(from_jabsorb(element))

        return result

    # Other ?
    return request


class DataServerClient(object):
    '''
    Represents a DataServer client
    '''

    def __init__(self, server_host="localhost", server_port=9210):
        '''
        Sets up members
        
        @param server_host: Data Server host
        @param server_port: Data Server port
        '''
        self.__rpc = jsonrpclib.Server("http://" + server_host \
                                       + ":" + str(server_port) \
                                       + "/JSON-RPC")


    def apply_cart(self, cart):
        """
        Calls 'applyCart'
        
        @param cart: A Cart object
        @return: A CErpActionReport object
        """
        cart = to_jabsorb(cart)
        return from_jabsorb(self.__rpc.dataserver.applyCart(cart))


    def get_item(self, item_id):
        """
        Calls 'getItem'
        
        @param item_id: A string
        @return: A CachedItemBean object or None
        """
        return from_jabsorb(self.__rpc.dataserver.getItem(item_id))


    def get_items(self, category, count=10, randomize=False, base_id=""):
        """
        Calls 'getItems'
        
        @param category: A string
        @param count: An integer
        @param randomize: A boolean
        @param base_id: A string
        @return: An array of CachedItemStockBean objects
        """
        response_array = self.__rpc.dataserver.getItems(category, count, \
                                                        randomize, base_id)

        return from_jabsorb(response_array)


    def get_items_stocks(self, items_ids):
        """
        Calls 'getItemsStock'
        
        @param items_ids: An array of strings
        @return: An array of CachedItemStockBean objects
        """
        response_array = self.__rpc.dataserver.getItemsStock(items_ids)

        return from_jabsorb(response_array)
