#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Shell commands to control the node composer

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 3.0.0

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

# Standard library
import logging

# Shell constants
from pelix.shell import SHELL_COMMAND_SPEC, SHELL_UTILS_SERVICE_SPEC

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate, Validate, Invalidate
from pelix.utilities import use_service

# Composer
import cohorte.composer

# ------------------------------------------------------------------------------

# Module version
__version_info__ = (3, 0, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------


@ComponentFactory()
@Requires('_utils', SHELL_UTILS_SERVICE_SPEC)
@Provides(SHELL_COMMAND_SPEC)
@Instantiate('cohorte-composer-shell-node')
class NodeComposerCommands(object):
    """
    Shell commands to control the node composer
    """
    def __init__(self):
        """
        Sets up members
        """
        # Injected services
        self._utils = None

        self._context = None
        self.logger = logging.getLogger('shell-composer-node')

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._context = None

    @staticmethod
    def get_namespace():
        """
        Retrieves the name space of this command handler
        """
        return "node"

    def get_methods(self):
        """
        Retrieves the list of tuples (command, method) for this command handler
        """
        return [('nodes', self.list_nodes),
                ('isolates', self.list_isolates), ]

    def list_nodes(self, io_handler):
        """
        Lists the nodes visible from this isolate
        """
        # Get all node composers
        svc_refs = self._context.get_all_service_references(
            cohorte.composer.SERVICE_COMPOSER_NODE)
        if not svc_refs:
            io_handler.write_line("No node composer found.")
            return

        # Prepare the table
        headers = ('Node Name', 'Node UID')
        lines = sorted((svc_ref.get_property(cohorte.composer.PROP_NODE_NAME),
                        svc_ref.get_property(cohorte.composer.PROP_NODE_UID))
                       for svc_ref in svc_refs)

        # Pretty print
        io_handler.write_line(self._utils.make_table(headers, lines))

    def list_isolates(self, io_handler, node=None):
        """
        Lists the isolates of the given node, or of all nodes
        """
        # Get all node composers
        svc_refs = self._context.get_all_service_references(
            cohorte.composer.SERVICE_COMPOSER_NODE)
        if not svc_refs:
            io_handler.write_line("No node composer found.")
            return

        # Node name/UID -> isolates
        isolates = {}

        # For each node, get the isolate bean
        for svc_ref in svc_refs:
            with use_service(self._context, svc_ref) as composer:
                try:
                    node = svc_ref.get_property(
                        cohorte.composer.PROP_NODE_NAME)
                    isolates[node] = composer.get_running_isolates()
                except Exception as ex:
                    self.logger.error("Error calling composer: %s", ex)

        # Sort by node name
        names = sorted(isolates.keys())

        # Make tree
        lines = []
        for name in names:
            lines.append('+ {0}'.format(name))
            for isolate in isolates[name]:
                lines.append('|- {0}'.format(isolate))

        io_handler.write_line('\n'.join(lines))
