#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Cohorte Composer module

Provides components to parse a JSON description of a composition and to
instantiate these components in iPOPO or in iPOJO in an embedded JVM.

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

# Utilities #

SERVICE_PARSER = 'cohorte.composer.parser'
""" Specification of a composition parser """

# Composers #

SERVICE_COMPOSER_TOP = 'cohorte.composer.top'
""" Specification of the Top Composer service """

SERVICE_COMPOSER_NODE = 'cohorte.composer.node'
""" Specification of the Node Composer service """

SERVICE_COMPOSER_ISOLATE = 'cohorte.composer.isolate'
""" Specification of the Isolate Composer service """

# Distributors #

SERVICE_DISTRIBUTOR_NODE = 'cohorte.composer.distributor.node'
""" Specification of a node distributor """

SERVICE_DISTRIBUTOR_ISOLATE = 'cohorte.composer.distributor.isolate'
""" Specification of a isolate distributor """

# Status #

SERVICE_STATUS_TOP = 'cohorte.composer.status.top'
""" Specification of the Top status storage """

SERVICE_STATUS_NODE = 'cohorte.composer.status.node'
""" Specification of the Node status storage """

# Commanders #

SERVICE_COMMANDER_TOP = 'cohorte.composer.commander.top'
""" Specification of the Top commander """

SERVICE_COMMANDER_NODE = 'cohorte.composer.commander.node'
""" Specification of the Node commander """

# Criterion #

SERVICE_CRITERION_DISTANCE = 'cohorte.composer.criterion.distance'
""" Specification of a distance criterion service """

SERVICE_CRITERION_RELIABILITY = 'cohorte.composer.criterion.reliability'
""" Specification of a factory reliability criterion service """

# ------------------------------------------------------------------------------

PROP_NODE_NAME = 'cohorte.composer.node.name'
""" Name of the node the service is executed on """
