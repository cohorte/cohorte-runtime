#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Python bootstrap

**WARNING:**
This module uses the ``argparse`` module if it executed directly to read its
arguments. Argparse is part of the Python standard library since 2.7 and 3.2,
but can be installed manually for previous versions, using
``easy_install -U argparse``.

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE modules
import cohorte

# Pelix framework
from pelix.ipopo.constants import get_ipopo_svc_ref
import pelix.framework

# Python standard library
from pprint import pformat
import logging
import os
import sys

# ------------------------------------------------------------------------------

MINIMAL_BUNDLES = ('pelix.ipopo.core', 'cohorte.config.finder',
                   'cohorte.config.reader', 'cohorte.config.parser')
""" List of bundles to be installed before the isolate loader """

# TODO: use a constant module
STATE_FAILED = -1
STATE_LOADING = 2
STATE_LOADED = 3

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

def _get_loader(context, loader_bundle):
    """
    Retrieves/instantiates the loader service/component from the given bundle
    
    :param context: A bundle context
    :param loader_bundle: The loader implementation bundle object
    :return: The loader component/service
    :raise TypeError: No loader found
    :raise ValueError: A loader component already exists
    :raise Exception: An error occurred initializing the component
    """
    # Get the loader component factory, if any
    loader_factory = getattr(loader_bundle.get_module(),
                             cohorte.BUNDLE_ISOLATE_LOADER_FACTORY, None)

    if loader_factory:
        # Instantiate the component
        ipopo = get_ipopo_svc_ref(context)[1]
        _logger.debug("Instantiating component of type '%s'", loader_factory)
        return ipopo.instantiate(loader_factory, 'cohorte-isolate-loader')

    else:
        # Find the loader service
        svc_ref = context.get_service_reference(cohorte.SERVICE_ISOLATE_LOADER)
        if svc_ref:
            # Service found
            _logger.debug("Found an isolate loader service")
            return context.get_service(svc_ref)

    # No loader found
    raise TypeError('No isolate loader found')


def load_isolate(pelix_properties, state_updater_url=None, fail_on_pdb=False):
    """
    Starts a Pelix framework, installs iPOPO and boot modules and waits for
    the framework to stop
    
    :param pelix_properties: Pelix framework instance properties
    :param state_updater_url: URL to access the isolate state updater
    :param fail_on_pdb: If true, ``pdb.post_mortem()`` is called if an exception
                        occurs starting the framework
    :raise Exception: All exceptions are propagated
    """
    _logger.debug("Starting Pelix framework with properties:\n%s",
                  pformat(pelix_properties))

    # Start the framework
    framework = pelix.framework.FrameworkFactory.get_framework(pelix_properties)
    framework.start()

    try:
        context = framework.get_bundle_context()

        # Install & start configuration bundles
        for name in MINIMAL_BUNDLES:
            _logger.debug('Installing bundle %s...', name)
            bid = context.install_bundle(name)
            context.get_bundle(bid).start()
            _logger.debug('Bundle %s (%d) started', name, bid)

        # Find the isolate loader to use
        if context.get_property(cohorte.PROP_CONFIG_BROKER):
            # If a broker has been given, use the Broker client...
            loader_bundle_name = 'cohorte.boot.loaders.broker'

        else:
            # ... else use the ForkerLoader
            loader_bundle_name = 'cohorte.boot.loaders.forker'

        # Install & start the loader bundle
        _logger.debug("Using isolate loader: %s.", loader_bundle_name)
        bid = context.install_bundle(loader_bundle_name)
        loader_bundle = context.get_bundle(bid)
        loader_bundle.start()

        # Retrieve the loader service & load the isolate
        loader = _get_loader(context, loader_bundle)
        _logger.debug("Isolate booting...")

        # Prepare the access to the state updater
        loader.prepare_state_updater(state_updater_url)
        loader.update_state(STATE_LOADING)

        try:
            # Load the isolate
            loader.load(None)

        except Exception as ex:
            # Something wrong occurred
            loader.update_state(STATE_FAILED, str(ex))
            raise

        else:
            # Isolate loaded
            loader.update_state(STATE_LOADED)
            _logger.debug("Isolate loaded.")

        # Wait forever for the framework to stop
        _logger.debug("Waiting for the isolate to stop...")
        try:
            loader.wait()

        except KeyboardInterrupt:
            # Stop waiting on keyboard interruption
            _logger.debug("Got keyboard interruption, stopping.")
            pass

        # Ensure the framework is stopped
        framework.stop()
        _logger.debug("Framework stopped.")

    except Exception as ex:
        _logger.error('Error running the isolate: %s', ex)

        if fail_on_pdb:
            # Start PDB to debug the exception
            import pdb
            pdb.post_mortem()

        else:
            # Propagate the exception
            raise

    finally:
        # Delete the framework (clean up)
        pelix.framework.FrameworkFactory.delete_framework(framework)
        _logger.debug("Framework deleted.")

