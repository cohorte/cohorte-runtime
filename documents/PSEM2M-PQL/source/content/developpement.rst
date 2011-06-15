.. Démarche de développement

Démarche de développement
#########################


Phase 1 : Documentation, étude des outils
*****************************************

Cette phase consiste à étudier, valider et documents les outils et bibliothèques
potentiellement utilisés pour la suite du développement.


Activités
=========

- Sélection des outils disponibles pour gérer le développement du projet
- Sélection des outils disponibles pour gérer le cycle de vie du projet
- Sélection des outils et bibliothèques disponibles pour les besoins potentiels
  du projet
- Analyse et tri des outils sélectionnés
- Documentation des outils sélectionnés


Documents produits en entrée de phase
=====================================

Néant


Documents produits
==================

.. tabularcolumns:: |p{5cm}|p{11cm}|

+-------------------------+---------------------------------------------+
| Document                | Description                                 |
+=========================+=============================================+
| Outillage – Comparaison | Comparaison des outils sélectionnés, avec   |
|                         | justification des choix par indication des  |
|                         | avantages et inconvénients de chacun.       |
+-------------------------+---------------------------------------------+
| Outillage – Outils      | Documentation sur l'utilisation des outils  |
|                         | sélectionnés.                               |
+-------------------------+---------------------------------------------+
| Outillage – Transaction | Documentation sur les outils de transaction |
|                         | sélectionnés.                               |
+-------------------------+---------------------------------------------+


Condition de passage à la phase 2
=================================

La sélection des outils doit être terminée et justifiée.
La documentation des outils peut être incomplète lors du passage à la phase 2.


Phase 2 : Étude de solutions techniques pour la création d'isolats
******************************************************************

Cette phase consiste à rechercher toutes les méthodes possibles pour créer des
isolats.
La durée de cette phase est indéterminée, de nouvelles techniques pouvant
apparaître en cours de recherche.


Activités
=========

- Rechercher différentes techniques pour créer des isolats
- Rechercher une technique de secours, fonctionnant dans tous les cas
- Rechercher des techniques performantes répondant à certains cas


Documents produits en entrée de phase
=====================================

.. tabularcolumns:: |p{5cm}|p{11cm}|

+-----------------+---------------------------------------------------+
| Document        | Description                                       |
+=================+===================================================+
| Isolats – Tests | Description des méthodes de tests d'une technique |
+-----------------+---------------------------------------------------+


Documents produits
==================

.. tabularcolumns:: |p{5cm}|p{11cm}|

+-----------------+-------------------------------------------------------------+
| Document        | Description                                                 |
+=================+=============================================================+
| Isolats – Tests | Document mis à jour avec les tests créés durant la phase et |
|                 | avec le récapitulatif des résultats de chaque technique     |
+-----------------+-------------------------------------------------------------+
| Isolats – XXX   | Description de la technique XXX et résultats de ses tests   |
+-----------------+-------------------------------------------------------------+


Condition de passage à la phase 3
=================================

La technique de secours doit être validée.
Au moins une technique de création d'isolat doit être validée, implémentée et
documentée.


Phase 3 : Conception
********************

Cette phase consiste à la conception complète du projet PSEM2M.


Activités
=========

- Analyse
- Définition du calendrier
- Conception de la plateforme
- Création des jeux de test
- Création des outils d'exécution des jeux de test


Documents produits en entrée de phase
=====================================

.. tabularcolumns:: |p{5cm}|p{11cm}|

+--------------------------------+-----------------------------------------------+
| Document                       | Description                                   |
+================================+===============================================+
| PSEM2M – Cahier des charges    | Mise à jour du cahier des charges en          |
|                                | fonctions des informations acquises lors des  |
|                                | phases précédentes                            |
+--------------------------------+-----------------------------------------------+
| PSEM2M – Plan de développement | (Re)Définition du calendrier de développement |
+--------------------------------+-----------------------------------------------+


Documents produits
==================

.. tabularcolumns:: |p{5cm}|p{11cm}|

+-----------------------------------+------------------------------------------+
| Document                          | Description                              |
+===================================+==========================================+
| PSEM2M – Dossier(s) de conception | Document décrivant la structure à        |
|                                   | développer                               |
+-----------------------------------+------------------------------------------+
| PSEM2M – Plan de tests            | Description des tests à utiliser pour    |
|                                   | valider chaque composant du projet       |
+-----------------------------------+------------------------------------------+
| PSEM2M – Outils tests             | Document décrivant les outils qui auront |
|                                   | été écrits afin d'exécuter des tests     |
|                                   | particuliers                             |
+-----------------------------------+------------------------------------------+
| PSEM2M – Configuration            | Définition des méthodes de configuration |
|                                   | du produit fini                          |
+-----------------------------------+------------------------------------------+


