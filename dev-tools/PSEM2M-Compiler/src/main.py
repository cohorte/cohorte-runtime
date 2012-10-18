#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
PSEM2M Compiler: Entry point

:author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

from builder.dependencies import BinaryBundleFinder, SourceBundleFinder, Dependencies
import compiler.config
import compiler.eclipseutils as eclipse
import compiler.generator

from pprint import pformat
import importlib
import logging
import optparse
import os
import subprocess
import sys

# ------------------------------------------------------------------------------

DEFAULT_CONFIGURATION_FILENAME = "compiler.conf"
""" Default configuration file name """

ENV_PSEM2M_COMPILER = "PSEM2M_COMPILER"
""" PSEM2M Compiler directory environment variable """

ENV_WORKSPACE = "WORKSPACE"
""" PSEM2M Compiler work space environment variable """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

def filter_projects(src_dirs, ignored_projects):
    """
    Filters given bundles sources directories according to their name
    
    :param src_dirs: Bundles sources directories
    :param ignored_projects: Projects to ignore
    :return: Filtered sources directories list
    """
    if not ignored_projects:
        # Nothing to do
        return src_dirs

    filtered = []

    for srcdir in src_dirs:
        # Use the base name (last element in path)
        project_name = os.path.basename(srcdir)
        if project_name not in ignored_projects and srcdir not in filtered:
            # Filter passed
            filtered.append(srcdir)

    return filtered


def find_eclipse_projects(root_dirs, ignored_projects):
    """
    Finds and parses all Eclipse projects of the given directories
    
    :param root_dirs: Root directories to search in
    :param ignored_projects: Projects to ignore
    :return: Filtered Eclipse projects list
    """
    projects = []

    for srcdir in root_dirs:
        for root, __, files in os.walk(srcdir):
            if '.project' not in files:
                # Not an Eclipse project directory
                continue

            project_dir = os.path.basename(root)
            project = eclipse.read_project(os.path.join(root, '.project'))

            if project.name not in ignored_projects \
            and project_dir not in ignored_projects:
                # Keep project
                projects.append(project)

    return projects

# ------------------------------------------------------------------------------

def load_extension(extension, params):
    """
    Loads the given extension
    
    :param extension: The extension module name
    :param params: PSEM2M Compiler configuration
    :raise ImportError: Error importing extension
    :raise ValueError: Invalid extension class name
    """
    module = importlib.import_module(extension)

    ext_class_name = getattr(module, "EXTENSION_CLASS", None)
    if ext_class_name is None:
        # No extension class, use the module itself
        return module

    # Load the extension class
    ext_class = getattr(module, ext_class_name, None)
    if ext_class is not None:
        # Return a new instance of the class
        return ext_class(params)

    raise ValueError("Class not found: %s in %s" % (ext_class_name, extension))

# ------------------------------------------------------------------------------

def read_parameters():
    """
    Reads the arguments from sys.argv
    
    :return: The OptionParser result
    """
    parser = optparse.OptionParser()

    # Standard options
    opt_help = "Remove generated build.xml files after compilation."
    parser.add_option("--clean-after-build", dest="clean_after_build",
                      action="store_true", default=False, help=opt_help)

    opt_help = "Sets the configuration file to use."
    parser.add_option('-c', '--config', dest='config_file',
                      action='store', default='compiler.conf', help=opt_help)

    opt_help = "Do not run post-build extensions"
    parser.add_option('-n', '--no-post-build', dest='use_post_build',
                      action='store_false', default=True, help=opt_help)

    # Parse the sys.args, ignore extra parameters
    return parser.parse_args()[0]

# ------------------------------------------------------------------------------

