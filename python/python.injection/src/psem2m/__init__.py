"""
Created on 25 janv. 2012

@author: Thomas Calmant
"""

def is_string(string):
    """
    Utility method to test if the given parameter is a string
    (Python 2.x, 3.x) or a unicode (Python 2.x) object
    
    @param string: A potential string object
    @return: True if the given object is a string object or a Python 2.6
    unicode object 
    """
    try:
        return isinstance(string, str) or isinstance(string, unicode)

    except NameError:
        return False


def get_unicode_creator():
    """
    Returns the unicode string constructor, according to the running Python
    version.
    
    Utility for Python 2.x & 3.x compatibility from :
    http://hacks-galore.org/aleix/blog/archives/2010/10/14/bitpacket-python-2-x-and-3-0-compatibility
    
    @return: The unicode string constructor
    """
    try:
        # Python 2.x : strings must be converted with the unicode method
        return unicode

    except NameError:
        # Python 3.x, "str" returns a unicode string
        return str
