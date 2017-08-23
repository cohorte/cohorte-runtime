#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Python package

Contains the lowest level of a COHORTE application

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

ENV_NAME = 'COHORTE_ISOLATE_NAME'
""" COHORTE Isolate name environment variable """

ENV_UID = 'COHORTE_ISOLATE_UID'
""" COHORTE Isolate UID environment variable """

ENV_NODE_NAME = 'COHORTE_NODE_NAME'
""" COHORTE Node name environment variable """

ENV_NODE_UID = 'COHORTE_NODE_UID'
""" COHORTE Node UID environment variable """

# ------------------------------------------------------------------------------
# Framework properties

PROP_DEBUG = 'cohorte.debug'
""" COHORTE logging debug mode """

PROP_VERBOSE = 'cohorte.verbose'
""" COHORTE logging verbose mode """

PROP_COLORED = 'cohorte.log.color'
""" COHORTE logging color mode """


PROP_ENV_STARTER = 'cohorte.forker.starter.environment'
""" environment parmeter for isolate starter """

PROP_CONFIG_BROKER = 'cohorte.configuration.broker.url'
""" URL to the configuration broker """

PROP_UID = 'cohorte.isolate.uid'
""" Unique identifier of the isolate """

PROP_NAME = 'cohorte.isolate.name'
""" Isolate name (can appear multiple time in an application) """

PROP_KIND = 'cohorte.isolate.kind'
""" Kind of the loader which started this isolate """

PROP_HOME = 'cohorte.home'
""" COHORTE Home directory framework property """

PROP_BASE = 'cohorte.base'
""" COHORTE Base directory framework property """

PROP_NODE_UID = 'cohorte.node.uid'
""" COHORTE Node UID framework property """

PROP_NODE_NAME = 'cohorte.node.name'
""" COHORTE Node name framework property """

PROP_NODE_DATA_DIR = 'cohorte.node.data.dir'
""" COHORTE Node Data directory path framework property """

PROP_RUN_TOP_COMPOSER = 'cohorte.composer.top.run'
""" If present, the monitor must load the top composer """

PROP_SHELL_CONSOLE = 'cohorte.shell.console'
""" If present, the isolate must start the shell text UI """

PROP_STATE_UPDATER = 'cohorte.state.updater.url'
""" URL to the state updater, use during boot only """

PROP_FORKER_HTTP_PORT = 'cohorte.forker.http.port'
""" HTTP port of the forker """

PROP_DUMPER_PORT = 'psem2m.directory.dumper.port'
"""
Port to the signal directory dumper

TODO: Change value ('cohortify'), put a complete URL instead of a port
"""

# ------------------------------------------------------------------------------
# Isolate naming constants

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

SERVICE_FILE_INCLUDER = 'cohorte.file.includer'
""" Specification provided by a file includer """

SERVICE_FILE_FINDER = 'cohorte.file.finder'
""" Specification provided by a file finder """

SERVICE_FILE_READER = 'cohorte.file.reader'
"""
Specification provided by a file reader, which parses a configuration file,
resolves its imports, but doesn't extract information from it

* ``load_boot(kind)``: Loads the boot configuration for the given isolate kind
* ``load_boot_dict(configuration)``: Parses the boot-like part of the given
  configuration dictionary
"""

# ------------------------------------------------------------------------------

SERVICE_CONFIGURATION_READER = 'cohorte.configuration.reader'
""" Specification provided by a configuration parser """

SERVICE_CONFIGURATION_BROKER = 'cohorte.configuration.broker'
""" Specification provided by a configuration broker (server side) """

# ------------------------------------------------------------------------------

SERVICE_LOOPER = 'cohorte.boot.looper'
"""
Specification of a main thread loop handler.

* ``stop()``: Exits the main thread loop
* ``run(method, *args, **kwargs)``: Runs the given method on the main thread
  and returns its result.
"""

SERVICE_ISOLATE_LOADER = 'cohorte.boot.loader'
"""
Specification provided by an isolate Loader.

* ``load(configuration)``: Loads the isolate according to the given
  configuration dictionary (optional), to framework properties and to process
  environment variables.
* ``wait()``: Blocks until the isolate has stopped
"""

SVCPROP_ISOLATE_LOADER_KIND = 'loader.kind'
""" Kinds of isolate handled by this loader """

# ------------------------------------------------------------------------------

SERVICE_FORKER = 'cohorte.forker'
""" Represents a forker service """

SERVICE_NODE_STARTER = 'cohorte.node_starter'
""" Specification of the node starter """

# ------------------------------------------------------------------------------

SERVICE_JAVA_RUNNER = 'cohorte.java.runner'
"""
Loads a Java Virtual Machine (JVM) in the current process. Only one JVM can be
loaded in a process.
"""