# ------------------------------------------------------------------------------

def find_cohorte_directories():
    """
    Finds the COHORTE Home and Base directories, according to process
    environment variables.
    
    :return: A (home, base) tuple.
    :raise KeyError: The Home and Base directories can't be determined
    """
    # Look for home in the process environment
    home = os.getenv(cohorte.ENV_HOME)

    # Use found home as default base
    base = os.getenv(cohorte.ENV_BASE, home)

    if not base:
        # Base and home are invalid (base is at least equal to home)
        raise KeyError('No correct value found in Home and Base '
                       'environment variables: {env_home}, {env_base}'
                       .format(env_home=cohorte.ENV_HOME,
                               env_base=cohorte.ENV_BASE))

    # Expand environment variables
    base = os.path.expanduser(os.path.expandvars(base))

    if not home:
        # Base has been found, but home is missing
        home = base

    else:
        # Expand variables in home too
        home = os.path.expanduser(os.path.expandvars(home))

    return home, base

# ------------------------------------------------------------------------------

def configure_logger(logfile, debug, verbose):
    """
    Configures the root logger.
    
    If the debug flag is on, debug messages will be logged in the file.
    If the verbose flag is on, the console output will log information 
    messages.
    If both verbose and debug flag are on, the console output will log debug
    messages.
    
    :param logfile: The name of the log file. If None, the log file will be
                    disabled
    :param debug: The debug mode flag
    :param verbose: The verbose mode flag (allows to set the console output
                    in debug mode)
    """
    # Get the root logger
    root_log = logging.root

    if debug:
        root_log.setLevel(logging.DEBUG)
    else:
        root_log.setLevel(logging.INFO)

    # Prepare the console handler
    consh = logging.StreamHandler()

    # ... set its level
    if verbose:
        if debug:
            consh.setLevel(logging.DEBUG)
        else:
            consh.setLevel(logging.INFO)
    else:
        consh.setLevel(logging.WARNING)

    # ... prepare its formatter
    formatter = logging.Formatter("%(asctime)s:%(levelname)-8s:%(name)-20s: "
                                  "%(message)s")
    consh.setFormatter(formatter)

    # ... register it
    root_log.addHandler(consh)

    # Get the log file name from the environment if necessary
    if not logfile:
        logfile = os.getenv(cohorte.ENV_LOG_FILE)

    # Prepare the file handler
    if logfile:
        fileh = logging.FileHandler(logfile, encoding='UTF-8')

        # ... set its level
        if debug:
            fileh.setLevel(logging.DEBUG)
        else:
            fileh.setLevel(logging.INFO)

        # ... prepare its formatter
        formatter = logging.Formatter("%(asctime)s:%(levelname)-8s:%(name)-20s:"
                                      "%(threadName)-15s: %(message)s "
                                      "(%(module)s.%(funcName)s() @%(lineno)d)")
        fileh.setFormatter(formatter)

        # ... register it
        root_log.addHandler(fileh)

    else:
        root_log.warning("No output log file given.")

    # Done
    root_log.info("Root log configured.")

