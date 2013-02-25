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

SERVICE_COMPONENT_RATING = 'cohorte.composer.rating.storage'
""" Specification of a component rating storage """

SERVICE_COMPONENT_COMPATIBILITY = 'cohorte.composer.compatibility.storage'
""" Specification of a component compatibility storage """

SERVICE_COMPATIBILITY_CHECKER = 'cohorte.composer.compatibility.checker'
""" Specification of a component compatibility checker """

SERVICE_DISTANCE_CALCULATOR = 'cohorte.composer.distance.calculator'
""" Specification of a component distance calculator """
