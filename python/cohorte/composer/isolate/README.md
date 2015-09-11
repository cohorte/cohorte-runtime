# Isolate Composer

This package contains the implementation of the *Isolate Composer* and of its agents.

## Public API

The *Isolate Composer* provides a service implementing the ``cohorte.composer.SERVICE_COMPOSER_ISOLATE`` specification. It implements the following methods, which shouldn't be used directly by the components of the application.

* ``get_isolate_uid() -> uid``: returns the UID of the isolate providing this service;
* ``get_isolate_info() -> Isolate``: returns a description of the isolate providing this service as a ``cohorte.composer.beans.Isolate`` object. It contains the name of the isolate, its language (Python or Java) and the components it hosts;
* ``instantiate(components: [RawComponent])``: instantiates the given set of components (list of ``RawComponent`` objects) or keeps them in a waiting list if their factories are missing.
* ``kill(names: [str])``: kills and clears the instances of components matching the given components names.

The other methods are used by Cohorte Composer internal components.
