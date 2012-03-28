#-- Content-Encoding: UTF-8 --
"""
Starts and populates the Pelix framework instance for the forker isolate

:author: Thomas Calmant
"""

import psem2m.services.pelix as pelix

# ------------------------------------------------------------------------------

import logging
import os
import sys

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

def run_debug_console(framework):
    """
    Runs a debug interactive console
    
    :param framework: A Pelix framework instance
    """
    import psem2m.component.constants

    import readline
    import code

    context = framework.get_bundle_context()

    cons_vars = {"framework": framework,
                 "context": context,
                 "ipopo": psem2m.component.constants.get_ipopo_svc_ref(context)[1]}

    code.InteractiveConsole(cons_vars).interact("FORKER DEBUG CONSOLE")


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


def run_isolate(required_bundles, debug=False):
    """
    Starts and populates the Pelix framework for the forker, then waits for
    it to stop
    
    :param required_bundles: The list of the isolate 4required bundles
    :param debug: If True, sets up a console before waiting for the framework to
                  stop
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

    if debug:
        _logger.debug("Debug mode ON")
        run_debug_console(framework)

    # All went well, wait for the framework to stop
    _logger.debug("Waiting for the framework to stop")
    framework.wait_for_stop()
    _logger.debug("Bye !")


# ------------------------------------------------------------------------------

def main(debug=False):
    # Test environment
    missing = validate_state()
    if missing is not None:
        # Missing environment variable(s)
        _logger.error("Can't start the forker. " \
                      "Missing environment variables :\n%s", missing)
        return 1

    # Required bundles list
    required_bundles = ('psem2m.component.ipopo', 'base.config',
                        'base.httpsvc', 'base.signals', 'base.remoteservices',
                        'psem2m.forker.core', 'psem2m.runner.java',
                        'psem2m.runner.python')

    # Start the forker framework
    run_isolate(required_bundles, debug)
    return 0

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    sys.exit(main())
