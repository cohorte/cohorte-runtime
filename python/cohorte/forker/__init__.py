#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Forker package

Contains all modules and packages specific to the forker

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

FORKER_NAME = 'cohorte.internals.forker'
""" All forkers have the same name """

# ------------------------------------------------------------------------------

SERVICE_FORKER_LISTENER = 'cohorte.forker.listener'
"""
Specification of a forker event listener:
- forker_ready(uid, node)
- forker_gone(uid, node)
"""

SERVICE_ENV_STARTER = 'cohorte.forker.starter.environment'
""" Specification of isolate starter environment context"""


SERVICE_STARTER = 'cohorte.forker.starter'
""" Specification of an isolate starter """

PROP_STARTER_KINDS = 'forker.starter.kinds'
""" Kinds of isolates handled by the starter """


SERVICE_WATCHER = 'cohorte.forker.watcher'
""" Specification of an isolate watcher service """

SERVICE_WATCHER_LISTENER = 'cohorte.forker.watcher.listener'
""" Specification of a watcher service listener """

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

SIGNAL_KILL_ISOLATE = "{0}/kill".format(__SIGNALS_FORKER_PREFIX)
"""
Request to the forker to stop or kill the given isolate
- sent by the monitor, to the forker associated to the isolate
- contains the UID of the isolate to stop.
"""

SIGNAL_STOP_FORKER = "{0}/stop".format(__SIGNALS_FORKER_PREFIX)
"""
A monitor tells a forker to stop itself
- sent by the monitor, to one or more forkers
- no content
"""

SIGNAL_FORKER_STOPPING = "{0}/stopping".format(__SIGNALS_FORKER_PREFIX)
"""
A forker indicates that it will stop soon
- sent by the stopping forker, to all
- contains the UID of the forker, its node and the list of its isolates (UIDs)
"""

SIGNAL_FORKER_LOST = "{0}/lost".format(__SIGNALS_FORKER_PREFIX)
"""
A monitor indicates that it lost contact with a forker
- sent by the monitor, to all
- contains the UID of the forker, its node and the list of its isolates (UIDs)
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
