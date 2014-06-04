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

STATE_INEXISTANT = 0
""" Isolate has been freshly registered in the state directory """

STATE_BOOTING = 1
""" Isolate is in booting stage (boot.py) """

STATE_LOADING = 2
""" Isolate is loading its bundles """

STATE_LOADED = 3
""" Isolate boot completed """
