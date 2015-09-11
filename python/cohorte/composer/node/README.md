# Node Composer

This package contains the implementation of the *Node Composer* and of its criteria. The distribution of components is done using a voting system. See ``cohorte.vote`` for more information.

## Public API

The *Node Composer* provides a service implementing the ``cohorte.composer.SERVICE_COMPOSER_NODE`` specification. It implements the following methods.

**Note:** Those methods are not intended to be used directly: instead, the *Top Composer* service should be used to manage sets of components as various compositions.
The other methods are used by Cohorte Composer internal components.

* ``instantiate(components: [RawComponent]) -> [str] or None``: manages (instantiates and keeps track of) the given components (a list of ``RawComponent`` objects).
  If some component factories are missing from the application repositories, the instantiation is aborted and the method returns the list of the names of the missing factories.
* ``kill(components: [RawComponent])  ``: kills the instances of the given components and clears the references to them.

