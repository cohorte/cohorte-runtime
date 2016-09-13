#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Pelix isolate loader

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
import logging

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Validate, \
    Invalidate, Property, Requires

# COHORTE constants
import cohorte
import cohorte.boot.loaders.utils as utils

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

ISOLATE_LOADER_FACTORY = 'cohorte-loader-pelix-factory'
""" Forker loader factory name """

LOADER_KIND = 'pelix'
""" Kind of isolate started with this loader """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------


@ComponentFactory(ISOLATE_LOADER_FACTORY)
@Provides(cohorte.SERVICE_ISOLATE_LOADER)
@Property('_handled_kind', cohorte.SVCPROP_ISOLATE_LOADER_KIND, LOADER_KIND)
@Requires('_config', cohorte.SERVICE_CONFIGURATION_READER)
class PelixLoader(object):
    """
    Pelix isolate loader. Needs a configuration to be given as a parameter of
    the load() method.
    """
    def __init__(self):
        """
        Sets up members
        """
        # Configuration reader
        self._config = None

        # Bundle _context
        self._context = None

    def load(self, configuration):
        """
        Loads the Pelix isolate

        :param configuration: Isolate configuration dictionary (required)
        :raise KeyError: A mandatory property is missing
        :raise ValueError: Invalid parameter/file encountered
        :raise BundleException: Error installing a bundle
        :raise Exception: Error instantiating a component
        """
        if not configuration:
            raise KeyError("A configuration is required to load a "
                           "Pelix isolate")

        # Parse the configuration (boot-like part)
        base_config = self._config.load_boot_dict(configuration)

        # Use the boot utility method to load the isolate
        utils.boot_load(self._context, base_config)
        # Nothing else to do...

    def wait(self):
        """
        Waits for the isolate to stop
        """
        # Simply wait for the framework to stop
        self._context.get_bundle(0).wait_for_stop()

    @Validate
    def validate(self, context):
        """
        Component validated

        :param context: The bundle context
        """
        # Store the framework access
        self._context = context

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated

        :param context: The bundle context
        """
        # Clear the framework access
        self._context = None
