'''
Created on 13 oct. 2011

@author: Thomas Calmant
'''

from erp.stresser import Stresser
from multiprocessing import Pool

import random

# ------------------------------------------------------------------------------

def _run(parameters):
    """
    Runs a stresser
    """
    scenario = parameters["scenario"]
    stresser_id = parameters["stresser_id"]
    nb_iterations = parameters["nb_iterations"]
    think_time = parameters["think_time"]

    stresser = Stresser("Stresser %3d" % stresser_id, think_time)
    stresser.run(scenario, nb_iterations)

# ------------------------------------------------------------------------------

def start_stressers(nb_scenario_1=15, nb_scenario_2=5, erp_off=True):
    """
    Runs the stressers in separate processes
    """
    # Scenario 1
    base_id = 1
    stressers = [{"scenario": 1, \
                  "stresser_id": base_id + stress_id, \
                  "think_time": random.randint(1, 3), \
                  "nb_iterations": 100}
                  for stress_id in xrange(0, nb_scenario_1)]

    # Scenario 2
    base_id = len(stressers)
    stressers += [{"scenario": 2, \
                   "stresser_id": base_id + stress_id, \
                   "think_time": random.uniform(.5, 2), \
                   "nb_iterations": 100}
                   for stress_id in xrange(0, nb_scenario_2)]

    # Scenario 3, if needed
    if erp_off:
        base_id = len(stressers)
        # 2 to 6 iterations
        nb_iter = random.choice([n * 2 for n in xrange(1, 4)])
        stressers += [{"scenario": 3, \
                       "stresser_id": base_id, \
                       "think_time": 0, \
                       "nb_iterations": nb_iter}]

    pool = Pool(len(stressers))
    pool.map(_run, stressers)

# ------------------------------------------------------------------------------

if __name__ == '__main__':
    start_stressers()
