#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Dummy node starter: simply provides a service required by the top composer.

Should aggregate some services that would start nodes (VMs, EC2, ...)

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 0.1.0

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
