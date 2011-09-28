.. Problèmes potentiels

Points sensibles
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
`Développez.com <http://rperrot.developpez.com/articles/algo/theorie/graphes/>`_
pourrait simplifier cette détection, mais serait consommatrice de mémoire.

Il existe différentes bibliothèques de gestion de graphes pour Java.
Celle qui semble le plus proche de nos attentes est
`GraphStream <http://graphstream.sourceforge.net/>`_, car elle dispose notamment
d'un algorithme de détection de cycle : ``org.jgrapht.alg.CycleDetector``.


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

Les JARs présents dans la *Target Platform* sont nécessaires à la compilation
des bundles du projet.

De fait, il est impératif que ces fichiers soient visibles par le mécanisme de
dépendances de Maven et doivent donc être installés à l'aide de la commande :

.. code-block:: bash

   mvn install:install-file -Dfile=<path-to-file> -DgroupId=<group-id> \
    -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=<packaging>
   

* ``<path-to-file>`` : chemin absolu ou relatif vers le fichier JAR à installer,
* ``<group-id>`` : groupe de l'artefact Maven,
* ``<artifact-id>`` : identifiant de l'artefact Maven,
* ``<version>`` : numéro de version de projet Maven, au format
  ``<major>.<minor>.<incremental>-<qualifier>``,
* ``<packaging>`` : type de projet Maven (*pom*, *jar*, *bundle*, ...).


Afin de différencier les bundles de chaque *Target Platform* et de ceux présents
sur les dépôts Maven, le plus simple est de leur donner un *group ID*
particulier, dépendant du nom de leur *Target Platform* d'origine.
On pourra par exemple utiliser les *group IDs* ``org.psem2m.target.felix`` et
``org.psem2m.target.equinox``.

L'identifiant de l'artifact devra être le nom symbolique du bundle installé,
afin de le retrouver facilement.

Le numéro de version sera également celui indiqué dans le fichier Manifest.
Il sera cependant nécessaire de convertir le qualificatif Eclipse ``.qualifier``
par sa version Maven ``-SNAPSHOT``.
Dans le cas particulier où un bundle de la *Target Platform* n'a pas de numéro
de version, il faudra lui en attribuer un par défaut : "0.0.0".

Enfin, le paramètre *packaging* devrait prendre comme option *bundle*.
L'impact du choix entre *bundle* et *jar* n'est pas connu.


Hiérarchie et gestion des ressources (*build.properties*)
*********************************************************

Dans les projets Eclipse, le fichier *build.properties* permet d'indiquer des
fichiers à insérer dans le JAR final.
Ces fichiers seront copiés dans l'archive selon leur chemin relatif à la racine
du projet.

Dans le cas de Maven, c'est le dossier ``src/main/resources`` qui contient
directement les ressources non compilables à insérer dans le JAR.

L'une des premières opérations de PSEM2M Compiler sera donc de modifier
l'arborescence du projet traité afin qu'elle corresponde à celle utilisée dans
les projets Maven standards :

::

   - projet Maven
   |-- src
   | |-- main
   | | |-- java (sources Java)
   | | |-- ressources (ressources non compilées)


Ré-utilisation ou génération du Manifest
****************************************

Le plug-in de génération de bundle pour Maven est capable de générer un fichier
Manifest à partir des propriétés indiquées dans le fichier *pom.xml*, mais il
est également capable d'utiliser un Manifest existant.

L'avantage de générer un Manifest depuis le fichier *pom.xml* est de pouvoir
partir sur un fichier neuf, n'ayant pas subit les modifications éventuellement
apportées par des plug-ins Eclipse au cours du développement.

À l'inverse, utiliser le Manifest existant permet de s'assurer que les éléments
de la plateforme compilés avec PSEM2M Compiler ont exactement les mêmes
propriétés que ceux utilisés dans l'environnement de développement.


Les instructions de configuration du traitement du Manifest sont décrites dans
`la documentation de Felix <http://felix.apache.org/site/apache-felix-maven-bundle-plugin-bnd.html>`_.


Instructions de génération de Manifest
======================================

Toutes les propriétés du bloc ``<instructions>`` et dont le nom commence par
une majuscule seront inscrite dans le Manifest généré.

.. code-block:: xml

   <plugin>
     <groupId>org.apache.felix</groupId>
     <artifactId>maven-bundle-plugin</artifactId>
     <extensions>true</extensions>
     <configuration>
       <instructions>
         <Export-Package>com.my.company.api</Export-Package>
         <Private-Package>com.my.company.*</Private-Package>
         <Bundle-Activator>com.my.company.Activator</Bundle-Activator>
       </instructions>
     </configuration>
   </plugin>


Instructions de réutilisation de Manifest
=========================================

La réutilisation se suffit à elle-même :

.. code-block:: xml

   <configuration>
     <instructions>
       <_include>META-INF/MANIFEST.MF</_include>
     </instructions>
   </configuration>

On peut également ajouter ou remplacer des entrées du Manifest existant :

.. code-block:: xml

   <configuration>
     <instructions>
       <_include>src/main/resources/META-INF/MANIFEST.MF</_include>
       <Export-Package>org.example.*</Export-Package>
     </instructions>
   </configuration>


Modèle de fichier projet Maven
******************************

Pour finir, voici à quoi devraient ressembler les fichiers *pom.xml* générés par
PSEM2M Compiler.

Projet Parent
=============

.. literalinclude:: ../_static/pom-root.xml
   :language: xml
   :tab-width: 4
   :linenos:


Modèle de base
==============

.. literalinclude:: ../_static/pom-base.xml
   :language: xml
   :tab-width: 4
   :linenos:


Modèle iPOJO
============

.. literalinclude:: ../_static/pom-ipojo.xml
   :language: xml
   :tab-width: 4
   :linenos:
