.. DHCP script documentation
.. |dhclient-script| replace:: `dhclient-script(8) <linux.die.net/man/8/dhclient-script>`_

DHCP Script for PSEM2M
######################

Installation
************

Copy the ``dhcp-psem2m-exit`` script or create a link to it in the
``/etc/dhcp/dhclient-exit-hooks.d`` (or ``/etc/dhcp3/dhclient-exit-hooks.d``,
depending of your version) directory.

The script doesn't need to have execution rights as it will be executed in-line
by ``dhclient-script``, using *bash* ``.`` command.

For more information, see |dhclient-script|.


Configuration
*************

In order to make the script work with your installation of PSEM2M, you need
to edit the following variables:

+----------------------+----------------------------------------------------+
| Variable             | Description                                        |
+======================+====================================================+
| DAEMON_RUN           | Complete path to a Python (>= 2.6) interpreter     |
+----------------------+----------------------------------------------------+
| DAEMON_USER          | The name of the user running PSEM2M                |
+----------------------+----------------------------------------------------+
| DAEMON_GROUP         | The name of the group running PSEM2M               |
+----------------------+----------------------------------------------------+
| PSEM2M_INTERFACE     | Interface on which PSEM2M will communicate         |
+----------------------+----------------------------------------------------+
| PSEM2M_NETWORK_ROUTE | IP of the gateway for the PSEM2M interface         |
|                      | (will become the default route)                    |
+----------------------+----------------------------------------------------+
| DAEMONIZER           | Complete path to the ``daemonize.py`` script       |
+----------------------+----------------------------------------------------+
| LOG                  | Path to use to log the output of the daemonizer    |
+----------------------+----------------------------------------------------+
| PSEM2M_LOGFILE       | Path to use to log the output of the PSEM2M Forker |
+----------------------+----------------------------------------------------+
| PSEM2M_HOME          | The PSEM2M Home directory                          |
+----------------------+----------------------------------------------------+
| PSEM2M_BASE          | The PSEM2M instance Base directory                 |
+----------------------+----------------------------------------------------+
| EXTRA_ARGS           | Extra arguments to give to the PSEM2M Controller   |
+----------------------+----------------------------------------------------+

.. important:: All variables to give to the daemonizer or to the controller
   must be declared as exported.


References
**********

* man |dhclient-script|
