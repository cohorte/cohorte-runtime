#!/bin/bash
#
# Launcher of PSEM2M "base-compo"
#
# @author Olivier Gattaz -- isandlaTech.com
#


wRepoPath="/Users/ogattaz/workspaces/PSEM2M_SDK_2012/_REPO_git"

export PSEM2M_BASE="$wRepoPath/platforms/base-compo"

export PSEM2M_HOME="$wRepoPath/platforms/psem2m.home" 

#export PLATFORM_ISOLATE_ID="org.psem2m.internals.isolates.monitor-1"


/usr/bin/python "$wRepoPath/trunk/python/psem2m.forker/src/controller.py" start


exit