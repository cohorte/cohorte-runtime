#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Monitor package

Contains all modules and packages specific to the monitor

:author: Thomas Calmant
:license: Apache Software License 2.0

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

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

MONITOR_NAME = 'cohorte.internals.monitor'
""" All monitors have the same name """

# ------------------------------------------------------------------------------

SERVICE_STATUS = "cohorte.monitor.status"
""" Specification of the monitor status service """

SERVICE_MONITOR = "cohorte.monitor.core"
""" Specification of the monitor core service """

# ------------------------------------------------------------------------------

__SIGNALS_PLATFORM_PREFIX = "/cohorte/platform"
""" Prefix to all platform signals """

SIGNALS_PLATFORM_PATTERN = "{0}/*".format(__SIGNALS_PLATFORM_PREFIX)
""" Pattern to catch all platform signals """

SIGNAL_STOP_PLATFORM = "{0}/stop".format(__SIGNALS_PLATFORM_PREFIX)
""" Signals requesting the platform to completely stop """

# ######### added by: Bassem D.
SIGNAL_STOP_NODE = "{0}/stopnode".format(__SIGNALS_PLATFORM_PREFIX)
""" Requests the node to stop """
# #########

SIGNAL_PLATFORM_STOPPING = "{0}/platform-stop"\
    .format(__SIGNALS_PLATFORM_PREFIX)
"""
Sets the monitors and forkers into platform stopping mode
- sent by monitor or forker, to all monitors and forkers
- no content
"""

# ------------------------------------------------------------------------------

__SIGNALS_ISOLATE_PREFIX = "/cohorte/isolate"
""" Prefix to all isolate signals """

SIGNALS_ISOLATE_PATTERN = "{0}/*".format(__SIGNALS_ISOLATE_PREFIX)
""" Pattern to catch all isolate status signals """

SIGNAL_STOP_ISOLATE = "{0}/stop".format(__SIGNALS_ISOLATE_PREFIX)
""" Requests the isolate to stop """

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
