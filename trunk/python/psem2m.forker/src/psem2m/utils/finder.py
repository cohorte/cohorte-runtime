#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
PSEM2M file finder

:author: Thomas Calmant
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Instantiate, Provides, \
    Property

import logging
import os

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory('psem2m-file-finder-factory')
@Provides('org.psem2m.isolates.services.dirs.IFileFinderSvc')
@Property('_format', 'configuration.format', 'json')
@Instantiate('psem2m-file-finder')
class FileFinder(object):
    """
    Simple file finder : tries to find the given file in the platform main
    directories.
    """
    def __init__(self):
        """
        Sets up the finder
        """
        # Set up the platform root directories
        self._platform_dirs = []
        for env in ('PSEM2M_BASE', 'PSEM2M_HOME'):
            path = os.getenv(env)
            if path:
                path = os.path.abspath(path)
                if path not in self._platform_dirs and os.path.isdir(path):
                    self._platform_dirs.append(path)



    def find(self, filename, base_file=None):
        """
        Tries to find the given file in the platform folders
        
        :param filename: Name of the file to find
        :param base_file: Try to find the file near this file first
        :return: A list of matching files
        """
        found_files = []

        if base_file:
            abspath = os.path.abspath
            file_exists = os.path.exists

            if os.path.isdir(base_file):
                base_dir = abspath(base_file)

            else:
                base_dir = abspath(os.path.dirname(base_file))

            if base_dir:
                path = abspath(os.path.join(base_dir, filename))
                if file_exists(path):
                    found_files.append(path)

                platform_subdir = self._extract_platform_path(base_dir)
                if platform_subdir:
                    path = os.path.join(platform_subdir, filename)
                    found_files.extend(self._internal_find(path))

        # Find files, the standard way
        found_files.extend(self._internal_find(filename))
        return found_files


    def _internal_find(self, filename):
        """
        Tries to find the given file in the platform directories. Never returns
        None.
        
        :param filename: Name of the file to find
        :return: The list of the corresponding files
        """
        files = []

        file_abspath = os.path.abspath
        file_exists = os.path.exists
        file_join = os.path.join

        for root_dir in self._platform_dirs:
            path = file_abspath(file_join(root_dir, filename))
            if file_exists(path):
                files.append(path)

        path = file_abspath(filename)
        if file_exists(path):
            files.append(path)

        return files
