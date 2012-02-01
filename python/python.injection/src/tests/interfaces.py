#-- Content-Encoding: utf-8 --
"""
Created on 1 févr. 2012

@author: Thomas Calmant
"""

__version__ = (1, 0, 0)

class IEchoService:
    """
    Interface of an echo service
    """
    def echo(self, value):
        """
        Returns the given value
        """
