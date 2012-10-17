#!/usr/bin/env python
#-- Content-Encoding: UTF-8 --
"""
Simple script to start a daemon with specific user and group IDs.

To use this script, you must call it with the following environment variables
set:

* DAEMON_RUN: complete path to the daemon executable
* DAEMON_USER: name of the user running the daemon (not its ID)
* DAEMON_GROUP: name of the group running the daemon (not its ID)

Arguments and environment given to this script are propagated to the daemon.

:author: Thomas Calmant (thomas.calmant@gmail.com)
:copyright: Copyright 2012, Thomas Calmant
:license: GPLv3
:version: 0.1
"""

import os
import sys
import grp
import pwd

# Daemon executable
daemon = os.getenv('DAEMON_RUN')
if not daemon or not os.path.isfile(daemon):
    print('Invalid daemon executable: {0}'.format(daemon))
    sys.exit(1)

# Get current user IDs
uid = os.getuid()
gid = os.getgid()

# Compute the daemon user ID
username = os.getenv('DAEMON_USER')
if username:
    try:
        uid = pwd.getpwnam(username).pw_uid
    except:
        print('Invalid user name: {0}'.format(username))
        sys.exit(1)

# Compute the daemon group ID, use user name as default
groupname = os.getenv('DAEMON_GROUP', username)
if groupname:
    try:
        gid = grp.getgrnam(groupname).gr_gid
    except:
        print('Invalid group name: {0}'.format(groupname))
        sys.exit(1)

# Change user and group IDs
os.setgroups([gid])
os.setgid(gid)
os.setuid(uid)

# Update user name in environment
# -> Avoids problems when bash loads its profile
os.environ['USERNAME'] = pwd.getpwuid(uid).pw_name

# Remove special environment entries
for key in ('HOME', 'DAEMON_RUN', 'DAEMON_USER', 'DAEMON_GROUP'):
    if key in os.environ:
        del os.environ[key]

# Compute the list of arguments
arguments = [daemon]
arguments.extend(sys.argv[1:])

# Start the daemon
os.spawnv(os.P_NOWAIT, daemon, arguments)
