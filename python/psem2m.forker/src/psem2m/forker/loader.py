#!/usr/bin/python
#-- Content-Encoding: UTF-8 --
"""
The isolate loader for PSEM2M Python.

This module shall be installed in a Pelix instance after iPOPO and PSEM2M base
configuration.

:author: Thomas Calmant
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

from pelix.ipopo.decorators import ComponentFactory, Instantiate, \
    Requires, Validate, Invalidate

# ------------------------------------------------------------------------------

import pelix.framework as pelix

import logging
_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("psem2m-isolate-loader-factory")
@Instantiate("psem2m-isolate-loader")
@Requires("_config", "org.psem2m.isolates.services.conf.ISvcConfig")
class IsolateLoader(object):
    """
    Bundle loader for PSEM2M Python isolates
    """
    def __init__(self):
        """
        Constructor
        """
        self._config = None
        self.context = None

        # Installed bundles list
        self._bundles = []


    def _configure(self, isolate_descr):
        """
        Installs the bundles according to the given isolate description.
        
        The description must be an object compatible with
        base.config._IsolateDescription and inner bundles with
        base.config._BundleDescription. 
        
        If an error occurs during a bundle installation, and if the bundle is
        not optional, the isolate is automatically reset.
        
        :param isolate_descr: Description of the current isolate
        :return: True on success, else False
        """
        for bundle in isolate_descr.get_bundles():
            # Install the bundle
            try:
                bid = self.context.install_bundle(bundle.name)
                bnd = self.context.get_bundle(bid)

                # Store the installed bundle
                self._bundles.append(bnd)

            except pelix.BundleException:
                _logger.exception("Error installing bundle %s",
                                  bundle.get_symbolic_name())

                if not bundle.optional:
                    # Reset isolate on error
                    self.reset()
                    return False

        # Special thing before starting bundle : set up the HTTP port property
        port = isolate_descr.get_access()[1]
        framework = self.context.get_bundle(0)
        framework.add_property("http.port", port)

        _logger.debug("HTTP Port set to %d", port)

        # Start bundles
        for bundle in self._bundles:
            try:
                bundle.start()

            except pelix.BundleException:
                _logger.exception("Error starting bundle %s",
                                  bundle.get_symbolic_name())

                if not bundle.optional:
                    # Reset isolate on error
                    self.reset()
                    return False

        return True


    def setup_isolate(self):
        """
        Configures the current isolate according to the configuration for the
        isolate ID indicated in the framework property ``psem2m.isolate.id``.
        
        :return: True on success, False on error
        """
        isolate_id = self.context.get_property("psem2m.isolate.id")
        if isolate_id is None:
            # No isolate ID found, do nothing
            return False

        # Get the isolate configuration
        app = self._config.get_application()
        isolate_descr = app.get_isolate(isolate_id)
        if isolate_descr is None:
            # No description for this isolate
            return False

        # Reset isolate
        self.reset()

        # Install required bundles
        return self._configure(isolate_descr)


    def reset(self):
        """
        Uninstalls all bundles installed by this component.
        
        Ignores bundles exceptions.
        """
        for bundle in self._bundles:
            try:
                # Uninstall each bundle
                bundle.uninstall()

            except pelix.BundleException:
                # Only log errors
                _logger.exception("Error uninstalling %s",
                                  bundle.get_symbolic_name())

        # Clean up the list
        del self._bundles[:]


    @Validate
    def validate(self, context):
        """
        Component validation
        
        :param context: The bundle context
        """
        self.context = context

        if not self.setup_isolate():
            # An error occurred, stop the framework
            _logger.error("An error occurred starting the bundle: Abandon.")
            context.get_bundle(0).stop()

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidation
        
        :param context: The bundle context
        """
        # Uninstall bundles
        self.reset()
        self.context = None
