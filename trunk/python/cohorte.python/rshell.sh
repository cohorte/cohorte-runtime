#!/bin/bash

port=$(echo shells | nc localhost 8001 | grep cohorte.internals.monitor | cut -d "|" -f 5)
rlwrap nc localhost $port

