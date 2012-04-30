.. Installation PSEM2M

Installation de PSEM2M
######################

Pré-requis
**********

Les pré-requis pour installer PSEM2M dépendent des modules et des langages
utilisés.

Systèmes supportés
==================

La plateforme PSEM2M a été testée sur les plateformes suivantes :

* Linux x86
* Linux x86_64
* Mac OS X x86_64
* Windows XP x86

Configuration minimale
======================

.. todo:: La configuration minimale n'a pas encore été calculée


Configuration recommandée
=========================

.. todo:: La configuration recommandée n'a pas encore été calculée

* 2 Go de RAM


Installation
************

Étape 1: Installation de la plateforme
======================================

La plateforme PSEM2M est livrée sous la forme d'une archive par plateforme hôte.
Celle-ci contient toutes les dépendances nécessaires à l'exécution de la
plateforme, contenant une machine virtuelle Java SE 6 et un interpréteur
Python 2.7.

Pour installer la plateforme,

#. décompresser l'archive PSEM2M dans un répertoire quelconque. Il n'est pas
   nécessaire que ce répertoire soit inscriptible par la suite.
#. Enregistrer le chemin vers le répertoire répertoire d'installation de PSEM2M
   (contenant *bin*, *conf*, etc) dans la variable d'environnement
   **PSEM2M_HOME**.

   * Sous Unix, ceci peut être fait dans le script de démarrage du service,
     décrit dans la section suivante.
   * Sous Windows, cela se fait à travers les propriétés systèmes.


Étape 2: Enregistrement du service PSEM2M
=========================================

Sous Unix
---------

.. todo:: Faire modèle de script (ou ré-utiliser starter directement)

Un modèle de script *init.d* est fourni avec les versions Linux et Mac OS X
de PSEM2M.
Il est nécessaire d'avoir un script par instance de PSEM2M exécutée.

Les assignations des variables suivantes doivent être modifiées afin de
correspondre aux dossiers à utiliser :

+-------------+------------------------------------------------+
| Variable    | Description                                    |
+=============+================================================+
| PSEM2M_HOME | Dossier d'installation de la plateforme PSEM2M |
+-------------+------------------------------------------------+
| PSEM2M_BASE | Dossier d'exécution d'une instance de PSEM2M   |
+-------------+------------------------------------------------+


.. note:: Dans l'état actuel de développement de la plateforme, le script
   *init.d* est le script Python permettant le démarrage direct de la
   plateforme, *$PSEM2M_HOME/bin/starter.py*. 


Sous Windows
------------

.. todo:: Faire outil de gestion de service

Dans sa version Windows, la plateforme est livrée avec un outil permettant
d'enregistrer et supprimer des instances PSEM2M dans les services Windows.

.. note:: Actuellement, cet outil n'est pas disponible. Pour démarrer PSEM2M
   sous Windows, il faut utiliser le script Python*$PSEM2M_HOME/bin/starter.py*.
