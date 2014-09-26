#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Dummy node starter: simply provides a service required by the top composer.

Should aggregate some services that would start nodes (VMs, EC2, ...)

:author: Thomas Calmant
:copyright: Copyright 2013, isandlaTech
:license: GPLv3
:version: 0.1.0

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

# Cohorte constants
import cohorte

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.SERVICE_NODE_STARTER)
@Instantiate("node-starter")
class NodeStarter(object):
    """
    Dummy node starter
    """
    def start_nodes(self, names):
        """
        Does nothing
        """
        pass

    def start_node(self, name):
        """
        Does nothing
        """
        pass

    def stop_node(self, name):
        """
        Does nothing
        """
        pass
