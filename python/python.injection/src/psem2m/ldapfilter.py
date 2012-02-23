"""
Created on 24 janv. 2012

@author: Thomas Calmant
"""

import io
from psem2m import is_string, get_unicode_creator

# ------------------------------------------------------------------------------

# Escape character
ESCAPE_CHARACTER = '\\'

# Escaped characters (do not insert the ESCAPE CHARACTER here)
# See http://www.ldapexplorer.com/en/manual/109010000-ldap-filter-syntax.htm)
ESCAPED_CHARACTERS = "()&|=<>~*/"

# ------------------------------------------------------------------------------

class LDAPFilter(object):
    """
    Represents an LDAP filter
    """

    AND = 0
    """ 'And' LDAP operation """

    OR = 1
    """ 'Or' LDAP operation """

    NOT = 2
    """ 'Not' LDAP operation """

    def __init__(self, operator):
        """
        Initializer
        """
        self.__subfilters = []
        self.__operator = operator


    def __repr__(self):
        """
        String representation
        """
        return "(%s %s)" % (operator2str(self.__operator), \
               "".join([repr(subfilter) for subfilter in self.__subfilters]))


    def __str__(self):
        """
        String description
        """
        return "Filter(%s, %s)" % (operator2str(self.__operator), \
               ", ".join([str(subfilter) for subfilter in self.__subfilters]))


    def append(self, ldap_filter):
        """
        Appends a filter or a criteria to this filter
        
        @param ldap_filter: An LDAP filter or criteria
        @raise TypeError: If the parameter is not of a known type
        @raise ValueError: If the more than one filter is associated to a
        NOT operator
        """
        if not isinstance(ldap_filter, LDAPFilter) \
        and not isinstance(ldap_filter, LDAPCriteria):
            raise TypeError("Invalid filter type : %s" \
                  % (type(ldap_filter).__name__))

        if len(self.__subfilters) >= 1 and self.__operator == LDAPFilter.NOT:
            raise ValueError("Not operator only handles one child")

        self.__subfilters.append(ldap_filter)


    def matches(self, properties):
        """
        Tests if the given properties matches this LDAP filter and its children
        
        @param properties: A dictionary of properties
        @return: True if the properties matches this filter, else False
        """
        result = False

        for criteria in self.__subfilters:
            if not criteria.matches(properties):
                if self.__operator == LDAPFilter.AND:
                    # A criteria doesn't match in an "AND" test : short cut
                    result = False
                    break

            else:
                result = True
                if self.__operator == LDAPFilter.OR:
                    # At least one match in a "OR" test : short cut
                    break

        if self.__operator == LDAPFilter.NOT:
            # Revert result
            return not result

        return result


    def normalize(self):
        """
        Returns the first meaningful object in this filter.
        """
        size = len(self.__subfilters)
        if size > 1:
            return self

        elif size == 1:

            if self.__operator == LDAPFilter.NOT:
                # NOT is the only operator to accept 1 operand
                return self

            else:
                # Return the only child
                return self.__subfilters[0].normalize()

        # Empty filter
        return None


class LDAPCriteria(object):
    """
    Represents an LDAP criteria
    """
    def __init__(self, name, value, comparator):
        """
        Sets up the criteria
        
        @raise ValueError: If one of the parameters is empty
        """
        if not name or not value or not comparator:
            raise ValueError("Invalid criteria parameter (%s, %s, %s)" \
                             % (name, value, comparator))

        self.name = name
        self.value = value
        self.comparator = comparator


    def __repr__(self):
        """
        String representation
        """
        comparator = comparator2str(self.comparator)
        return "Criteria(%s, %s, %s)" % (self.name, comparator, self.value)


    def __str__(self):
        """
        String description
        """
        comparator = comparator2str(self.comparator)
        return "(%s%s%s)" % (self.name, comparator, self.value)


    def matches(self, properties):
        """
        Tests if the given criteria matches this LDAP criteria
        
        @param properties: A dictionary of properties
        @return: True if the properties matches this criteria, else False
        """
        if self.name not in properties:
            # Property is not even is the property
            return False

        # Use the comparator
        return self.comparator(self.value, properties[self.name])


    def normalize(self):
        """
        Returns this criteria
        """
        return self

# ------------------------------------------------------------------------------

def comparator2str(comparator):
    """
    Converts an operator method to a string
    """
    if comparator == __comparator_approximate:
        return "~="

    elif comparator == __comparator_eq:
        return "="

    elif comparator == __comparator_le:
        return "<="

    elif comparator == __comparator_lt:
        return "<"

    elif comparator == __comparator_ge:
        return ">="

    elif comparator == __comparator_gt:
        return ">"


