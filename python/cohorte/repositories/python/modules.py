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

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# Repository beans
import cohorte.repositories
from cohorte.repositories.beans import Artifact
# ######### added by: Bassem D.
from cohorte.repositories.beans import Version
# #########

# Pelix
from pelix.ipopo.decorators import ComponentFactory, Provides, Property, \
    Invalidate, Validate
from pelix.utilities import is_string

# Standard library
import ast
import imp
import logging
import os

# ######### added by: Bassem D.
import json
# ######### 

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
    def __init__(self):
        """
        Sets up the visitor
        """
        ast.NodeVisitor.__init__(self)
        self.imports = set()
        self.version = None

    def generic_visit(self, node):
        """
        Custom default visit method that avoids to visit further that the
        module level.
        """
        if type(node) is ast.Module:
            ast.NodeVisitor.generic_visit(self, node)

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
        self.imports.add(node.module)

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


def _extract_module_info(filename):
    """
    Extract the version and the imports from the given Python file

    :param filename: Path to the file to parse
    :return: A (version, [imports]) tuple
    :raise ValueError: Unreadable file
    """
    visitor = AstVisitor()

    try:
        with open(filename) as filep:
            source = filep.read()
    except (OSError, IOError) as ex:
        raise ValueError("Error reading {0}: {1}".format(filename, ex))

    try:
        module = ast.parse(source, filename, 'exec')
    except (ValueError, SyntaxError) as ex:
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

    def __compute_name(self, filename):
        """
        Computes the module name of the given file by looking for '__init__.py'
        files in its parent directories

        :param filename: Path of the module file
        :return: The Python name of the module
        :raise ValueError: Invalid directory name
        """
        # Compute the complete module name
        filename = os.path.abspath(filename)
        dirname = os.path.dirname(filename)

        module = os.path.basename(os.path.splitext(filename)[0])
        package_parts = []

        while dirname:
            if dirname in self._directory_package:
                # Known directory: stop there
                package_parts.append(self._directory_package[dirname])
                break
            elif not os.path.exists(os.path.join(dirname, "__init__.py")):
                # Not a package anymore
                break
            else:
                package = os.path.basename(dirname)
                if ' ' in package:
                    # Invalid package name
                    raise ValueError("Invalid package name: {0}"
                                     .format(package))
                else:
                    # Go further up
                    package_parts.append(package)
                    dirname = os.path.dirname(dirname)

        # Store the package information
        package_parts.reverse()
        package = None
        package_path = dirname

        for part in package_parts:
            try:
                package = '.'.join((package, part))
            except TypeError:
                package = part

            package_path = os.path.join(package_path, part)
            self._directory_package[package_path] = package

        if module == '__init__':
            # Do not append the __init__ in packages
            return package
        else:
            return '{0}.{1}'.format(package, module)

    def __test_import(self, name):
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

    def add_file(self, filename):
        """
        Adds a Python file to the repository

        :param filename: A Python file name
        :raise ValueError: Unreadable file
        """
        # Compute the complete module name
        name = self.__compute_name(filename)

        # Compute the real name of the Python file
        realfile = os.path.realpath(filename)

        # Parse the file
        version, imports = _extract_module_info(realfile)

        # Store the module
        self.__add_module(Module(name, version, imports, realfile))

    def add_directory(self, dirname):
        """
        Recursively adds all .py modules found in the given directory into the
        repository

        :param dirname: A path to a directory
        """        
        for root, _, filenames in os.walk(dirname, followlinks=True):
            for filename in filenames:
                if os.path.splitext(filename)[1] == '.py':
                    fullname = os.path.join(root, filename)
                    try:
                        self.add_file(fullname)
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
        if use_cache:
            if use_cache.lower() == "true":
                if os.path.isfile('cache.js'):
                    with open('cache.js') as input_file:
                        cache = json.load(input_file)
                        if cache:
                            _logger.info("loading repository from cache...")
                            # load modules
                            for module in cache["modules"]:

                                language = module["language"]
                                name = module["name"]
                                version = Version(module["version"])
                                filename = module["filename"]                        
                                
                                m = Module(name, version, [], filename)
                                self.__add_module(m, self._modules)
                                #self._modules[name].append(m)

                            for directory in cache["directories"]:
                                self._directory_package[directory["dir_name"]] = directory["pkg_name"]
                            return 0
        return 1

    def save_cache(self):
        """
        Saves the cache from memory to system file 
        """
        use_cache = os.environ.get('COHORTE_USE_CACHE')
        if use_cache:
            if use_cache.lower() == "true":
                # Name -> [Modules]
                #self._modules = {}
                # dump modules
                cache = {"modules": [], "directories": []}
                _logger.info("dumping cache info...")
                for name, modules in self._modules.items():
                    for module in modules:
                        m = {"name": module.name,
                             "version": str(module.version),
                             "language": module.language,
                             "filename": module.file}
                        cache["modules"].append(m)

                # Directory name -> Package name
                #self._directory_package = {}
                for key1 in self._directory_package:
                    d = {"dir_name": key1, "pkg_name": self._directory_package[key1]}
                    cache["directories"].append(d)

                # File -> Module
                #self._files = {}

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
        if status == 1:
            _logger.info("loading repository from file system...")
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
