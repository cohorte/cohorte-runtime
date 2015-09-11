# cohorte.boot.loaders

This package contains the boot loaders, which install and start the bundles required to boot an isolate and to let it be manageable by the monitor isolate.

## Existing loaders

* ``forker``: This loader is used by default by the bootstrap script to start the monitor isolate;
* ``broker``: Pseudo-loader, which gets the isolate configuration from the monitor isolate *via* its broker servlet. This loader then calls the *real* loader, to continue the loading process;
* ``osgi_inner``: Loads a Java isolate. It starts a Java Virtual Machine inside the Python interpreter using [``jpype1-py3``](https://pypi.python.org/pypi/JPype1-py3) and starts an OSGi framework inside it;
* ``pelix_inner``: Continues the loading of the isolate in the current Pelix framework.

## API

The module providing a loader component must define the name of the component factory in the ``ISOLATE_LOADER_FACTORY`` module-level constant.

The loader must be implemented as a component factory, which name must be the value of ``ISOLATE_LOADER_FACTORY``. The component must provide a ``cohorte.SERVICE_ISOLATE_LOADER`` service, with the ``cohorte.SVCPROP_ISOLATE_LOADER_KIND`` property set to kind of isolate it can load.

* ``prepare_state_updater(url)``: sets the URL to the state updater servlet;
* ``update_state(new_state, extra=None)``: notifies the state updater servlet that the isolate loading reached a new state;
* ``load(configuration)``: loads the isolate. Installs and starts bundles, instantiates components, *etc.* according to the given configuration dictionary;
* ``wait()``: waits for the isolate content to stop (OSGi framework, ...).

### Loader Skeleton

```python
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Provides, Property
import cohorte

ISOLATE_LOADER_FACTORY = 'sample-loader-factory'
""" Loader factory name """

LOADER_KIND = 'sample'
""" Kind of isolate started with this loader """

@ComponentFactory(ISOLATE_LOADER_FACTORY)
@Provides(cohorte.SERVICE_ISOLATE_LOADER)
@Property('_handled_kind', cohorte.SVCPROP_ISOLATE_LOADER_KIND, LOADER_KIND)
class SampleLoader(object):
    """
    Sample isolate loader
    """
    def prepare_state_updater(self, url):
        """
        Prepares the access to the state updater servlet
        """
        pass

    def update_state(self, new_state, extra=None):
        """
        Sends the new state to the state updater servlet
        """
        pass

    def load(self, configuration):
        """
        Installs and starts bundles, instantiates components, etc.
        """
        pass

    def wait(self):
        """
        Waits for the isolate content (OSGi framework, ...) to stop
        """
        pass
```
