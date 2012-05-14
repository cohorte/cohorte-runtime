#!/bin/bash

echo "Setting environment variables..."
export PSEM2M_GIT=/home/thomas/git/psem2m
export PYTHONPATH=$PSEM2M_GIT/python/psem2m.forker/src:$PSEM2M_GIT/python/psem2m.base/src

export PSEM2M_HOME=$PWD/psem2m.home
export PSEM2M_BASE=$PWD/$1
echo "Done"

# -d : Debug ON, Empty : Debug OFF
DEBUG=-d

echo "Running the platform..."
python -m controller $2 $DEBUG

