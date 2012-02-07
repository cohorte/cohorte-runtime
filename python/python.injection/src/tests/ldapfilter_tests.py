#!/usr/bin/python3
#-- Content-Encoding: utf-8 --
"""
Created on 7 févr. 2012

@author: Thomas Calmant
"""

from psem2m.ldapfilter import get_ldap_filter
import unittest

# ------------------------------------------------------------------------------

__version__ = (1, 0, 0)

def applyTest(self, filters, key):
    """
    Applies a list of tests according to the given dictionary
    
    Dictionary format : filter -> ([True, results], [False, results])
    
    @param filters: A filters test dictionary
    @param key: The key to use in the property dictionary
    """
    props = {}

    for filter_str, tests in filters.items():
        ldap_filter = get_ldap_filter(filter_str)
        self.assertIsNotNone(ldap_filter, "%s is a valid filter" \
                             % filter_str)

        for good in tests[0]:
            props[key] = good
            self.assertTrue(ldap_filter.matches(props), \
                    "Filter '%s' should match %s" % (ldap_filter, props))

        for bad in tests[1]:
            props[key] = bad
            self.assertFalse(ldap_filter.matches(props), \
                    "Filter '%s' should not match %s" \
                    % (ldap_filter, props))

# ------------------------------------------------------------------------------

class LDAPCriteriaTest(unittest.TestCase):
    """
    Tests for the LDAP criteria behavior
    """

    def testEmptyCriteria(self):
        """
        Empty filter test
        """
        self.assertIsNone(get_ldap_filter(None), "None filter must return None")
        self.assertIsNone(get_ldap_filter(""), "Empty filter must return None")
        self.assertIsNone(get_ldap_filter(" "), \
                          "Trimmed filter must return None")


    def testSimpleCriteria(self):
        """
        Simple boolean filter test
        """
        props = {}

        ldap_filter = get_ldap_filter("(valid=True)")
        self.assertIsNotNone(ldap_filter, "Filter should not be None")

        # Test with a single property
        props["valid"] = True
        self.assertTrue(ldap_filter.matches(props), \
                        "Filter '%s' should match %s" % (ldap_filter, props))

        props["valid"] = False
        self.assertFalse(ldap_filter.matches(props), \
                        "Filter '%s' should not match %s" \
                        % (ldap_filter, props))

        # Test the ignorance of other properties
        props["valid2"] = True
        self.assertFalse(ldap_filter.matches(props), \
                        "Filter '%s' should not match %s" \
                        % (ldap_filter, props))

        props["valid"] = "True"
        self.assertTrue(ldap_filter.matches(props), \
                        "Filter '%s' should match %s" % (ldap_filter, props))


    def testPresenceCriteria(self):
        """
        Test the presence filter
        """
        props = {}

        ldap_filter = get_ldap_filter("(valid=*)")
        self.assertIsNotNone(ldap_filter, "Filter should not be None")

        # Missing value
        self.assertFalse(ldap_filter.matches(props), \
                        "Filter '%s' should not match %s" \
                        % (ldap_filter, props))

        # Still missing
        props["valid2"] = True
        self.assertFalse(ldap_filter.matches(props), \
                        "Filter '%s' should not match %s" \
                        % (ldap_filter, props))

        # Value present
        props["valid"] = True
        self.assertTrue(ldap_filter.matches(props), \
                        "Filter '%s' should match %s" % (ldap_filter, props))

        props["valid"] = False
        self.assertTrue(ldap_filter.matches(props), \
                        "Filter '%s' should match %s" % (ldap_filter, props))

        # Some other type
        props["valid"] = "1234"
        self.assertTrue(ldap_filter.matches(props), \
                        "Filter '%s' should match %s" % (ldap_filter, props))

        # Empty string
        props["valid"] = ""
        self.assertFalse(ldap_filter.matches(props), \
                        "Filter '%s' should not match %s" \
                        % (ldap_filter, props))

        # Empty list
        props["valid"] = []
        self.assertFalse(ldap_filter.matches(props), \
                        "Filter '%s' should not match %s" \
                        % (ldap_filter, props))


    def testStarCriteria(self):
        """
        Tests the start filter on strings
        """
        filters = {}
        # Simple string test
        filters["(string=after*)"] = (("after", "after1234"), \
                                      ("1324after", "before", "After"))

        filters["(string=*before)"] = (("before", "1234before"), \
                                       ("after", "before1234"), "Before")

        filters["(string=*middle*)"] = (("middle", "aaamiddle1234", \
                                         "middle456", "798middle"), \
                                        ("miDDle"))

        filters["(string=*mi*ed*)"] = (("mixed", "mixed1234", "798mixed",
                                        "mi O_O ed"), ("Mixed"))

        # List test
        filters["(string=*li*ed*)"] = ((["listed"], ["toto", "aaaliXed123"]), \
                                      ([], ["LixeD"], ["toto"]))

        applyTest(self, filters, "string")


    def testListCriteria(self):
        """
        Test the presence filter on lists
        """
        filters = {}
        filters["(list=toto)"] = ((["toto"], ["titi", "toto"], \
                                   ["toto", "titi"], \
                                   ["titi", "toto", "tutu"]), \
                                  ([], ["titi"], ["*toto*"]))

        applyTest(self, filters, "list")


    def testInequalityCriteria(self):
        """
        Test the inequality operators
        """
        filters = {}
        filters["(id<10)"] = (("-10", -10, 0, 9), (10, 11, "12"))
        filters["(id<=10)"] = (("-10", -10, 0, 9, 10), (11, "12"))
        filters["(id>=10)"] = ((10, 11, "12"), ("-10", -10, 0, 9))
        filters["(id>10)"] = ((11, "12"), ("-10", -10, 0, 9, 10))

        applyTest(self, filters, "id")


    def testApproximateCriteria(self):
        """
        Tests the approximate criteria
        """
        filters = {}

        # Simple string test
        filters["(string~=aBc)"] = (("abc", "ABC", "aBc", "Abc"),
                                    ("bac", "aDc"))

        # Simple list test
        filters["(string~=dEf)"] = ((["abc", "def"], ["DEF"]),
                                    ([], ["aBc"]))

        # Star test
        filters["(string~=*test*)"] = ((["bigTest", "def"], "test", "TEST42"),
                                    ([], ["aBc"], "T3st"))

        applyTest(self, filters, "string")

