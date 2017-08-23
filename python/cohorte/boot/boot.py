#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Python bootstrap

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

# Python standard library
import argparse
import cohorte
import logging
import os
import pelix.framework
from pelix.ipopo.constants import get_ipopo_svc_ref
from pprint import pformat
import sys
import threading
import traceback

import cohorte.boot.constants as constants


# Ensure that the content of PYTHONPATH has priority over other paths
# This is necessary on Windows, where packages installed in 'develop' mode
# have priority over the PYTHONPATH.
try:
    for path in os.environ['PYTHONPATH'].split(os.pathsep):
        try:
            p = os.path.normpath(path)
            if p in sys.path:
                sys.path.remove(p)
        except IndexError:
            pass

        sys.path.insert(0, path)
except KeyError:
    pass

# COHORTE modules

# Pelix framework

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

MINIMAL_BUNDLES = ('pelix.ipopo.core', 'cohorte.config.finder',
                   'cohorte.config.reader', 'cohorte.config.parser', 'cohorte.config.includer')
""" List of bundles to be installed before the isolate loader """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


def _get_looper(context, looper_name):
    """
    Retrieves the main thread looper corresponding to the system

    :param context: The bundle context
    :param looper_name: Name of the loop handler bundle (or internal name)
    :return: The main thread handler
    :raise ValueError: Unknown looper
    """
    if '.' not in looper_name:
        # Internal name
        looper_name = "cohorte.boot.looper.{0}".format(looper_name)

    # Do not start the bundle, as the framework is still stopped here
    # Also, we only need the bundle module, not its services
    bundle = context.install_bundle(looper_name)
    return bundle.get_module().get_looper()


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


def load_isolate(pelix_properties, state_updater_url=None,
                 looper_name=None, fail_on_pdb=False):
    """
    Starts a Pelix framework, installs iPOPO and boot modules and waits for
    the framework to stop

    :param pelix_properties: Pelix framework instance properties
    :param state_updater_url: URL to access the isolate state updater
    :param looper_name: Name of the main thread loop handler
    :param fail_on_pdb: If true, ``pdb.post_mortem()`` is called if an
                        exception occurs starting the framework
    :raise Exception: All exceptions are propagated
    """
    # Give some information
    _logger.debug("Running Python %s from %s",
                  '.'.join(str(part) for part in sys.version_info),
                  sys.executable)
    _logger.debug("Starting Pelix framework with properties:\n%s",
                  pformat(pelix_properties))

    # Update Python path (if necessary)
    for key in (cohorte.PROP_HOME, cohorte.PROP_BASE):
        repo = os.path.join(pelix_properties[key], "repo")
        if repo not in sys.path:
            _logger.debug("Adding %s to Python path", repo)
            sys.path.insert(0, repo)

    # Prepare the framework
    framework = \
        pelix.framework.FrameworkFactory.get_framework(pelix_properties)

    if not looper_name:
        # Run the framework in the main thread (nothing to do)
        _run_framework(framework, state_updater_url, fail_on_pdb)
    else:
        # Get the main thread handler
        context = framework.get_bundle_context()
        looper = _get_looper(context, looper_name)
        looper.setup(sys.argv)

        # Register the looper as a service
        context.register_service(cohorte.SERVICE_LOOPER, looper, None)

        # Run the framework in a new thread
        threading.Thread(target=_safe_run_framework,
                         args=(framework, looper,
                               state_updater_url, fail_on_pdb),
                         name="framework").start()

        # Let the main thread loop
        try:
            _logger.debug("Entering main thread loop...")
            looper.loop()
            _logger.debug("Exiting main thread loop...")
        finally:
            # Stop the framework if the looper stops
            _logger.debug("Stopping the framework...")
            framework.stop()


def _safe_run_framework(framework, looper, state_updater_url, fail_on_pdb):
    """
    Starts the framework, logs exceptions

    :param framework: An instance of framework
    :param looper: The main thread handler
    :param state_updater_url: URL to access the isolate state updater
    :param fail_on_pdb: If true, ``pdb.post_mortem()`` is called if an
                        exception occurs starting the framework
    """
    try:
        # Run the framework
        current_thread = threading.current_thread()
        _logger.debug("Starting framework in thread '%s' (%s)",
                      current_thread.name, current_thread.ident)
        _run_framework(framework, state_updater_url, fail_on_pdb)
    except Exception as ex:
        # Log the exception
        _logger.exception("Error running the framework: %s", ex)
    finally:
        # Stop the looper
        _logger.debug("Stopping the looper...")
        looper.stop()