def operator2str(operator):
    """
    Converts an operator value to a string
    """
    if operator == LDAPFilter.AND:
        return '&'

    elif operator == LDAPFilter.OR:
        return '|'

    elif operator == LDAPFilter.NOT:
        return '!'

    return '<unknown>'


def escape_LDAP(ldap_string):
    """
    Escape a string to let it go in an LDAP filter
    
    @param ldap_string: The string to escape
    @return: The protected string
    """
    if ldap_string is None:
        return None

    assert is_string(ldap_string)

    # Protect escape character previously in the string
    ldap_string = ldap_string.replace(ESCAPE_CHARACTER, \
                                      ESCAPE_CHARACTER + ESCAPE_CHARACTER)

    # Escape other characters
    for escaped in ESCAPED_CHARACTERS:
        ldap_string = ldap_string.replace(escaped, ESCAPE_CHARACTER + escaped)

    return ldap_string


def unescape_LDAP(ldap_string):
    """
    Unespaces an LDAP string
    
    @param ldap_string: The string to unescape
    @return: The unprotected string
    """
    if ldap_string is None:
        return None

    assert is_string(ldap_string)

    # Prepare the unicode string constructor
    u_str = get_unicode_creator()

    i = 0
    escaped = False

    output = io.StringIO()
    while i < len(ldap_string):

        if not escaped and ldap_string[i] == ESCAPE_CHARACTER:
            # Escape character found
            escaped = True

        else:
            escaped = False
            # Convert the sub-string to unicode if needed 
            output.write(u_str(ldap_string[i]))

        i += 1

    result = output.getvalue()
    output.close()
    return result

# ------------------------------------------------------------------------------

def __comparator_star(filter_value, tested_value):
    """
    Tests a filter with a joker  
    """
    if filter_value == '*':
        # The filter value is a joker : simple presence test
        if tested_value is None:
            # Refuse None values
            return False

        elif hasattr(tested_value, "__len__"):
            # Refuse empty values
            return len(tested_value) != 0

        else:
            # Presence validated
            return True

    if not is_string(tested_value):
        # Unhandled value type...
        return False

    parts = filter_value.split('*')

    i = 0
    last_part = len(parts) - 1

    idx = 0
    for part in parts:
        # Find the part in the tested value
        idx = tested_value.find(part, idx)

        if idx == -1:
            # Part not found
            return False

        if i == 0 and len(part) != 0 and idx != 0:
            # First part is not a star, but the tested value is not at
            # position 0 => Doesn't match
            return False

        if i == last_part and len(part) != 0 \
        and idx != len(tested_value) - len(part):
            # Last tested part is not at the end of the sequence
            return False

        # Be sure to test the next part
        idx += len(part)
        i += 1

    # Whole test passed
    return True


def __comparator_eq(filter_value, tested_value):
    """
    Tests if the filter value is equal to the tested value 
    """
    if isinstance(tested_value, list):
        # Special case : lists

        if '*' in filter_value:
            # Special case : jokers (stars) in the filter
            for value in tested_value:
                if __comparator_star(filter_value, value):
                    # One match
                    return True

        # Convert the list items to strings
        for value in tested_value:

            # Try with the string conversion
            if not is_string(value):
                value_str = repr(value)
            else:
                value_str = value

            if filter_value == value_str:
                # Match !
                return True

        # Value not found
        return False

    elif '*' in filter_value:
        # Special case : jokers (stars) in the filter
        return __comparator_star(filter_value, tested_value)

    # Standard comparison
    if not is_string(tested_value):
        return filter_value == repr(tested_value)

    return filter_value == tested_value


def __comparator_approximate(filter_value, tested_value):
    """
    Tests if the filter value is nearly equal to the tested value.
    
    If the tested value is a string or an array of string, it compares their
    lower case forms
    """
    if is_string(tested_value):
        return __comparator_eq(filter_value.lower(), tested_value.lower())

    elif isinstance(tested_value, list):

        new_tested = [value.lower() for value in tested_value \
                      if is_string(value)]

        if len(new_tested) != len(tested_value):
            # Not strings, use the basic comparison
            return __comparator_eq(filter_value, tested_value)

        else:
            # Compare converted values
            return __comparator_eq(filter_value.lower(), new_tested)

    else:
        return __comparator_eq(filter_value, tested_value)


