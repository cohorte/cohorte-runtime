#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Instrumentation UI

:author: Thomas Calmant
:copyright: Copyright 2014, isandlaTech
:license: GPLv3
:version: 1.0.0

..

    This file is part of Cohorte.

    Cohorte is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cohorte is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cohorte. If not, see <http://www.gnu.org/licenses/>.
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

from cohorte.instruments.common import CommonHttp

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
