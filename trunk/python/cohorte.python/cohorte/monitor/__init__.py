#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Monitor package

Contains all modules and packages specific to the monitor

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

MONITOR_NAME = 'cohorte.internals.forker'
""" All monitors have the same name """

# ------------------------------------------------------------------------------

SERVICE_STATUS = "cohorte.monitor.status"
""" Specification of the monitor status service """

SERVICE_MONITOR = "cohorte.monitor.core"
""" Specification of the monitor core service """

# ------------------------------------------------------------------------------

SIGNAL_STOP_PLATFORM = "/cohorte/platform/stop"
""" Signals requesting the platform to completely stop """

SIGNAL_STOP_ISOLATE = "/cohorte/isolate/stop"
""" Requests the isolate to stop """

# ------------------------------------------------------------------------------

__SIGNALS_ISOLATE_PREFIX = "/cohorte/isolate"
""" Prefix to all isolate signals """

SIGNALS_ISOLATE_PATTERN = "{0}/*".format(__SIGNALS_ISOLATE_PREFIX)
""" Pattern to catch all isolate status signals """

SIGNAL_ISOLATE_LOST = "{0}/lost".format(__SIGNALS_ISOLATE_PREFIX)
"""
Isolate lost signal:
- sent by the forker
- contains the UID of the lost isolate
"""

SIGNAL_ISOLATE_READY = "{0}/ready".format(__SIGNALS_ISOLATE_PREFIX)
"""
Isolate status: ready
- sent by the isolate itself
- no content
"""

SIGNAL_ISOLATE_STOPPING = "{0}/stopping".format(__SIGNALS_ISOLATE_PREFIX)
"""
Isolate status: stopping (an "isolate lost" signal should follow)
- sent by the isolate itself
- no content
"""

# ------------------------------------------------------------------------------
