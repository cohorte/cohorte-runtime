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

from (**alternatif**)

   Indique un fichier de configuration où trouver un tableau JSON contenant des
   configurations d'isolats.
   Ce fichier peut également en inclure d'autres.

   .. attention:: Les cycles dans les imports de fichiers de configuration ne
      sont pas surveillés: ils entraineront l'arrêt de l'application.

   Lorsque *from* est utilisé, aucune autre entrée n'est nécessaire.

   La valeur de ce champ est une chaîne de caractère JSON contenant un nom ou
   un chemin (relatif ou absolu) vers un fichier.

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

bundles (**obligatoire**, peut être une liste vide)

   La liste des modules à importer dans un isolat. Ceux-ci seront installés
   dans l'ordre indiqué dans le fichier.

   La valeur de ce champ est une liste de définitions de modules, décrites dans
   la section :ref:`config-bundles`.

httpPort (**obligatoire**)

   Le port de communication principal de l'isolat. Ce port est utilisé pour la
   transmission de signaux entre isolats d'une même application, ainsi que
   pour les appels de services distants entre isolats.
   Chaque isolat travaille sur un port dédié.

   La valeur de ce champ est un numéro de port entier.

host (*optionnel*)

   Le nom d'hôte indique le nom d'hôte ou l'IP à utiliser pour se connecter à
   cet isolat.
   Il est préférable d'utiliser un nom d'hôte plutôt qu'une adresse IP, afin
   que chaque isolat utilise la même configuration.
   Ce champ de configuration n'est utile que dans les applications PSEM2M
   réparties sur plusieurs machines.

   La valeur de ce champ est une chaîne de caractères JSON.

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

.. _config-bundles:

Configuration des modules
=========================

Les modules (*bundles*) peuvent avoir diverses significations, selon le type de
l'isolat auquel ils appartiennent.

Les champs à utiliser pour configurer un module sont les suivants :

from (**alternatif**)

   Indique un fichier de configuration où trouver un tableau JSON contenant des
   configurations d'isolats.
   Ce fichier peut également en inclure d'autres.

   .. attention:: Les cycles dans les imports de fichiers de configuration ne
      sont pas surveillés: ils entraineront l'arrêt de l'application.

   Lorsque *from* est utilisé, seul les entrées *overriddenProperties* et
   *override* sont prises en compte.

   La valeur de ce champ est une chaîne de caractère JSON contenant un nom ou
   un chemin (relatif ou absolu) vers un fichier.

symbolicName (**obligatoire**)

   Le nom symbolique du module à installer.
   Dans le cas des isolats OSGi (Java), il correspond au nom symbolique du
   bundle à installer.
   Dans le cas des isolats Pelix (Python), il correspond à un nom de module
   accessible depuis l'interpréteur.

   La valeur de ce champ est une chaîne de caractère, généralement ne contenant
   pas de caractères d'espacement.

optional (*optionnel*)

   Drapeau indiquant si la présence de ce module est absolument nécessaire à
   l'exécution de l'isolat.
   Si le drapeau vaut *vrai* (*true*), l'isolat continuera de fonctionner si ce
   module est absent ou s'il disparait. Le module sera ré-installé dès que
   possible.

   La valeur de ce champ est un booléen.

properties (*optionnel*)

   Un ensemble de propriétés associées au module.
   Techniquement, ces propriétés sont attribuées à la plateforme hôte, les
   bundles les retrouvant par le mécanisme standard de la plateforme ou par
   héritage des propriétés.

   La valeur de ce champ est un objet JSON, ayant des chaînes de caractères
   pour clés et valeurs.

overriddenProperties (*optionnel*)

   Un ensemble de propriétés associées au module, mettant à jour celles
   indiquées dans les modules chargés à l'aide de *from*.
   Les propriétés ajoutées à ce niveau sont ignorées, seules les propriétés
   définies avec le module peuvent être mise à jour.
   Ce champ n'a aucun effet s'il est utilisé autrement qu'avec *from*.

   La valeur de ce champ est un objet JSON, ayant des chaînes de caractères
   pour clés et valeurs.


override (*optionnel*)

   Ce champ permet de remplacer la définition de tout ou partie des modules
   importés par *from*.
   Il permet notamment de remplacer l'intégralité des propriétés d'un
   module défini dans le fichier importé.
   Ce champ n'a aucun effet s'il est utilisé autrement qu'avec *from*.

   Le contenu de ce champ est un tableau JSON de définitions de modules.


Exemple : Configuration du moniteur
===================================

L'exemple le plus complet est la configuration du moniteur Java de PSEM2M.
Le premier fichier à voir est *psem2m-application.js*, qui est la racine de la
configuration d'une instance PSEM2M.

Fichier *psem2m-application.js*
-------------------------------

Ce fichier sera lu par le service de configuration du démarreur PSEM2M.
Étant donné qu'il configure une instance de la plateforme PSEM2M, il est
recommandé de placer ce fichier dans le dossier **$PSEM2M_BASE/conf**.

