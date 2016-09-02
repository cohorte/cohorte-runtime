#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Instrumentation web UI

:author: Thomas Calmant
:license: Apache Software License 2.0
:version: 1.1.0

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

from cohorte.instruments.common import CommonHttp

# ------------------------------------------------------------------------------

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

SERVICE_INSTRUMENT_UI = 'cohorte.instrument.ui'
"""
Specification of the UI of an instrument, providing:

* handle_request(path, subpath, request, response)
"""

PROP_INSTRUMENT_NAME = 'cohorte.instrument.name'
"""
Name of an instrument, used as servlet sub-path
"""
