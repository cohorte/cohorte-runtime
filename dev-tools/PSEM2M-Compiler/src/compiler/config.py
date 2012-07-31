#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
PSEM2M Compiler: Configuration utilities

:author: Thomas Calmant
"""

import ConfigParser

# ------------------------------------------------------------------------------

class ExtSafeConfigParser(ConfigParser.SafeConfigParser):
    """
    SafeConfigParser with utility methods
    """
    def get_default(self, section, option, default=None):
        """
        Retrieves the found configuration value, or the default one
        
        :param section: Configuration section
        :param option: Configuration option
        :param default: Default value
        :return: The option value or *default*
        """
        try:
            return self.get(section, option)

        except (ConfigParser.NoOptionError, ConfigParser.NoSectionError):
            # Not found
            return default


    def get_list(self, section, option, default=[]):
        """
        Retrieves a list from the configuration
        
        :param section: Configuration section
        :param option: Configuration option
        :param default: Default value
        :return: The parsed list or *default*
        """
        values = self.get_default(section, option)
        if not values:
            # Option not found
            return default

        # Split values
        values = values.split(',')

        # Return the list with stripped entries
        return [value.strip() for value in values if value.strip()]
