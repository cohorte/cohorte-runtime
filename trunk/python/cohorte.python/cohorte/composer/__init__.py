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

SERVICE_COMPOSITION_PARSER = 'cohorte.composer.parser'
""" Specification of a composition parser """

SERVICE_COMPOSITION_LOADER = 'cohorte.composer.loader'
""" Specification of a composition loader """
