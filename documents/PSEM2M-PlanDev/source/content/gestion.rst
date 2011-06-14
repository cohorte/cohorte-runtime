.. Processus de gestion
.. |forkall| replace:: ``forkall()``

Processus de gestion
####################

Estimations du projet
*********************

La partie isolation du projet devra être terminé au mois de septembre, il sera
possible de repousser cette échéance.


Plan de projet
**************

Plan des phases
===============

.. tabularcolumns:: |p{7cm}|p{3cm}|p{3cm}|

+----------------------------------+----------------+----------------+
| Phase                            | Date de début  | Date de fin    |
+==================================+================+================+
| 1 Étude de l'outillage           | 01/09/10       | 29/10/10       |
+----------------------------------+----------------+----------------+
| 2 Étude isolation et |forkAll|   | 25/11/10       | 03/06/11       |
+----------------------------------+----------------+----------------+
| 3 Conception                     | 10/06/11       | 08/07/11       |
+----------------------------------+----------------+----------------+
| 4 Développement de la plateforme | 11/07/11       | Septembre 2011 |
+----------------------------------+----------------+----------------+
| 5 Développement de l'IDE         | Septembre 2011 | Janvier 2012   |
+----------------------------------+----------------+----------------+



Calendrier des phases
=====================

.. todo:: Insérer le planning simplifié des phases


Objectif des phases
===================

Étude de l'outillage
--------------------

Recherche et comparaison des différents outils permettant d'assurer la gestion
des isolats.

Cette recherche concerne principalement les moyens garantissant la transmission
fiable de données entre isolats.


Étude isolation et |forkAll|
----------------------------

Étude des différentes méthodes permettant la création d'isolats (pur Java et
méthodes dépendantes du système d'exploitation).

Tentative de portage de l'appel système |forkAll| de Solaris, non POSIX, sous
Linux.


Conception
----------

Conception de l'architecture du projet, en prenant en compte les résultats des
étapes précédentes et des remarques des experts extérieurs à l'entreprise.


Développement de la plateforme
------------------------------

Phase de développement et tests de la plateforme PSEM2M, cœur du projet.


Développement de l'IDE
----------------------

Développement de l'environnement de développement permettant de développer un
serveur d'échange sur la plateforme PSEM2M.

Il s'agira principalement de développer un certain nombre de plug-ins pour la
plateforme Eclipse.

