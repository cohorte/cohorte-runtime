.. Configuration de PSEM2M

Configuration de PSEM2M
#######################

La configuration d'une instance de PSEM2M se découpe en trois grandes
catégories :

#. Configuration de démarrage :

   * par exemple, le fichier *platform.framework* indique le nom du fichier JAR
     à utiliser comme framework OSGi pour le moniteur Java.

#. Configuration des isolats :

   * indique la configuration à charger pour chaque isolat lancé, isolats
     internes compris (moniteur, forker)

#. Configuration des compositions :

   * définit les compositions à mettre en place dans une instance PSEM2M
   * nécessite l'installation de PSEM2M Composer Core et Agent


.. _emplacement-config:

Emplacements de configuration
*****************************

Les emplacements des fichiers de configuration d'une instance de PSEM2M sont
résolus par une recherche dans les dossiers PSEM2M selon l'ordre suivant :

#. *$PSEM2M_BASE*/conf
#. *$PSEM2M_BASE*
#. *$PSEM2M_HOME*/conf
#. *$PSEM2M_HOME*
#. Répertoire de travail de l'isolat

Dans le cas des isolats et des compositions, un fichier de configuration peut en
importer un autre afin de regrouper des paramétrages.
La résolution des fichiers importés se fait dans l'ordre suivant :

#. Répertoire du fichier de configuration importateur
#. *$PSEM2M_BASE*/conf
#. *$PSEM2M_BASE*
#. *$PSEM2M_HOME*/conf
#. *$PSEM2M_HOME*
#. Répertoire de travail de l'isolat


Fichier *psem2m.bundles*
************************

Ce fichier n'est pas censé être édité par les utilisateurs de PSEM2M.
Il indique les noms symboliques des bundles devant absolument être installés
dans chaque isolat Java de PSEM2M, dans l'ordre du fichier.

Format
======

Les fichiers *.bundles* sont des fichiers textes en UTF-8, supportant la syntaxe
suivante :

+------------------------------+-----------------------------------------------+
| Ligne                        | Description                                   |
+==============================+===============================================+
| nom.symbolique               | Indication du nom symbolique du bundle à      |
|                              | installer                                     |
+------------------------------+-----------------------------------------------+
| # Commentaire                | Ligne de commentaire                          |
+------------------------------+-----------------------------------------------+
| include: nom_fichier.bundles | Import du fichier indiqué à l'endroit indiqué |
+------------------------------+-----------------------------------------------+

Exemple
=======

.. code-block:: bash
   :linenos:
   
   # ...
   # Base PSEM2M services
   org.psem2m.isolates.base

   # iPOJO Core and Temporal bundles
   include: ipojo.bundles
   # ...


Fichier *platform.framework*
****************************

Il est possible d'indiquer le framework OSGi que doit utiliser le moniteur Java
PSEM2M à l'aide du *platform.framework*.

Format
======

*platform.framework* est un fichier texte en UTF-8 contenant le
**nom du fichier** JAR à utiliser comme framework OSGi.

Le nom peut être un chemin complet ou un simple nom de fichier, il sera
alors recherché dans les répertoires suivants :

#. *$PSEM2M_BASE*/repo
#. *$PSEM2M_BASE*
#. *$PSEM2M_HOME*/repo
#. *$PSEM2M_HOME*
#. Répertoire de travail du script de démarrage

Depuis l'utilisation du démarreur Python, le fichier peut contenir des
commentaires, c'est-à-dire des lignes démarrant par un caractère ``#`` (dièse).
La première du fichier non commentée et non vide est considérée comme étant le
nom du fichier framework à utiliser.

.. note:: Dans la version précédente, utilisant un script Bash, le nom du
   framework était forcément indiqué dans la première ligne du fichier
   *platform.framework*.


Exemple
=======

.. code-block:: bash
   :linenos:
   
   org.apache.felix.main-3.2.2.jar
   # En mettant le commentaire en 2e ligne
   # le fichier reste compatible avec le démarreur Bash


Fichier *psem2m-application.js*
*******************************

Ce fichier est la racine de la configuration des isolats de l'instance PSEM2M
utilisée.
Il n'est censé être présent que dans le dossier de configuration de chaque
instance de PSEM2M (**PSEM2M_BASE**) et non dans le dossier d'installation
central (**PSEM2M_HOME**).

Ce fichier de configuration est lu par le *slave agent*, c'est-à-dire une fois
que tous les bundles décrits dans *psem2m.bundles* ont été installés et démarrés
avec succès.

Format
======

C'est un fichier au format JSON en UTF-8, contenant les clés suivantes :

