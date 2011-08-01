#!/bin/bash

echo "Setting environment variables..."
export PSEM2M_HOME=$PWD/psem2m.home
export PSEM2M_BASE=$PWD/base
echo "Done"


echo "Running the platform..."
/bin/bash $PSEM2M_HOME/bin/psem2m.sh start
