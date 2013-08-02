#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
COHORTE isolate main thread handlers package

Provides the modules that will handle OS-specific main thread loops.

All modules must provide a method: get_looper(), which returns an instance
of the loader.

All loopers must provide the following methods:

* setup(argv=None): Sets up the handler
* loop(): The blocking event queue loop
* stop(): Exits the loop
* run(method, *args, **kwargs): Runs the given method on the main thread
  and returns its result.

The handler will be registered as a service in the Pelix framework.

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"
