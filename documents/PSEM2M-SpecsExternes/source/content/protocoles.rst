.. Description des protocoles utilisés

Protocoles réseaux
##################

.. _protocole-moniteur:

Administration du moniteur
**************************

Le protocole utilisé pour gérer le moniteur est basé sur XML.
De cette manière, celui-ci peut être spécifié de manière suffisamment précise
pour être implémenté par des applications tierces.

.. todo:: Préparer un fichier XSD spécifiant les requêtes et les réponses

Ce protocole ne peut pas être décrit tant que les fonctionnalités du moniteur
et du forker n'ont pas été clairement arrêtée.

.. _protocole-sondes:

Sondes des isolats
******************

Le protocole dépend du type de sonde utilisé.

Dans le cas de JMX, il existe plusieurs protocoles sous-jacents (RMI, ...), qui
devraient être configurables selon les bundles installés et la politique du
pare-feu local.