def main():
    """
    Entry point
    """
    try:
        # Read the configuration file
        params = read_parameters()

        _logger.info('Reading configuration file...')
        config = compiler.config.ExtSafeConfigParser()

        config_files = []

        # Default local file
        local_file = os.path.join(os.getcwd(), DEFAULT_CONFIGURATION_FILENAME)
        if os.path.isfile(local_file):
            config_files.append(local_file)

        # Given file
        config_files.append(compiler.config.expand_path(params.config_file))

        # Read configuration
        config.read(config_files)

    except Exception as ex:
        _logger.exception("Error reading parameters : %s", ex)
        return 1

    # 0. Normalize configuration values
    lib_dirs = config.get_paths_list('main', 'lib.dirs')
    src_dirs = config.get_paths_list('main', 'src.dirs')
    output_dir = config.get_path('main', 'output')

    # Make the output directory if necessary
    if not os.path.isdir(output_dir):
        os.makedirs(output_dir)

    ignored_projects = config.get_list('main', 'projects.ignored')
    eclipse_only = config.getboolean('main', 'eclipse.only')

    # Print configuration
    _logger.info("Libraries: %s", pformat(lib_dirs))
    _logger.info("Sources: %s", pformat(src_dirs))
    _logger.info("Ignored Projects: %s", pformat(ignored_projects))
    _logger.info("Eclipse only: %s", eclipse_only)

    # 1. Load the target platform files
    _logger.info("Looking for binary bundles...")
    lib_finder = BinaryBundleFinder()
    lib_finder.find(lib_dirs)
    lib_finder.load()
    _logger.info("%d binary bundles found", len(lib_finder.unique_bundles))

    # 2. Prepare file system to match Eclipse projects
    _logger.info("Looking for Eclipse projects...")
    eclipse_projects = find_eclipse_projects(src_dirs, ignored_projects)
    for project in eclipse_projects:
        project.resolve_links(eclipse_projects)

    if eclipse_only:
        # Only compile Eclipse projects (already filtered)
        _logger.info("Compile Eclipse projects only.")
        source_dirs = [project.path for project in eclipse_projects]

    else:
        # Filter projects found
        _logger.info("Compile all found projects.")
        source_dirs = src_dirs

    # 3. Load the bundles directories
    _logger.info("Looking for source bundles (projects)...")
    src_finder = SourceBundleFinder(ignored_projects)
    src_finder.find(source_dirs)
    src_finder.load()
    _logger.info("%d source bundles found", len(src_finder.bundles))

    # 4. Resolve dependencies and build order
    deps = Dependencies(lib_finder, src_finder, lib_finder.target_platform)

    _logger.info("Resolving dependencies...")
    if not deps.resolve():
        _logger.error("Error resolving dependencies. Abandon.")
        return 10

    _logger.info("Computing build order...")
    deps.sort()

    # 5. Prepare Ant scripts
    ant_generator = compiler.generator.AntGenerator(
                                config.get('main', 'name'),
                                deps.src.bundles,
                                deps.target_platform,
                                output_dir,
                                config.get_default('main', 'ant_script',
                                                   'build.xml'))

    # 6. Load extensions
    _logger.info("Loading extensions...")
    for ext in config.get_list('main', 'extensions'):
        try:
            print 'Loading:', ext
            ant_generator.add_extension(load_extension(ext, config))

        except ImportError as ex:
            print("Can't import extension %s: %s" % (ext, ex))
            return 11

    # 7. Generate scripts
    _logger.info("Generating scripts...")
    master_file = ant_generator.generate_build_files()

    # 8. Run Ant
    _logger.info("Running Ant...")
    result = subprocess.call(['ant', '-f', master_file, 'package'])
    _logger.debug("Ant result = %d", result)

    if result == 0 and params.use_post_build:
        # No error, call extensions
        ant_generator.post_build()

    # 9. Clean up
    if params.clean_after_build:
        _logger.info("Cleaning up...")
        ant_generator.clean()

    _logger.info("Done.")
    return result

# ------------------------------------------------------------------------------

if __name__ == '__main__':
    # Set up the logging
    logging.basicConfig()
    logging.getLogger().setLevel(logging.INFO)

    # Set up the environment, if needed
    if not os.getenv(ENV_PSEM2M_COMPILER):
        os.environ[ENV_PSEM2M_COMPILER] = os.getcwd()

    if not os.getenv(ENV_WORKSPACE):
        # Development relative position (psem2m/dev-tools/PSEM2M-Compiler)
        dev_workspace = os.path.join(os.getcwd(), "..", "..", "..")
        os.environ[ENV_WORKSPACE] = os.path.abspath(dev_workspace)

    sys.exit(main())
