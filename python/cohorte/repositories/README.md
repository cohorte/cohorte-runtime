# cohorte.repositories

This package contains the components handling repositories of artifacts and component factories. They are used by the monitor to check which artifact provides a component factory and to check if its dependencies are available.

There are two kinds of repositories: the artifacts repositories handle the artifacts (bundles) and their inter-dependencies; the factories repositories associates factory names with the artifacts providing them.
The model to use in the implementations of repositories is defined in ``cohorte.repositories.beans``.

## Existing repositories

### Artifacts repositories

* ``python.modules``: parses Python modules and keeps track of their dependencies (``import``, ...);
* ``java.bundles``: parses the manifest of OSGi bundles and keeps track of their dependencies (``Import-Package``, ...) and of their capabilities (``Export-Package``, ...).


### Factories repositories

* ``python.ipopo``: parses the modules found by ``python.modules`` and extracts the name of the iPOPO factories they define;
* ``java.ipojo``: parses the manifest of the OSGi bundles found by ``java.bundles`` and extracts the name of the iPOJO factories they define.

## API

### Artifacts repository

A repository of artifacts is a component providing the ``cohorte.repositories.SERVICE_REPOSITORY_ARTIFACTS`` service. It must indicate the language of implementation of its artifacts in the ``cohorte.repositories.PROP_REPOSITORY_LANGUAGE`` service property. It must implement the following methods:

* ``add_directory(dirname)``: adds a directory to the repository. The component must analyze all the artifacts it can find in the given folder and its sub-folders.
* ``clear()``: cleans up all the content of the repository (cache, ...);
* ``get_artifact(name=None, version=None, filename=None, registry=None) -> Artifact``: returns the first artifact matching the given parameters;
* ``resolve_installation(artifacts, system_artifacts=None)``: Returns all the artifacts that must be installed in order to have the given artifacts resolved. The *system_artifacts* parameter can contains modules considered as available even if they are not in the repository (ciphered artifacts, ...);
* ``get_language()``: returns the language of implementation of the artifacts handled by this repository;
* ``walk()``: a generator to walk through the known artifacts.

### Factories repository

A repository of factories is a component providing the ``cohorte.repositories.SERVICE_REPOSITORY_FACTORIES`` service. It must indicate the language of implementation of its factories in the ``cohorte.repositories.PROP_REPOSITORY_LANGUAGE`` service property and the component model it handles in the ``cohorte.repositories.PROP_FACTORY_MODEL`` service property. Generally, it depends on a repository of artifacts and uses its ``walk()`` method to analyze of its artifacts. 
The implementation must provide the following methods:

* ``load_repositories()``: looks for the factories in all the repositories of artifacts the component depends on. This method must be called by the component itself when it is validated and should be called when a repository of artifacts is updated;
* ``add_artifact(artifact: Artifact)``: extracts the component factories from the given artifact;
* ``clear()``: cleans up all the content of the repository (cache, ...);
* ``find_factory(factory: str, artifact_name=None, artifact_version=None) -> [Artifact]``: finds the artifacts which provide the given factory. The artifacts can be filtered by name and version;
* ``find_factories(factories: [str]) -> ({Name -> [Artifacts]}, [Not found factories])``: returns the list of artifacts which provide the given factories. The result associates each factory name to the artifacts providing it, and the list of factories which haven't been found;
* ``get_language()``: returns the language of implementation of the factories handled by this repository;
* ``get_model()``: returns the component model of the factories handled by this repository.

## Skeletons

### Artifacts repository

```python
import threading

import cohorte.repositories
from cohorte.repositories.beans import Artifact
from pelix.ipopo.decorators import ComponentFactory, Provides, Invalidate, \
    Property, Requires, Validate


@ComponentFactory("sample-artifacts-repository-factory")
@Provides(cohorte.repositories.SERVICE_REPOSITORY_ARTIFACTS)
@Property('_language', cohorte.repositories.PROP_REPOSITORY_LANGUAGE, "sample-language")
class SampleArtifactsRepository(object):
    """
    Sample artifacts repository
    """
    def add_directory(self, dirname):
        """
        Recursively adds all artifacts found in the given directory into the
        repository

        :param dirname: A path to a directory
        """
        pass
    
    def clear(self):
        """
        Clears the content of the repository
        """
        pass
        
    def get_artifact(self, name=None, version=None, filename=None,
                     registry=None):
        """
        Retrieves an artifact from the repository

        :param name: The module name (mutually exclusive with filename)
        :param version: The module version (None or '0.0.0' for any), ignored
                        if filename is used
        :param filename: The module file name (mutually exclusive with name)
        :param registry: Registry where to look for the module
        :return: The first matching module
        :raise ValueError: If the module can't be found
        """

    def resolve_installation(self, artifacts, system_artifacts=None):
        """
        Returns all the artifacts that must be installed in order to have the
        given modules resolved.

        :param artifacts: A list of artifacts
        :param system_artifacts: Artifacts considered as available
        :return: A tuple: (artifacts, dependencies, missing artifacts, [])
        """
        pass
    
    def walk(self):
        """
        # Walk through the known artifacts
        """
        for artifact in ...:
            yield artifact
    
    def get_language(self):
        """
        Retrieves the language of the artifacts stored in this repository
        """
        return self._language

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # The content of the repository must be loaded here

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.clear()
```