def __comparator_le(filter_value, tested_value):
    """
    Tests if the filter value is greater than the tested value
    
    tested_value <= filter_value
    """
    return __comparator_lt(filter_value, tested_value) \
        or __comparator_eq(filter_value, tested_value)


def __comparator_lt(filter_value, tested_value):
    """
    Tests if the filter value is strictly greater than the tested value
    
    tested_value < filter_value
    """
    if is_string(filter_value):
        try:
            # Try a conversion
            filter_value = type(tested_value)(filter_value)
        except:
            # Incompatible values
            return False

    return tested_value < filter_value


def __comparator_ge(filter_value, tested_value):
    """
    Tests if the filter value is lesser than the tested value
    
    tested_value >= filter_value
    """
    return __comparator_gt(filter_value, tested_value) \
        or __comparator_eq(filter_value, tested_value)


def __comparator_gt(filter_value, tested_value):
    """
    Tests if the filter value is strictly lesser than the tested value
    
    tested_value > filter_value
    """
    if is_string(filter_value):
        try:
            # Try a conversion
            filter_value = type(tested_value)(filter_value)
        except:
            # Incompatible values
            return False

    return tested_value > filter_value

# ------------------------------------------------------------------------------

def _compute_comparator(string, idx):
    """
    Tries to compute the LDAP comparator at the given index
    
    Valid operators are :
    
    * = : equality
    * <= : less than
    * >= : greater than
    * ~= : approximate
    
    @param string: A LDAP filter string
    @param idx: An index in the given string
    @return: The corresponding operator, None if unknown
    """
    part1 = string[idx]
    if part1 == '=':
        # Equality
        return __comparator_eq

    elif len(string) < idx + 2:
        # Too short string
        return None

    elif string[idx + 1] != '=':
        # It's a "strict" operator
        if part1 == '<':
            # Strictly lesser
            return __comparator_lt

        elif part1 == '>':
            # Strictly greater
            return __comparator_gt

        else:
            # Invalid operator
            return None

    if part1 == '<':
        # Less or equal
        return __comparator_le

    elif part1 == '>':
        # Greater or equal
        return __comparator_ge

    elif part1 == '~':
        # Approximate equality
        return __comparator_approximate

    return None


def _compute_operation(string, idx):
    """
    Tries to compute the LDAP operation at the given index
    
    Valid operations are :
    
    * & : AND
    * | : OR
    * ! : NOT
    
    @param string: A LDAP filter string
    @param idx: An index in the given string
    @return: The corresponding operator (AND, OR or NOT)
    """
    operator = string[idx]

    if operator == '&':
        return LDAPFilter.AND

    elif operator == '|':
        return LDAPFilter.OR

    elif operator == '!':
        return LDAPFilter.NOT

    return None


def _skip_spaces(string, idx):
    """
    Retrieves the next non-space character after idx index in the given string
    
    @param string: The string to look into
    @param idx: The base search index
    @return: The next non-space character index, -1 if not found
    """
    i = idx
    size = len(string)

    while i < size:
        if not string[i].isspace():
            return i

        i += 1

    return -1


def _parse_LDAP_criteria(ldap_filter, startidx, endidx):
    """
    Parses an LDAP sub filter (criteria)
    
    @param ldap_filter: An LDAP filter string
    @param startidx: Sub-filter start index
    @param endidx: Sub-filter end index
    @return: The LDAP sub-filter
    @raise ValueError: Invalid sub-filter
    """
    comparators = "=<>~"

    # Get the comparator
    i = startidx
    escaped = False
    while i < endidx:

        if not escaped:

            if ldap_filter[i] == ESCAPE_CHARACTER:
                # Next character escaped
                escaped = True

            elif ldap_filter[i] in comparators:
                # Comparator found
                break

        else:
            # Escaped character ignored
            escaped = False

        i += 1

    else:
        # Comparator never found
        raise ValueError("Comparator not found in '%s'" \
                         % ldap_filter[startidx:endidx])

    if i == startidx:
        # Attribute name is missing
        raise ValueError("Attribute name is missing in '%s'" \
                         % ldap_filter[startidx:endidx])

    comparator = _compute_comparator(ldap_filter, i)
    if comparator is None:
        # Unknown comparator
        raise ValueError("Unknown comparator in '%s' - %s\nFilter : %s" \
                         % (ldap_filter[startidx:endidx], ldap_filter[i], \
                            ldap_filter))

    # The attribute name can be extracted directly
    attribute_name = ldap_filter[startidx:i].strip()

    # Find the end of the comparator
    i += 1
    while ldap_filter[i] in comparators:
        i += 1

    # Skip spaces
    i = _skip_spaces(ldap_filter, i)

    # Extract the value name
    value = ldap_filter[i:endidx].strip()

    return LDAPCriteria(unescape_LDAP(attribute_name), unescape_LDAP(value),
                        comparator)


