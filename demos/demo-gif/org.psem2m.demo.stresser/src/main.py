'''
Created on 13 oct. 2011

@author: Thomas Calmant
'''

from erp.stresser_maps import Stresser

from multiprocessing import Pool
from optparse import OptionParser

import random

# ------------------------------------------------------------------------------

def _run(parameters):
    """
    Runs a stresser
    """
    scenario = parameters["scenario"]
    stresser_id = parameters["stresser_id"]

    stresser = Stresser("Stresser %3d" % stresser_id, parameters["host"], \
                        parameters["data_port"], parameters["erp_port"])
    stresser.run(scenario, parameters)

# ------------------------------------------------------------------------------

def start_stressers(options):
    """
    Runs the stressers in separate processes
    """
    # Scenario 1
    base_id = 1
    stressers = [{"scenario": 1, \
                  "stresser_id": base_id + stress_id, \
                  "think_time": random.randint(1, 3), \
                  "nb_iterations": options.nb_iter_1,
                  "host": options.host,
                  "erp_port": options.erp_port,
                  "data_port": options.data_port}
                  for stress_id in xrange(0, options.nb_scenario_1)]

    # Scenario 2
    base_id = 50
    stressers += [{"scenario": 2, \
                   "stresser_id": base_id + stress_id, \
                   "think_time": random.uniform(.5, 2), \
                   "nb_iterations": options.nb_iter_2,
                   "host": options.host,
                   "erp_port": options.erp_port,
                   "data_port": options.data_port}
                   for stress_id in xrange(0, options.nb_scenario_2)]

    # Scenario 3, if needed
    if options.toggle_erp:
        base_id = 70

        nb_iter = options.nb_iter_erp
        if nb_iter <= 0:
            # 2 to 6 iterations
            nb_iter = random.choice([n * 2 + 1 for n in xrange(1, 4)])

        stressers += [{"scenario": 3, \
                       "stresser_id": base_id, \
                       "think_time": 0, \
                       "nb_iterations": nb_iter,
                       "delay": options.delay_erp,
                       "think_time_min":options.think_min_erp,
                       "think_time_max":options.think_max_erp,
                       "host": options.host,
                       "erp_port": options.erp_port,
                       "data_port": options.data_port}]

    # Scenario 4, if needed
    if options.toggle_quarterback:
        base_id = 80
        nb_iterations = options.nb_iter_quarterback

        stressers += [{"scenario": 4, \
                       "stresser_id": base_id, \
                       "think_time": 0, \
                       "nb_iterations": nb_iterations,
                       "delay": options.delay_quarterback,
                       "think_time_min":options.think_min_quarterback,
                       "think_time_max":options.think_max_quarterback,
                       "host": options.host,
                       "erp_port": options.erp_port,
                       "data_port": options.data_port}]

    pool = Pool(len(stressers))
    pool.map(_run, stressers)

# ------------------------------------------------------------------------------

def main():
    """
    Stresser entry point
    """
    # Prepare the arguments parser
    parser = OptionParser()

    # Connection to the Data Server and to the ERP
    parser.add_option("--host", dest="host", default="localhost", \
                      help="Data server host name", metavar="HOST")

    parser.add_option("--data-port", dest="data_port", type="int", \
                      default=9210, \
                      help="Data server port", metavar="PORT")

    parser.add_option("--erp-port", dest="erp_port", type="int", \
                      default=8080, \
                      help="ERP port", metavar="PORT")

    # Scenario 1
    parser.add_option("--nb-processes-1", dest="nb_scenario_1", \
                      type="int", default=15, \
                      help="Number of scenario 1 processes", metavar="NUMBER")

    parser.add_option("--nb-iter-1", dest="nb_iter_1", \
                      type="int", default=20, \
                      help="Number of scenario 1 iterations in each process", \
                      metavar="NUMBER")

    # Scenario 2
    parser.add_option("--nb-processes-2", dest="nb_scenario_2", \
                      type="int", default=5, \
                      help="Number of scenario 2 processes", metavar="NUMBER")

    parser.add_option("--nb-iter-2", dest="nb_iter_2", \
                      type="int", default=20, \
                      help="Number of scenario 2 iterations in each process", \
                      metavar="NUMBER")

    # Scenario 3
    parser.add_option("--use-scenario-3", dest="toggle_erp", \
                      action="store_true", default=False, \
                      help="Use the scenario 3 : ERP on/off toggling")

    parser.add_option("--nb-iter-3", dest="nb_iter_erp", \
                      type="int", default= -1, \
                      help="Number of scenario 3 iterations in each process", \
                      metavar="NUMBER")

    parser.add_option("--delay-scenario-3", dest="delay_erp",
                      type="float", default=0, \
                      help="Delay the scenario 3 of TIME seconds", \
                      metavar="TIME")

    parser.add_option("--think-min-3", dest="think_min_erp",
                      type="int", default=15, \
                      help="Minimum think time for scenario 3, in seconds", \
                      metavar="TIME")

    parser.add_option("--think-max-3", dest="think_max_erp",
                      type="int", default=20, \
                      help="Maximum think time for scenario 3, in seconds", \
                      metavar="TIME")

    # Scenario 4
    parser.add_option("--use-scenario-4", dest="toggle_quarterback", \
                      action="store_true", default=False, \
                      help="Use the scenario 4 : Quarterback on/off toggling")

    parser.add_option("--nb-iter-4", dest="nb_iter_quarterback", \
                      type="int", default=2, \
                      help="Number of scenario 4 iterations in each process", \
                      metavar="NUMBER")

    parser.add_option("--delay-scenario-4", dest="delay_quarterback",
                      type="float", default=0, \
                      help="Delay the scenario 4 of TIME seconds", \
                      metavar="TIME")

    parser.add_option("--think-min-4", dest="think_min_quarterback",
                      type="int", default=5, \
                      help="Minimum think time for scenario 4, in seconds", \
                      metavar="TIME")

    parser.add_option("--think-max-4", dest="think_max_quarterback",
                      type="int", default=10, \
                      help="Maximum think time for scenario 4, in seconds", \
                      metavar="TIME")

    # Parse arguments
    start_stressers(parser.parse_args()[0])

if __name__ == '__main__':
    main()