Condition de passage à la phase 4
=================================

Les documents produits doivent être validés.
Les jeux de tests doivent être prêts à être exécutés.


Phase 4a : Développement prototype
**********************************

Cette phase est facultative et correspond à l'écriture du projet en utilisant la
méthode de secours de création d'isolat.


Activités
=========

- Définition d'une norme de codage
- Développement des différents composants du projet
- Exécution des tests unitaires
- Rédaction du manuel développeur


Documents produits en entrée de phase
=====================================

.. tabularcolumns:: |p{5cm}|p{11cm}|

+---------------------------+-------------------------------------------------+
| Document                  | Description                                     |
+===========================+=================================================+
| PSEM2M – Normes de codage | Définition des normes d'écriture de code devant |
|                           | être respectées, pour chaque langage de         |
|                           | programmation utilisé                           |
+---------------------------+-------------------------------------------------+


Documents produits
==================

.. tabularcolumns:: |p{5cm}|p{11cm}|

+------------------------------------+-----------------------------------------+
| Document                           | Description                             |
+====================================+=========================================+
| PSEM2M – Dossier de conception     | Mise à jour du document avec l'état des |
|                                    | composants.                             |
+------------------------------------+-----------------------------------------+
| PSEM2M – Documentation développeur | Document décrivant la structure du code |
|                                    | source afin de guider d'éventuels       |
|                                    | contributeurs                           |
+------------------------------------+-----------------------------------------+
| PSEM2M – Configuration             | Mise à jour des possibilité de          |
|                                    | configuration                           |
+------------------------------------+-----------------------------------------+
| PSEM2M – Traçabilité               | Suivi du code source et origine du code |
|                                    | externe                                 |
+------------------------------------+-----------------------------------------+


Condition de passage en phase 5
===============================

Tous les composants sont écrits, fonctionnels et correspondent à leur
description dans le dossier de conception.
Tous les tests unitaires doivent réussir.


Phase 4b : Développement
************************

Le projet final sera écrit durant cette phase.
Si la phase 4a a eu lieu, les composants communs seront directement réutilisés.


Activités
=========

- Définition d'une norme de codage
- Développement des différents composants du projet
- Exécution des tests unitaires
- Rédaction du manuel développeur


Documents produits en entrée de phase
=====================================

.. tabularcolumns:: |p{5cm}|p{11cm}|

+---------------------------+-------------------------------------------------+
| Document                  | Description                                     |
+===========================+=================================================+
| PSEM2M – Normes de codage | Définition des normes d'écriture de code devant |
|                           | être respectées, pour chaque langage de         |
|                           | programmation utilisé                           |
+---------------------------+-------------------------------------------------+


Documents produits
==================

.. tabularcolumns:: |p{5cm}|p{11cm}|

+------------------------------------+-----------------------------------------+
| Document                           | Description                             |
+====================================+=========================================+
| PSEM2M – Dossier de conception     | Mise à jour du document avec l'état des |
|                                    | composants.                             |
+------------------------------------+-----------------------------------------+
| PSEM2M – Documentation développeur | Document décrivant la structure du code |
|                                    | source afin de guider d'éventuels       |
|                                    | contributeurs                           |
+------------------------------------+-----------------------------------------+
| PSEM2M – Configuration             | Mise à jour des possibilité de          |
|                                    | configuration                           |
+------------------------------------+-----------------------------------------+
| PSEM2M – Traçabilité               | Suivi du code source et origine du code |
|                                    | externe                                 |
+------------------------------------+-----------------------------------------+


Condition de passage en phase 5
===============================

Tous les composants sont écrits, fonctionnels et correspondent à leur
description dans le dossier de conception.
Tous les tests unitaires doivent réussir.


Phase 5 : Qualification
***********************

Cette phase consiste à valider le comportement du projet


Activités
=========

- Exécution de la totalité des tests définis en phase 3
- Rédaction du manuel utilisateur


Documents produits en entrée de phase
=====================================

Néant


Documents produits
==================

.. tabularcolumns:: |p{5cm}|p{11cm}|

+------------------------------------+------------------------------------------+
| Document                           | Description                              |
+====================================+==========================================+
| PSEM2M – Dossier de validation     | Résultat de l'exécution des tests        |
+------------------------------------+------------------------------------------+
| PSEM2M – Documentation utilisateur | Document décrivant comment configurer et |
|                                    | exécuter le projet                       |
+------------------------------------+------------------------------------------+


Condition de fin de phase
=========================

Tous les tests doivent réussir.
En cas d'échec, le projet revient en phase 4.

Si la phase 4b n'a pas encore eu lieu, le projet passe en phase 4b.
