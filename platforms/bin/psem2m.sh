#!/bin/bash
#
# PSEM2M Control script
#
# @author Thomas Calmant -- isandlaTech.com
#

# Common constants
JAVA="java"

# Platform folders
DIR_CONF="conf"
DIR_REPO="repo"
FELIX_CACHE="felix-cache"

# Platform configuration
PLATFORM_EXTRA_CONF_FOLDERS=""
PLATFORM_PROVISION_FILENAME=platform.bundles
PLATFORM_FRAMEWORK_FILENAME=platform.framework

# Bootstrap configuration
BOOTSTRAP_FILENAME=bootstrap.jar
BOOTSTRAP_MAIN_CLASS=org.psem2m.utilities.bootstrap.Main

#
# Start function
#
start() {
    echo "Cleaning cache..."
    rm -fr $FELIX_CACHE

    echo "Starting platform..."
    CONF_FOLDERS="$PSEM2M_BASE/$DIR_CONF $PSEM2M_HOME/$DIR_CONF $PLATFORM_EXTRA_CONF_FOLDERS"

    # Try to find the base platform framework file
    PLATFORM_FRAMEWORK_FILE=`find $CONF_FOLDERS -name $PLATFORM_FRAMEWORK_FILENAME -print -quit 2>/dev/null`
    if [ -z $PLATFORM_FRAMEWORK_FILE ]
    then
        echo "Error: Can't find the platform framework file '$PLATFORM_FRAMEWORK_FILENAME'" >&2
        return 1
    fi

    # Read the framework file name and test if it really exists
    PLATFORM_FRAMEWORK_BUNDLE=`grep -vsh \# $PLATFORM_FRAMEWORK_FILE | grep .jar$ | head -n 1`
    if [ -z $PLATFORM_FRAMEWORK_BUNDLE ]
    then
        echo "Error: can't read the OSGi framework main bundle name in $PLATFORM_FRAMEWORK_FILE"
        return 1
    fi

    PLATFORM_FRAMEWORK_BUNDLE_FILE=`find $PLATFORM_FRAMEWORK_BUNDLE $PSEM2M_BASE/$DIR_REPO $PSEM2M_HOME/$DIR_REPO -name $PLATFORM_FRAMEWORK_BUNDLE -print -quit 2>/dev/null`
    if [ ! -e $PLATFORM_FRAMEWORK_BUNDLE_FILE ]
    then
        echo "Error: can't find the OSGi framework main bundle file '$PLATFORM_FRAMEWORK_BUNDLE'"
        return 1
    fi
    echo "OSGi Framework: $PLATFORM_FRAMEWORK_BUNDLE_FILE"

    # Try to find the base platform provisionning file
    PROVISION_FILE=`find $CONF_FOLDERS -name $PLATFORM_PROVISION_FILENAME -print -quit 2>/dev/null`
    if [ -z $PROVISION_FILE ]
    then
        echo "Error: Can't find the platform provision file '$PLATFORM_PROVISION_FILENAME'" >&2
        return 1
    fi
    echo "Provision file: $PROVISION_FILE"

    # Find the bootstrap : base then home then error
    BOOTSTRAP_FILE=`find $PSEM2M_BASE/$DIR_REPO/$BOOTSTRAP_FILENAME $PSEM2M_HOME/$DIR_REPO/$BOOTSTRAP_FILENAME -print -quit 2>/dev/null`
    if [ -z $BOOTSTRAP_FILE ]
    then
        echo "Error: Can't find the bootstrap file '$BOOTSTRAP_FILENAME'" >&2
        return 1
    fi
    echo "Bootstrap: $BOOTSTRAP_FILE"

    # Run all
    echo "Running bootstrap..."
    $PSEM2M_JAVA -cp "$BOOTSTRAP_FILE:$PLATFORM_FRAMEWORK_BUNDLE_FILE" $BOOTSTRAP_MAIN_CLASS --human --lines --file=$PROVISION_FILE psem2m.home="$PSEM2M_HOME" psem2m.base="$PSEM2M_BASE" psem2m.isolate.id="master" &

    echo "Started"
    return 0
}

help() {
    echo "Usage : $0 (start|stop|status)"
    return 0
}

# ----- Base environment -----

echo "===== PSEM2M ====="

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

echo "Home: $PSEM2M_HOME"
echo "Base: $PSEM2M_BASE"
echo "Java: $PSEM2M_JAVA"
echo "=================="

# Go to base
cd $PSEM2M_BASE

# ------ Main code ------

case $1 in
    start)
        start
        ;;

    *)
        help
        ;;
esac

exit $?
