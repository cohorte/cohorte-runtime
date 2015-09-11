# Isolate Composer Agent

This package contains the implementation of the agents of the *Isolate Composer*. Currently, two agents have been implemented for iPOPO (Python) and iPOJO (Java). The source code of the iPOJO agent is in the Java part of this project.

## API

An agent for the Python version of the *Isolate Composer* must provide a service with the ``cohorte.composer.SERVICE_AGENT_ISOLATE`` specification. It implements the following methods, which shouldn't be used directly by the components of the application.

* ``handle(components: [RawComponent]) -> [RawComponent]``: converts the given set of components (list of ``RawComponent`` objects) to a description usable by the underlying component model and tries to instantiate them. It returns the list of the components that have been instantiated.
* ``kill(name: str)``: kills and clears the instance of component matching the given name.

The other methods are used by Cohorte Composer internal components.

### Skeleton

```python
# Composer
import cohorte.composer

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

@ComponentFactory()     # No need for factory: auto-instantiation
@Provides(cohorte.composer.SERVICE_AGENT_ISOLATE)
@Instantiate('sample-isolate-agent')
class SampleAgent(object):
    """
    Sample isolate agent
    """
    def handle(self, components):
        """
        Tries to instantiate the given components immediately and stores the
        remaining ones to instantiate them as soon as possible

        :param components: A set of RawComponent beans
        :return: The immediately instantiated components
        """
        return set()
    
    def kill(self, name):
        """
        Kills the component with the given name

        :param name: Name of the component to kill
        :raise KeyError: Unknown component
        """
        raise KeyError(name)
```