def _parse_LDAP(ldap_filter):
    """
    Parses the given LDAP filter string
    
    @param ldap_filter: An LDAP filter string
    @return: An LDAPFilter object, None if the filter was empty
    @raise ValueError: The LDAP filter string is invalid
    """
    if not ldap_filter:
        return None

    assert is_string(ldap_filter)

    # Beginning of the filter
    idx = _skip_spaces(ldap_filter, 0)
    if idx == -1:
        # No non-space character found
        return None

    escaped = False
    filter_len = len(ldap_filter)
    root = None
    stack = []
    subfilter_stack = []

    while idx < filter_len:

        if not escaped:
            if ldap_filter[idx] == '(':
                # Opening filter : get the operator
                idx = _skip_spaces(ldap_filter, idx + 1)
                if idx == -1:
                    raise ValueError("Missing filter operator")

                operator = _compute_operation(ldap_filter, idx)
                if operator is not None:
                    # New sub-filter
                    stack.append(LDAPFilter(operator))

                else:
                    # Sub-filter content
                    subfilter_stack.append(idx)

            elif ldap_filter[idx] == ')':
                # Ending filter : store it in its parent

                if len(subfilter_stack) != 0:
                    # Criteria finished
                    startidx = subfilter_stack.pop()
                    criteria = _parse_LDAP_criteria(ldap_filter, startidx, idx)

                    if len(stack) != 0:
                        top = stack.pop()
                        top.append(criteria)
                        stack.append(top)
                    else:
                        # No parent : filter contains only one criteria
                        # Make a parent to stay homogeneous
                        root = LDAPFilter(LDAPFilter.AND)
                        root.append(criteria)

                elif len(stack) != 0:
                    # Sub filter finished
                    ended_filter = stack.pop()

                    if len(stack) != 0:
                        top = stack.pop()
                        top.append(ended_filter)
                        stack.append(top)

                    else:
                        # End of the parse
                        root = ended_filter

                else:
                    raise ValueError("Too many end of parenthesis @%d: %s" % \
                                     (idx, ldap_filter[idx:]))

            elif ldap_filter[idx] == '\\':
                # Next character must be ignored
                escaped = True

        else:
            # Escaped character ignored
            escaped = False

        # Don't forget to increment...
        idx += 1

    # Return the root of the filter
    return root.normalize()


def get_ldap_filter(ldap_filter):
    """
    Retrieves the LDAP filter object corresponding to the given filter.
    Parses it the argument if it is an LDAPFilter instance
    
    @param ldap_filter: An LDAP filter (LDAPFilter or string)
    @return: The corresponding filter, can be None
    @raise ValueError: Invalid filter string found
    @raise TypeError: Unknown filter type
    """
    if ldap_filter is None:
        return None

    if isinstance(ldap_filter, LDAPFilter) \
    or isinstance(ldap_filter, LDAPCriteria):
        # No conversion needed
        return ldap_filter

    elif is_string(ldap_filter):
        # Parse the filter
        return _parse_LDAP(ldap_filter)

    # Unknown type
    raise TypeError("Unhandled filter type %s" % type(ldap_filter).__name__)


def combine_filters(filters, operator=LDAPFilter.AND):
    """
    Combines two LDAP filters, which can be strings or LDAPFilter objects
    
    @param filters: Filters to combine
    @param operator: The operator for combination
    @return: The combined filter, can be None if all filters are None
    @raise ValueError: Invalid filter string found
    @raise TypeError: Unknown filter type
    """
    if not filters:
        return None

    if not isinstance(filters, list):
        raise TypeError("Filters argument must be a list")

    # Remove None filters and convert others
    ldap_filters = []
    for sub_filter in filters:
        if sub_filter is None:
            # Ignore None filters
            continue

        ldap_filter = get_ldap_filter(sub_filter)
        if ldap_filter is not None:
            # Valid filter
            ldap_filters.append(ldap_filter)

    if len(ldap_filters) == 0:
        # Do nothing
        return None

    elif len(ldap_filters) == 1:
        # Only one filter, return it
        return ldap_filters[0]

    new_filter = LDAPFilter(operator)

    for sub_filter in ldap_filters:
        # Direct combination
        new_filter.append(sub_filter)

    return new_filter.normalize()
