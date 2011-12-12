#-- Content-Encoding: utf-8 --
"""
Common classes, methods and constants for the PSEM2M starter scripts

@author: Thomas Calmant
"""

# ------------------------------------------------------------------------------

# Bootstrap JAR file name
BOOTSTRAP_FILENAME = "org.psem2m.utilities.bootstrap.jar"

# Bootstrap main class name
BOOTSTRAP_MAIN_CLASS = "org.psem2m.utilities.bootstrap.Main"

# ------------------------------------------------------------------------------

# Monitor access file (relative to PSEM2M Base)
MONITOR_ACCESS_FILE = "var/monitor.access"

# Monitor PID file (relative to PSEM2M Base)
MONITOR_PID_FILE = "var/monitor.pid"

# ------------------------------------------------------------------------------

# Platform base bundles provision file
PLATFORM_PROVISION_FILENAME = "platform.bundles"

# OSGi framework definition file
PLATFORM_FRAMEWORK_FILENAME = "platform.framework"

# ------------------------------------------------------------------------------

# PSEM2M Base directory Java property
PROP_PLATFORM_BASE = "org.psem2m.platform.base"

# PSEM2M Home directory Java property
PROP_PLATFORM_HOME = "org.psem2m.platform.home"

# PSEM2M Isolate ID Java property
PROP_PLATFORM_ISOLATE_ID = "org.psem2m.platform.isolate.id"

# PSEM2M debug port Java property
PROP_PLATFORM_DEBUG_PORT = "org.psem2m.debug.port"

# ------------------------------------------------------------------------------

# Default isolate ID
DEFAULT_ISOLATE_ID = "org.psem2m.internals.isolates.monitor-1"

# ------------------------------------------------------------------------------

# Stop signal
SIGNAL_STOP = "/psem2m-signal-receiver/psem2m/platform/stop"

# ------------------------------------------------------------------------------

WORKING_DIRECTORY = "var/work"
