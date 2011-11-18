#-- Content-Encoding: utf-8 --
'''
Created on 5 oct. 2011

@author: "Thomas Calmant"
'''

import os
import random
import threading
import time
import xml_item_parser

# ------------------------------------------------------------------------------

class ErpActionReport(object):
    """
    Describes an ERP result bean
    """
    def __init__(self, code=200, message="", reason=""):
        """
        Prepares the report members
        """
        self.code = code
        self.message = message
        self.reason = reason

# ------------------------------------------------------------------------------

class Erp(object):
    """
    Describes the ERP state
    """
    def __init__(self):
        """
        Prepares the ERP members
        """
        # Prepare random stuffs
        random.seed()

        # Prepare members 
        self.__running = True
        self.__categories = dict()
        self.__items = dict()
        self.__stocks = dict()
        self.__nb_requests = 0
        self.__start_time = 0
        self.__last_time = 0

        # Stock lock
        self.__stock_lock = threading.RLock()


    def load_content(self, source_folder):
        """ 
        Loads the ERP data from XML files in the given folder (not recursive)
        
        @param source_folder: Folder to look into
        """
        self.__categories = dict()
        self.__items = dict()
        self.__stocks = dict()

        folder_content = os.listdir(source_folder)

        for member in folder_content:
            if member.endswith(".xml"):

                category = member[:-len(".xml")]
                source_file = source_folder + os.sep + member

                parser = xml_item_parser.XmlItemParser()
                xml_nodes = parser.parse_file(source_file)

                if not xml_nodes or "items" not in xml_nodes:
                    # No items in the file
                    continue

                items = []
                for items_node in xml_nodes["items"]:
                    if "item" in items_node:
                        for item_node in items_node["item"]:
                            items.append(item_node)

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


    def apply_cart(self, cart_id, cart_content, update_stats=True):
        """
        Applies the stock modifications according to the cart content
        
        @param cart_id: The identifier of the cart
        @param cart_content: Content of the cart, a dictionary with at least
        "id" and "quantity" entries.
        @param update_stats: Update the ERP statistics
        @return: An ERP Action Report
        """
        if not cart_id or not cart_content:
            # Invalid cart
            return ErpActionReport(400, "Cart is invalid", \
                                   "No cart has no ID or content.")

        if update_stats:
            self.__update_stats()

        success = True
        reason = ""

        with self.__stock_lock:

            applied_mods = []

            for item in cart_content:
                # Validate the cart
                item_id = item["id"]
                quantity = int(item["quantity"])

                current_stock = self.get_item_stock(item_id, False)
                new_stock = current_stock - quantity

                if new_stock < 0:
                    return ErpActionReport(500, "Insufficient stock", \
                                           "Can't get " + str(quantity) \
                                           + item_id)

                # Valid item
                applied_mods.append((item_id, new_stock))


            for item_id, new_stock in applied_mods:
                # Apply modifications to the stock
                if not self.set_item_stock(item_id, new_stock, False):
                    success = False
                    reason += "Can't set item '" + str(item_id)
                    reason += "' stock value to : " + str(new_stock) + "\n"
                    return


        if success:
            code = 200
            message = "Cart applied"

        else:
            code = 500
            message = "Can't apply the cart"

        return ErpActionReport(code, message, reason)


    def get_categories(self, update_stats=True):
        """
        Retrieves the list of all known categories
        
        @param update_stats: Update the ERP statistics
        @return: All known categories
        """
        if update_stats:
            self.__update_stats()

        return self.__categories.keys()


    def get_item(self, item_id, update_stats=True):
        """
        Retrieves the item bean corresponding to the given ID. Returns None
        if not found.
        
        @param update_stats: Update the ERP statistics
        @return: The item bean (map) or None
        """
        if update_stats:
            self.__update_stats()

        if item_id == "?" and len(self.__items) > 0:
            # Special name for random item
            item_id = random.choice(self.__items.keys())

        if item_id in self.__items:
            return self.__items[item_id]

        return None


    def get_items(self, category, count=0, randomize=False, base_id=None, \
                  update_stats=True):
        """
        Retrieves *count* items of the given category, None if the category is
        unknown.
        
        If *randomize* is set to True, returns a random list of *count* items.
        If *base_id* is set, returns the list of *count* items after it.
        
        *randomize* and *base_id* are mutually exclusive.
        
        @param category: The category to list
        @param count: Number of items to retrieve (<= 0 for unlimited)
        @param randomize: Prepare a random result
        @param base_id: Base item identifier for pagination
        @param update_stats: Update the ERP statistics
        @return: A list of items, or None
        """
        if update_stats:
            self.__update_stats()

        if category not in self.__categories:
            # No category found
            print "Category", category, "not in", str(self.__categories)
            return []

        if randomize:
            # Retrieve random items
            item_ids = self.__random_items(category, count)

        else:
            # Retrieve elements after base_id
            item_ids = self.__paginate_items(category, base_id, count)

        items = []
        if item_ids:
            for item_id in item_ids:
                # Store each element in a list
                if item_id in self.__items:
                    items.append(self.__items[item_id])

        return items


    def __random_items(self, category, max_count):
        """
        Retrieves *count* random item IDs from the given *category*.
        
        @param category: A valid category
        @param max_count: Maximum number of items to retrieve (<= 0 for
        unlimited)
        
        @return: Random item IDs, None on error
        """
        item_ids = self.__categories[category]
        if not item_ids:
            return None

        if max_count <= 0:
            # Unlimited result size
            sample_size = len(item_ids)

        else:
            # Sample size can't be greater than the number of items
            sample_size = min(len(item_ids), max_count)

        return random.sample(item_ids, sample_size)


    def __paginate_items(self, category, base_id, count):
        """
        Returns a page of items from the given category
        
        @param category: Items category
        @param base_id: All retrieved item are stored after the given ID
        (None to retrieve data from the beginning)
        @param count: Maximum number of items to retrieve
        """
        items_ids = self.__categories[category]
        if not items_ids:
            return None

        # Compute the base index
        base_index = 0
        if base_id and base_id in items_ids:
            # + 1 : we want the element after the base index
            base_index = items_ids.index(base_id) + 1

        # Base index is the last element : return an empty array
        if base_index >= len(items_ids):
            return []

        # Set the end index
        if count <= 0:
            end_index = len(items_ids)
        else:
            end_index = base_index + count

        # Return the IDs subset
        return items_ids[base_index:end_index]


    def get_item_stock(self, item_id, update_stats=True):
        """
        Retrieves the availability of the given item (int)
        
        @param item_id: ID of the item
        @param update_stats: Update the ERP statistics
        @return: The available stock for the given item, -1 if the item is
        unknown
        """
        if update_stats:
            self.__update_stats()

        if item_id not in self.__stocks:
            return -1

        # Synchronized block
        with self.__stock_lock:
            return int(self.__stocks[item_id])


    def reset_stocks(self, new_stock):
        """
        Resets all stocks
        """
        for item in self.__stocks:
            self.__stocks[item] = new_stock


    def set_item_stock(self, item_id, new_stock, update_stats=True):
        """
        Set the stock value for the given item
        
        @param item_id: ID of the item
        @param new_stock: New item stock (int)
        @param update_stats: Update the ERP statistics
        @return: True on success, False on error (unknown id, ...) 
        """
        if update_stats:
            self.__update_stats()

        if item_id not in self.__stocks:
            return False

        # Synchronized block
        with self.__stock_lock:
            try:
                old_stock = self.__stocks[item_id]
                self.__stocks[item_id] = int(new_stock)

                print "Item :", item_id, "- old stock :", old_stock, \
                      "- new stock :", int(new_stock)
                return True

            except:
                # Error in conversion or in assignment...
                return False


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


    def reset_stats(self):
        """
        Resets the requests statistics
        """
        self.__nb_requests = 0
        self.__start_time = 0
        self.__last_time = 0


    def __update_stats(self):
        """
        Updates the ERP statistics
        """
        if not self.__nb_requests:
            # First handled request
            self.__start_time = time.time()

        self.__nb_requests += 1
        self.__last_time = time.time()


    def get_stats(self):
        """
        Retrieves the number of requests handled
        """
        nb_requests = self.__nb_requests
        total_time = self.__last_time - self.__start_time

        if not total_time:
            # Avoid a division by 0
            total_time = 1

        return {"requests": self.__nb_requests, "time": total_time, \
                "average": nb_requests / total_time}
