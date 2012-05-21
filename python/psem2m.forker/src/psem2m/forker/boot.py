#!/usr/bin/python
#-- Content-Encoding: UTF-8 --
"""
PSEM2M Python bootstrap

**WARNING:**
This module uses the ``argparse`` module if it executed directly to read its
arguments. Argparse is part of the Python standard library since 2.7 and 3.2,
but can be installed manually for previous versions, using
``easy_install -U argparse``.

:author: Thomas Calmant
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

import psem2m
import pelix.framework as pelix

# ------------------------------------------------------------------------------

import logging
import os
import sys

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

def string_to_bool(string):
    """
    Converts the given string to a boolean
    
    If string is one of "true", "1", "yes" or "y", the method returns True
    (case insensitive).
    Else, the method returns False
    
    :param string: The string to parse
    :return: True or False
    """
    if not string:
        # No value
        return False

    # Accept multiple forms of "true"
    return string.lower() in ("true", "yes", "y", "1")

# ------------------------------------------------------------------------------

def _store_forker_pid(base):
    """
    Stores the current PID to the forker.pid file
    
    :param base: The PSEM2M instance base directory
    """
    pid_file = os.path.join(base, "var", "forker.pid")

    if not os.path.exists(pid_file):
        # Prepare the parent directories
        parent = os.path.dirname(pid_file)
        if not os.path.isdir(parent):
            os.makedirs(parent)

    with open(pid_file, "w") as pid_fp:
        # Write the PID
        pid_fp.write(str(os.getpid()))
        pid_fp.write("\n")


def validate_env():
    """
    Returns the missing environment variables, needed to start the forker.
    
    Looks for PSEM2M_HOME and PSEM2M_BASE
    
    :return: The list of missing variables, None if all are present
    """
    needed_env = ("PSEM2M_HOME", "PSEM2M_BASE")

    missing = []

    for key in needed_env:
        if not os.getenv(key):
            missing.append(key)

    if not missing:
        return None

    return missing

# ------------------------------------------------------------------------------

def _run_isolate(required_bundles, properties, wait_for_stop):
    """
    Starts and populates the Pelix framework for the forker, then waits for
    it to stop.
    If start_monitor is True, the forker will try to start the first monitor 
    found in the configuration files as soon as possible.
    
    :param required_bundles: The list of the isolate 4required bundles
    :param properties: The Pelix framework properties
    :param wait_for_stop: If True, the method won't return before the framework
                          stops
    :return: An integer, 0 for success, 1 for an error starting the framework
    """
    # Start a framework
    framework = pelix.FrameworkFactory.get_framework(properties)

    # Get its context
    context = framework.get_bundle_context()

    try:
        for bundle_name in required_bundles:
            # Install and start bundles
            _logger.debug("Installing %s...", bundle_name)
            bid = context.install_bundle(bundle_name)
            context.get_bundle(bid)
            _logger.debug("%s installed", bundle_name)

        _logger.debug("Starting framework...")
        framework.start()
        _logger.debug("Framework started")

    except:
        # Abandon on error at this level
        _logger.exception("Error starting the forker framework bundles.")
        framework.stop()
        pelix.FrameworkFactory.delete_framework(framework)
        return 1

    if wait_for_stop:
        # All went well, wait for the framework to stop
        _logger.debug("Waiting for the framework to stop")
        framework.wait_for_stop()

    return 0

# ------------------------------------------------------------------------------

def start_isolate(isolate_id, properties=None):
    """
    Starts a Pelix framework with the given properties.
    
    Return codes can be :
    
    * 0: Success
    * 1: Error starting the framework
    * 2: Invalid environment (variable missing)
    
    :param isolate_id: The ID of the started isolate
    :param properties: The framework properties (dictionary)
    :return: An integer error code, 0 for success
    """
    # Test environment
    missing = validate_env()
    if missing is not None:
        # Missing environment variable(s)
        _logger.error("Can't start the forker. " \
                      "Missing environment variables :\n%s", missing)
        return 1

    # Framework properties
    if not isinstance(properties, dict):
        # We need a dictionary
        properties = {}

    # Override some given properties, just to be sure
    overridden_props = {
                        "psem2m.home": os.getenv(psem2m.PSEM2M_HOME),
                        "psem2m.base": os.getenv(psem2m.PSEM2M_BASE),
                        "psem2m.isolate.id": isolate_id
                        }

    properties.update(overridden_props)

    # Required bundles list (using the loader)
    required_bundles = ('pelix.ipopo.core', 'base.config',
                        'psem2m.forker.loader')

    _logger.debug("Starting isolate '%s'", isolate_id)

    # Run the isolate (do not return until it's stopped
    return _run_isolate(required_bundles, properties, True)


def start_forker(start_monitor, debug_mode):
    """
    Starts a PSEM2M Python forker based on Pelix.
    
    :param start_monitor: If True, the forker must start a monitor as soon as
                          it can
    :param debug_mode: Sets the ``pelix.debug`` framework property value
    :return: An integer error code, 0 for success (see start_isolate())
    """
    # Forker specific properties
    properties = {
                  "pelix.debug": debug_mode,
                  "psem2m.forker.start_monitor": start_monitor
                  }

    # Special isolate ID
    forker_id = "org.psem2m.internals.isolates.forker"

    # Store the process PID
    _store_forker_pid(os.getenv(psem2m.PSEM2M_BASE))

    return start_isolate(forker_id, properties)

# ------------------------------------------------------------------------------

def main():
    """
    Script entry point if called directly. Uses sys.argv to determine the boot
    options.
    
    **WARNING:** This method changes the log level of the logging module.
    
    :return: An integer error code, 0 for success (see start_isolate())
    """
    # The argparse module must be loaded as late as possible
    import argparse
    parser = argparse.ArgumentParser(description="PSEM2M Python bootstrap")

    # Start forker and start isolate are mutually exclusive
    group = parser.add_mutually_exclusive_group()
    group.add_argument("-i", "--start-isolate", action="store",
                        dest="isolate_id", default=None,
                        metavar="ISOLATE_ID",
                        help="Starts the isolate ISOLATE_ID")

    group.add_argument("-f", "--start-forker", action="store_true",
                              dest="start_forker", default=False,
                              help="Start the forker isolate")

    parser.add_argument("-m", "--with-monitor", action="store_true",
                              dest="with_monitor", default=False,
                              help="The forker must start a monitor")

    parser.add_argument("-d", "--debug-mode", action="store_true",
                        dest="debug", default=False,
                        help="Sets the forker framework in debug mode")

    parser.add_argument("-v", "--version", action="version",
                        version="PSEM2M Python bootstrap 1.0.0")

    # Parse arguments
    args = parser.parse_args()

    # Set up the log level according to the debug flag
    if args.debug:
        logging.basicConfig(level=logging.DEBUG)

    else:
        logging.basicConfig(level=logging.INFO)

    if args.start_forker:
        # Run the forker
        return start_forker(args.with_monitor, args.debug)

    elif args.isolate_id is not None:
        # Run the isolate
        return start_isolate(args.isolate_id, {"pelix.debug": args.debug})

    else:
        # Nothing to do
        parser.print_help()

    # Invalid parameter
    return 1


if __name__ == "__main__":
    # Call the main method if the script is executed directly
    sys.exit(main())
