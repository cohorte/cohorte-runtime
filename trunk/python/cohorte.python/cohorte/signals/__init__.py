#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Signals package

Provides the implementation of COHORTE Signals services

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

SERVICE_ISOLATE_PRESENCE_LISTENER = "cohorte.signals.isolate.presence.listener"
"""
Isolate presence listener specification. Notified by the Signals Directory
when a new isolate is detected or when a known one is gone.

* handle_isolate_presence(uid, node, event): notifies the listener of an
  event (PRESENCE_REGISTERED or PRESENCE_UNREGISTERED) about the isolate with
  given UID, on the given node
"""

ISOLATE_REGISTERED = 0
""" Isolate registration event """

ISOLATE_UNREGISTERED = 1
""" Isolate disappearing event """

# ------------------------------------------------------------------------------

GROUP_ALL = "ALL"
""" All isolates, including the current one """

GROUP_FORKERS = "FORKERS"
""" All forkers, including the current isolate if it is a forker """

GROUP_ISOLATES = "ISOLATES"
"""
All isolates, including monitors and the current one, excluding forkers.
If the current isolate is a forker, it is excluded.
"""

GROUP_CURRENT = "CURRENT"
""" Current isolate """

GROUP_MONITORS = "MONITORS"
""" All monitors, including the current isolate if it is a monitor """

GROUP_NEIGHBOURS = "NEIGHBOURS"
""" All isolates on the current node, excluding the current one """

GROUP_OTHERS = "OTHERS"
""" All isolates, with monitors and forkers, but this one """

# ------------------------------------------------------------------------------

__SIGNAL_PREFIX = "/cohorte/signals/directory-updater"
""" Prefix of all directory updater signals """

SIGNAL_PATTERN = "{0}/*".format(__SIGNAL_PREFIX)
""" Pattern to match all directory updater signals """

SIGNAL_DUMP = "{0}/dump".format(__SIGNAL_PREFIX)
""" Directory dump request """

SIGNAL_REGISTER = "{0}/register".format(__SIGNAL_PREFIX)
""" Isolate registration notification """

SIGNAL_REGISTER_SYNACK = "{0}/syn-ack".format(SIGNAL_REGISTER)
""" Isolate registration acknowledgment synchronization """

SIGNAL_REGISTER_ACK = "{0}/ack".format(SIGNAL_REGISTER)
""" Isolate registration acknowledgment """

SIGNAL_CONTACT = "{0}/contact".format(__SIGNAL_PREFIX)
""" Special case for early starting forkers: a monitor signals its dump port """

# ------------------------------------------------------------------------------
