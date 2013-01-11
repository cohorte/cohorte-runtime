#!/bin/bash

# Python interpreter to use
PYTHON_INTERPRETER='python'

# Python path
export PYTHONPATH=$(pwd):/home/tcalmant/programmation/workspaces/psem2m/trunk/python/psem2m.base/src

# COHORTE directories
export COHORTE_HOME=${COHORTE_HOME:="/home/tcalmant/programmation/workspaces/psem2m/platforms/psem2m.home"}
export COHORTE_BASE=${COHORTE_BASE:="/home/tcalmant/programmation/workspaces/psem2m/platforms/base-demo-july2012"}

# COHORTE node name
export COHORTE_NODE=${COHORTE_NODE:=central}

# Run the damn thing
$PYTHON_INTERPRETER -- cohorte/boot/boot.py -d -v $*

