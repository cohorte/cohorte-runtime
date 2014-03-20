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

# Local
import demo.ghost

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Property

# Standard library
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory('ghost-crasher-factory')
@Property('_id', 'ghost.id', 'crasher')
class Crasher(demo.ghost.Ghost):
    """
    Ghost crasher
    """
    def handle_order(self, event):
        """
        Handles a ghost order
        """
        if event == "crash":
            _logger.warning("Crasher will crash")
            self.crash()
