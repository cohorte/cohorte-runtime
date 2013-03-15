#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Cohorte isolate loaders package

Provides the bundles to load a Python isolate, using built-in constants, a
configuration broker...

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Version
__version__ = '1.0.0'

# ------------------------------------------------------------------------------

# Pelix / iPOPO
import pelix.ipopo.constants as constants
import pelix.framework

# Python standard library
import logging

# ------------------------------------------------------------------------------

def boot_load(context, boot_config):
    """
    Utility method to do the common isolate boot operations, i.e. applies the
    properties, install the bundles and finally instantiate the components
    indicated in the given boot configuration.
    
    :param context: The bundle context to use to install bundles
    :param boot_config: The boot configuration
    :raise BundleException: Error installing a bundle
    :raise Exception: Unknown error while instantiating components
    """
    logger = logging.getLogger(__name__ + "::boot_load")
    framework = context.get_bundle(0)

    # Set up framework properties, if possible
    for key, value in boot_config.properties.items():
        if not framework.add_property(key, value):
            logger.debug("Couldn't set the '%s' property to '%s' (current: %s)",
                         key, value, context.get_property(key))

    # Load Forker bundles
    for bundle in boot_config.bundles:
        try:
            # Install it
            bundle_id = context.install_bundle(bundle.name)

            # Start it
            context.get_bundle(bundle_id).start()

        except pelix.framework.BundleException as ex:
            if bundle.optional:
                # The error can be ignored
                logger.info("Error installing bundle '%s': %s", bundle.name, ex)
            else:
                # Fatal error
                raise

    if boot_config.composition:
        # Instantiate iPOPO components, if any
        ipopo_ref, ipopo_svc = constants.get_ipopo_svc_ref(context)
        for component in boot_config.composition:
            # If not indicated, tell iPOPO to restart this component after
            # bundle update
            if constants.IPOPO_AUTO_RESTART not in component.properties:
                component.properties[constants.IPOPO_AUTO_RESTART] = True

            # Instantiate the component
            ipopo_svc.instantiate(component.factory, component.name,
                                  component.properties)

        # We don't need iPOPO anymore
        context.unget_service(ipopo_ref)

    else:
        logger.debug("No component to instantiate")
