#!/usr/bin/python
#-- Content-Encoding: utf-8 --
"""
iPOPO Benchmark : benchmark starter

@author: Thomas Calmant
"""

import logging
import timeit

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("main")

def run(title, loops, nb_repeat):
    """
    Runs the benchmark
    
    :param title: Benchmark run title
    :param setup: Statement to call before running
    :param stmt: Statement to run
    :param loops: Number of loops per timeit calls
    :param nb_repeat: Number of timeit loops
    :return: Minimum benchmark time
    """
    logger.info("Preparing %s benchmark...", title)
    timer = timeit.Timer("bench.run(42, 128)",
"""
import {module}
{module}.clear_module()
{module}.prepare_module()
bench = {module}.get_benchmark()
""".format(module=title))

    logger.info("Running %s benchmark...", title)
    min_time = min(timer.repeat(nb_repeat, loops))
    logger.info("%s: Minimum run time for %d loops : %.3f", title, loops,
                min_time)

    return min_time

def ipopo_start_stop_run(loops, nb_repeat):
    """
    Runs the start / stop benchmark on iPOPO
    """
    timer = timeit.Timer("""
import {module}
{module}.prepare_module()
{module}.clear_module()
""".format(module="ipopo"), "import logging; logging.info('Running...')")

    logger.info("Running start/stop benchmark...")
    min_time = min(timer.repeat(nb_repeat, loops))
    logger.info("Start/Stop: Minimum run time for %d loops : %.3f", loops,
                min_time)

    return min_time


def main(loops=10 ** 6):
    """
    Entry point
    """
    logger.info("--- iPOPO start/stop run ---")
    ipopo_start_stop_run(1000, 5)

    logger.info("--- Loop runs ---")
    run("normal", loops, 5)
    run("ipopo", loops, 5)


if __name__ == '__main__':
    main()
