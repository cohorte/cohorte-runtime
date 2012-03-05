"""
Utility methods for PSEM2M Python tools

:author: Thomas Calmant
:copyright: Copyright 2012, isandlaTech
:license: GPLv3
:version: 0.2
:status: Alpha

..

    This file is part of iPOPO.

    iPOPO is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    iPOPO is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with iPOPO. If not, see <http://www.gnu.org/licenses/>.
"""

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

def is_string(string):
    """
    Utility method to test if the given parameter is a string
    (Python 2.x, 3.x) or a unicode (Python 2.x) object
    
    :param string: A potential string object
    :return: True if the given object is a string object or a Python 2.6
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
    
    :return: The unicode string constructor
    """
    try:
        # Python 2.x : strings must be converted with the unicode method
        return unicode

    except NameError:
        # Python 3.x, "str" returns a unicode string
        return str
