#!/usr/bin/python
#-- Content-Encoding: UTF-8 --

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


def store_pid(base):
    """
    Stores the current PID to the forker.pid file
    
    :param base: The PSEM2M instance base
    """
    pid_file = os.path.join(base, "var", "forker.pid")

    if not os.path.exists(pid_file):
        # Prepare the parent directories
        parent = os.path.dirname(pid_file)
        if not os.path.isdir(parent):
            os.makedirs(parent)

    with open(pid_file, "w") as fp:
        # Write the PID
        fp.write(str(os.getpid()))
        fp.write("\n")


def main(start_monitor):
    """
    Forker utility entry point
    
    Be sure that psem2m and forker.start packages are accessible before
    calling this method
    
    :param start_monitor: If True, a monitor must be started with the forker
    """
    # Import PSEM2M modules
    import psem2m
    import forker.starter

    # Run !
    # Set up environment variables
    os.environ[psem2m.PSEM2M_HOME] = os.getenv(psem2m.PSEM2M_HOME, \
                            get_abs_path("../../../platforms/psem2m.home"))

    base = os.environ[psem2m.PSEM2M_BASE] = os.getenv(psem2m.PSEM2M_BASE, \
                            get_abs_path("../../../platforms/base-compo"))

    os.environ[psem2m.PSEM2M_ISOLATE_ID] = os.getenv(psem2m.PSEM2M_ISOLATE_ID,
                            "org.psem2m.internals.isolates.forker")

    # FIXME: Should be read from configuration...
    os.environ["HTTP_PORT"] = "9001"

    # Store the process PID
    store_pid(base)

    # Run !
    forker.starter.main(start_monitor)

# ------------------------------------------------------------------------------

if __name__ == "__main__":
    # Set up Python path
    sys.path.append(os.getcwd())
    for python_path in ("../../python.injection/src", "../../psem2m.base/src"):
        sys.path.append(get_abs_path(python_path))

    # Arguments...
    start_monitor = "--start-monitor" in sys.argv

    # Run !
    main(start_monitor)