### Factories repository

This skeleton provides more code because the logic behind most of the methods is always the same and it can rely directly on the types provided by ``cohorte.repositories.beans``.

```python
import threading

import cohorte.repositories
from pelix.ipopo.decorators import ComponentFactory, Provides, Invalidate, \
    Property, Requires, Validate


@ComponentFactory("sample-factories-repository-factory")
@Provides(cohorte.repositories.SERVICE_REPOSITORY_FACTORIES, controller="_controller")
@Requires('_repositories', cohorte.repositories.SERVICE_REPOSITORY_ARTIFACTS,
          True, False, "({0}=sample-language)".format(cohorte.repositories.PROP_REPOSITORY_LANGUAGE))
@Property('_model', cohorte.repositories.PROP_FACTORY_MODEL, "sample-model")
@Property('_language', cohorte.repositories.PROP_REPOSITORY_LANGUAGE, "sample-language")
class SampleFactoriesRepository(object):
    """
    Sample factories repository
    """
    def __init__(self):
        """
        Sets up the repository
        """
        # Properties
        self._model = 'sample-model'
        self._language = 'sample-language'

        # Service controller
        self._controller = False

        # Injected service
        self._repositories = []
        
        # Name -> [Factories]
        self._factories = {}

        # Artifact -> [Factories]
        self._artifacts = {}

        # Thread safety
        self.__lock = threading.RLock()
        
    def add_artifact(self, artifact):
        """
        Adds the factories provided by the given artifact
        """
        # Fill the repository with the parsed factories
        pass
    
    def clear(self):
        """
        Cleans up the content of the repository
        """
        with self.__lock:
            self._artifacts.clear()
            self._factories.clear()
    
    def find_factories(self, factories):
        """
        Returns the list of artifacts that provides the given factories

        :param factories: A list of factory names
        :return: A tuple ({Name -> [Artifacts]}, [Not found factories])
        """
        with self.__lock:
            factories_set = set(factories)
            resolution = {}
            unresolved = set()

            if not factories:
                # Nothing to do...
                return resolution, factories_set

            for name in factories_set:
                try:
                    # Get the list of factories for this name
                    factories = self._factories[name]
                    providers = resolution.setdefault(name, [])
                    providers.extend(factory.artifact for factory in factories)
                except KeyError:
                    # Factory name not found
                    unresolved.add(name)

            # Sort the artifacts
            for artifacts in resolution.values():
                artifacts.sort(reverse=True)

            return resolution, unresolved

    def find_factory(self, factory, artifact_name=None, artifact_version=None):
        """
        Find the artifacts that provides the given factory, filtered by name
        and version.

        :return: The list of artifacts providing the factory, sorted by name
                 and version
        :raise KeyError: Unknown factory
        """
        with self.__lock:
            # Copy the list of artifacts for this factory
            artifacts = [factory.artifact
                         for factory in self._factories[factory]]
            if artifact_name is not None:
                # Artifact must be selected
                # Prepare the version bean
                version = cohorte.repositories.beans.Version(artifact_version)

                # Filter results
                artifacts = [artifact for artifact in artifacts
                             if artifact.name == artifact_name
                             and version.matches(artifact.version)]

                if not artifacts:
                    # No match found
                    raise KeyError("No matching artifact for {0} -> {1} {2}"
                                   .format(factory, artifact_name, version))

            # Sort results
            artifacts.sort(reverse=True)
            return artifacts
    
    def get_language(self):
        """
        Returns the language of the artifacts stored in this repository
        """
        return self._language

    def get_model(self):
        """
        Returns the component model that can handle the factories of this
        repository
        """
        return self._model
    
    def load_repositories(self):
        """
        Loads the factories according to the repositories
        """
        with self.__lock:
            if not self._repositories:
                # No repository
                return

            # Walk through artifacts
            for repository in self._repositories:
                for artifact in repository.walk():
                    self.add_artifact(artifact)


    def __initial_loading(self):
        """
        Initial repository loading
        """
        self.load_repositories()
        
        # Provide the service only once the repository has been filled
        self._controller = True

    @Validate
    def validate(self, context):
        """
        Component validated
        """
        self._controller = False

        # Load repositories in another thread (let iPOPO continue its work)
        threading.Thread(target=self.__initial_loading,
                         name="sample-factory-repository-loader").start()

    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        self.clear()
```
