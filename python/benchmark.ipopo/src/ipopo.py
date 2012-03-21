#!/usr/bin/python
#-- Content-Encoding: utf-8 --
"""
iPOPO Benchmark : normal Python implementation

@author: Thomas Calmant
"""

# No need to do things twice
import normal

# Import pelix and iPOPO
from psem2m.component.decorators import ComponentFactory, Requires, Provides
import psem2m.component.constants as constants
import psem2m.services.pelix as pelix

import logging
logger = logging.getLogger("bench.ipopo")

# Factory name
BENCHMARK_FACTORY = "BenchmarkFactory"
OPERATOR_SPEC = "bench.ipopo.operator"

_framework = None
_ipopo = None

# Benchmark class
@ComponentFactory(BENCHMARK_FACTORY)
@Requires("add", OPERATOR_SPEC, spec_filter="(op=adder)")
@Requires("mul", OPERATOR_SPEC, spec_filter="(op=multiplier)")
class Benchmark():
    """
    Benchmark
    """
    def __init__(self):
        """
        Constructor
        """
        self.add = None
        self.mul = None


    def run(self, a, b):
        """
        Runs an addition and a multiplication
        
        :param a: Operand A
        :param b: Operand B
        :return: Result tuple (add, mul)
        """
        return self.add.operate(a, b), self.mul.operate(a, b)


def get_benchmark():
    """
    Sets up the benchmark object and returns it
    """
    return _ipopo.instantiate(BENCHMARK_FACTORY, "benchmark.ipopo")


def prepare_module():
    """
    Prepares the module
    """
    global _ipopo, _framework
    # Start Pelix and get the reference to iPOPO
    _framework = pelix.FrameworkFactory.get_framework()
    context = _framework.get_bundle_context()

    bid = _framework.install_bundle("psem2m.component.ipopo")
    b_ipopo = context.get_bundle(bid)
    b_ipopo.start()

    ref = context.get_service_reference(constants.IPOPO_SERVICE_SPECIFICATION)
    _ipopo = context.get_service(ref)
    del ref

    # Manipulate normal classes
    add_fac = ComponentFactory("AdderFactory")(Provides(OPERATOR_SPEC)(normal.Adder))
    mul_fac = ComponentFactory("MulFactory")(Provides(OPERATOR_SPEC)(normal.Multiplier))

    # Register all factories
    _ipopo.register_factory(context, add_fac)
    _ipopo.register_factory(context, mul_fac)
    _ipopo.register_factory(context, Benchmark)

    # Instantiate adder and multiplier
    _ipopo.instantiate("AdderFactory", "BlackAdder", {"op": "adder"})
    _ipopo.instantiate("MulFactory", "DrMuller", {"op": "multiplier"})


def clear_module():
    """
    Clears the module
    """
    if _framework is not None:
        _framework.stop()
        pelix.FrameworkFactory.delete_framework(_framework)

