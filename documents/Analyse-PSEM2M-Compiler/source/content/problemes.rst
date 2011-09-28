.. Problèmes potentiels

.. _Dev.net - Théorie des graphes: http://rperrot.developpez.com/articles/algo/theorie/graphes/
.. _GraphStream: http://graphstream.sourceforge.net/

Points critiques
################

Analyse des fichiers Manifest.mf
********************************

L'analyse du fichier Manifest.mf se fait en trois temps :

#. Lecture du fichier Manifest. En Java, ceci est fait directement à l'aide de
   la classe Manifest.

#. Lecture des champs *utiles*. PSEM2M Compiler aura principalement besoin
   d'analyser des entrées telles que ``Import-Package``, ``Export-Package``
   pour préparer le graphe de dépendance.

   Ces entrées sont dans un format particulier, défini par le standard OSGi, et
   peuvent être analysées avec
   ``org.apache.felix.framework.util.manifestparser.ManifestParser``.
   L'utilisation de cette classe rendra l'outil dépendant de Felix.

#. Analyse des relations de dépendances entre le bundle en cours de traitement
   et le graphe de dépendance actuel.


Graphe de dépendances
*********************

Le graphe de dépendance est un graphe orienté devant représenter les relations
d'imports et d'exports entre les bundles.

Ce graphe se basera sur les entrées des fichiers Manifest suivantes :

* ``Import-Package``, liste des packages Java importés avec quelques
  informations supplémentaires :

  * ``version`` : indique un numéro ou un domaine de version de package
  * ``resolution`` : vaut ``optional`` si le package est optionnel.

* ``Require-Bundle``, liste des noms symboliques de bundles à importer.
  Plus rare que ``Import-Package`` mais tout aussi standard, il dispose
  également d'informations supplémentaires :

  * ``bundle-version`` : indique un numéro ou un domaine de version de bundle
  * ``resolution`` : vaut ``optional`` si le bundle est optionnel.
  * ``visibility`` : vaut ``reexport`` si le bundle doit être ré-exporté par
    l'actuel. Cette information est à conserver dans le Manifest final, mais
    n'a pas vraiment d'intérêt dans notre graphe de dépendance.

* ``Export-Package``, liste des packages Java exportés par le bundle.
  Chaque package peut indiquer un numéro de version :

  * ``version`` : indique un **et un seul** numéro de version de package.


Gestion des cycles
==================

Étant donné que l'outil n'a pas de contrôle complet sur ses entrées, il est
possible que le graphe de dépendance généré contienne des cycles (boucles).

La détection des cycles dans un graphe est un problème en soi.
L'utilisation d'une matrice d'adjacence, telle que décrite dans
`Dev.net - Théorie des graphes`_ pourrait simplifier cette détection, mais
serait consommatrice de mémoire.

Une bibliothèque de gestion et de visualisation de graphes pour Java est
disponible ici : `GraphStream`_.
Elle dispose notamment d'un algorithme de détection de cycle :
``org.jgrapht.alg.CycleDetector``.


Gestion des numéros de version
==============================

Trois cas description de version sont possibles lors de la validation d'un
import :

* Pas de version indiquée, ou une version spéciale "0.0.0" : n'importe quelle
  version peut être importée

* Un numéro de version complet doit correspondre : "1.0.0".
  Les qualificatifs ne doivent pas être pris en compte lors du test :
  "1.0.0.201109281528" est valide pour "1.0.0" mais pas pour "1.0.1".

* Un domaine de version : la version exportée doit appartenir au domaine
  importé.
  Les bornes du domaine peuvent être inclusives (``[``, ``]``) ou exclusives
  (``(``, ``)``).

  Exemples :

  * ``[1.0.0,2.0.0)`` : 1.0.0 correspond, pas 2.0.0
  * ``[2.5.0, 2.5.4]`` : 2.5.0 et 2.5.4 sont dans le domaine
  * ``(3.0.0, 3.1.0)`` : les versions de 3.0.1 à 3.0.n correspondent
  * ``(0.0.0, 1.0.0]`` : les versions de 0.0.1 à 1.0.0 correspondent


Le standard OSGi défini une **classe** ``org.osgi.framework.Version``,
implémentant l'interface ``Comparable``, permettant ainsi de représenter et de
comparer des numéros de version.


Target Platform : Définition des artefacts Maven
************************************************


Gestion des ressources incluses (*build.properties*)
****************************************************


Modèle de fichier projet Maven
******************************

Modèle de base
==============


Modèle iPOJO
============
