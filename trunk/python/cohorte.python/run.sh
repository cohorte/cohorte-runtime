#!/bin/bash

# Common variables
. ./common.sh

# Python interpreter to use
PYTHON_INTERPRETER=${PYTHON_INTERPRETER:="python"}

# Python path
export PYTHONPATH=$(pwd):/home/tcalmant/programmation/workspaces/psem2m/trunk/python/psem2m.base/src

# COHORTE node name
export COHORTE_NODE=${COHORTE_NODE:=central}

# Remove previous environment
rm -fr $COHORTE_BASE/var/*

# Run the damn thing
$PYTHON_INTERPRETER -- cohorte/boot/boot.py -d -v $*

