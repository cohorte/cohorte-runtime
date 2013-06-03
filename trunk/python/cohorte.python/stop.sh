#!/bin/bash

# Get the PIDs of all running isolates, i.e. started by boot.py
# (-ef output is UID,PID,PPID,...)
pids=$(ps -ef | grep 'cohorte/boot/boot.py' | grep -v 'grep' | awk '{print $2}')

# Check emptyness
if [ -z "$pids" ]
then
    echo "Nothing to kill"
    exit 1
else
    # Kill'em all
    kill $pids && echo "Done" || echo "Error: $?"
fi
exit 0
