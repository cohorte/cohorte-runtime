..comment:


Le serveur d'exécution
**********************

Le principe adopté consiste à installer sur une machine un noyau d'exécution capable de retrouver la configuration et les différents composants constituant l'application devant fonctionner sur ce noeud.

La plate-forme d'exécution fourni des services de bases

- installation

- démarrage

- supervision

- enregistrement de l'activité


Installation
============

Le serveur d'exécution s'installe sur des machines fonctionnant sous Windows , Linux ou Mac OS X à l'aide d'un outil d'installation automatisé. Celui-ci est capable de déposer et configurer le serveur d'exécution en respectant les règles des systèmes d'exploitation ciblés:

- service Windows, structuration des dossiers windows, 

- service Linux, structuration LSB

Pour la cible Android, le serveur d'exécution s'installera via le packaging « apk ».

Pour ne dépendre d'aucun logiciel installé sur la machine cible, le serveur d'exécution embarquera toutes les ressources tierces nécessaire :

- la machine virtuelle adaptée (hors Mac OS X et Android).

- le « wrapper » de service adapté au systèmes d'exploitation hôte.

- L'ensemble des composants de base (cf.
  la « target plateform ») sur lesquels s'appuient les composants propres aux logiciels.


gestion des versions
--------------------

Les numéros de versions majeures avec rupture de compatibilité font partie de d'identifiant des packages pour pouvoir les installer simultanément sur une même machine.

Une organisation utilisatrice sera à même d'installer une version supérieure pour des test de qualification sans altérer la version en production.


Mises à jour
------------

Sur les plateforme Linux , on pourra utiliser les automatisme des plateformes (ex: apt) pour automatiser les mises à jour en utilisant un dépôt local dont on contrôlera le contenu.


Démarrage
=========

Il est possible de lancer plusieurs instances d'une seule installation.
Les variables d'environnement suivantes permettent de fournir :

- PSEM2M_HOME : racine d'installation (ex: /usr/local/psem2m_v1_0)

- PSEM2M_BASE : racine des données d'une instance (ex: /var/psem2m/erp_inflow_controler )

Lors des ses démarrage et arrêts, le serveur d'exécution (l'isolat MASTER) retourne des indications de progression au système d'exploitation hôte, 


Supervision
===========

Le serveur d'exécution (l'isolat MASTER) met un ensemble de métriques à la disposition d'une console de surveillance:

Des agents SNMP sont utilisables par des systèmes de monitoring comme IBM Tivoli pour observer ces même métriques.


Enregistrement de l'activité
============================

Le serveur d'exécution (l'isolat MASTER) produit une « log » d'activité générale au format texte, limitée en volume (ex: 20 fichiers de 10 méga octets).


