#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE file finder

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version__ = '1.0.0'

# ------------------------------------------------------------------------------

# COHORTE constants
import cohorte

# iPOPO decorators
from pelix.ipopo.decorators import ComponentFactory, Instantiate, Provides, \
    Validate, Invalidate

# Python standard library
import logging
import os

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory('cohorte-file-finder-factory')
@Provides(cohorte.SERVICE_FILE_FINDER)
@Instantiate('cohorte-file-finder')
class FileFinder(object):
    """
    Simple file finder : tries to find the given file in the platform main
    directories.
    """
    def __init__(self):
        """
        Sets up the finder
        """
        # Keep the Bundle context to access framework properties
        self._context = None

        # Search roots
        self._roots = None
        self._custom_roots = None


    def _extract_platform_path(self, path):
        """
        Tries to remove a platform root prefix from the given path.
        Non-None result indicates that the given path is a root sub-path.
        
        :param path: Path to be transformed
        :retun: The root-path if any, else None
        """
        if not path:
            return None

        # COHORTE root directories
        for root_list in (self._roots, self._custom_roots):
            for root_dir in root_list:
                if path.startswith(root_dir):
                    return os.path.relpath(path, root_dir)

        # No match found
        return None


    def _internal_find(self, filename):
        """
        Tries to find the given file in the platform directories. Never returns
        None.
        
        :param filename: Name of the file to find
        :return: The list of the corresponding files
        """
        file_abspath = os.path.realpath
        file_exists = os.path.exists
        file_join = os.path.join

        if filename[0] == os.path.sep:
            # os.path.join won't work if the name starts with a path separator
            filename = filename[len(os.path.sep):]

        files = []
        # Look into root directories
        for root_list in (self._roots, self._custom_roots):
            for root_dir in root_list:
                path = file_abspath(file_join(root_dir, filename))
                if file_exists(path):
                    files.append(path)

        # Test the absolute file name
        path = file_abspath(filename)
        if file_exists(path):
            files.append(path)

        return files


    def find(self, filename, base_file=None):
        """
        Tries to find the given file in the platform folders
        
        :param filename: The file to look for (tries its absolute path then its
                          name)
        :param base_file: Base file reference (file_name can be relative to it)
        :return: All found files with the given information, an empty list
                 if none found
        """
        found_files = []

        if base_file:
            abspath = os.path.abspath
            file_exists = os.path.exists

            if os.path.isdir(base_file):
                # The given base is a directory
                base_dir = abspath(base_file)

            elif file_exists(base_file):
                # The given base exists: get its directory
                base_dir = abspath(os.path.dirname(base_file))

            else:
                # Unknown kind of base (maybe a relative one)
                base_dir = base_file

            if base_dir:
                # Try the base directory directly (as a relative directory)
                path = os.path.join(base_dir, filename)
                found_files.extend(self._internal_find(path))

                # Try without the platform prefix, if any
                platform_subdir = self._extract_platform_path(base_dir)
                if platform_subdir:
                    path = os.path.join(platform_subdir, filename)
                    found_files.extend(self._internal_find(path))

        # Find files, the standard way
        found_files.extend(self._internal_find(filename))
        return found_files


    def add_custom_root(self, root):
        """
        Adds a custom search root (not ordered)
        
        :param root: The custom root to add
        """
        if root:
            self._custom_roots.add(root)


    def remove_custom_root(self, root):
        """
        Removes a custom search root
        
        :param root: The custom root to remove
        """
        if root:
            try:
                self._custom_roots.remove(root)

            except KeyError:
                # Ignore error
                pass


    def update_roots(self):
        """
        Updates the platform roots, according to framework properties
        """
        del self._roots[:]

        # Search in Base, then Home
        for name in (cohorte.PROP_BASE, cohorte.PROP_HOME):
            value = self._context.get_property(name)
            if value and value not in self._roots:
                self._roots.append(value)


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        # Store the framework access
        self._context = context

        # Prepare the sets
        self._roots = []
        self._custom_roots = set()

        # Update the roots list
        self.update_roots()


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        # Store the framework access
        self._context = None
        self._roots = None
        self._custom_roots = None

