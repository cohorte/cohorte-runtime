.. Description de l'implémentation de PSEM2M Compiler

PSEM2M Compiler
###############

PSEM2M Compiler est un outil se chargeant de compiler tout ou partie du code
Java de la plateforme PSEM2M et de regrouper les fichiers JAR en résultant.


Fonctionnement
**************

L'implémentation actuelle de PSEM2M Compiler se présente sous la forme d'une
application Python 2, basée sur une bibliothèque de génération de fichiers Ant,
en Python 2 également.

Le programme fonctionne en deux phases :

#. La bibliothèque AutoBuilder est utilisée pour générer les fichiers Ant de
   base. Pour cela, elle :

   * analyse les fichiers Manifest.mf de chaque projet
   * tri les projets en fonction de leurs dépendances
   * génère un fichier Ant dans chaque dossier projet
   * génère un fichier Ant racine compilant chaque projet dans le bon ordre.

#. Le code spécifique à PSEM2M Compiler modifie ensuite les fichiers Ant de
   chaque projet, en prenant en compte les informations spécifiques à Eclipse :

   * copie des fichiers liés avec un *link source*
   * gestion des dossiers sources multiples
   * gestion du plug-in iPOJO Nature, ajoutant la manipulation du JAR généré
     avant sa copie dans le dossier de destination.

De cette manière, PSEM2M Compiler gère la plupart des cas rencontrés dans les
projets de la plateforme PSEM2M.


Dépendances
***********

PSEM2M Compiler dépend de la bibliothèque
`AutoBuilder <http://empty-set.net/?p=9>`_, une bibliothèque permettant de
générer des fichiers Ant pour compiler un ensemble de projets PDE, en fonction
de leur fichier Manifest.


Perspectives
************

Gestion des bibliothèques tierces
=================================

En plus de gérer les dossiers sources multiples, PSEM2M Compiler pourrait
profiter de sa connaissance du fichier *classpath* utilisé par JDT afin
d'ajouter les bibliothèques tierces dont dépend un projet aux paramètres de
compilation.
