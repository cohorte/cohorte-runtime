#!/bin/bash

# Compute the repository root, and normalize it
COHORTE_ROOT=${COHORTE_ROOT:="../../.."}
old_pwd="$(pwd)"
cd "$COHORTE_ROOT"
export COHORTE_ROOT="$(pwd)"
cd "$old_pwd"

# Run the default configuration
. ./run_conf/default.sh

# Compute the local configuration file name
conf_name="./run_conf/$(hostname).sh"
if [ -x conf_name ]
then
    # Execute it
    . "$conf_name"
fi

# COHORTE node name
export COHORTE_NODE=${COHORTE_NODE:="central"}

# Forker log file
export COHORTE_LOGFILE="$COHORTE_BASE/var/forker.log"

# Default Python interpreter to use (Python 3)
PYTHON_INTERPRETER=${PYTHON_INTERPRETER:="python3"}

# Python path: Current path + demo path
PYTHON_DEMOPATH="$COHORTE_ROOT/demos/demo-july2012/demo.july2012.python"
export PYTHONPATH="$(pwd):$PYTHON_DEMOPATH:$PYTHONPATH"

# Remove previous environment
mkdir -f $COHORTE_BASE/var
rm -fr $COHORTE_BASE/var/*

# Run the damn thing
$PYTHON_INTERPRETER -- cohorte/boot/boot.py -d -v $*
