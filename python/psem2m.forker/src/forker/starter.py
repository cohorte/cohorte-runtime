#-- Content-Encoding: UTF-8 --
"""
Starts and populates the Pelix framework instance for the forker isolate

@author: Thomas Calmant
"""

import psem2m.services.pelix as pelix

# ------------------------------------------------------------------------------

import logging
import os

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
                  "HTTP_PORT", "RPC_PORT")

    missing = []

    for key in needed_env:
        if not os.getenv(key):
            missing.append(key)

    if not missing:
        return None

    return missing


def run_isolate(required_bundles):
    """
    Starts and populates the Pelix framework for the forker, then waits for
    it to stop
    
    :param required_bundles: The list of the isolate 4required bundles
    """
    # Start a framework
    framework = pelix.FrameworkFactory.get_framework()

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
        return

    # All went well, wait for the framework to stop
    _logger.debug("Waiting for the framework to stop")
    framework.wait_for_stop()
    _logger.debug("Bye !")


# ------------------------------------------------------------------------------

if __name__ == "__main__":
    # Test environment
    missing = validate_state()
    if missing is not None:
        # Missing environment variable(s)
        _logger.error("Can't start the forker. " \
                      "Missing environment variables :\n%s", missing)
        return

    # Required bundles list
    required_bundles = ('psem2m.component.ipopo', 'base.config',
                        'base.httpsvc', 'base.signals')

    # Start the forker framework
    run_isolate(required_bundles)
