#!/bin/bash
echo "Current system : $(uname)";

if [ $(uname) == "Darwin" ]
then 
	./controller.py force-stop && (ps -ef | egrep '/bin/java|/bin/python' | egrep 'psem2m' | awk '{print $1}' | xargs kill);
else
	./controller.py force-stop && (ps | egrep 'java|python' | awk '{print $1}' | xargs kill);
fi