# ------------------------------------------------------------------------------

class LDAPFilterTest(unittest.TestCase):
    """
    Tests for the LDAP filter behavior
    """

    def testNot(self):
        """
        Tests the NOT operator
        """
        filters = {}

        filters["(test=False)"] = ((False, [False], [True, False]),
                                   (True, [True], "1123", 1, 0))

        filters["(!(test=False))"] = ((True, [True], "1123", 1, 0),
                                      (False, [False], [True, False]))

        # Simple cases
        applyTest(self, filters, "test")

        # NOT handles only one operand
        self.assertRaises(ValueError, get_ldap_filter, \
                          "(!(test=True)(test2=False))")


    def testAnd(self):
        """
        Tests the AND operator
        """
        props = {}
        ldap_filter = get_ldap_filter("(&(test=True)(test2=False))")

        # Valid
        props["test"] = True
        props["test2"] = False
        self.assertTrue(ldap_filter.matches(props), \
                    "Filter '%s' should match %s" % (ldap_filter, props))

        # Invalid...
        props["test"] = False
        props["test2"] = False
        self.assertFalse(ldap_filter.matches(props), \
                    "Filter '%s' should not match %s" % (ldap_filter, props))

        props["test"] = False
        props["test2"] = True
        self.assertFalse(ldap_filter.matches(props), \
                    "Filter '%s' should not match %s" % (ldap_filter, props))

        props["test"] = True
        props["test2"] = True
        self.assertFalse(ldap_filter.matches(props), \
                    "Filter '%s' should not match %s" % (ldap_filter, props))

    def testOr(self):
        """
        Tests the OR operator
        """
        props = {}
        ldap_filter = get_ldap_filter("(|(test=True)(test2=False))")

        # Valid ...
        props["test"] = True
        props["test2"] = False
        self.assertTrue(ldap_filter.matches(props), \
                    "Filter '%s' should match %s" % (ldap_filter, props))

        props["test"] = False
        props["test2"] = False
        self.assertTrue(ldap_filter.matches(props), \
                    "Filter '%s' should match %s" % (ldap_filter, props))

        props["test"] = True
        props["test2"] = True
        self.assertTrue(ldap_filter.matches(props), \
                    "Filter '%s' should match %s" % (ldap_filter, props))

        # Invalid...
        props["test"] = False
        props["test2"] = True
        self.assertFalse(ldap_filter.matches(props), \
                    "Filter '%s' should not match %s" % (ldap_filter, props))

# ------------------------------------------------------------------------------

if __name__ == "__main__":
    unittest.main()
