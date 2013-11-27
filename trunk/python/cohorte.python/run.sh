#!/bin/bash

# Keep the current working directory
old_pwd="$(pwd)"

# Compute the path to this file
cd "$(dirname $0)"
run_home="$(pwd)"

# Compute the repository root, and normalize it
cohorte_root=${COHORTE_ROOT:="$run_home/../../.."}
cd "$cohorte_root"
export COHORTE_ROOT="$(pwd)"
cd "$old_pwd"

echo "Cohorte root   => $COHORTE_ROOT"
echo "Cohorte runner => $run_home"

# Run the default configuration
echo Reading default configuration
. "$run_home/run_conf/default.sh"

# Compute the local configuration file name
conf_name="$run_home/run_conf/$(hostname).sh"
if [ -x "$conf_name" ]
then
    # Execute it
    echo "Reading local configuration => $conf_name"
    . "$conf_name"
else
    echo "No local configuration to read ($conf_name)"
fi

# COHORTE node name
export COHORTE_NODE_NAME=${COHORTE_NODE_NAME:="central"}

# Forker log file
export COHORTE_LOGFILE="$COHORTE_BASE/var/forker.log"

# Default Python interpreter to use (Python 3)
PYTHON_INTERPRETER=${PYTHON_INTERPRETER:="python3"}

# Python path: Current path + demo path
PYTHON_DEMOPATH="$COHORTE_ROOT/demos/demo-july2012/demo.july2012.python"
export PYTHONPATH="$(pwd):$PYTHON_DEMOPATH:$PYTHONPATH"

# Remove previous environment
if [ ! -d "$COHORTE_BASE/var" ]
then
    mkdir "$COHORTE_BASE/var"
else
    rm -r $COHORTE_BASE/var/*
fi

# Run the damn thing
$PYTHON_INTERPRETER -- cohorte/boot/boot.py -d -v $*
