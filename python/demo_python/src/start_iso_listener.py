#!/usr/bin/python
#-- Content-encoding: utf-8 --
"""
@author: Thomas Calmant
"""

import readline
import code
import logging
import os.path
import sys

# ------------------------------------------------------------------------------

# Set logging level
logging.basicConfig(level=logging.DEBUG)

# ------------------------------------------------------------------------------

logging.info("--- Setting up environment ---")
os.environ["HTTP_PORT"] = "10000"
os.environ["RPC_PORT"] = "10001"
os.environ["PSEM2M_ISOLATE_ID"] = "isolate-listener"

logging.info("--- Extending sys.path ---")
sys.path.append(os.getcwd())

for other in ("../../psem2m.base/src", "../../python.injection/src", '.'):
    other_path = os.path.abspath(os.path.join(os.getcwd(), other))
    logging.info("Adding : %s", other_path)
    sys.path.append(other_path)

# ------------------------------------------------------------------------------

logging.info("--- Import Pelix ---")

import psem2m.services.pelix as pelix
from psem2m.services.pelix import Framework, BundleContext
from psem2m.component import constants

# ------------------------------------------------------------------------------

logging.info("--- Start Pelix ---")
framework = pelix.FrameworkFactory.get_framework({'debug': True})
assert isinstance(framework, Framework)
context = framework.get_bundle_context()
assert isinstance(context, BundleContext)

logging.info("-- Install iPOPO --")
bid = framework.install_bundle("psem2m.component.ipopo")
b_ipopo = context.get_bundle(bid)
b_ipopo.start()

ref = context.get_service_reference(constants.IPOPO_SERVICE_SPECIFICATION)
ipopo = context.get_service(ref)
del ref

logging.info("-- Install HTTP Service --")
bid = framework.install_bundle("base.httpsvc")
b_http = context.get_bundle(bid)
b_http.start()

logging.info("-- Install signals --")
bid = framework.install_bundle("base.signals")
b_signals = context.get_bundle(bid)
b_signals.start()

logging.info("-- Install composer agent --")
bid = framework.install_bundle("base.composer")
b_compo = context.get_bundle(bid)
b_compo.start()

logging.info("-- Install Remote Services --")
bid = framework.install_bundle("base.remoteservices")
b_rs = context.get_bundle(bid)
b_rs.start()

logging.info("-- Install the listener --")
bid = framework.install_bundle("listener")
b_listener = context.get_bundle(bid)
b_listener.start()

def stop():
    logging.info("--- Stop Pelix ---")
    framework.stop()

logging.info("Ready to roll...")

try:
    # Run shell
    code.InteractiveConsole(globals()).interact()
finally:
    stop()

