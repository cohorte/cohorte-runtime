#!/bin/bash

# Common variables
. ./common.sh

# Python interpreter to use
PYTHON_INTERPRETER=${PYTHON_INTERPRETER:="python"}

# Python path: Current path + demo path
PYTHON_DEMOPATH=$(readlink -f "../../../demos/demo-july2012/demo.july2012.python")
export PYTHONPATH="$(pwd):$PYTHON_DEMOPATH"

# COHORTE node name
export COHORTE_NODE=${COHORTE_NODE:="central"}

# Forker log file
export COHORTE_LOGFILE="$COHORTE_BASE/var/forker.log"

# Remove previous environment
rm -fr $COHORTE_BASE/var/*

# Run the damn thing
$PYTHON_INTERPRETER -- cohorte/boot/boot.py -d -v $*
