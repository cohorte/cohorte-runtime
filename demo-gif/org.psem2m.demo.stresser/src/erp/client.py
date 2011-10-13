'''
Created on 13 oct. 2011

@author: Thomas Calmant
'''

import jsonrpclib

import beans

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
        return beans.CErpActionReport(self.__rpc.dataserver.applyCart(cart))


    def get_item(self, item_id):
        """
        Calls 'getItem'
        
        @param item_id: A string
        @return: A CachedItemBean object or None
        """
        return beans.CachedItemBean(self.__rpc.dataserver.getItem(item_id))


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

        result_array = []
        if response_array != None:
            # Convert the array
            for item in response_array:
                result_array.append(beans.CachedItemBean(item))

        return result_array


    def get_items_stocks(self, items_ids):
        """
        Calls 'getItemsStock'
        
        @param items_ids: An array of strings
        @return: An array of CachedItemStockBean objects
        """
        response_array = self.__rpc.dataserver.getItemsStock(items_ids)

        result_array = []
        if response_array != None:
            # Convert the array
            for stock in response_array:
                result_array.append(beans.CachedItemStockBean(stock))

        return result_array
