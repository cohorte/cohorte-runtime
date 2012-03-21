#!/usr/bin/python
#-- Content-Encoding: utf-8 --
"""
iPOPO Benchmark : normal Python implementation

@author: Thomas Calmant
"""

class Operator(object):
    """
    Basic operator
    """

    def __init__(self, operator):
        """
        Constructor
        """
        self.op = operator


    def operate(self, a, b):
        """
        Applies the operator on a and b
        
        :param a: Operand A
        :param b: Operand B
        :return: Result of Operator(A, B)
        """
        return self.op(a, b)


class Adder(Operator):
    """
    Adder
    """
    def __init__(self):
        """
        Constructor
        """
        Operator.__init__(self, self.add)


    def add(self, a, b):
        """
        Adds a and b
        """
        return a + b


class Multiplier(Operator):
    """
    Multiplier
    """
    def __init__(self):
        """
        Constructor
        """
        Operator.__init__(self, self.mul)


    def mul(self, a, b):
        """
        Multiplies a by b
        """
        return a * b

class Benchmark():
    """
    Benchmark
    """
    def __init__(self):
        """
        Constructor
        """
        self.add = Adder()
        self.mul = Multiplier()


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
    return Benchmark()


def prepare_module():
    """
    Prepares the module
    """
    pass


def clear_module():
    """
    Cleans up the module
    """
    pass