def _run_framework(framework, state_updater_url, fail_on_pdb):
    """
    Starts the framework

    :param framework: An instance of framework
    :param state_updater_url: URL to access the isolate state updater
    :param fail_on_pdb: If true, ``pdb.post_mortem()`` is called if an
                        exception occurs starting the framework
    :raise Exception: All exceptions are propagated
    """
    try:
        context = framework.get_bundle_context()
        framework.start()

        # Install & start configuration bundles
        for name in MINIMAL_BUNDLES:
            _logger.debug('Installing bundle %s...', name)
            bundle = context.install_bundle(name)
            bundle.start()
            _logger.debug('Bundle %s (%d) started',
                          name, bundle.get_bundle_id())

        # Install the text UI if requested. It still needs the Shell Core
        # service to be usable
        if context.get_property(cohorte.PROP_SHELL_CONSOLE):
            _logger.debug("Installing the Pelix Shell UI "
                          "(requires pelix.shell.core to work)")
            context.install_bundle("pelix.shell.console").start()

        # Find the isolate loader to use
        if context.get_property(cohorte.PROP_CONFIG_BROKER):
            # If a broker has been given, use the Broker client...
            loader_bundle_name = 'cohorte.boot.loaders.broker'
        else:
            # ... else use the ForkerLoader
            loader_bundle_name = 'cohorte.boot.loaders.forker'

        # Install & start the loader bundle
        _logger.debug("Using isolate loader: %s.", loader_bundle_name)
        loader_bundle = context.install_bundle(loader_bundle_name)
        loader_bundle.start()

        # Retrieve the loader service & load the isolate
        loader = _get_loader(context, loader_bundle)
        _logger.debug("Isolate booting...")

        # Prepare the access to the state updater
        loader.prepare_state_updater(state_updater_url)
        loader.update_state(constants.STATE_LOADING)

        try:
            # Load the isolate
            loader.load(None)
        except Exception as ex:
            # Something wrong occurred
            loader.update_state(constants.STATE_FAILED, str(ex))
            raise
        else:
            # Isolate loaded
            loader.update_state(constants.STATE_LOADED)
            _logger.debug("Isolate loaded.")

        # Wait forever for the framework to stop
        _logger.debug("Waiting for the isolate to stop...")
        try:
            loader.wait()
        except KeyboardInterrupt:
            # Stop waiting on keyboard interruption
            _logger.debug("Got keyboard interruption, stopping.")

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


class ColorFormatter(logging.Formatter):
    """
    Colored logging output for Linux
    """
    RESET_SEQ = "\033[0m"
    """ Reset color sequence """

    COLOR_SEQ_FORMAT = "\033[1;{0:d}m"
    """ Model of a color sequence """

    def __init__(self, fmt=None, datefmt=None, use_colors=True):
        """
        Sets up the format

        Inspired from:
        http://stackoverflow.com/questions/384076/how-can-i-color-python-logging-output
        and:
        http://pueblo.sourceforge.net/doc/manual/ansi_color_codes.html

        :param fmt: Log line format
        :param datefmt: Format of the log time stamp
        :param use_colors: If True, uses colors control sequences
        """
        logging.Formatter.__init__(self, fmt=fmt, datefmt=datefmt)

        # Color usage flag
        self.use_colors = use_colors

        # Log level -> color
        self.colors = {
            logging.DEBUG: 34,  # BLUE
            logging.INFO: 30,  # BLACK
            logging.WARNING: 33,  # YELLOW
            logging.ERROR: 41,  # RED (background)
            logging.CRITICAL: 45,  # PURPLE (background)
        }

    def format(self, record):
        """
        Formats a log record
        """
        loglevel = record.levelno
        levelname = record.levelname

        if self.use_colors and loglevel in self.colors:
            # Colorize the log level name
            color = self.COLOR_SEQ_FORMAT.format(self.colors[loglevel])
            record.levelname = "{colorseq}{level}{resetseq}" \
                               .format(level=record.levelname,
                                       colorseq=color,
                                       resetseq=self.RESET_SEQ)

        # Format the line
        result = logging.Formatter.format(self, record)

        # Reset the log record
        record.levelname = levelname
        return result


def configure_logger(logfile, debug, verbose, color):
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
    :param color: The console colored output flag
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
    console_format = "%(asctime)s:%(levelname)-8s:%(name)-20s: %(message)s"
    if color:
        formatter = ColorFormatter(console_format)
    else:
        formatter = logging.Formatter(console_format)

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
        formatter = logging.Formatter(
            "%(asctime)s:%(levelname)-8s:%(name)-20s:%(threadName)-15s: "
            "%(message)s (%(module)s.%(funcName)s() @%(lineno)d)")
        fileh.setFormatter(formatter)

        # ... register it
        root_log.addHandler(fileh)
    else:
        root_log.warning("No output log file given.")

    # Calm down some loggers
    for name in ('pelix.remote', 'io_watcher', 'sleekxmpp', 'requests'):
        logging.getLogger(name).setLevel(logging.WARNING)

    # Done
    root_log.info("Root log configured.")

# ------------------------------------------------------------------------------


