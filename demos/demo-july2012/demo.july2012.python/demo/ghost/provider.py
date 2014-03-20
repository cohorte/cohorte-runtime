#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Basic crasher

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 0.1

..

    This file is part of Cohorte.

    Cohorte is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cohorte is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cohorte. If not, see <http://www.gnu.org/licenses/>.
"""

# Module version
__version_info__ = (0, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Validate, Invalidate, \
    Property

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory('ghost-provider-factory')
@Property('_specification', 'specification')
class Provider(object):
    """
    Ghost crasher
    """
    def __init__(self):
        """
        Sets up members
        """
        # Service registration
        self._registration = None

        # Service specification property
        self._specification = None


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        if not self._specification:
            # Nothing to do
            _logger.error("No specification set...")
            return

        _logger.info("Providing %s...", self._specification)
        self._registration = context.register_service(self._specification,
                                                      self, {})


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        if self._registration is not None:
            _logger.info("Unregistering %s...", self._specification)
            self._registration.unregister()
            self._registration = None
