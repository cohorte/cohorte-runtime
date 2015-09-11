# cohorte.boot

This package contains the modules necessary to boot a Cohorte Isolate, i.e. a process that is part of a Cohorte application.

## cohorte.boot.boot

This is the bootstrap script, which starts a [Pelix/iPOPO](https://ipopo.coderxpress.net) framework and installs bundles according to the arguments it has been given.

## cohorte.boot.constants

Defines the boot-state constants, sent by the booting isolate to the monitor isolate.
