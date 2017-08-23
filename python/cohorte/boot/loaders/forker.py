#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE forker/monitor loader

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
import cohorte
import logging
import os
import pelix.framework
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Requires, Property
import uuid

import cohorte.boot.loaders.utils as utils
import herald


# iPOPO Decorators
# COHORTE constants
# Herald
# ------------------------------------------------------------------------------
# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

ISOLATE_LOADER_FACTORY = 'cohorte-loader-forker-factory'
""" Forker loader factory name """

LOADER_KIND = 'forker'
""" Kind of isolate started with this loader """

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

        :return: A tuple: (node UID, node name)
        """
        # Generate the node UID
        uid = str(uuid.uuid4())

        # Get the possibly configured node name
        name = self._context.get_property(cohorte.PROP_NODE_NAME)

        if not name:
            # Get name from environment
            name = os.getenv(cohorte.ENV_NODE_NAME)
            if name:
                # Allow the user to use variables in the node name
                name = os.path.expandvars(name)

        if not name:
            # Still no node name, use the UID
            name = uid

            # Print a message to indicate the situation
            _logger.warning("No node name given, using a generated one. "
                            "To specify a node name, use the %s environment "
                            "property or the %s framework property.",
                            cohorte.ENV_NODE_NAME, cohorte.PROP_NODE_NAME)

        # Update framework properties
        self._framework.add_property(cohorte.PROP_NODE_NAME, name)
        self._framework.add_property(cohorte.PROP_NODE_UID, uid)
        return uid, name

    def _update_uid(self):
        """
        Sets the isolate UID property. Generates it if not indicated in
        framework properties.
        Updates the framework properties if needed.

        :return: The isolate UID
        """
        uid = self._context.get_property(cohorte.PROP_UID)
        if not uid:
            # Use the framework UID
            uid = self._context.get_property(pelix.framework.FRAMEWORK_UID)
            self._framework.add_property(cohorte.PROP_UID, uid)
        return uid

    @staticmethod
    def prepare_state_updater(url):
        """
        Does nothing
        """
        pass

    @staticmethod
    def update_state(new_state, extra=None):
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

        # Get the node UID and name
        node_uid, node_name = self._update_node()

        # Generate the forker UID if needed
        uid = self._update_uid()
       
        # Update the file finder, as framework properties may have been
        # modified
        self._finder.update_roots()

        # Set up isolate/forker specific framework properties
        self._framework.add_property(cohorte.PROP_KIND, LOADER_KIND)
        self._framework.add_property(cohorte.PROP_NAME, cohorte.FORKER_NAME)

        # Setup Herald property
        self._framework.add_property(herald.FWPROP_PEER_UID, uid)
        self._framework.add_property(herald.FWPROP_PEER_NAME,
                                     cohorte.FORKER_NAME)
        self._framework.add_property(herald.FWPROP_NODE_UID, node_uid)
        self._framework.add_property(herald.FWPROP_NODE_NAME, node_name)
        self._framework.add_property(herald.FWPROP_PEER_GROUPS,
                                     ('all', 'forkers', 'monitors',
                                      node_uid, node_name))

        # Load the boot components
        boot_config = self._config.load_boot(LOADER_KIND)

        # Get the node Data directory
        node_data_dir = boot_config.properties['cohorte.node.data.dir']
        _logger.info('''Loading a forker with the following properties:
* Home.....: %s
* Base.....: %s
* Node UID.: %s
* Node Name: %s
* Node Data: %s
* UID......: %s''', home, base, node_uid, node_name, node_data_dir, uid)        

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
