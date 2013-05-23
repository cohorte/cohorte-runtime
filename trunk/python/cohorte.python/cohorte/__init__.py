#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Python package

Contains the lowest level of a COHORTE application

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------
# Environment variables

ENV_JAVA_HOME = 'JAVA_HOME'
""" Java home directory environment variable """

ENV_DEBUG = 'COHORTE_DEBUG'
""" COHORTE Debug environment variable """

ENV_LOG_FILE = 'COHORTE_LOGFILE'
""" Path to the COHORTE log file """

ENV_HOME = 'COHORTE_HOME'
""" COHORTE Home directory environment variable """

ENV_BASE = 'COHORTE_BASE'
""" COHORTE Base directory environment variable """

ENV_NODE = 'COHORTE_NODE'
""" COHORTE Node name environment variable """

# ------------------------------------------------------------------------------
# Framework properties

PROP_DEBUG = 'cohorte.debug'
""" COHORTE logging debug mode """

PROP_VERBOSE = 'cohorte.verbose'
""" COHORTE logging verbose mode """

PROP_CONFIG_BROKER = 'cohorte.configuration.broker.url'
""" URL to the configuration broker """

PROP_UID = 'cohorte.isolate.uid'
""" Unique IDentifier of the isolate """

PROP_NAME = 'cohorte.isolate.name'
""" Isolate name (can appear multiple time in an application) """

PROP_KIND = 'cohorte.isolate.kind'
""" Kind of the loader which started this isolate """

PROP_HOME = 'cohorte.home'
""" COHORTE Home directory framework property """

PROP_BASE = 'cohorte.base'
""" COHORTE Base directory framework property """

PROP_NODE = 'cohorte.node'
""" COHORTE Node name framework property """

PROP_START_MONITOR = 'cohorte.forker.start_monitor'
""" If present, the forker must start a monitor """

PROP_STATE_UPDATER = 'cohorte.state.updater.url'
""" URL to the state updater, use during boot only """

PROP_DUMPER_PORT = 'psem2m.directory.dumper.port'
"""
Port to the signal directory dumper

TODO: Change value ('cohortify'), put a complete URL instead of a port
"""

# ------------------------------------------------------------------------------
# Isolate naming constants

MONITOR_NAME = 'cohorte.internals.monitor'
""" All monitors have the same name """

FORKER_NAME = 'cohorte.internals.forker'
""" All forkers have the same name """

# ------------------------------------------------------------------------------
# Bundle-level constants

BUNDLE_ISOLATE_LOADER_FACTORY = "ISOLATE_LOADER_FACTORY"
""" Name of the isolate loader component factory """

# ------------------------------------------------------------------------------
# Remote Services constants
SVCPROP_SYNONYM_INTERFACES = "cohorte.remote.synonyms"
""" Exported interfaces synonyms (array of strings) """

# ------------------------------------------------------------------------------
# Core services specifications

SERVICE_FILE_FINDER = 'cohorte.file.finder'
""" Specification provided by a file finder """

SERVICE_FILE_READER = 'cohorte.file.reader'
"""
Specification provided by a file reader, which parses a configuration file,
resolves its imports, but doesn't extract information from it

* load_boot(kind): Loads the boot configuration for the given isolate kind
* load_boot_dict(configuration): Parses the boot-like part of the given
  configuration dictionary
"""

# ------------------------------------------------------------------------------

SERVICE_CONFIGURATION_READER = 'cohorte.configuration.reader'
""" Specification provided by a configuration parser """

SERVICE_CONFIGURATION_BROKER = 'cohorte.configuration.broker'
""" Specification provided by a configuration broker (server side) """

# ------------------------------------------------------------------------------

SERVICE_ISOLATE_LOADER = 'cohorte.boot.loader'
"""
Specification provided by an isolate Loader.

* load(configuration): Loads the isolate according to the given
  configuration dictionary (optional), to framework properties and to process
  environment variables.
* wait(): Blocks until the isolate has stopped
"""

SVCPROP_ISOLATE_LOADER_KIND = 'loader.kind'
""" Kinds of isolate handled by this loader """

# ------------------------------------------------------------------------------

SERVICE_FORKER = 'cohorte.forker'
""" Represents a forker service """

# ------------------------------------------------------------------------------

SERVICE_JAVA_RUNNER = 'cohorte.java.runner'
"""
Loads a Java Virtual Machine (JVM) in the current process. Only one JVM can be
loaded in a process.
"""

# ------------------------------------------------------------------------------

SERVICE_SIGNALS_DIRECTORY = 'cohorte.signals.directory'
""" Specification provided by a signals directory """

SERVICE_SIGNALS_RECEIVER = 'cohorte.signals.receiver'
""" Specification provided by a signals receiver """

SERVICE_SIGNALS_SENDER = 'cohorte.signals.sender'
""" Specification provided by a signals sender """
