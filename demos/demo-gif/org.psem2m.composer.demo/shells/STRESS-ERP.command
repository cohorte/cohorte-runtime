#!/bin/bash
#
# Launcher of PSEM2M "stress test 4 "
#
# @author Olivier Gattaz -- isandlaTech.com
#


cd /Users/ogattaz/workspaces/PSEM2M_SDK_git/psem2m/demo-gif/org.psem2m.demo.stresser/src



#  -h, --help            show this help message and exit
#  --host=HOST           Data server host name
#  --data-port=PORT      Data server port
#  --erp-port=PORT       ERP port
#  --nb-processes-1=NUMBER
#                        Number of scenario 1 processes
#  --nb-iter-1=NUMBER    Number of scenario 1 iterations in each process
#  --nb-processes-2=NUMBER
#                        Number of scenario 2 processes
#  --nb-iter-2=NUMBER    Number of scenario 2 iterations in each process
#  --use-scenario-3      Use the scenario 3 : ERP on/off toggling
#  --nb-iter-3=NUMBER    Number of scenario 3 iterations in each process
#  --delay-scenario-3=TIME
#                        Delay the scenario 3 of TIME seconds
#  --think-min-3=TIME    Minimum think time for scenario 3, in seconds
#  --think-max-3=TIME    Maximum think time for scenario 3, in seconds
#  --use-scenario-4      Use the scenario 4 : Quarterback on/off toggling
#  --nb-iter-4=NUMBER    Number of scenario 4 iterations in each process
#  --delay-scenario-4=TIME
#                        Delay the scenario 4 of TIME seconds
#  --think-min-4=TIME    Minimum think time for scenario 4, in seconds
#  --think-max-4=TIME    Maximum think time for scenario 4, in seconds



/usr/bin/python main.py --nb-processes-1=0 --nb-processes-2=0 --use-scenario-4 --nb-iter-4=2 --delay-scenario-4=4 --think-min-4=4 --think-max-4=4
