#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE forker loader

**TODO:**
* Read a *forker.js* file ?

  * Update framework properties accordingly

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
import cohorte.boot.loaders.utils as utils

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Requires, Property

# Python standard library
import logging
import os
import uuid

# ------------------------------------------------------------------------------

ISOLATE_LOADER_FACTORY = 'cohorte-loader-forker-factory'
""" Forker loader factory name """

LOADER_KIND = 'forker'
""" Kind of isolate started with this loader """

FORKER_NAME = 'cohorte.internals.forker'
""" All forkers have the same name """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory(ISOLATE_LOADER_FACTORY)
@Provides(cohorte.SERVICE_ISOLATE_LOADER)
@Property('_handled_kind', cohorte.SVCPROP_ISOLATE_LOADER_KIND, LOADER_KIND)
@Requires('_finder', cohorte.SERVICE_FILE_FINDER)
@Requires('_config', cohorte.SERVICE_CONFIGURATION_READER)
class ForkerLoader(object):
    """
    Forker isolate loader component. Automatically instantiated.
    """
    def __init__(self):
        """
        Sets up members
        """
        # File finder
        self._finder = None

        # Configuration reader
        self._config = None

        # Bundle _context
        self._context = None

        # Framework instance
        self._framework = None


    def _find_cohorte_directories(self):
        """
        Finds the COHORTE Home and Base directories, according to existing
        framework properties or process environment.
        
        :return: A (home, base) tuple.
        :raise KeyError: The Home and Base directories can't be determined
        """
        # Look in the framework properties
        home = self._context.get_property(cohorte.PROP_HOME)
        if not home:
            # Look in the process environment
            home = os.getenv(cohorte.ENV_HOME)

        # Same thing for base
        base = self._context.get_property(cohorte.PROP_BASE)
        if not base:
            # Use found home as default
            base = os.getenv(cohorte.ENV_BASE, home)

        if not base:
            # Base and home are invalid (base is at least equal to home)
            raise KeyError('No correct value found in Home and Base '
                           'properties: {env_home}, {env_base}, '
                           '{prop_home}, {prop_base}'
                           .format(env_home=cohorte.ENV_HOME,
                                   env_base=cohorte.ENV_BASE,
                                   prop_home=cohorte.PROP_HOME,
                                   prop_base=cohorte.PROP_BASE))

        # Expand environment variables
        home = os.path.expanduser(os.path.expandvars(home))
        base = os.path.expanduser(os.path.expandvars(base))

        if not home:
            # Base has been found, but home is missing
            home = base

        # Update framework properties
        self._framework.add_property(cohorte.PROP_HOME, home)
        self._framework.add_property(cohorte.PROP_BASE, base)

        return home, base


    def _update_node(self):
        """
        Sets the isolate Node property, according to existing framework
        property or process environment.
        
        :return: The isolate node name
        :raise KeyError: The node name can't be determined
        """
        node = self._context.get_property(cohorte.PROP_NODE)
        if not node:
            node = os.path.expandvars(os.getenv(cohorte.ENV_NODE))

        if not node:
            # Node name not found
            raise KeyError('No correct value found for the Node name: '
                           '{env_node}, {prop_node}'
                           .format(env_node=cohorte.ENV_NODE,
                                   prop_node=cohorte.PROP_NODE))

        # Update framework properties
        self._framework.add_property(cohorte.PROP_NODE, node)

        return node


    def _update_uid(self):
        """
        Sets the isolate UID property. Generates it if not indicated in
        framework properties.
        Updates the framework properties if needed.
        
        :return: The isolate UID
        """
        uid = self._context.get_property(cohorte.PROP_UID)
        if not uid:
            # Generate a new UID (only keep its string representation)
            uid = str(uuid.uuid4())
            self._framework.add_property(cohorte.PROP_UID, uid)

        return uid


    def prepare_state_updater(self, url):
        """
        Does nothing
        """
        pass


    def update_state(self, new_state, extra=None):
        """
        Does nothing
        """
        pass


    def load(self, configuration):
        """
        Loads the forker
        
        :param configuration: API compatibility argument (raises ValueError
                              if present)
        :raise KeyError: A mandatory property is missing
        :raise ValueError: Invalid parameter/file encountered
        :raise BundleException: Error installing a bundle
        :raise Exception: Error instantiating a component
        """
        if configuration is not None:
            raise ValueError("Do NOT try to give a configuration to the "
                             "Forker loader")

        # To simplify...
        context = self._context

        # Get/Update the Home and Base directories
        home, base = self._find_cohorte_directories()

        # Get the node name
        node = self._update_node()

        # Generate the forker UID if needed
        uid = self._update_uid()

        _logger.info('''Loading a forker with the following properties:
* Home: {home}
* Base: {base}
* Node: {node}
* UID : {uid}'''.format(home=home, base=base, node=node, uid=uid))

        # Update the file finder, as framework properties may have been modified
        self._finder.update_roots()

        # TODO: Set up isolate/forker specific framework properties
        self._framework.add_property(cohorte.PROP_KIND, LOADER_KIND)
        self._framework.add_property(cohorte.PROP_NAME, FORKER_NAME)

        # TODO: Load forker.js configuration file
        # All forkers have the same default ID (discrimination by UID), but the
        # configuration file can indicate a special one
        # ...

        # Load the boot components
        boot_config = self._config.load_boot(LOADER_KIND)

        # Let the utility method do its job
        utils.boot_load(context, boot_config)


    def wait(self):
        """
        Waits for the isolate to stop
        """
        # The framework can be None if something wrong happened
        if self._framework is not None:
            # Simply wait for the framework to stop
            self._framework.wait_for_stop()


    @Validate
    def validate(self, context):
        """
        Component validated
        
        :param context: The bundle context
        """
        # Store the framework access
        self._context = context
        self._framework = context.get_bundle(0)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        
        :param context: The bundle context
        """
        # Clear the framework access
        self._context = None
        self._framework = None
