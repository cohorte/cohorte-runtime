.. Documentation

Documentation
#############


Documents de gestion de projet
******************************

.. tabularcolumns:: |p{7cm}|p{2cm}|p{6cm}|

+-----------------------+-------+-------------------------+
| Document              | Phase | Statut                  |
+=======================+=======+=========================+
| Plan Qualité Logiciel | 3     | En cours de réalisation |
+-----------------------+-------+-------------------------+
| Cahier des charges    | 3     | En cours de réalisation |
+-----------------------+-------+-------------------------+
| Plan de développement | 3     | Ébauche                 |
+-----------------------+-------+-------------------------+
| Gestion des risques   | 3     | En cours de validation  |
+-----------------------+-------+-------------------------+


Plan Qualité Logiciel
=====================

Ce document livrable s'inspire du standard ISO 9001, c'est-à-dire qu'il respecte
le contenu de chaque chapitre.

Il doit au moins contenir la description des aspects suivants :

- Responsabilités
- Organisation
- Démarche de développement
- Documentation
- Gestion de la configuration
- Gestion des modifications
- Méthodes, outils et règles
- Suivi de l'application du plan qualité

La responsabilité de la rédaction et de l'application de ce document est à la
charge du responsable qualité.


Cahier des charges
==================

Ce document livrable décrit de manière précise les besoins auxquels doit
répondre le projet.

Il doit contenir la description des aspects suivants :

- Contexte du projet
- Description des besoins
- Description des aspects fonctionnels
- Contraintes de développement et de maintenance
- Facteurs et critères et méthodes de mesure de qualité du logicielle

La rédaction de ce document est sous la responsabilité de l'équipe de
développement.
Son respect est sous la responsabilité du chef de projet.


Plan de développement
=====================

Ce document livrable constitue le planning prévisionnel du projet établi dès
validation du cahier des charges.

Il doit contenir la description des aspects suivants :

- Durées et dates clés du projet
- Décomposition du temps par tâches
- Estimation de la taille du logiciel
- Planning général et détaillé

La responsabilité de la rédaction et de l'application du document est sous la
responsabilité du chef de projet.


Gestion des risques
===================

Ce document livrable décrit les risques auxquels est exposé le projet.
Pour chaque risque défini, il doit en préciser les facteurs possibles, en en
détaillant la probabilité, les méthodes préventives et les méthodes curatives.

Il doit se terminer par un tableau récapitulatif des facteurs de risques et leur
probabilité.

La rédaction et l'application de ce document sont sous la responsabilité de
l'équipe de développement.


Documents techniques de réalisation
***********************************

.. tabularcolumns:: |p{7cm}|p{2cm}|p{6cm}|

+-----------------------+-------+------------+
| Document              | Phase | Statut     |
+=======================+=======+============+
| Dossier de conception | 3     | Inexistant |
+-----------------------+-------+------------+
| Plan de tests         | 3     | Ébauche    |
+-----------------------+-------+------------+
| Outils tests          | 3     | Inexistant |
+-----------------------+-------+------------+
| Configuration         | 3     | Inexistant |
+-----------------------+-------+------------+
| Normes de codage      | 4     | Ébauche    |
+-----------------------+-------+------------+
| Traçabilité du code   | 4     | Inexistant |
+-----------------------+-------+------------+
| Dossier de validation | 5     | Inexistant |
+-----------------------+-------+------------+


Dossier de conception
=====================

Ce document interne représente la structure du projet et de ses composants.

Il doit décrire les aspects suivants :

- Structure globale du projet
- Structure générale de chaque composant

La rédaction de ce document est sous la responsabilité de l'équipe de
développement.
Son respect est sous la responsabilité du chef de projet.


Plan de tests
=============

Ce document interne décrit la suite de tests que le projet doit réussir pour
être considéré valide.
Il définit les tests unitaires, les tests d'intégration et les tests
d'acceptation.

