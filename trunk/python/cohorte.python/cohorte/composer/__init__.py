#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Cohorte Composer module

Provides components to parse a JSON description of a composition and to
instantiate these components in iPOPO or in iPOJO in an embedded JVM.

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# ------------------------------------------------------------------------------

SERVICE_COMPOSITION_PARSER = 'cohorte.composer.parser'
""" Specification of a composition parser """

SERVICE_COMPOSITION_LOADER = 'cohorte.composer.loader'
""" Specification of a composition loader """

SERVICE_DISTRIBUTOR = 'cohorte.composer.distributor'
""" Specification of a distributor """

SERVICE_UPDATABLE_RATING = 'cohorte.composer.rating.updable'
""" Specification of a rating with an update(event) method """


SERVICE_COMPONENT_RATING = 'cohorte.composer.rating.storage'
""" Specification of a component rating storage """

SERVICE_COMPONENT_COMPATIBILITY = 'cohorte.composer.compatibility.storage'
""" Specification of a component compatibility storage """

SERVICE_COMPATIBILITY_CHECKER = 'cohorte.composer.compatibility.checker'
""" Specification of a component compatibility checker """

SERVICE_DISTANCE_CALCULATOR = 'cohorte.composer.distance.calculator'
""" Specification of a component distance calculator """

# ------------------------------------------------------------------------------

SIGNAL_AGENT_EVENT = "/cohorte/composer/agent/event"


__EVENTS_PREFIX = '/cohorte/composer'
""" Prefix common to all composer events """

__COMPONENT_EVENTS_PREFIX = '{0}/component'.format(__EVENTS_PREFIX)
""" Prefix common to all component events """

__FACTORY_EVENTS_PREFIX = '{0}/factory'.format(__EVENTS_PREFIX)
""" Prefix common to all factory events """

EVENTS_PATTERN = '{0}/*'.format(__EVENTS_PREFIX)
""" Pattern to match all composer events topics """

EVENT_COMPONENT_STATE = '{0}/state'.format(__COMPONENT_EVENTS_PREFIX)
"""
The state of the component changed:

* isolate: UID of the isolate hosting the component
* factory: Name of the component factory
* name: Name of the component
* state: One of INSTANTIATED, VALIDATED, INVALIDATED or KILLED
"""

EVENT_COMPONENT_INSTANTIATION = "{0}/instantiation" \
    .format(__COMPONENT_EVENTS_PREFIX)
"""
Result of a late instantiation of waiting components, i.e. if the agent
instantiate() method was called with ``until_possible=True``

* isolate: UID of the isolate hosting the components
* success: List of component names that have been instantiated
* errors: Mapping of failed instantiations: Name -> Error message
"""

EVENT_FACTORY_REGISTERED = '{0}/registered'.format(__FACTORY_EVENTS_PREFIX)
""" Factory event: a factory has been registered """

EVENT_FACTORY_UNREGISTERED = '{0}/unregistered'.format(__FACTORY_EVENTS_PREFIX)
""" Factory event: a factory has been unregistered """