+----------+---------------------------------------------------------------+
| Clé      | Description                                                   |
+==========+===============================================================+
| appId    | Indique le nom de l'application (chaîne de caractères)        |
+----------+---------------------------------------------------------------+
| isolates | Tableau de configuration des isolats (décrite dans la section |
|          | :ref:`config-isolats`)                                        |
+----------+---------------------------------------------------------------+

Le fichier *psem2m-application.js* supporte le champ ``from`` dans les champs
``isolates`` et ``bundles``, décrits ci-dessous.
Ce champ permet d'importer directement le contenu d'un autre fichier JSON à cet
emplacement, afin de réutiliser des portions de configuration communes.
Il prend pour valeur le nom du fichier à importer, sous forme de chaîne de
caractères JSON; celui-ci sera résolu comme décrit en section
:ref:`emplacement-config`.


.. _config-isolats:

Configuration des isolats
-------------------------

Chaque isolat est décrit par un identifiant, unique pour une application, un
type, un port de communication et une liste de modules.

Les champs à utiliser pour configurer un isolat sont les suivants :

id (**obligatoire**)

   L'identifiant de l'isolat, tel qu'il sera utilisé dans la plateforme.
   Cet identifiant sera utilisé pour définir les noms des fichiers journaux et
   du répertoire de travail de l'isolat, il est donc recommandé qu'il ne
   contienne pas de caractères spéciaux.

   Les identifiants commençant par ``org.psem2m.internals.`` sont réservés à la
   plateforme.

   La valeur de ce champ est une chaîne de caractères JSON.

kind (**obligatoire**)

   Le type de l'isolat, déterminant quelle technique utiliser pour le démarrer
   et le peupler.

   La valeur de ce champ est une chaîne de caractères JSON.

   Les types actuellement supportés sont les suivants :

   +---------+---------------------------------------------------------------+
   | Type    | Description                                                   |
   +=========+===============================================================+
   | felix   | Isolat Java/OSGi utilisant Felix                              |
   +---------+---------------------------------------------------------------+
   | equinox | Isolat Java/OSGi utilisant Equinox (mal supporté, voir        |
   |         | :ref:`pb-equinox`)                                            |
   +---------+---------------------------------------------------------------+
   | python  | Isolat Python 2.x, le premier bundle est exécuté comme module |
   +---------+---------------------------------------------------------------+
   | python3 | Isolat Python 3.x, le premier bundle est exécuté comme module |
   +---------+---------------------------------------------------------------+

   Les spécificités de configuration de chaque type d'isolat sont décrites dans
   les sections correspondantes :

   * Java : :ref:`java-config`
   * Python : :ref:`python-config`

host (*optionnel*)

   Le nom d'hôte indique le nom d'hôte ou l'IP à utiliser pour se connecter à
   cet isolat.
   Il est préférable d'utiliser un nom d'hôte plutôt qu'une adresse IP, afin
   que chaque isolat utilise la même configuration.
   Ce champ de configuration n'est utile que dans les applications PSEM2M
   réparties sur plusieurs machines.

   La valeur de ce champ est une chaîne de caractères JSON.

httpPort (**obligatoire**)

   Le port de communication principal de l'isolat. Ce port est utilisé pour la
   transmission de signaux entre isolats d'une même application, ainsi que
   pour les appels de services distants entre isolats.
   Chaque isolat travaille sur un port dédié.

   La valeur de ce champ est un entier.


vmArgs (*optionnel*)

   Les arguments à passer à la machine virtuelle Java ou à l'interpréteur
   Python. Ces arguments seront donnés avant ceux correspondant au démarrage de
   l'isolat.

   La valeur de ce champ est une liste de chaînes de caractères JSON.

appArgs (*optionnel*)

   Les arguments à passer à l'isolat. Ces arguments seront donnés après ceux
   correspondant au démarrage de l'isolat.

   La valeur de ce champ est une liste de chaînes de caractères JSON.

environment (*optionnel*)

   Les variables à ajouter à l'environnement d'exécution de l'isolat.
   Les variables d'environnement réservées à PSEM2M ne seront pas prises en
   compte.

   La valeur de ce champ est un objet JSON, ayant des chaînes de caractères
   pour clés et valeurs.

bundles (**obligatoire**, peut être une liste vide)

   La liste des modules à importer dans un isolat.

   La valeur de ce champ est une liste de définitions de modules, décrites dans
   la section :ref:`config-bundles`.

.. _config-bundles:

Configuration des modules
=========================


Exemples
========

Configuration des compositions
******************************
