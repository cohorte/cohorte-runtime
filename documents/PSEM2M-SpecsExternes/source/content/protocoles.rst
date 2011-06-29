.. Description des protocoles utilisés

Protocoles réseaux
##################

.. _protocole-moniteur:

Administration du moniteur
**************************

Le moniteur sera administré via JMX, celui-ci exposant un certain nombre de
méthodes au système, comme indiqué dans la section :ref:`interfaces-moniteur`.
L'administration distante sera activée à l'aide des options correspondantes de
la JVM.

.. _protocole-sondes:

Sondes des isolats
******************

Le protocole dépend du type de sonde utilisé.

Dans le cas de JMX, il existe plusieurs protocoles sous-jacents (RMI, ...), qui
devraient être configurables selon les bundles installés et la politique du
pare-feu local.
