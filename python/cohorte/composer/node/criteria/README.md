# Cohorte Node Composer criteria

This package and its sub-packages define the criteria which are called to decide which isolate must host a component. Five criteria exist:

* ``configuration``: follows the explicit indications given in the composition file, *via* the ``isolate`` entry.
* ``compatibility``: keeps track of the components that were together while an isolate crashed and uses a compatibility rating to avoid them to be reinstantiated in the same isolate. This criterion has been replaced by ``history``.
* ``history``: keeps track of the components that were together while an isolate crashed and forbids to reuse a distribution of components that crashed.
* ``crashing``: associates a *reliability rating* to each component instance and decreases it when a component disappears due to the crash of its host isolate.
* ``timer``: compensates the ``crashing`` criterion by increasing the *reliability rating* of each component instance which didn't crashed after a given amount of time.

Those criteria are called by the *Node Composer Distributor* in order to vote for the isolate which should host a given component.

## API

A *Node Composer* criterion is a component providing a service that follows the ``cohorte.composer.SERVICE_NODE_CRITERION_DISTANCE`` or ``cohorte.composer.SERVICE_NODE_CRITERION_RELIABILITY``specification. It must implement the following methods:

* ``handle_event(event: Event)``: the criterion is notified by the *Node Composer* that an event concerning a set of components occurred. Those events can also be sent by a criterion, in order to trigger the update of the internal state of other criteria.
  The two main events are ``"isolate.lost"``, which indicates that the isolate hosting the set of components has been lost, and ``"timer"``, which notifies the criteria that nothing happened to the given components for some time.
* ``vote(candidates: [ElligibleIsolate], subject: RawComponent, ballot: cohorte.vote.Ballot)``: the criterion must indicate on its *ballot* the candidate(s) which could host the given component (vote subject). The list of candidates contains the list of active isolates, of the isolates that will be created after this distribution and a neutral isolate, to use when none other would fit.

``Event`` and ``ElligibleIsolate`` are defined in the ``cohorte.composer.node.beans`` module.

### Skeleton

```python
# Composer
import cohorte.composer

# iPOPO Decorators
from pelix.ipopo.decorators import ComponentFactory, Requires, Provides, \
    Instantiate

@ComponentFactory()     # No need for factory: auto-instantiation
@Provides(cohorte.composer.SERVICE_NODE_CRITERION_DISTANCE)
@Instantiate('sample-node-criterion')
class SampleNodeCriterion(object):
    """
    Sample criterion implementation
    """
    def handle_event(self, event):
        """
        Does nothing: this elector only cares about what is written in
        configuration files
        """
        # Get the implicated components
        components = set(component.name for component in event.components)
        # ...

    def vote(self, candidates, subject, ballot):
        """
        Votes for the isolate(s) with the minimal compatibility distance

        :param candidates: Isolates to vote for
        :param subject: The component to place
        :param ballot: The vote ballot
        """
        ballot.append_for(candidates[0])
        
        # Lock the ballot, to avoid modifying it afterwards
        ballot.lock()
```
