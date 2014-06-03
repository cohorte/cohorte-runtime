#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Python bootstrap constants

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

STATE_FAILED = -1
""" Boot failed """

STATE_LOADING = 1
""" Isolate is booting (framework is loading) """

STATE_LOADED = 2
""" Isolate boot completed """
