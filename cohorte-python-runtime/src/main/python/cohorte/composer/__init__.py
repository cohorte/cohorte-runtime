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
# Service specifications

# Utilities #

SERVICE_PARSER = 'cohorte.composer.parser'
""" Specification of a composition parser """

SERVICE_COMPONENT_FINDER = 'cohorte.composer.finder'
""" Specification of a component finder """

SERVICE_TOP_STORAGE = 'cohorte.composer.top.storage'
""" Specification of a distribution storage, hosted by node composers """

# Composers #

SERVICE_COMPOSER_TOP = 'cohorte.composer.top'
""" Specification of the Top Composer service """

SERVICE_COMPOSER_NODE = 'cohorte.composer.node'
""" Specification of the Node Composer service """

# FIXME: 'cohorte.composer.isolate'
SERVICE_COMPOSER_ISOLATE = 'java:/org.cohorte.composer.api.IIsolateComposer'
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

SERVICE_STATUS_ISOLATE = 'cohorte.composer.status.isolate'
""" Specification of the Isolate status storage """

# History  #

SERVICE_HISTORY_NODE = 'cohorte.composer.history.node'
""" Specification of the Node status history """

# Commanders #

SERVICE_COMMANDER_TOP = 'cohorte.composer.commander.top'
""" Specification of the Top commander """

SERVICE_COMMANDER_NODE = 'cohorte.composer.commander.node'
""" Specification of the Node commander """

# Criterion #

SERVICE_TOP_CRITERION_DISTANCE = 'cohorte.composer.top.criterion.distance'
""" Specification of a distance criterion service for the top distributor """

SERVICE_NODE_CRITERION_DISTANCE = 'cohorte.composer.node.criterion.distance'
""" Specification of a distance criterion service for the node distributor """

SERVICE_NODE_CRITERION_RELIABILITY = \
    'cohorte.composer.node.criterion.reliability'
""" Specification of a factory reliability criterion service """

# Isolate agent #

SERVICE_AGENT_ISOLATE = 'cohorte.composer.isolate.agent'
""" Specification of an isolate composer agent """

# ------------------------------------------------------------------------------
# Node and Isolate Composer services properties

PROP_NODE_NAME = 'cohorte.composer.node.name'
""" Name of the node the service is executed on """

PROP_NODE_UID = 'cohorte.composer.node.uid'
""" UID of the node the service is executed on """

PROP_ISOLATE_NAME = 'cohorte.composer.isolate.name'
""" Name of the isolate the service is executed in """

# ------------------------------------------------------------------------------
# Constants

LANGUAGE_PYTHON = "python"
""" Isolate language: Python 2 """

LANGUAGE_PYTHON3 = "python3"
""" Isolate language: Python 3 """

LANGUAGES_PYTHON = (LANGUAGE_PYTHON, LANGUAGE_PYTHON3)
""" Isolate language: Python 2 or 3 """

LANGUAGE_JAVA = "java"
""" Isolate language: Java """
