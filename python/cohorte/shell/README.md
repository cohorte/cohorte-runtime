# Cohorte shell commands

The modules in this package provide commands for the Pelix Shell.

## Agent

Installed in all isolates, this bundle uses the agents from the other isolates to return access details.

The commands are in the ``shell`` namespace:

* ``pids [<uid or name>]``: prints the PID (process ID) of all the isolates of the application;
* ``shells [<uid or name> [<kind of shell>]]``: prints the port to access the remote shells of each isolate of the application. As a reminder, a Java isolate has two remote shells: one in Python (Pelix Shell) and one in Java (Gogo Shell);
* ``http [<uid or name>]``: prints the HTTP port of each isolate.

## Composer Node

This bundle is installed in monitor isolates. The commands it provides are in the ``node`` namespace:

* ``nodes``: lists the nodes of the *Node Composer* services visible by this isolate;
* ``isolates [<node>]``: lists the isolates running on the nodes of the visible *Node Composer* services, as well as the components they host.

## Composer Top

This bundle is installed in the monitor isolate hosting the *Top Composer*. The commands it provides are in the ``top`` namespace:

* ``read [<filename> [<base>]]``: reads and prints a composition file, to test the configuration reader (imports, comments, JSON parsing, ...);
* ``dist [<filename> [<base>]]``: reads and parses the given composition file, then computes and prints a node distribution;
* ``load [<filename> [<base>]]``: instantiates the composition from the given file and prints its UID;
* ``dump``: prints the content of the *Top Composer* status;
* ``stop <uid>``: stops the composition with the given UID.

## Forker

This bundle is installed in monitor isolates. The commands it provides are in the ``forker`` namespace:

* ``stop <uid or name>``: stops the isolate(s) with the given UID or name;
* ``ping <uid or name>``: pings the isolate(s) with the given UID or name and prints its state;
* ``shutdown``: shuts down the whole application
