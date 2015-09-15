# cohorte.vote

This package contains the implementation of the vote system used by the *Node Composer* to compute the distribution of components.
The model of the vote system is defined in the ``cohorte.vote.beans`` module.

## Vote Core

The vote core component is the entry point to start a vote. It is able to handle multiple vote engines and a vote store.

It provides the following methods:

* ``get_kinds() -> [str]``: lists the kinds of votes that can be used (one kind per vote engine);
* ``vote(electors, candidates, subject=None, name=None, kind=None, parameters=None) -> <kind-dependent result>``: runs a vote of the given kind with the given *candidates*. The *electors* are called each one at a time, in the given order. The *subject* of a vote is optional; it is given to the electors to indicate what they voting for. The *kind* of vote indicates which vote engine to choose. Finally, *parameters* is a dictionary containing the configuration of the vote engine (vote engine-dependent).

The electors are given a ``Ballot`` object, on which indicate the candidate(s) they vote for or against. The order of the vote in kept, as it can be taken into account by the vote engine.

## Vote Store

The vote store is called by the vote core component to keep track of the results of all votes.
The default implementation, ``cohorte.vote.dummy_store``, does nothing. The implementation from ``cohorte.vote.servlet`` stores the votes and uses ``cohorte.vote.cartoonist`` to draw the results charts in an HTML page.

### API

A vote store must provide the ``cohorte.vote.SERVICE_VOTE_STORE`` service and implement the following method:

* ``store_vote(vote: Vote)``: stores the latest vote handled by vote core.

### Skeleton

```python
import cohorte.vote
from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate


@ComponentFactory()
@Provides(cohorte.vote.SERVICE_VOTE_STORE)
@Instantiate('sample-vote-store')
class SampleVoteStore(object):
    """
    Sample vote store
    """
    def __init__(self):
        """
        Sets up members
        """
        self.__votes = []
    
    def store_vote(self, vote):
        """
        Stores the latest vote
        """
        self.__votes.append(vote)
```

## Vote Engine

A vote engine computes the result of a vote according to its own rules.

There are three engines implementations:

* ``alternative``: an implementation of the alternative vote (instant run-off vote). It takes the order of the vote on each ballot into account;
* ``approbation``: an approbation vote. It can handle negative (against) votes and the exclusion of a candidate if it has too many negative votes. Each elector is given a maximum number of votes;
* ``presidentielle``: an implementation of the French presidential election (absolute majority, in two turns).

### API

A vote engine is implemented as a component providing the ``cohorte.vote.SERVICE_VOTE_ENGINE`` service. It must set the ``cohorte.vote.PROP_VOTE_KIND`` to the kind of vote it implements. The component must provide the following methods:

* ``get_kind() -> str``: returns the kind of vote this component implements;
* ``get_options() -> dict``: returns the parameters this engine accepts and their description;
* ``analyze(vote_round, ballots, candidates, parameters, vote_bean) -> the elected candidate or a list of``: Analyzes the results of a round in a vote. *vote_round* indicates the round number, *ballots* contains all the ballots of this round, *candidates* contains the list of candidates of the vote. The *parameters* argument is the one given to ``VoteCore.vote()``. The *vote_bean* argument must be updated by calling its ``set_results(results)`` method, by indicating for each candidates how many votes it received.
  If a vote requires a new round, the ``beans.NextRound(candidates)`` exception must be raised, with the list of candidates kept in the next round.

### Skeleton

```python
import cohorte.vote

from pelix.ipopo.decorators import ComponentFactory, Provides, Instantiate, \
    Property

@ComponentFactory()
@Provides(cohorte.vote.SERVICE_VOTE_ENGINE)
@Property('_kind', cohorte.vote.PROP_VOTE_KIND, 'sample')
@Instantiate('vote-engine-sample')
class SampleEngine(object):
    """
    Sample vote engine
    """
    def __init__(self):
        """
        Sets up members
        """
        # Supported kind of vote
        self._kind = None

    def get_kind(self):
        """
        Returns supported kind of vote
        """
        return self._kind

    def get_options(self):
        """
        Returns the options available for this engine

        :return: An option -> description dictionary
        """
        return {}

    def analyze(self, vote_round, ballots, candidates, parameters, vote_bean):
        """
        Analyzes the results of a vote

        :param vote_round: Round number (starts at 1)
        :param ballots: All ballots of the vote
        :param candidates: List of all candidates
        :param parameters: Parameters for the vote engine
        :param vote_bean: A VoteResults bean
        :return: The candidate(s) with the most votes
        """
        # Compute the number of votes for each candidate and update the Vote bean
        results = {}
        vote_bean.set_results(results)
        
        # Compute the winner(s)...
        return winner_s_
```