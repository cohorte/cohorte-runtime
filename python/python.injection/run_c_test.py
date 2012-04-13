#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Starts a framework to run the c_test bundle 
"""

from pelix.framework import Framework, FrameworkFactory

import logging
import time
import threading

# ------------------------------------------------------------------------------

# Set logging level
logging.basicConfig(level=logging.DEBUG)

# ------------------------------------------------------------------------------

logging.info("--- Start Pelix ---")
framework = FrameworkFactory.get_framework({'debug': True})
framework.start()
assert isinstance(framework, Framework)

logging.info("-- Install iPOPO --")
bid = framework.install_bundle("pelix.ipopo.core")
bundle = framework.get_bundle_by_id(bid)
bundle.start()

logging.info("-- Install run_c_test --")
bid = framework.install_bundle("c_test")
logging.info("> Bundle ID : %d", bid)
bundle = framework.get_bundle_by_id(bid)
bundle.start()

# For interactive mode
bc = framework.get_bundle_context()

def stop():
    logging.info("--- Stop Pelix ---")
    framework.stop()

# For execution mode
time.sleep(3)

start = time.time()
stop()
end = time.time()

logging.info("--- Time to stop the framework : %.2f sec", (end - start))

while threading.active_count() > 1:
    logging.info("Waiting for threads to stop...")
    time.sleep(.2)

logging.info("Bye !")
