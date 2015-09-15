# Forker

This package contains the implementation of the *Forker* which starts, stops and monitors isolates at process level.

The *forker* relies on *starters* to start and stop isolates according to their kind. This allows to stop an isolate gracefully, according to its capabilities. For example, the ``cohorte_boot`` starter sends an Herald message to tell the isolate to stop by itself, and kills it if it doesn't stop fast enough.
The (un)expected stop of isolates is monitored by the *watcher* component. It notifies the *forker*, the *monitor*, and the *node composer* that an isolate disappeared.

## Public API

The *Forker* provides a service implementing the ``cohorte.SERVICE_FORKER`` specification. The following methods can be used from any component.

* ``start_isolate(isolate_config: dict) -> int``: starts the isolate using the given configuration. The forker uses a *starter* to handle the isolate (see the ``cohorte.forker.starters`` package). The result code can be:

    * 0: ``SUCCESS``, the isolate has been successfully started
    * 1: ``ALREADY_RUNNING``, the isolate is already running
    * 2: ``RUNNER_EXCEPTION``, an error occurred starting the isolate process
    * 3: ``INVALID_PARAMETER``, a parameter is missing or invalid
* ``stop_isolate(uid: str)``: stops or terminates the isolate with the given UID. The stopping process is handled by the *starter* which started the isolate;
* ``ping(uid) -> int``: checks the state of the given isolate. The result code can be:

    * 0: ``ALIVE``, the isolate is running
    * 1: ``DEAD``, the isolate is not running (or unknown)
    * 2: ``STUCK``, the isolate is running but doesn't answer to the ping request. *(not yet implemented)*
* ``is_alive() -> bool``: checks if the forker is usable, *i.e.* if it can start new isolates (if the platform is still running).

The other methods are used by the *forker* and the *monitor*.
