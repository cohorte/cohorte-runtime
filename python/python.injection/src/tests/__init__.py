"""
Test package for Pelix

@author: Thomas Calmant
"""

import unittest

# Small trick to add missing assertions in Python 2.6
def assertNotIn(self, member, container, msg=None):
    """
    Just like self.assertTrue(a not in b), but with a nicer default message.
    
    (code from CPython 3.1)
    """
    if member in container:
        standardMsg = '%r unexpectedly found in %r' % (member, container)
        self.fail(self._formatMessage(msg, standardMsg))


def assertIs(self, expr1, expr2, msg=None):
    """
    Just like self.assertTrue(a is b), but with a nicer default message.
    
    (code from CPython 3.1)
    """
    if expr1 is not expr2:
        standardMsg = '%r is not %r' % (expr1, expr2)
        self.fail(self._formatMessage(msg, standardMsg))


def assertIsNone(self, obj, msg=None):
    """
    Same as self.assertTrue(obj is None), with a nicer default message.
    
    (code from CPython 3.1)
    """
    if obj is not None:
        standardMsg = '%r is not None' % obj
        self.fail(self._formatMessage(msg, standardMsg))


def assertIsNotNone(self, obj, msg=None):
    """
    Included for symmetry with assertIsNone.
    
    (code from CPython 3.1)
    """
    if obj is None:
        standardMsg = 'unexpectedly None'
        self.fail(self._formatMessage(msg, standardMsg))

INJECTED_METHODS = (assertNotIn, assertIs, assertIsNone, assertIsNotNone)

for method in INJECTED_METHODS:
    if not hasattr(unittest.TestCase, method.__name__):
        # Inject the missing method
        setattr(unittest.TestCase, method.__name__, method)
