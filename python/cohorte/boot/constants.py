#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE Python bootstrap constants

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

# Boot module version
__version_info__ = (1, 1, 0)
__version__ = ".".join(str(x) for x in __version_info__)

# ------------------------------------------------------------------------------

STATE_FAILED = -1
""" Boot failed """

STATE_NONEXISTENT = 0
""" Isolate has been freshly registered in the state directory """

STATE_BOOTING = 1
""" Isolate is in booting stage (boot.py) """

STATE_LOADING = 2
""" Isolate is loading its bundles """

STATE_LOADED = 3
""" Isolate boot completed """
