#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Composer Core: Auto-run composition file loader

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE
import cohorte.composer

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Provides, Property

# Standard library
import logging

# ------------------------------------------------------------------------------

AUTORUN_FILE = "autorun_conf.js"
""" Auto-run composition file """

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-composer-loader-factory")
@Provides(cohorte.composer.SERVICE_COMPOSITION_LOADER)
@Requires('_parser', cohorte.composer.SERVICE_COMPOSITION_PARSER)
@Requires('_distributor', cohorte.composer.SERVICE_DISTRIBUTOR)
@Property('_autorun_file', 'autorun.file', AUTORUN_FILE)
@Property('_autorun', 'autorun.active', True)
class Loader(object):
    """
    Loads the composition stored in the auto-run file
    """
    def __init__(self):
        """
        Sets up members
        """
        # Properties
        self._autorun = False
        self._autorun_file = None

        # Dependencies
        self._parser = None
        self._distributor = None


    def load_composition(self, filename):
        """
        Loads the composition with the given file name and requests the
        distributor to instantiate it
        
        :param filename: The composition file name
        :raise IOError: Error reading the file
        :raise ValueError: Error parsing the composition
        """
        # Load the composition (look for the file in the "conf" folder first)
        composition = self._parser.load(filename, 'conf')

        # Use the distributor to instantiate the composition
        self._distributor.instantiate(composition)


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        if self._autorun and self._autorun_file:
            # Auto-run activated and valid
            _logger.info("Loading auto-run composition file: %s",
                         self._autorun_file)

            try:
                # Load the composition
                self.load_composition(self._autorun_file)

            except (ValueError, IOError) as ex:
                _logger.exception("Error loading the auto-run composition file"
                                  " (%s): %s", self._autorun_file, ex)
