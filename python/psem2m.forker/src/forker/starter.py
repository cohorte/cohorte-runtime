#-- Content-Encoding: UTF-8 --
"""
Starts and populates the Pelix framework instance for the forker isolate

:author: Thomas Calmant
"""

import psem2m
import psem2m.services.pelix as pelix

# ------------------------------------------------------------------------------

import logging
import os
import sys

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

def validate_state():
    """
    Returns the missing environment variables, needed to start the forker.
    
    Looks for PSEM2M_HOME, PSEM2M_BASE, PSEM2M_ISOLATE_ID, HTTP_PORT and
    RPC_PORT
    
    :return: The list of missing variables, None if all are present
    """
    needed_env = ("PSEM2M_HOME", "PSEM2M_BASE", "PSEM2M_ISOLATE_ID",
                  "HTTP_PORT")

    missing = []

    for key in needed_env:
        if not os.getenv(key):
            missing.append(key)

    if not missing:
        return None

    return missing


def run_isolate(required_bundles, properties):
    """
    Starts and populates the Pelix framework for the forker, then waits for
    it to stop.
    If start_monitor is True, the forker will try to start the first monitor 
    found in the configuration files as soon as possible.
    
    :param required_bundles: The list of the isolate 4required bundles
    :param properties: The Pelix framework properties
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
        return 1

    # All went well, wait for the framework to stop
    _logger.debug("Waiting for the framework to stop")
    framework.wait_for_stop()
    return 0

# ------------------------------------------------------------------------------

def main(start_monitor, debug=False):
    """
    Starts and populates the forker Pelix framework. Waits for it to stop before
    returning.
    
    :param start_monitor: If True, the forker must try to start a PSEM2M monitor
    :param debug: If True, sets up a console before waiting for the framework to
                  stop
    :return: An integer, 0 for success, 1 for an error starting the framework
    """
    # Test environment
    missing = validate_state()
    if missing is not None:
        # Missing environment variable(s)
        _logger.error("Can't start the forker. " \
                      "Missing environment variables :\n%s", missing)
        return 1

    # Required bundles list
    required_bundles = ('psem2m.component.ipopo', 'base.config',
                        'psem2m.forker.bootstrap')

    # Framework properties
    properties = {"pelix.debug": debug,
                  "psem2m.forker.start_monitor": start_monitor,
                  "psem2m.isolate.id": os.getenv(psem2m.PSEM2M_ISOLATE_ID)}

    # Start the forker framework
    return run_isolate(required_bundles, properties)

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    sys.exit(main())