Il doit contenir les éléments suivants :

- Spécification des scénarios de tests
- Moyens et critères d'évaluation
- Critères de réussite

La rédaction et l'application de ce document sont sous la responsabilité de
l'équipe de développement.


Outils tests
============

Ce document interne décrit les outils qui ont dû ou qui devront être développer
afin de réaliser les décrits dans le document précédent.

Pour chaque outil développé, il doit aborder les aspects suivants :

- Description de l'outil
- Manuel d'utilisation
- Exemple d'utilisation
- Structure de l'outil
- Maintenance de l'outil

La rédaction de ce document est sous la responsabilité de l'équipe de
développement des outils de tests.


Configuration
=============

Ce document livrable doit fournir la description précise et complète de la
manière de configurer le projet.

Il doit contenir les éléments suivants :

- Paramètres configurables
- Contraintes du format de fichier de configuration
- Syntaxe du fichier de configuration
- Exemples types

La rédaction de ce document est sous la responsabilité de l'équipe de
développement.


Normes de codage
================

Ce document livrable décrit les règles que doivent suivre les développeur pour
uniformiser le code source du projet.

Pour chaque langage de programmation utilisé, il doit aborder les aspects
suivants :

- Formatage des fichiers

  - Encodage
  - Types de saut de ligne
  - Types d'indentation
  - Commentaire d'en-tête

- Conventions de nommage

  - Modules / Packages
  - Classes
  - Variables
  - Méthodes
  - Arguments

- Format des commentaires de documentation

La rédaction et l'application de ce document sont sous la responsabilité de
l'équipe de développement.


Traçabilité du code
===================

Ce document interne contient toutes les informations sur l'origine de chaque
portion du code source, aussi bien des fichiers sources que de copies de code
d'un autre projet.
Le principe est de savoir qui a fait quoi et quand.

Pour chaque modification de portion de chaque composant du projet, il doit
indiquer :

- La position dans le fichier source
- L'auteur
- La date
- La raison

En cas d'ajout de code externe, il faudra également indiquer :

- Son origine : composant, projet, URL
- Son auteur
- Sa licence

La rédaction de ce document est sous la responsabilité de l'équipe de
développement.
Sa validité est sous la responsabilité du chef de projet.


Dossier de validation
=====================

Ce document livrable contient les résultats complets des jeux de tests décrit
dans le dossier de tests.
Il indique les résultats aux tests de chaque composant.

La rédaction et la validité de ce document sont sous la responsabilité de
l'équipe de développement.


Manuels d'utilisation et d'exploitation
***************************************

.. tabularcolumns:: |p{7cm}|p{2cm}|p{6cm}|

+---------------------------+-------+------------+
| Document                  | Phase | Statut     |
+===========================+=======+============+
| Documentation développeur | 4     | Inexistant |
+---------------------------+-------+------------+
| Documentation utilisateur | 5     | Inexistant |
+---------------------------+-------+------------+


Documentation développeur
=========================

Ce document interne décrit la structure du code source du projet et les
références nécessaires à sa compréhension.
Le but est d'obtenir un document permettant à tout nouveau contributeur de se
repérer facilement dans le code source et d'être capable de le modifier
correctement.

Il doit au moins traiter les aspects suivants :

- Structure sur disque du projet et des modules
- Description de chaque module
- Inter dépendances des modules
- API externe (SDK)
- API interne
- Références nécessaires à la compréhension du code

La rédaction de ce document est sous la responsabilité de l'équipe de
développement.


Documentation utilisateur
=========================

Ce document livrable comporte la description des modalités d'utilisation du
logiciel par l'utilisateur.

Il doit contenir au minimum les instructions de :

- Configuration minimale
- Installation du logiciel
- Configuration de base et valeurs par défaut
- Mise en service
- Maintenance
- Règles à respecter

L'équipe de développement est responsable de la rédaction de ce document.
