#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Top Composer entry point service

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

:updates:
    MOD_BD_20160919 adding get_composition_json method

"""

# ######### added by: Bassem D.
# Standard Library
import logging
import json
import os
# #########

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Validate, Invalidate, Property

# Composer
import cohorte
import cohorte.composer
import cohorte.monitor

# ------------------------------------------------------------------------------

# Bundle version
import cohorte.version
__version__=cohorte.version.__version__

# ------------------------------------------------------------------------------

# ######### added by: Bassem D.
_logger = logging.getLogger(__name__)
# #########

# ------------------------------------------------------------------------------


@ComponentFactory("cohorte-composer-top-factory")
@Provides(cohorte.composer.SERVICE_COMPOSER_TOP)
@Requires('_distributor', cohorte.composer.SERVICE_DISTRIBUTOR_NODE)
@Requires('_status', cohorte.composer.SERVICE_STATUS_TOP)
@Requires('_commander', cohorte.composer.SERVICE_COMMANDER_TOP)
@Requires('_monitor', cohorte.monitor.SERVICE_MONITOR)
@Requires('_node_starter', cohorte.SERVICE_NODE_STARTER)
# ######### added by: Bassem D.
@Requires('_parser', cohorte.composer.SERVICE_PARSER,
          optional=False)
@Property('_autostart', 'autostart', "True")
@Property('_composition_filename', 'composition.filename', "composition.js")
# #########
# @Instantiate('cohorte-composer-top')
class TopComposer(object):
    """
    The Top Composer entry point
    """
    def __init__(self):
        """
        Sets up components
        """
        self._distributor = None
        self._status = None
        self._commander = None
        self._context = None
        self._node_starter = None
        # ######### added by: Bassem D.
        self._parser = None
        self._autostart = None
        self._composition_filename = None
        self._composition_json = None 
        # #########

    def _set_default_node(self, distribution):
        """
        Chooses a default node for unassigned components

        :param distribution: A Node -> set(RawComponent) dictionary
        :return: The dictionary, with each component assigned to a node
        """
        try:
            # Get the unassigned components
            unassigned = distribution[None]
            del distribution[None]

        except KeyError:
            # Nothing to do
            return distribution

        # FIXME: use a configurable default node
        default_node = self._context.get_property(cohorte.PROP_NODE_NAME)

        # Add the unassigned components to the default one
        distribution.setdefault(default_node, set()).update(unassigned)
        return distribution

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._context = context
        # ######### added by: Bassem D.
        _logger.info("Auto-start composition: %s", self._autostart)
        if str(self._autostart).lower() in ("true", "yes"):
            # Load the composition
            try:
                composition = self._parser.load(
                    self._composition_filename, "conf")
                if composition:
                    _logger.info("Loading composition...")
                    uid = self.start(composition)
                    _logger.info("Started composition: %s -> %s",
                                 composition.name, uid)
                else:
                    _logger.warning("No composition found in %s",
                                    self._composition_filename)
            except OSError:
                _logger.error("Error reading the composition file %s",
                              self._composition_filename)

        else:
            _logger.info("composition should be started manually!")
        # #########

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self._context = None

    def __start(self, name, distribution, uid):
        """
        Stores and instantiates the given distribution

        :param name: The name of the composition
        :param distribution: The computed node distribution
        :param uid: Distribution UID, given only if reloaded from a top storage
        :return: The UID of the instantiated composition
        """
        # Handle components without assigned node
        self._set_default_node(distribution)

        # Store the distribution
        uid = self._status.store(name, distribution, uid)

        # Start required nodes
        self._node_starter.start_nodes(distribution.keys())

        # Tell the commander to start the instantiation on existing nodes
        self._commander.start(distribution)
        return uid

    def start(self, composition):
        """
        Instantiates the given composition

        :param composition: A RawComposition bean
        :return: The UID of the instantiated composition
        """
        # Distribute components
        distribution = self._distributor.distribute(composition)

        # Instantiate them
        return self.__start(composition.name, distribution, None)

    def reload_distribution(self, name, distribution, uid):
        """
        Loads the given distribution, typically given by the store loader

        :param name: The name of the composition
        :param distribution: The previously computed distribution
        :param uid: Distribution UID
        :return: The UID of the instantiated composition
        :raise KeyError: Already known UID
        """
        try:
            # See if it is already in the status
            self._status.get(uid)
        except KeyError:
            # Unknown distribution, instantiate it out of the exception block
            pass
        else:
            # Already known: error
            raise KeyError("Already used distribution UID: %s", uid)

        return self.__start(name, distribution, uid)

    def stop(self, uid):
        """
        Stops the given composition

        :param uid: The UID of the composition to stop
        :raise KeyError: Unknown composition
        """
        # Pop the distribution
        distribution = self._status.pop(uid)

        # Tell all node composers to stop their components
        self._commander.stop(distribution)

    def get_composition_json(self):
        """
        Gets composition JSON file raw content
        """
        if not self._composition_json:
            # parse the composition file
            conf_dir = os.path.join(self._context.get_property("cohorte.base"), "conf")
            file_name = os.path.join(conf_dir, self._composition_filename)
            with open(file_name, "r") as comp_json_file:
                self._composition_json = json.load(comp_json_file)
        return self._composition_json