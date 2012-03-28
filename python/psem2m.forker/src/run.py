#!/usr/bin/python
#-- Content-Encoding: utf-8 --

import logging
import os
import sys

# ------------------------------------------------------------------------------

# Set up the logging configuration
logging.basicConfig(level=logging.DEBUG)

# ------------------------------------------------------------------------------

def get_abs_path(path):
    """
    Retrieves the absolute path of the given path
    
    :param path: A relative or absolute path
    """
    return os.path.abspath(os.path.join(os.getcwd(), path))

# ------------------------------------------------------------------------------

# Set up environment variables
os.environ["PSEM2M_HOME"] = get_abs_path("../../../platforms/psem2m.home")
os.environ["PSEM2M_BASE"] = get_abs_path("../../../platforms/base-demo-py")
os.environ["PSEM2M_ISOLATE_ID"] = "org.psem2m.internals.isolates.forker"
os.environ["HTTP_PORT"] = "9001"
os.environ["RPC_PORT"] = "9002"

# ------------------------------------------------------------------------------

# Set up Python path
sys.path.append(os.getcwd())
for path in ("../../python.injection/src", "../../psem2m.base/src"):
    sys.path.append(get_abs_path(path))

# ------------------------------------------------------------------------------

import forker.starter
forker.starter.main(True)