.. code-block:: javascript
   :linenos:
   
   {
    "appId":"sample-app",
    "isolates":[
        {
            "from":"monitor.js"
        },
        {
            "from":"forker.js"
        },
        {
            "from":"data-server.js",
            "overriddenProperties":{
                "org.psem2m.remote.filters.include":"org.psem2m.demo.*"
            }
        }
    ]
   }

La configuration ci-dessus indique que :

* le nom de l'application, c'est-à-dire de l'instance de PSEM2M est *sample-app*
* les isolats moniteur et forker sont déclarés dans des fichiers à part
* l'isolat data-server est également déclaré dans un fichier à part, mais sa
  propriété *org.psem2m.remote.filters.include* est ajoutée ou remplacée.

Un fichier décrivant un isolat est décrit dans la section suivante.


Fichier *monitor.js*
--------------------

Le fichier *monitor.js* décrit, généralement, la configuration du moniteur
central d'une instance PSEM2M.
Une version utilisable par défaut est présente dans le dossier
**$PSEM2M_HOME/conf**, mais il est tout à fait possible d'utiliser une version
spécifique à une instance en la plaçant dans le dossier **PSEM2M_BASE/conf**.
Il est également possible de décrire cet isolat dans n'importe quel autre
fichier.

.. code-block:: javascript
   :linenos:

   {
    "id":"org.psem2m.internals.isolates.monitor-1",
    "kind":"felix",
    "httpPort":9000,
    "vmArgs":[
        "-Xms32M",
        "-Xmx64M"
    ],
    "bundles":[
        {
            "symbolicName":"org.psem2m.isolates.ui.admin",
            "optional":true,
            "properties":{
                "psem2m.demo.ui.viewer.top":"0scr",
                "psem2m.demo.ui.viewer.left":"0scr",
                "psem2m.demo.ui.viewer.width":"0.25scr",
                "psem2m.demo.ui.viewer.height":"0.66scr",
                "psem2m.demo.ui.viewer.color":"SkyBlue"
            }
        },
        {
            "symbolicName":"org.apache.felix.shell.remote",
            "properties":{
                "osgi.shell.telnet.port":"6000"
            }
        },
        {
            "from":"signals-http.js"
        },
        {
            "from":"jsonrpc.js"
        },
        {
            "from":"remote-services.js",
            "overriddenProperties":{
                    "org.psem2m.remote.filters.exclude":"*demo.*"
            },
            "override":[{
               "symbolicName":"org.psem2m.isolates.remote.importer",
               "properties":{
                    "org.psem2m.isolates.remote.importer.excludes":"org.psem2m.demo.*"
                }
            }]
        }
    ]
   }

La configuration ci-dessus indique que le moniteur est :

* un isolat interne : son identifiant commence par *org.psem2m.internals*,
* de type *felix* : c'est un isolat Java/OSGi basé sur la plateforme d'Apache,
* en écoute sur le port TCP 9000,

On voit également que l'entrée *vmArgs* est utilisée pour contrôler la
consommation mémoire de la machine virtuelle Java.

Vient ensuite (une partie de) la liste des modules à installer dans l'isolat.

* Le *bundle* contenant l'interface graphique d'administration est déclaré
  optionnel : toute erreur lors de son installation ou de son démarrage sera
  ignorée. De cette manière, cette configuration peut être utilisée sans
  modification sur des machines sans interface graphique.
* Une propriété est associée à la console distante, indiquant sont port d'écoute
* Les modules apportant les couches de signalisation et de services distants
  PSEM2M sont définis dans d'autres fichiers :

  * *signals-http.js* indique les modules à installer pour utiliser
    l'implémentation HTTP du service de signalisation (voir la document
    développeur pour plus d'informations)
  * *jsonrpc.js* contient la liste des modules gérant la sérialisation et la
    dé-sérialisation d'objets Java en JSON.
  * *remote-services.js* ajoute les modules implémentant la spécification
    Remote Services d'OSGi.
    Sa propriété permet d'exclure l'export des services contenant le mode *demo*
    dans leur spécification.
    L'entrée *override* permet d'ajouter une propriété d'exclusion dans le
    module d'import de service, alors qu'elle n'est pas définie dans le fichier
    *remote-services.js*.


Pour terminer cet exemple, la section suivante montre le contenu d'un fichier
de modules importé.


Fichier *signals-http.js*
-------------------------

Les fichiers décrivant les modules sont de simples tableaux d'objets JSON.
L'intérêt est de pouvoir indiquer, dans un même fichier centralisé, l'ordre
nécessaire pour réussir l'installation. d'un module et ses dépendances.

.. code-block:: javascript
   :linenos:
   
   [
    {
        "symbolicName":"org.apache.felix.http.bundle"
    },
    {
        "symbolicName":"org.psem2m.signals.http"
    }
   ]

Dans ce fichier, deux modules sont importés : le service HTTP de Felix et
l'implémentation des signaux HTTP de PSEM2M.

