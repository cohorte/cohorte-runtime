# Top Composer

This package contains the implementation of the *Top Composer* and of its criteria.

## Public API

The *Top Composer* provides a service implementing the ``cohorte.composer.SERVICE_COMPOSER_TOP`` specification. The following methods can be used from any component.

* ``start(composition: RawComposition) -> str``: starts the given composition (a ``RawComposition`` object) and returns a UID to identify the composition instance.
  No reference to the given composition object is kept after a call to ``start``: the caller must use the returned UID to manage the active composition.
* ``stop(uid: str)``: stops and clears the active composition with the given UID. A ``KeyError`` exception is raised if the UID is unknown.

The other methods are used by Cohorte Composer internal components.