def main(args=None):
    """
    Script entry point if called directly.
    Uses sys.argv to determine the boot options if the *args* parameter is
    None.

    **WARNING:** This method changes the log level of the logging module.

    :param args: An optional list of arguments (used instead of sys.argv)
    :return: An integer error code, 0 for success (see load_isolate())
    """
    # override main module for  the start of isolate 
    sys.modules["__main__"] = sys.modules[__name__]
    
    parser = argparse.ArgumentParser(description="Cohorte Python bootstrap")
    # Isolate boot parameters
    group = parser.add_argument_group("Isolate boot")
    group.add_argument("--uid", action="store",
                       dest="isolate_uid", default=None, metavar="UID",
                       help="Unique identifier of this isolate.")

    group.add_argument('--node', action="store",
                       dest="isolate_node_name", default=None, metavar="NAME",
                       help="Sets the isolate node name. Only used by the "
                       "forker, i.e. if no configuration broker is given")

    group.add_argument("--configuration-broker", action="store",
                       dest="config_broker", default=None, metavar="URL",
                       help="URL to the configuration broker. If not given, "
                       "this isolate will be a forker.")

    group.add_argument("--state-updater", action="store",
                       dest="state_updater", default=None, metavar="URL",
                       help="URL to the state updater. Should be given if "
                       "--configuration-broker is.")

    group.add_argument("--forker-http-port", action="store",
                       dest="forker_http_port", default=None,
                       help="Port of the Http service of the Forker.")   

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

    group.add_argument("-c", "--color", action="store_true",
                       dest="color", default=False,
                       help="Colors the console output")

    # Threading options
    group = parser.add_argument_group("Threading options")
    group.add_argument('-l', "--looper", action="store",
                       dest="looper_name", default=None, metavar="NAME",
                       help="The main thread loop handler name")

    # Other options
    parser.add_argument('-t', "--top-composer", action="store_true",
                        dest="top_composer", default=False,
                        help="If True and given to a monitor, starts the "
                        "TopComposer")

    parser.add_argument("--console", action="store_true",
                        dest="install_shell_console", default=False,
                        help="If True, the shell console will be started")

    parser.add_argument("--version", action="version",
                        version="Cohorte bootstrap {0}".format(__version__))
    
  
    parser.add_argument('--env', action='append', dest="env_isolate_param")
    # Parse arguments
    args = parser.parse_args(args)
    
    
    # Set up the logger
    configure_logger(args.logfile, args.debug, args.verbose, args.color)

    # Log isolate arguments
    _logger.debug("Isolate boot arguments:\n - %s", '\n - '.join(sys.argv))

    # Find HOME and BASE
    home, base = find_cohorte_directories()

    # Prepare the framework properties
    framework_properties = {'pelix.debug': args.debug,
                            cohorte.PROP_DEBUG: args.debug,
                            cohorte.PROP_VERBOSE: args.verbose,
                            cohorte.PROP_COLORED: args.color,
                            cohorte.PROP_HOME: home,
                            cohorte.PROP_BASE: base}


    # TODO add envs property if it's passed in order to be retrieve by environmentParameter component 
    if args.env_isolate_param:
         # The isolate environment paremter
        framework_properties[cohorte.PROP_ENV_STARTER] = args.env_isolate_param

    if args.isolate_uid:
        # The isolate UID has been given
        framework_properties[cohorte.PROP_UID] = args.isolate_uid

        # Force the isolate UID as framework UID
        framework_properties[pelix.framework.FRAMEWORK_UID] = args.isolate_uid

    if args.config_broker:
        # The configuration broker URL has been given, i.e. not a forker
        framework_properties[cohorte.PROP_CONFIG_BROKER] = args.config_broker
    elif args.isolate_node_name:
        # The node name has been given, and the configuration broker is missing
        # (in here if no broker given, i.e. if isolate is a forker)
        framework_properties[cohorte.PROP_NODE_NAME] = args.isolate_node_name

    if args.state_updater:
        # The state updater URL has been given
        framework_properties[cohorte.PROP_STATE_UPDATER] = args.state_updater

    if args.top_composer:
        # The isolate contains the TopComposer
        framework_properties[cohorte.PROP_RUN_TOP_COMPOSER] = True

    if args.install_shell_console:
        # The isolate must activate its shell console (text UI)
        framework_properties[cohorte.PROP_SHELL_CONSOLE] = True
    
    if args.forker_http_port:
        # The forker Http port
        framework_properties[cohorte.PROP_FORKER_HTTP_PORT] = args.forker_http_port

    # Run PDB on unhandled exceptions, in debug mode
    use_pdb = args.debug and sys.stdin.isatty()
    if use_pdb:
        import pdb

        def pm_exception(exctype, value, traceb):
            """
            Post-mortem exception handling (starts PDB)
            """
            traceback.print_exception(exctype, value, traceb)
            pdb.pm()

        sys.excepthook = pm_exception
    else:
        def log_exception(ex_cls, ex, traceb):
            """
            Logs an unhandled exception
            """
            logging.critical('%s: %s', ex_cls, ex)
            logging.critical(''.join(traceback.format_tb(traceb)))

        sys.excepthook = log_exception

    try:
        # Load the isolate and wait for it to stop
        load_isolate(framework_properties, args.state_updater,
                     args.looper_name, use_pdb)
        return 0
    except Exception as ex:
        _logger.exception("Error running the isolate: %s", ex)
        return 1

# ------------------------------------------------------------------------------

if __name__ == "__main__":
    sys.exit(main())
