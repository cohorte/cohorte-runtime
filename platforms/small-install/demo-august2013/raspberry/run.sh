#!/bin/bash

# Keep the current working directory
old_pwd="$(pwd)"

# Compute the path to this file
cd "$(dirname $0)"
COHORTE_ROOT="$(pwd)"
cd "$old_pwd"

echo "Cohorte root   => $COHORTE_ROOT"

# COHORTE directories
export COHORTE_HOME=${COHORTE_HOME:="$COHORTE_ROOT/home"}
export COHORTE_BASE=${COHORTE_BASE:="$COHORTE_ROOT/base"}

# COHORTE node name
export COHORTE_NODE=${COHORTE_NODE:="raspberry"}

# Forker log file
export COHORTE_LOGFILE="$COHORTE_BASE/var/forker.log"

# Default Python interpreter to use
PYTHON_INTERPRETER=${PYTHON_INTERPRETER:="python"}

# Python path: Current path + demo path
PYTHON_DEMOPATH="$COHORTE_ROOT/demo.july2012.python"
export PYTHONPATH="$(pwd):$PYTHON_DEMOPATH:$PYTHONPATH"

# Remove previous environment
if [ ! -d "$COHORTE_BASE/var" ]
then
    mkdir -p "$COHORTE_BASE/var"
else
    rm -r $COHORTE_BASE/var/*
fi

# Run the damn thing
$PYTHON_INTERPRETER -- cohorte/boot/boot.py -d -v $*
