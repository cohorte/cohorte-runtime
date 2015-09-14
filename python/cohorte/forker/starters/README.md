# cohorte.forker.starters

This package contains the isolates starters. They are used by the forker core component to start and stop isolates according to their kind.

## Existing starters

* ``cohorte_boot``: Starts a Cohorte isolate using the ``cohorte.boot.boot`` script, after having prepared its arguments. The isolate is stopped using an Herald message, and terminated if it doesn't stop fast enough.
* ``exe``: Starts any executable as a black box.

## API

The loader must provide a ``cohorte.forker.SERVICE_STARTER`` service, with the ``cohorte.forker.PROP_STARTER_KINDS`` property set to kind of isolate it can start. It must implement the following methods:

* ``start(configuration: dict, state_updater_url: str) -> bool``: starts the isolate with the given configuration. The configuration contains the UID of the isolate. The state updater URL can be given to the isolate to let it indicate its current loading state;
* ``stop(uid: str)``: stops the isolate with the given UID. If the isolate doesn't stop fast enough, it must be terminated. A ``KeyError`` is raised if the isolate is unknown;
* ``terminate(uid: str)``: terminates the isolate with the given UID (forced stop)
* ``ping(uid: str) -> bool``: checks if the isolate is still active.

A utility class ``CommonStarter``, defined in ``cohorte.forker.starers.common``, can be used as a parent class for starters. It provides some useful methods to handle the most common parts of the configuration of an isolate:

* ``setup_environment(configuration: dict) -> dict``: sets up an environment dictionary according to the *environment* entry in the configuration.
* ``normalize_environment(environment: dict) -> dict``: ensures that the environment dictionary only contains strings;
* ``prepare_working_directory(configuration: dict) -> str``: computes the path of the working directory of the isolate, either a new directory in ``$COHORTE_BASE`` or the directory given in the configuration (*working_directory*). It also ensures that this directory exists.
* ``uids() -> [str]``: returns the UIDs of the isolates this starter started.

This class also implements basic implementations of ``stop()``, ``terminate()`` and ``ping()``. 

### Starter Skeleton

```python
from pelix.ipopo.decorators import ComponentFactory, Property, Provides, \
    Instantiate
import cohorte.forker
import cohorte.forker.starters.common as common
import subprocess

@ComponentFactory()
@Provides(cohorte.forker.SERVICE_STARTER)
@Property('_kinds', cohorte.forker.PROP_STARTER_KINDS, 'sample')
@Instantiate('cohorte-starter-sample')
class SampleStarter(common.CommonStarter):
    """
    Sample starter
    """
    def start(self, configuration, state_udpater_url):
        """
        Starts an isolate with the given configuration and its monitoring
        threads

        :param configuration: An isolate configuration
        :return: True if the forker must wait for the state updater, else False
        :raise KeyError: A mandatory configuration option is missing
        :raise OSError: Error starting the isolate
        """
        uid = configuration['uid']
        name = configuration.get('name', '<no-name>')

        # Prepare environment variables
        environment = self.setup_environment(configuration)

        # Prepare arguments
        arguments = self._prepare_arguments(configuration)

        # Prepare working directory
        working_directory = self.prepare_working_directory(configuration)
        
        # Check I/O watch thread flag
        io_watch = configuration.get('io_watch', True)
        
        
        # Start the isolate
        process = subprocess.Popen(...)
        
        # Store the isolate process information
        self._isolates[uid] = process
        
        # Start watching after the isolate
        self._watcher.watch(uid, name, process, io_watch)
        
        # Return True if the isolate can tell it's ready using the state updater
        # else, return False
        return True
```