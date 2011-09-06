#!/bin/bash
#
# PSEM2M Control script
#
# @author Thomas Calmant -- isandlaTech.com
#

# Common constants
JAVA="java"
MONITOR_ISOLATE_ID="psem2m.master"
FORKER_ISOLATE_ID="psem2m.forker"

PROP_PLATFORM_ISOLATE_ID="org.psem2m.platform.isolate.id"
PROP_PLATFORM_HOME="org.psem2m.platform.home"
PROP_PLATFORM_BASE="org.psem2m.platform.base"

PROP_PLATFORM_DEBUG_PORT="org.psem2m.debug.port"

# Platform folders
DIR_CONF="conf"
DIR_REPO="repo"
FELIX_CACHE="felix-cache"

# Forker script file (relative to FORKER_BASE)
FORKER_SCRIPT_FILE="/var/run_forker.sh"
FORKER_PROVISION_FILENAME=forker.bundles
FORKER_FRAMEWORK_FILENAME=forker.framework

# Platform configuration
PLATFORM_EXTRA_CONF_FOLDERS=""
PLATFORM_PROVISION_FILENAME=platform.bundles
PLATFORM_FRAMEWORK_FILENAME=platform.framework

# Bootstrap configuration
BOOTSTRAP_FILENAME=bootstrap.jar
BOOTSTRAP_MAIN_CLASS=org.psem2m.utilities.bootstrap.Main

#
# Tries to find the given configuration file in the platform folders
#
find_conf_file() {

    local conf_folders="$PSEM2M_BASE/$DIR_CONF $PSEM2M_HOME/$DIR_CONF $PLATFORM_EXTRA_CONF_FOLDERS"

    local conf_file=`find -L $conf_folders -name $1 -print -quit 2>/dev/null`
    if [ -z $conf_file ]
    then
        return 1
    else
        echo $conf_file
        return 0
    fi
}

#
# Tries to find the given bundle file
#
find_bundle_file() {

    local bundle_file=`find -L $1 $PSEM2M_BASE/$DIR_REPO $PSEM2M_HOME/$DIR_REPO -name $1 -print -quit 2>/dev/null`
    if [ -e $bundle_file ]
    then
        echo $bundle_file
        return 0
    else
        return 1
    fi
}

#
# Read the first non commented .jar file name in a framework definition file
#
read_framework_file() {

    local bundle=`grep -vsh \# $1 | grep .jar$ | head -n 1`
    if [ -z $bundle ]
    then
        return 1
    else
        echo $bundle
        return 0
    fi
}

#
# Clear the /var/work directory
#
clear_cache() {
    rm -fr "./var/work"
    rm -fr "$PSEM2M_BASE/var/work"
    rm -fr "$PSEM2M_HOME/var/work"
}

#
# Start function
#
start() {
    echo "Cleaning cache..."
    clear_cache

    echo "Starting platform..."
    local MONITOR_PID_FILE="$PSEM2M_BASE/var/monitor.pid"

    # Try to find the base platform framework file
    local framework_file=$(find_conf_file $PLATFORM_FRAMEWORK_FILENAME)
    if [ -z $framework_file ]
    then
        echo "Error: Can't find the platform framework file '$PLATFORM_FRAMEWORK_FILENAME'" >&2
        return 1
    fi

    # Read the framework file name and test if it really exists
    local framework_bundle=$(read_framework_file $framework_file)
    if [ -z $framework_bundle ]
    then
        echo "Error: can't read the OSGi framework main bundle name in $framework_file"
        return 1
    fi

    local framework_bundle_file=$(find_bundle_file $framework_bundle)
    if [ ! -e $framework_bundle_file ]
    then
        echo "Error: can't find the OSGi framework main bundle file '$framework_bundle'"
        return 1
    fi
    echo "OSGi Framework: $framework_bundle_file"

    # Try to find the base platform provisionning file
    local provision_file=$(find_conf_file $PLATFORM_PROVISION_FILENAME)
    if [ -z $provision_file ]
    then
        echo "Error: Can't find the platform provision file '$PLATFORM_PROVISION_FILENAME'" >&2
        return 1
    fi
    echo "Provision file: $provision_file"

    # Run all
    echo "Running bootstrap..."
    touch $MONITOR_PID_FILE
    $PSEM2M_JAVA $JVM_EXTRA_ARGS -cp "$BOOTSTRAP_FILE:$framework_bundle_file" $BOOTSTRAP_MAIN_CLASS --human --lines --file="$provision_file" $PROP_PLATFORM_HOME="$PSEM2M_HOME" $PROP_PLATFORM_BASE="$PSEM2M_BASE" $PROP_PLATFORM_ISOLATE_ID="$MONITOR_ISOLATE_ID" org.osgi.service.http.port=9000 org.apache.felix.http.jettyEnabled=true osgi.shell.telnet.port=6000 &
    echo $! > $MONITOR_PID_FILE

    echo "Started"
    return 0
}

stop() {

    local MONITOR_PID_FILE="$PSEM2M_BASE/var/monitor.pid"

    local forker_pid=$(cat $MONITOR_PID_FILE 2>/dev/null)

    if [ -n $forker_pid ]
    then
        kill $forker_pid
    fi
}