# ------------------------------------------------------------------------------

def main(args=None):
    """
    Script entry point if called directly.
    Uses sys.argv to determine the boot options if the *args* parameter is None.
    
    **WARNING:** This method changes the log level of the logging module.
    
    :param args: An optional list of arguments (used instead of sys.argv)
    :return: An integer error code, 0 for success (see load_isolate())
    """
    # The argparse module must be loaded as late as possible
    import argparse
    parser = argparse.ArgumentParser(description="Cohorte Python bootstrap")

    # Isolate boot parameters
    group = parser.add_argument_group("Isolate boot")
    group.add_argument("--uid", action="store",
                       dest="isolate_uid", default=None, metavar="UID",
                       help="Unique IDentifier of this isolate.")

    # TODO: Are node & broker mutually exclusive ?
    group.add_argument('--node', action="store",
                       dest="isolate_node", default=None, metavar="NODE",
                       help="Sets the isolate node name. Only used by the "
                       "forker, i.e. if no configuration broker is given")

    group.add_argument("--configuration-broker", action="store",
                       dest="config_broker", default=None, metavar="URL",
                       help="URL to the configuration broker. If not given, " \
                            "this isolate will be a forker.")

    group.add_argument("--state-updater", action="store",
                       dest="state_updater", default=None, metavar="URL",
                       help="URL to the state updater. Should be given if " \
                            "--configuration-broker is.")

    group.add_argument("--start-monitor", action="store_true",
                       dest="start_monitor", default=False,
                       help="Start the monitor once initialized " \
                            "(only works on a forker)")

    # Logging options
    group = parser.add_argument_group("Logging options")
    group.add_argument("--logfile", action="store",
                       dest="logfile", default=None, metavar="FILE",
                       help="Sets the output log file")

    group.add_argument("-d", "--debug", action="store_true",
                       dest="debug", default=False,
                       help="Sets the isolate logging in debug mode")

    group.add_argument("-v", "--verbose", action="store_true",
                       dest="verbose", default=False,
                       help="Sets the isolate logging in verbose mode")

    # Other options
    parser.add_argument("--version", action="version",
                        version="Cohorte bootstrap {0}".format(__version__))

    # Parse arguments
    args = parser.parse_args(args)

    # Set up the logger
    configure_logger(args.logfile, args.debug, args.verbose)

    # Find HOME and BASE
    home, base = find_cohorte_directories()

    # Prepare the framework properties
    framework_properties = {'pelix.debug': args.debug,
                            cohorte.PROP_DEBUG: args.debug,
                            cohorte.PROP_VERBOSE: args.verbose,
                            cohorte.PROP_HOME: home,
                            cohorte.PROP_BASE: base}

    if args.isolate_uid:
        # The isolate UID has been given
        framework_properties[cohorte.PROP_UID] = args.isolate_uid

    if args.config_broker:
        # The configuration broker URL has been given
        framework_properties[cohorte.PROP_CONFIG_BROKER] = args.config_broker

    elif args.isolate_node:
        # The node name has been given, and the configuration broker is missing
        framework_properties[cohorte.PROP_NODE] = args.isolate_node

    if args.state_updater:
        # The state updater URL has been given
        framework_properties[cohorte.PROP_STATE_UPDATER] = args.state_updater

    if args.start_monitor:
        # The forker must start a monitor
        framework_properties[cohorte.PROP_START_MONITOR] = True


    # Run PDB on unhandled exceptions, in debug mode
    use_pdb = args.debug and sys.stdin.isatty()
    if use_pdb:
        import pdb, traceback
        def info(exctype, value, tb):
            traceback.print_exception(exctype, value, tb)
            pdb.pm()
        sys.excepthook = info

    try:
        # Load the isolate and wait for it to stop
        load_isolate(framework_properties, args.state_updater, use_pdb)
        return 0

    except Exception as ex:
        _logger.exception("Error running the isolate: %s", ex)
        return 1

# ------------------------------------------------------------------------------

if __name__ == "__main__":
    sys.exit(main())
