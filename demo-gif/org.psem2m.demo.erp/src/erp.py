#-- Content-Encoding: utf-8 --
'''
Created on 5 oct. 2011

@author: "Thomas Calmant"
'''

import os
import random
import xml_item_parser

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
                xmlNodes = parser.parse_file(source_file)

                if not xmlNodes or "items" not in xmlNodes:
                    # No items in the file
                    continue

                items = []
                for itemsNode in xmlNodes["items"]:
                    if "item" in itemsNode:
                        for itemNode in itemsNode["item"]:
                            items.append(itemNode)

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


    def get_item(self, item_id):
        """
        Retrieves the item bean corresponding to the given ID. Returns None
        if not found.
        
        @return: The item bean (map) or None
        """
        if item_id == "?" and len(self.__items) > 0:
            # Special name for random item
            item_id = random.choice(self.__items)

        if item_id in self.__items:
            return self.__items[item_id]

        return None


    def get_items(self, category, count=0, randomize=False, base_id=None):
        """
        Retrieves *count* items of the given category, None if the category is
        unknown.
        
        If *randomize* is set to True, returns a random list of *count* items.
        If *base_id* is set, returns the list of *count* items after it.
        
        *randomize* and *base_id* are mutually exclusive.
        
        @param category: The category to list
        @param count: Number of items to retrieve (<= 0 for unlimited)
        @param randomize: 
        @return: A list of items, or None
        """
        if category not in self.__categories:
            # No category found
            return

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
