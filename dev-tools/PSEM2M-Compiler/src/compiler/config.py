#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
PSEM2M Compiler: Configuration utilities

:author: Thomas Calmant
"""

import ConfigParser
import os

# ------------------------------------------------------------------------------

def expand_path(path):
    """
    Expands the given path
    
    :param path: A path
    :return: The expanded path
    """
    return os.path.abspath(os.path.expanduser(os.path.expandvars(path)))

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


    def get_path(self, section, option, default=None):
        """
        Retrieves a path from the configuration.
        
        Same as ``get_default()``, but expanding the path
        
        :param section: Configuration section
        :param option: Configuration option
        :param default: Default value
        :return: The expanded path or *default*
        """
        path = self.get_default(section, option, None)
        if not path:
            # Path not found
            return default

        # Expand the path
        return expand_path(path)


    def get_paths_list(self, section, option, default=[]):
        """
        Retrieves a list of paths from the configuration.
        
        Same as ``get_list()``, but expanding the paths
        
        :param section: Configuration section
        :param option: Configuration option
        :param default: Default value
        :return: The expanded paths list or *default*
        """
        values = self.get_list(section, option, None)
        if not values:
            # No values found, return the default argument
            return default

        paths = []
        for path in values:
            # Expand the trimmed path
            path = expand_path(path.strip())
            if path:
                # Only keep valid paths
                paths.append(path)

        return paths
