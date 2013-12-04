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

# Run the default configuration
echo Reading default configuration
. "$run_home/run_conf/default.sh"

# Set up script for fake raspberry pi
export COHORTE_NODE_NAME="raspberry"
export COHORTE_BASE="$COHORTE_ROOT/platforms/base-demo-august2013-raspi-2"

# Run the forker
./run.sh $*

