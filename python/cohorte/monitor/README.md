# Monitor

This package contains the implementation of the *Monitor* which handles isolates at model level.

**Note:** It is the *Monitor* which loads the *Top Composer* if the *-t* argument is given to the boot script. 

## Public API

The *Monitor* provides a service implementing the ``cohorte.monitor.SERVICE_MONITOR`` specification. The following methods can be used from any component.

* ``start_isolate(name, kind, level, sublevel, bundles=None, uid=None)``: starts a Cohorte isolate of the given level. ``kind`` represents the exact type of isolate to boot: *pelix*, *osgi*, *forker*, ... ``level`` indicates the level after boot: *boot*, *java*, *python*, ... and ``sublevel`` indicates the category of isolate: *monitor*, *isolate*, ...
* ``stop_isolate(uid: str) -> bool``: tells the *forker* to stop the isolate with the given UID. The method returns ``False`` if the UID is unknown;
* ``ping(uid) -> int``: checks the state of the given isolate (calls the ``ping(uid)`` method from the *forker*). The result code can be:

    * 0: ``ALIVE``, the isolate is running
    * 1: ``DEAD``, the isolate is not running (or unknown)
    * 2: ``STUCK``, the isolate is running but doesn't answer to the ping request. *(not yet implemented)*

The other methods are used by the *forker* and the *monitor*.
