#!/bin/bash

# Default Monitor shell port
DEFAULT_PORT=8001

# Get the port
if [ -z "$1" ]
then
    port=$(echo shells | nc localhost 8001 | grep cohorte.internals.monitor | cut -d "|" -f 5 | tr -d ' ')
else
    port="$1"
fi

# Validate it
int_regex='^[0-9]+$'
if ! [[ $port =~ $int_regex ]]
then
    echo "Not a number: $port"
    port=$DEFAULT_PORT
fi

echo "Connecting to localhost:$port..."
rlwrap nc localhost $port

