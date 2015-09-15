#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Debug utility package

Provides modules that may help debugging a Python isolate

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

SERVICE_DEBUG = "java:/org.cohorte.isolates.debug.IDebug"
""" Debug Agent Service """

# ------------------------------------------------------------------------------

_SUBJECT_PREFIX = "cohorte/debug/agent"
""" Common prefix to cohorte agent  """

_SUBJECT_MATCH_ALL = "{0}/*".format(_SUBJECT_PREFIX)
""" Filter to match agent signals """

SUBJECT_GET_ISOLATE_DETAIL = "{0}/get_isolate_detail".format(_SUBJECT_PREFIX)
""" Signal to request the detail of the local isolate """

SUBJECT_GET_BUNDLES = "{0}/get_bundles".format(_SUBJECT_PREFIX)
""" Signal to request the bundles of the isolate """

SUBJECT_GET_BUNDLE_DETAIL = "{0}/get_bundle_detail".format(_SUBJECT_PREFIX)
""" Signal to request the bundle details """

SUBJECT_GET_FACTORIES = "{0}/get_factories".format(_SUBJECT_PREFIX)
""" Signal to request the component factories of the isolate """

SUBJECT_GET_FACTORY_DETAIL = "{0}/get_factory_detail".format(_SUBJECT_PREFIX)
""" Signal to request the component factory details """

SUBJECT_GET_INSTANCES = "{0}/get_instances".format(_SUBJECT_PREFIX)
""" Signal to request the instances of the isolate """

SUBJECT_GET_INSTANCE_DETAIL = "{0}/get_instance_detail".format(_SUBJECT_PREFIX)
""" Signal to request the detail of one instance """

SUBJECT_GET_SERVICES = "{0}/get_services".format(_SUBJECT_PREFIX)
""" Signal to request the services of the isolate """

SUBJECT_GET_THREADS = "{0}/get_threads".format(_SUBJECT_PREFIX)
""" Signal to request the current threads of the isolate """

SUBJECT_GET_ISOLATE_LOGS = "{0}/get_isolate_logs".format(_SUBJECT_PREFIX)
""" Signal to request the list of isolate logs """

SUBJECT_GET_ISOLATE_LOG = "{0}/get_isolate_log".format(_SUBJECT_PREFIX)
""" Signal to request the request logs """
