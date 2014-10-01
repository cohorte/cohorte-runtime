#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE isolate main thread handlers package

Provides the modules that will handle OS-specific main thread loops.

All modules must provide a method: get_looper(), which returns an instance
of the loader.

All loopers must provide the following methods:

* ``setup(argv=None)``: Sets up the handler
* ``loop()``: The blocking event queue loop
* ``stop()``: Exits the loop
* ``run(method, *args, **kwargs)``: Runs the given method on the main thread
  and returns its result.

The handler will be registered as a service in the Pelix framework.

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
