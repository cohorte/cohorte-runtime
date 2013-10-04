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

SERVICE_EXECUTOR = "cohorte.composer.core.executor"
""" Specification of an action executor """

SERVICE_QUEUE = "cohorte.composer.core.queue"
""" Specification of an action queue """

SERVICE_STATUS = "cohorte.composer.core.status"
""" Specification of the composer core status """

SERVICE_RULE_ENGINE = "cohorte.composer.core.ruleengine"
""" Specification of a rule engine """

PROP_ENGINE_FILE = "rules.file"
""" Rule file to load """

PROP_ENGINE_KIND = "rules.kind"
""" Kind of rules for this rule engine (e.g. distribution, status, ...) """
