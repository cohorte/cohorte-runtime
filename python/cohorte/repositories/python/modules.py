#!/usr/bin/python
# -- Content-Encoding: UTF-8 --
"""
Python modules repository

:author: Thomas Calmant
:license: Apache Software License 2.0

..

    Copyright 2014 isandlaTech

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
"""

# Standard library
import ast
import imp
import json
import logging
import os

import cohorte
import cohorte.repositories
from cohorte.repositories.beans import Artifact, Version
import cohorte.version
from pelix.ipopo.decorators import ComponentFactory, Provides, Property, \
    Invalidate, Validate
from pelix.utilities import is_string


# ######### added by: Bassem D.
# #########
# Pelix
# Repository beans
# ------------------------------------------------------------------------------
# Bundle version
__version__ = cohorte.version.__version__

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


class Module(Artifact):
    """
    Represents a bundle
    """

    def __init__(self, name, version, imports, filename):
        """
        Sets up the bundle details

        :param name: Name of the module
        :param version: Version of the module (as a string)
        :param imports: List of names of imported modules
        :param filename: Path to the .py file
        :raise ValueError: Invalid argument
        """
        Artifact.__init__(self, "python", name, version, filename)

        # Store information
        self.all_imports = imports

    def imports(self, artifact):
        """
        Tests if this module might import the given artifact

        :param artifact: Another artifact
        :return: True if this module imports the given one
        """
        if artifact.language != self.language:
            # No inter-language imports
            return False

        return artifact.name in self.all_imports


# ------------------------------------------------------------------------------


class AstVisitor(ast.NodeVisitor):
    """
    AST visitor to extract imports and version
    """
    # pylint: disable=invalid-name
    def __init__(self, module_name, is_package):
        """
        Sets up the visitor
        :param module_name: The module name
        :param is_package: Whether the name is a package name
        """
        ast.NodeVisitor.__init__(self)
        self.imports = set()
        self.version = None
        self.module_parts = module_name.split(".")
        # Drop module name, keeping only packages' names
        if not is_package:
            self.module_parts = self.module_parts[:-1]
        self.module_name = module_name

    def generic_visit(self, node):
        """
        Custom default visit method that avoids to visit further that the
        module level.
        """
        if type(node) is ast.Module:
            ast.NodeVisitor.generic_visit(self, node)

    def resolve_relative_import_from(self, node):
        """
        Converts a relative import (import .module) into an absolute one

        :param node: An ImportFrom AST node
        :return: The absolute module name
        """
        if node.level > 0:
            # Relative import
            if node.level == 1:
                parent = '.'.join(self.module_parts)
            else:
                parent = '.'.join(self.module_parts[:-node.level + 1])
            if node.module:
                # from .module import ...
                return '.'.join((parent, node.module))
            else:
                # from . import ...
                return parent
        else:
            # Absolute import
            return node.module

    def visit_Import(self, node):
        """
        Found an "import"
        """
        for alias in node.names:
            self.imports.add(alias.name)

    def visit_ImportFrom(self, node):
        """
        Found a "from ... import ..."
        """
        imported = self.resolve_relative_import_from(node)
        self.imports.add(imported)

    def visit_Assign(self, node):
        """
        Found an assignment
        """
        field = getattr(node.targets[0], 'id', None)
        if not self.version \
                and field in ('__version__', '__version_info__'):
            try:
                version_parsed = ast.literal_eval(node.value)
                if isinstance(version_parsed, (tuple, list)):
                    self.version = ".".join(str(version_parsed))
                else:
                    self.version = str(version_parsed)
            except ValueError:
                # Ignore errors
                pass


def _extract_module_info(filename, module_name, is_package):
    """
    Extract the version and the imports from the given Python file

    :param filename: Path to the file to parse
    :param module_name: The fully-qualified module name
    :param is_package: Whether the name is a package name
    :return: A (version, [imports]) tuple
    :raise ValueError: Unreadable file
    """
    try:
        with open(filename,encoding="utf8") as filep:
            source = filep.read()
    except (OSError, IOError,TypeError) as ex:
        try:
            import io
            with io.open(filename,encoding="utf8") as filep:
                source = filep.read()
        except (OSError, IOError) as ex2:
            _logger.exception(ex2)
            raise ValueError("Error reading {0}: {1}".format(filename, ex))


    visitor = AstVisitor(module_name, is_package)
    try:
        module = ast.parse(source, filename, 'exec')
    except (ValueError, SyntaxError, TypeError) as ex:
        raise ValueError("Error parsing {0}: {1}".format(filename, ex))

    visitor.visit(module)
    return visitor.version, visitor.imports


# ------------------------------------------------------------------------------


