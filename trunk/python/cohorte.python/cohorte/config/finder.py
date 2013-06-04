#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE file finder

**TODO:**
* Review API

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
import fnmatch
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
        for root_dir in self._gen_roots():
            if path.startswith(root_dir):
                return os.path.relpath(path, root_dir)

        # No match found
        return None


    def _gen_roots(self):
        """
        Generator to have the Cohorte roots (base then home) and the custom
        roots.
        """
        for root_list in (self._roots, self._custom_roots):
            if root_list:
                for root in root_list:
                    yield root


    def _internal_find(self, filename):
        """
        A generator to find the given file in the platform directories.
        
        :param filename: Name of the file to find
        """
        if os.path.isabs(filename) and os.path.exists(filename):
			# The file name is absolute and valid
			yield filename
        
        if filename.startswith(os.path.sep):
            # os.path.join won't work if the name starts with a path separator
            filename = filename[len(os.path.sep):]

        # Look into root directories
        for root_dir in self._gen_roots():
            path = os.path.realpath(os.path.join(root_dir, filename))
            if os.path.exists(path):
                yield path

        # Test the absolute file name
        path = os.path.realpath(filename)
        if os.path.exists(path):
            yield path


    def find_rel(self, filename, base_file):
        """
        A generator to find the given file in the platform folders
        
        :param filename: The file to look for (tries its absolute path then its
                          name)
        :param base_file: Base file reference (filename can be relative to it)
        :return: The matching files
        """
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
                for found_file in self._internal_find(path):
                    yield found_file

                # Try without the platform prefix, if any
                platform_subdir = self._extract_platform_path(base_dir)
                if platform_subdir:
                    path = os.path.join(platform_subdir, filename)
                    for found_file in self._internal_find(path):
                        yield found_file

        # Find files, the standard way
        for found_file in self._internal_find(filename):
            yield found_file


    def find_gen(self, pattern, base_dir=None, recursive=True):
        """
        Generator to find the files matching the given pattern looking
        recursively in the given directory in the roots (base, home and customs)
        
        :param pattern: A file pattern
        :param base_dir: The name of a sub-directory of "home" or "base"
        :param recursive: If True, searches recursively for the file
        :return: The matching files
        """
        if base_dir[0] == os.path.sep:
            # os.path.join won't work if the name starts with a path separator
            base_dir = base_dir[len(os.path.sep):]

        for root in self._gen_roots():
            # Prepare the base directory
            if base_dir is not None:
                base_path = os.path.join(root, base_dir)
            else:
                base_path = root

            # Walk the directory
            for root, _, filenames in os.walk(base_path, followlinks=True):
                for filename in fnmatch.filter(filenames, pattern):
                    # Return the real path of the matching file
                    yield os.path.realpath(os.path.join(root, filename))

                if not recursive:
                    # Stop on first directory
                    return


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

