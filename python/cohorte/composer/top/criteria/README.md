# Cohorte Top Composer criteria

This package and its sub-packages define the criteria which are called to decide on which node a component must be hosted. Two criteria exist:

* ``configuration`` follows the explicit indications given in the composition file, *via* the ``node`` and ``isolate`` entries.
* ``language`` does nothing, but could be use to select a node according to the versions of Python and Java installed on it, to avoid trying to run a Java component on a node where no Java Virtual Machine is available.

## API

A *Top Composer* criterion is a component providing a service that follows the ``cohorte.composer.SERVICE_TOP_CRITERION_DISTANCE`` specification. It must implement the following method:

* ``group(components: list, groups:dict) -> (dict,list)``: tries to group components according to implementation-specific details. The first argument contains the list of components to group while the second one is the dictionary of the current distribution. The method returns the dictionary of its new distribution (or the current one) and the list of components it ignored and that are still to be grouped.
  The groups defined by criteria will then be associated to a node in the *Top Composer*

### Skeleton

```python
# Composer
import cohorte.composer

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

@ComponentFactory()     # No need for factory: auto-instantiation
@Provides(cohorte.composer.SERVICE_TOP_CRITERION_DISTANCE)
@Instantiate('sample-top-criterion')
class SampleTopCriterion(object):
    """
    Sample criterion implementation
    """
    def group(self, components, groups):
        """
        Groups components according to their implementation language

        :param components: List of components to group
        :param groups: Dictionary of current groups
        :return: A tuple:

                 * Dictionary of grouped components (group -> components)
                 * List of components that haven't been grouped
        """
        return groups, components
```