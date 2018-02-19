#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Cohorte isolate loaders package

Provides the bundles to load a Python isolate, using built-in constants, a
configuration broker...

:author: Thomas Calmant
:license: Apache Software License 2.0
"""

# Python standard library
import logging

# Pelix / iPOPO
import pelix.framework
import pelix.ipopo.constants as constants

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

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
            current = context.get_property(key)
            if current != value:
                logger.debug(
                    "Couldn't set the property %r to %r (current: %r)",
                    key, value, current)

    # Load Forker bundles
    for bundle in boot_config.bundles:
        try:
            # Install & start it
            context.install_bundle(bundle.name).start()
        except pelix.framework.BundleException as ex:
            if bundle.optional:
                logger.exception(ex)
                # The error can be ignored
                logger.info("Error installing bundle '%s': %s",
                            bundle.name, ex)
            else:
                # Fatal error
                raise

    if boot_config.composition:
        # Instantiate iPOPO components, if any
        with constants.use_ipopo(context) as ipopo:
            for component in boot_config.composition:
                # If not indicated, tell iPOPO to restart this component after
                # bundle update
                if constants.IPOPO_AUTO_RESTART not in component.properties:
                    component.properties[constants.IPOPO_AUTO_RESTART] = True

                # Instantiate the component
                ipopo.instantiate(component.factory, component.name,
                                  component.properties)
    else:
        logger.debug("No component to instantiate")
