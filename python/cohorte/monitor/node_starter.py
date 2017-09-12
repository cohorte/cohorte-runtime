#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Dummy node starter: simply provides a service required by the top composer.

Should aggregate some services that would start nodes (VMs, EC2, ...)

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

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate

# Cohorte constants
import cohorte

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------


@ComponentFactory()
@Provides(cohorte.SERVICE_NODE_STARTER)
@Instantiate("node-starter")
class NodeStarter(object):
    """
    Dummy node starter
    """
    @staticmethod
    def start_nodes(names):
        """
        Does nothing
        """
        pass

    @staticmethod
    def start_node(name):
        """
        Does nothing
        """
        pass

    @staticmethod
    def stop_node(name):
        """
        Does nothing
        """
        pass
