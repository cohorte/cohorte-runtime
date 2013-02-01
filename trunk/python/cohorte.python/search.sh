#!/bin/zsh

# Load common variables
. ./common.sh

# Lines before & after matching lines
BEFORE=4
AFTER=4

# Search !
for file in $COHORTE_BASE/var/**/log/LogService-*.txt
do
    echo "Reading... $file"
    grep --color -i -n -B "$BEFORE" -A "$AFTER" "$1" $file
    echo "\n"
done

