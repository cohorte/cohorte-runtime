'''
Created on 13 oct. 2011

@author: Thomas Calmant
'''

# ------------------------------------------------------------------------------

class CCartLine(object):
    '''
    Represents a cart line
    '''

    def __init__(self, representation):
        '''
        Constructor
        '''
        self.lineId = representation["lineId"]
        self.itemId = representation["itemId"]
        self.quantity = float(representation["quantity"])


class CCart(object):
    '''
    Represents a DataServer cart
    '''

    def __init__(self, representation):
        '''
        Constructor
        '''
        self.cartId = representation["cartId"]
        self.cartLines = []

        # Parse lines
        for line in representation["cartLines"]:
            self.cartLines.append(CCartLine(line))

# ------------------------------------------------------------------------------

class CErpActionReport(object):
    '''
    Represents an ERP Action Report
    '''
    __slots__ = ('status', 'message', 'reasonInfos', 'qualityLevel')

    def __init__(self, representation):
        '''
        Constructor
        '''

        if not representation:
            self.status = -1
            self.message = "NULL REPRESENTATION"
            self.reasonInfos = "NULL REPRESENTATION"
            self.qualityLevel = 100

        else:
            try:
                self.status = int(representation["status"])
                self.message = representation["message"]
                self.reasonInfos = representation["reasonInfos"]
                self.qualityLevel = int(representation["qualityLevel"])

            except Exception as e:
                print "Error reading CErpActionReport :", e


# ------------------------------------------------------------------------------

class CachedItemBean(object):
    '''
    Represents a cached item
    '''
    __slots__ = ('id', 'name', 'description', 'price', 'qualityLevel')

    def __init__(self, representation):
        '''
        Constructor
        '''
        try:
            self.id = representation["id"]
            self.name = representation["name"]
            self.description = representation["description"]
            self.price = representation["price"]
            self.qualityLevel = int(representation["qualityLevel"])

        except Exception as e:
            print "Error reading CachedItemBean :", e

# ------------------------------------------------------------------------------

class CachedItemStockBean(object):
    '''
    Represents a cached item stock
    '''
    __slots__ = ('id', 'stock', 'qualityLevel')

    def __init__(self, representation):
        '''
        Constructor
        '''
        try:
            self.id = representation["id"]
            self.stock = int(representation["stock"])
            self.qualityLevel = int(representation["qualityLevel"])

        except Exception as e:
            self.id = None
            self.stock = 0
            print "Error reading CachedItemStockBean :", e
