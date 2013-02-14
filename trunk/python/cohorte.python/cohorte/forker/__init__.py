#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Forker package

Contains all modules and packages specific to the forker

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

FORKER_NAME = 'cohorte.internals.forker'
""" All forkers have the same name """

# ------------------------------------------------------------------------------

SERVICE_AGGREGATOR = 'cohorte.forker.aggregator'
""" Specification of a forker aggregator """

SERVICE_FORKER_LISTENER = 'cohorte.forker.listener'
"""
Specification of a forker event listener:
- forker_ready(uid, node)
- forker_gone(uid, node)
"""

# ------------------------------------------------------------------------------

__SIGNALS_FORKER_PREFIX = "/cohorte/forker"
""" Prefix to all forker signals """

SIGNALS_FORKER_PATTERN = "{0}/*".format(__SIGNALS_FORKER_PREFIX)
""" Pattern to catch all forker signals """

SIGNAL_PING_ISOLATE = "{0}/ping".format(__SIGNALS_FORKER_PREFIX)
"""
Ping isolate request:
- sent by any isolate
- contains the UID of the isolate to ping
"""

SIGNAL_START_ISOLATE = "{0}/start".format(__SIGNALS_FORKER_PREFIX)
"""
Start isolate request:
- sent by the monitor
- contains the whole configuration of the isolate to start (with a UID)
"""

SIGNAL_STOP_ISOLATE = "{0}/stop".format(__SIGNALS_FORKER_PREFIX)
"""
Stop isolate request:
- sent by the monitor
- contains the UID of the isolate to stop.
"""

SIGNAL_PLATFORM_STOPPING = "{0}/platform-stop".format(__SIGNALS_FORKER_PREFIX)
"""
Sets the forker into platform stopping mode
- sent by the monitor
- no content
"""

# ------------------------------------------------------------------------------

REQUEST_NO_MATCHING_FORKER = -20
""" No forker for the node """

REQUEST_NO_PROCESS_REF = 2
""" No reference to the isolate process, unknown state """

REQUEST_NO_WATCHER = 3
""" No isolate watcher could be started (active isolate waiter) """

REQUEST_ERROR = -3
""" Error sending the request """

REQUEST_NO_RESULT = -2
""" Forker didn't returned any result """

REQUEST_TIMEOUT = -1
""" Forker timed out """

REQUEST_SUCCESS = 0
""" Successful operation """

REQUEST_ALREADY_RUNNING = 1
""" The isolate is already running """

REQUEST_RUNNER_EXCEPTION = 4
""" An error occurred calling the forker """

REQUEST_UNKNOWN_KIND = 5
""" Unknown kind of isolate """

REQUEST_SUCCESSES = (REQUEST_SUCCESS, REQUEST_ALREADY_RUNNING)
""" A request is a success if the isolate is running """

# ------------------------------------------------------------------------------

PING_ALIVE = 0
""" Isolate is alive (response to ping) """

PING_DEAD = 1
""" Process is dead (not running) """

PING_STUCK = 2
""" Process is stuck (running, but not responding) """
