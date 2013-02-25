#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: Rating and compatibility storage

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE
import cohorte.composer

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Property

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-rating-factory")
@Provides(cohorte.composer.SERVICE_COMPONENT_RATING)
@Property("_default_rating", "default.rating", 50)
class RatingStorage(object):
    """
    Stores the rating of the factories of a composition
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Default rating
        self._default_rating = 50

        # Factory name -> Rating (double)
        self.__ratings = {}


    def get(self, factory):
        """
        Retrieves the rating associated to the given factory, or returns the
        default rating.
        
        :param factory: A factory name
        :return: The associated rating
        """
        return self.__ratings.get(factory, self._default_rating)


    def store(self, factory, rating):
        """
        Stores/Update the rating for a factory
        
        :param factory: A factory name
        :param rating: Its associated rating
        :return: The previous rating
        """
        if rating > 100:
            # Upper bound
            rating = 100

        old = self.__ratings.get(factory, self._default_rating)
        self.__ratings[factory] = rating
        return old

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-compatibility-factory")
@Provides(cohorte.composer.SERVICE_COMPONENT_COMPATIBILITY)
@Property("_default_compatibility", "default.compatibility", 50)
class CompatibilityStorage(object):
    """
    Stores the rating of the factories of a composition
    """
    def __init__(self):
        """
        Sets up the component
        """
        # Default compatibility
        self._default_compatibility = 50

        # (Factory Name, Factory Name) -> Value
        self.__compatibilities = {}


    def get(self, factoryA, factoryB):
        """
        Retrieves the compatibility level between the given factories
        
        :param factoryA: A factory name
        :param factoryB: Another factory name
        :return: The associated compatibility level
        """
        if factoryA == factoryB:
            # Same factory: maximum value
            return 100

        return self.__compatibilities.get((factoryA, factoryB),
                                          self._default_compatibility)


    def store(self, factoryA, factoryB, compatibility):
        """
        Stores/Update the compatibility level between two factories
        
        :param factoryA: A factory name
        :param factoryB: Another factory name
        :param compatibility: The associated compatibility level
        :return: The previous rating
        """
        if factoryA == factoryB:
            raise ValueError("Trying to change auto-compatibility of {0}" \
                             .format(factoryA))

        if compatibility > 100:
            # Upper bound
            compatibility = 100

        key = (factoryA, factoryB)
        old = self.__compatibilities.get(key, self._default_rating)
        self.__ratings[key] = compatibility
        return old
