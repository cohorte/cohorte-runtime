#!/usr/bin/python3
#-- Content-Encoding: utf-8 --
"""
Common classes, methods and constants for the PSEM2M scripts

@author: Thomas Calmant
"""

import sys
from psem2m.runner.core import PSEM2MRunner

# ------------------------------------------------------------------------------

class Main(object):
    """
    Entry point class
    """

    def __init__(self):
        """
        Constructor
        """
        self._runner = PSEM2MRunner()


    def start(self):
        """
        Starts the platform
        """
        print("Starting...")
        result = self._runner.start()
        print("Done :", result)
        return result


    def stop(self):
        """
        Stops the platform
        """
        print("Stopping...")
        result = self._runner.stop()
        print("Done :", result)
        return result


    def restart(self):
        """
        Restarts the platform
        """
        self.stop()
        return self.start()


    def try_restart(self):
        """
        The platform must be restarted only if it is actually running
        """
        if self._runner.is_running():
            print("Platform is started...")
            return self.restart()

        else:
            print("Platform is not running")

        return 1


    def force_reload(self):
        """
        Forces the application to reload its configuration or to restart
        """
        return self.restart()


    def reload(self):
        """
        The application must reload its configuration (or restart)
        """
        return self.restart()


    def status(self):
        """
        Prints any relevant status info, and return a status code, an integer:
    
        0          program is running or service is OK
        1          program is dead and /var/run pid file exists
        2          program is dead and /var/lock lock file exists
        3          program is not running
        4          program or service status is unknown
        5-99      reserved for future LSB use
        100-149      reserved for distribution use
        150-199      reserved for application use
        200-254      reserved
    
        @see: http://dev.linux-foundation.org/betaspecs/booksets/LSB-Core-generic/LSB-Core-generic/iniscrptact.html
        """

        if self._runner.is_running():
            print("Platform is running")
            return 0

        else:
            print("Platform seems to be stopped")
            return 3

# ------------------------------------------------------------------------------

def print_usage():
    """
    Prints out the script usage
    """
    print("""%s (start|stop|status|restart|try-restart|reload|force-reload)

    start        : Starts the PSEM2M platform
    stop         : Stops the PSEM2M platform
    status       : Prints the platform execution status
    restart      : (Re)starts the PSEM2M platform
    try-restart  : Restarts the PSEM2M platform if it is already running
    reload       : (Re)starts the PSEM2M platform
    force-reload : (Re)starts the PSEM2M platform
""" % sys.argv[0])


if __name__ == "__main__":
    """
    Entry point
    """

    if len(sys.argv) < 2:
        print_usage()
        sys.exit(1)

    # Compute action name
    action = sys.argv[1].strip().lower().replace("-", "_")

    main = Main()
    if not hasattr(main, action):
        # Unknown action
        print_usage()
        sys.exit(1)

    # Run it
    action_impl = getattr(main, action)

    sys.exit(action_impl())