@ComponentFactory("cohorte-repository-artifacts-python-factory")
@Provides(cohorte.repositories.SERVICE_REPOSITORY_ARTIFACTS)
@Property('_language', cohorte.repositories.PROP_REPOSITORY_LANGUAGE, "python")
class PythonModuleRepository(object):
    """
    Represents a repository
    """

    def __init__(self):
        """
        Sets up the repository
        """
        self._language = "python"

        # Name -> [Modules]
        self._modules = {}

        # Directory name -> Package name
        self._directory_package = {}

        # File -> Module
        self._files = {}

    def __contains__(self, item):
        """
        Tests if the given item is in the repository

        :param item: Item to be tested
        :return: True if the item is in the repository
        """
        if isinstance(item, Artifact):
            # Test artifact language
            if item.language != "python":
                return False

            # Test if the name is in the modules
            return item.name in self._modules
        elif item in self._modules:
            # Item matches a module name
            return True
        else:
            # Test the file name
            for name in (item, os.path.realpath(item)):
                if name in self._files:
                    return True

        # No match
        return False

    def __len__(self):
        """
        Length of a repository <=> number of individual artifacts
        """
        return sum((len(modules) for modules in self._modules.values()))

    def __add_module(self, module, registry=None):
        """
        Adds a module to the registry

        :param module: A Module object
        :param registry: Registry where to store the module
        """
        if registry is None:
            registry = self._modules

        # Add the module to the registry
        modules_list = registry.setdefault(module.name, [])
        if module not in modules_list:
            modules_list.append(module)
            modules_list.sort(reverse=True)

        # Associate the file name with the module
        self._files[module.file] = module

    @staticmethod
    def __compute_name(root, filename):
        """
        Computes the module name of the given file by looking for '__init__.py'
        files in its parent directories

        :param filename: Path of the module file
        :return: The Python name of the module, and a boolean indicating
                 whether the name is a package name
        :raise ValueError: Invalid directory name
        """
        # Subtract the root part
        filename = os.path.relpath(filename, root)
        # Drop extension
        filename = os.path.splitext(filename)[0]
        name_parts = filename.split(os.path.sep)
        is_package = name_parts[len(name_parts) - 1] == "__init__"
        if is_package:
            name_parts = name_parts[:-1]
        return ".".join(name_parts), is_package

    @staticmethod
    def __test_import(name):
        """
        Tries to import the given module, using imp.find_module().

        :param name: A module name
        :return: True if the module can be imported
        """
        try:
            # find_module() uses a path-like name, not a dotted one
            path_name = name.replace('.', os.sep)
            result = imp.find_module(path_name)
        except ImportError:
            # Module not found
            return False
        else:
            # Module found: close the file opened by find_module(), if any
            if result[0] is not None:
                result[0].close()

            return True

    def add_file(self, root, filename):
        """
        Adds a Python file to the repository

        :param root: Path to the python package base of the added file
        :param filename: A Python full-path file name
        :raise ValueError: Unreadable file
        """
        # Compute the real name of the Python file
        realfile = os.path.realpath(filename)
        if realfile in self._files:
            # Already read it: ignore
            return

        if os.path.basename(filename).startswith('.'):
            # Hidden file: ignore
            return

        # Compute the complete module name
        name, is_package = self.__compute_name(root, filename)

        # Parse the file
        version, imports = _extract_module_info(realfile, name, is_package)

        # Store the module
        self.__add_module(Module(name, version, imports, realfile))

    @staticmethod
    def __is_module(dirname):
        """
        Class method testing whether a directory, given its name, contains a
        valid python package.
        :param dirname: The directory' name
        :return: True if the directory contains a valid python package.
                 False otherwise.
        """
        init_file = os.path.join(dirname, "__init__.py")
        return os.path.exists(init_file)

    def add_directory(self, dirname):
        """
        Recursively adds all .py modules found in the given directory into the
        repository

        :param dirname: A path to a directory
        """
        for root, dirnames, filenames in os.walk(dirname, followlinks=True):
            # Check if the current directory, ie. root, is either the base
            # directory or a valid python package.
            # Otherwise, do not walk through sub-directories.
            if not os.path.samefile(dirname, root) \
                    and not self.__is_module(root):
                continue
            for filename in filenames:
                if os.path.splitext(filename)[1] == '.py':
                    fullname = os.path.join(root, filename)
                    try:
                        self.add_file(dirname, fullname)
                    except ValueError as ex:
                        _logger.warning("Error analyzing %s: %s", fullname, ex)

    def clear(self):
        """
        Clears the repository content
        """
        self._modules.clear()
        self._files.clear()
        self._directory_package.clear()

    def get_artifact(self, name=None, version=None, filename=None,
                     registry=None):
        """
        Retrieves a module from the repository

        :param name: The module name (mutually exclusive with filename)
        :param version: The module version (None or '0.0.0' for any), ignored
                        if filename is used
        :param filename: The module file name (mutually exclusive with name)
        :param registry: Registry where to look for the module
        :return: The first matching module
        :raise ValueError: If the module can't be found
        """
        if registry is None:
            registry = self._modules

        if filename:
            # Use the file name (direct search)
            module = self._files.get(filename)
            if module:
                # Found it
                return module

            for bundle_file in self._files:
                # Search by file base name
                if os.path.basename(bundle_file) == filename:
                    return self._files[bundle_file]

        if not name:
            # Not found by file name, and no name to look for
            raise ValueError("Module file not found: {0}".format(filename))

        if isinstance(name, Module):
            # Got a module
            module = name
            if module in registry:
                return module
            else:
                # Use the module name and version
                name = module.name
                version = module.version

        matching = registry.get(name, None)
        if not matching:
            raise ValueError('Module {0} not found.'.format(name))

        for module in matching:
            if module.version.matches(version):
                return module

        raise ValueError('Module {0} not found for version {1}'
                         .format(name, version))

    def get_language(self):
        """
        Retrieves the language of the artifacts stored in this repository
        """
        return self._language

    def resolve_installation(self, artifacts, system_artifacts=None):
        """
        Returns all the artifacts that must be installed in order to have the
        given modules resolved.

        :param artifacts: A list of bundles to be modules
        :param system_artifacts: Modules considered as available
        :return: A tuple: (modules, dependencies, missing artifacts, [])
        """
        # Name -> Module for this resolution
        local_modules = {}

        # Module -> [Modules]
        dependencies = {}

        # Missing elements
        missing_modules = set()

        # Consider system modules already installed
        if system_artifacts:
            for module in system_artifacts:
                if is_string(module):
                    if module in self._modules:
                        module = self._modules[module]
                    else:
                        module = Module(str(module), None, None, None)

                if isinstance(module, Module):
                    # Only accept modules
                    self.__add_module(module, local_modules)

        # Resolution loop
        to_install = [self.get_artifact(name) for name in artifacts]
        i = 0
        while i < len(to_install):
            # Loop control
            module = to_install[i]
            i += 1

            # Add the current module
            self.__add_module(module, local_modules)
            dependencies[module] = []

            # Resolve import ...
            for imported in module.all_imports:
                # Find the module
                registry = None
                provider = None
                for registry in (local_modules, self._modules):
                    try:
                        provider = self.get_artifact(imported, None, None,
                                                     registry)
                        # Found one
                        break
                    except ValueError:
                        # Try next
                        pass
                else:
                    # No provider found, try to import the file
                    if not self.__test_import(imported):
                        # Totally unknown module
                        missing_modules.add(imported)

                    # Resolve next import
                    continue

                # Store the module we found
                dependencies[module].append(provider)

                if registry is self._modules:
                    # The provider was found in the global registry, store it
                    self.__add_module(provider, local_modules)

                    # Store the dependency
                    dependencies[module].append(provider)

                    # The new module will be resolved later
                    if provider not in to_install:
                        # We'll have to resolve it
                        to_install.append(provider)

        return to_install, dependencies, missing_modules, []

    def walk(self):
        """
        # Walk through the known artifacts
        """
        for modules in self._modules.values():
            for module in modules:
                yield module

    # ######### added by: Bassem D.
    def load_cache(self):
        """
        Loads the cache from system file to memory
        """
        use_cache = os.environ.get('COHORTE_USE_CACHE')
        if use_cache and use_cache.lower() == "true":
            try:
                with open('cache.js') as input_file:
                    cache = json.load(input_file)
                    if cache:
                        _logger.info("loading repository from cache...")
                        # load modules
                        for module in cache["modules"]:
                            name = module["name"]
                            version = Version(module["version"])
                            filename = module["filename"]

                            module_bean = Module(name, version, [], filename)
                            self.__add_module(module_bean, self._modules)

                        for directory in cache["directories"]:
                            self._directory_package[directory["dir_name"]] \
 = directory["pkg_name"]

                        return True
            except (IOError, ValueError):
                # Error reading/parsing cache file
                return False
        # No cache
        return False

    def save_cache(self):
        """
        Saves the cache from memory to system file
        """
        use_cache = os.environ.get('COHORTE_USE_CACHE')
        if use_cache and use_cache.lower() == "true":
            # dump modules
            _logger.info("Dumping cache info...")

            # Name -> [Modules]
            cache_modules = [
                {"name": module.name, "version": str(module.version),
                 "language": module.language, "filename": module.file}
                for name, modules in self._modules.items()
                for module in modules]

            # Directory name -> Package name
            cache_directories = [
                {"dir_name": dir_name,
                 "pkg_name": self._directory_package[dir_name]}
                for dir_name in self._directory_package]

            # Write cache
            cache = {"modules": cache_modules,
                     "directories": cache_directories}
            with open('cache.js', 'w') as outfile:
                json.dump(cache, outfile, indent=4)

    # #########

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # ######### added by: Bassem D.
        # check if there is a cache file, load it if so
        # visit repo files and check if the modification date is changed
        # if so, load the file and update the cached entry
        # if there were no cache file, we create it at the end of the parsing
        status = self.load_cache()
        if not status:
            _logger.info("Loading repository from file system...")
            # #########

            # Load repositories in another thread
            # Home/Base repository
            for key in (cohorte.PROP_BASE, cohorte.PROP_HOME):
                repository = os.path.join(context.get_property(key), "repo")
                self.add_directory(repository)

            # Python path directories
            python_path = os.getenv("PYTHONPATH", None)
            if python_path:
                for path in python_path.split(os.pathsep):
                    self.add_directory(path)

            # ######### added by: Bassem D.
            self.save_cache()
            # #########

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.clear()
