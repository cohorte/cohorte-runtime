#!/bin/zsh

# Load common variables
. ./common.sh

# Lines before & after matching lines
BEFORE=4
AFTER=4

# Java log files
LOG_FILES=$(echo $COHORTE_BASE/var/**/log/LogService-*.txt)

# Search it
grep --color -i -n -B "$BEFORE" -A "$AFTER" "$1" $LOG_FILES

