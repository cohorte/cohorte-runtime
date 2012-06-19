#!/bin/bash
./controller.py force-stop && (ps | egrep 'java|python' | awk '{print $1}' | xargs kill)