#
# Writes the script to be used to start the forker isolate
#
write_forker_starter() {

    # Prepare the file
    local OUTPUT_FILE=$PSEM2M_BASE/$FORKER_SCRIPT_FILE
    mkdir -p `dirname $OUTPUT_FILE`
    touch $OUTPUT_FILE

    # Make it executable
    chmod +x $OUTPUT_FILE

    # Find the forker configuration files
    local framework_file=$(find_conf_file $FORKER_FRAMEWORK_FILENAME)
    if [ -z $framework_file ]
    then
        echo "Error: Can't find the forker framework file '$FORKER_FRAMEWORK_FILENAME'" >&2
        return 1
    fi

    # Read the framework file name and test if it really exists
    local framework_bundle=$(read_framework_file $framework_file)
    if [ -z $framework_bundle ]
    then
        echo "Error: can't read the OSGi framework main bundle name in $framework_file"
        return 1
    fi

    local framework_bundle_file=$(find_bundle_file $framework_bundle)
    if [ ! -e $framework_bundle_file ]
    then
        echo "Error: can't find the OSGi framework main bundle file '$framework_bundle'"
        return 1
    fi

    # Try to find the base platform provisionning file
    local provision_file=$(find_conf_file $FORKER_PROVISION_FILENAME)
    if [ -z $provision_file ]
    then
        echo "Error: Can't find the platform provision file '$FORKER_PROVISION_FILENAME'" >&2
        return 1
    fi

    # Write it down

    # First part : constants
    echo "#!/bin/bash

# Constants
FORKER_PID_FILE=\"$PSEM2M_BASE/var/forker.lock\"
" > $OUTPUT_FILE

    # Second part : PID test
    echo '
# Test if the forker is already running...
forker_pid=$(cat $FORKER_PID_FILE 2>/dev/null)

if [ -n $forker_pid ]
then
    # Non empty string, test the given PID
    ps --no-headers --pid $forker_pid >/dev/null 2>&1
    if [ $? -eq 0 ]
    then
        echo Forker already running with PID $forker_pid >&2
        exit 1
    fi
fi
' >> $OUTPUT_FILE

    # Third part : the bootstrap line
    echo "
# Run the forker isolate
$PSEM2M_JAVA $JVM_FORKER_EXTRA_ARGS -cp \"$BOOTSTRAP_FILE:$framework_bundle_file\" $BOOTSTRAP_MAIN_CLASS --human --lines --file=\"$provision_file\" $PROP_PLATFORM_HOME=\"$PSEM2M_HOME\" $PROP_PLATFORM_BASE=\"$PSEM2M_BASE\" $PROP_PLATFORM_ISOLATE_ID=\"$FORKER_ISOLATE_ID\" org.osgi.service.http.port=9001 org.apache.felix.http.jettyEnabled=true osgi.shell.telnet.port=6001 $PROG_FORKER_EXTRA_ARGS &
" >> $OUTPUT_FILE

    # Fourth (and last) part : the PID file
    echo '
# Write the forker PID
echo $! > $FORKER_PID_FILE
' >> $OUTPUT_FILE
}

prepare_debug() {

    DEBUG_MODE=1

    # Monitor arguments
    local PORT=$PSEM2M_DEBUG_PORT
    if [ -z $PORT ]
    then
        echo "No debug port indicated. Abandon."
        exit 1
    fi

    JVM_EXTRA_ARGS="-Xdebug -Xrunjdwp:transport=dt_socket,address=127.0.0.1:$PORT,suspend=y"

    # Forker arguments
    local PORT=`expr $PORT + 1`
    JVM_FORKER_EXTRA_ARGS="-Xdebug -Xrunjdwp:transport=dt_socket,address=127.0.0.1:$PORT,suspend=y"
    PROG_FORKER_EXTRA_ARGS="$PROP_PLATFORM_DEBUG_PORT=$PORT"

    echo "DEBUG MODE..."
    echo "mono   : $JVM_EXTRA_ARGS"
    echo "forker : $JVM_FORKER_EXTRA_ARGS"
    echo "extra  : $PROG_FORKER_EXTRA_ARGS"
    echo "...DEBUG MODE"
}

help() {
    echo "Usage : $0 (start|debug|stop|status|test)"
    return 0
}

# ----- Base environment -----

echo "==========================="
echo "========== PSEM2M ========="
echo "==========================="

# Find home
if [ -z $PSEM2M_HOME ]
then
    PSEM2M_HOME=$PWD
fi

# Find base
if [ -z $PSEM2M_BASE ]
then
    PSEM2M_BASE=$PSEM2M_HOME
fi

# Find Java
if [ -z $PSEM2M_JAVA ]
then

    if [ -e $PSEM2M_HOME/java/bin/java ]
    then
        # Try to use the platform java version
        PSEM2M_JAVA=$PSEM2M_HOME/java/bin/java
    else
        # Find the first 'java' in the shell
        PSEM2M_JAVA=`which java`
    fi

    # Nothing found => Error
    if [ -z $PSEM2M_JAVA ]
    then
        echo "Error: can't find the Java interpreter '$JAVA'" >&2
        exit 1
    fi
fi

# Find the bootstrap : base then home then error
BOOTSTRAP_FILE=`find $PSEM2M_BASE/$DIR_REPO/$BOOTSTRAP_FILENAME $PSEM2M_HOME/$DIR_REPO/$BOOTSTRAP_FILENAME -print -quit 2>/dev/null`
if [ -z $BOOTSTRAP_FILE ]
then
    echo "Error: Can't find the bootstrap file '$BOOTSTRAP_FILENAME'" >&2
    exit 1
fi

echo "Home      : $PSEM2M_HOME"
echo "Base      : $PSEM2M_BASE"
echo "Java      : $PSEM2M_JAVA"
echo "Bootstrap : $BOOTSTRAP_FILE"
echo "==========================="

# Go to base
cd $PSEM2M_BASE

# ------ Main code ------

case $1 in
    start)
        echo "========== Start =========="
        write_forker_starter
        start
        ;;

    debug)
        echo "========== Debug =========="
        prepare_debug
        write_forker_starter
        start
        ;;

    stop)
        echo "========== Stop  =========="
        stop
        ;;

    test)
        echo "=========== Test =========="
        write_forker_starter
        ;;

    *)
        help
        ;;
esac

exit $?
