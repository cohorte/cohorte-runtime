'''
Created on 13 oct. 2011

@author: Thomas Calmant
'''

import beans
import client

import random
import sys
import time
import traceback
import urllib

class Stresser(object):
    '''
    The DataServer stresser
    '''
    CATEGORIES = ('screens', 'mouses')

    def __init__(self, name, server_host="localhost", \
                 server_port=9210, erp_port=8080):
        '''
        Constructor
        '''
        self.name = name
        self.__client = client.DataServerClient(server_host, server_port)
        self.__server_host = server_host
        self.__server_port = server_port
        self.__erp_port = erp_port

        # Scenario 1
        self._last_id = ""

        # Scenario 3
        self._mode = "ON"


    def scenario_1(self):
        """
        Scenario 1 : getItems
        """
        # Get items
        category = random.choice(Stresser.CATEGORIES)
        nb_items = random.randint(5, 30)
        items = self.__client.get_items(category, nb_items, False, \
                                        self._last_id)

        # Validate result
        if not items:
            # Special case : we may be at the last page of items for this
            # category
            self._last_id = ""
            return True

        elif len(items) > nb_items:
            print >> sys.stderr, "Too many items returned :", len(items)
            return False

        # Get stocks
        items_ids = [item.id for item in items]
        stocks = self.__client.get_items_stocks(items_ids)

        # Validate result
        if not stocks:
            print >> sys.stderr, "No stocks returned"
            return False

        elif len(stocks) != len(items):
            print >> sys.stderr, "Bad number of stocks returned :", \
                                    len(stocks), "/", len(items)
            return False

        # Set last ID
        self._last_id = items_ids[-1]
        return True


    def scenario_2(self):
        """
        Scenario 2 : applyCart
        """
        # Get items
        category = random.choice(Stresser.CATEGORIES)
        items = self.__client.get_items(category, 10, True, "")

        # Validate result
        if not items:
            print >> sys.stderr, "No items returned"
            return False

        # Get stocks
        items_ids = [item.id for item in items]
        stocks = self.__client.get_items_stocks(items_ids)

        # Validate result
        if not stocks:
            print >> sys.stderr, "No stocks returned"
            return False

        elif len(stocks) != len(items):
            print >> sys.stderr, "Bad number of stocks returned :", \
                                    len(stocks), "/", len(items)
            return False

        # Filter items
        selected_items = [item for item in zip(items_ids, stocks) \
                          if item[1].stock > 0]
        if not selected_items:
            print >> sys.stderr, "No item available"
            return False

        # Prepare cart lines
        line = 0
        cart_lines = []
        for item in selected_items:
            quantity = random.randint(1, item[1].stock)
            cart_lines.append({"lineId": line, "itemId": item[0], \
                               "quantity": quantity})
            line += 1

        # Prepare the cart
        cart = beans.CCart({"cartId":"cart " + self.name, \
                            "cartLines": cart_lines})

        # Send it
        self.__client.apply_cart(cart)
        return True


    def scenario_3(self):
        """
        Scenario 3 : start/stop ERP
        """
        try:
            urllib.urlopen("http://" + self.__server_host + ":" \
                           + str(self.__erp_port) + "/setStateERP?activate=" \
                           + self._mode)

            if self._mode == "ON":
                self._mode = "OFF"
            else:
                self._mode = "ON"

            return True

        except:
            return False


    def scenario_4(self):
        """
        Scenario 4 : toggle the Quarterback component state
        """
        #try:
        # Use HTTP lib
        urllib.urlopen("http://" + self.__server_host + ":" \
                       + str(self.__server_port) + "/psem2m-signal-receiver" + \
                       "/demo/core/quarterback/toggle", data="")

        return True

        #except:
        #    return False

    def run(self, scenario_id, parameters):
        """
        Main loop
        """

        # Delay if any
        if "delay" in parameters:
            print self.name, "delayed..."
            time.sleep(parameters["delay"])

        print self.name, "running..."

        if scenario_id == 1:
            method = self.scenario_1
        elif scenario_id == 2:
            method = self.scenario_2
        elif scenario_id == 3:
            method = self.scenario_3
        elif scenario_id == 4:
            method = self.scenario_4
        else:
            print "Bad scenario number :", scenario_id
            return

        i = 0
        nb_iterations = parameters["nb_iterations"]
        errors = 0

        param_think_time = parameters["think_time"]

        if "think_time_min" in parameters:
            param_think_time_min = parameters["think_time_min"]
        else:
            param_think_time_min = 5

        if "think_time_max" in parameters:
            param_think_time_max = parameters["think_time_max"]
        else:
            param_think_time_max = 10

        start_time = time.time()
        while i < nb_iterations:

            try:
                if not method():
                    errors += 1

            except Exception as ex:
                errors += 1
                print "Error calling DataServer :", ex
                traceback.print_exc()

            if param_think_time <= 0:
                think_time = random.randint(param_think_time_min, \
                                            param_think_time_max)
            else:
                think_time = param_think_time

            time.sleep(think_time)
            i += 1
            if not i % 10:
                print self.name, ":", i, "requests done"

        delta = time.time() - start_time
        print self.name, ":", nb_iterations, "requests in", \
                delta, "seconds -", errors, "errors occurred"
