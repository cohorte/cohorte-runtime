.. Configuration des compositions

Utilisation des compositions
############################

Principes généraux
******************

Outils
======

Les compositions sont gérées par PSEM2M Composer, un ensemble de modules Java
et Python séparés en deux catégories :

* le *composer* : il gère les compositions qui lui sont données en donnant des
  ordres aux agents, suivant les informations qu'ils fournissent.
  Actuellement, il ne peut y avoir qu'un *composer* par instance de PSEM2M,
  généralement placé dans le moniteur central de l'instance PSEM2M.

* les *agents* : ils signalent au *composer* quels types de composants ils
  peuvent prendre en charge et exécutent ses ordres d'instanciation et d'arrêt
  de composants.
  Actuellement, il ne peut y avoir qu'un agent par isolat, mais celui-ci peut
  travailler avec plusieurs gestionnaires de composants.


Composition
===========

La composition est un ensemble de composants et de *composets*.

Tous les *composets* et composants ont un nom complet unique. Le nom complet
d'un composant ou d'un *composet* est calculé en joignant les noms de chaque
aïeul et le nom de l'élément par un point.

Par exemple, un composant ``bar`` ayant pour *composet* père ``foo`` dans la
composition ``toto`` aura pour nom : ``toto.foo.bar``.

Les *composets* (*components sets*) sont des groupes virtuels de composants,
permettant de décrire une composition verticale.
Les *composets* peuvent contenir à la fois des composants et d'autres
*composets*.

Les composants sont liés entre eux par des fils (*wires*) ou des filtres
(*filters*).
Le type de la dépendance représentée par un fil est défini dans le type du
composant la requérant (voir la documentation d'iPOJO et d'iPOPO).
Si le filtre d'un fil est vide et qu'il n'y a pas de cible explicite, seul le
type de la dépendance est une contrainte pour sélectionner la dépendance à
injecter.
Les cibles explicites (*wires*) de chaque fil sont forcément d'autres
composants, tandis que les filtres (*filters*) permettent au composant d'être
lié à n'importe quel service de la plateforme.

Chaque composant peut avoir des propriétés qui lui sont propres.

Modules requis
**************

Communs
=======

Tous les modules de PSEM2M Composer dépendent du module API, regroupant tous
les types et toutes les constantes nécessaires de l'outil.

Tous les isolats utilisant un élément de PSEM2M Composer doivent avoir installé
le module suivant au préalable :

* org.psem2m.composer.api

Composer Core
=============

Configuration
-------------

Pour pouvoir fonctionner, le *composer core* a besoin d'un service de
configuration.
L'implémentation de base, supportant le format de configuration JSON est fournie
par le module :

* org.psem2m.composer.config

Si la composition est décrite par un ensemble de fichiers XML, suivant la
spécification SCA, il faudra également installer les modules suivants :

* org.psem2m.libs.xerces
* org.psem2m.sca.converter

Core
----

Le *composer core* n'est disponible qu'en version Java/OSGi et ne peut donc être
installé que dans les isolats de type *felix* ou *equinox*.

Le *composer core* est disponible en un seul module :

* org.psem2m.composer.core


Composer Agent
==============

Il existe deux modules d'implémentations de l'agent :

* une version Java/OSGi, utilisable dans les isolats *felix* et *equinox* :

  * org.psem2m.composer.agent

* une version Python/Pelix, utilisable dans les isolats *pelix* :

  * base.composer

Il est déconseillé d'instancier des composants dans l'isolat contenant le
*composer core*.
Par conséquent, il n'est pas recommandé d'installer un agent dans l'isolat
content le *composer core*.


Configuration
*************

La configuration au format JSON est la plus proche du modèle interne du
*composer*.

Le lecteur de configuration SCA ne supporte que les composants dont
l'implémentation est PSEM2M. Les autres composants sont ignorés.

Format JSON (PSEM2M)
====================

La racine d'une composition est forcément un *composet*. Son nom doit être
unique dans une instance de PSEM2M.

Composet
--------

Un *composet* est défini par les champs suivants :

name (**obligatoire**)

   Le nom local du *composet*. Le nom complet (identifiant) du *composet* est
   calculé à partir du nom de chaque aïeul du *composet* et de son nom.

   La valeur de ce champ est une chaîne de caractères JSON, ne contenant pas
   de caractères d'espacement, ni de caractères spéciaux.

components (*optionnel*)

   Contient les définitions des composants de l'actuel *composet*.

   La valeur de champ est un tableau JSON contenant des définitions de
   composants.

composets (*optionnel*)

   Contient les définitions des *composets* fils de l'actuel.

   La valeur de champ est un tableau JSON contenant des définitions de
   *composets*.

Bien que les entrées *components* et *composets* soient facultatives, au moins
l'une d'entre elles doit être présente pour qu'un *composet* soit valide.


Composant
---------

Un composant est décrit par les entrées suivantes :

name (**obligatoire**)

   Le nom local du composant. Le nom complet (identifiant) du composant est
   calculé à partir du nom de chaque aïeul du composant et de son nom.

   La valeur de ce champ est une chaîne de caractères JSON, sans caractères
   spéciaux ni d'espacement.

type (**obligatoire**)

   Le type du composant.
   Dans le cas des agents iPOJO et iPOPO, il s'agit du nom de la fabrique
   (*factory*) permettant d'instancier le composant.

   La valeur de ce champ est une chaîne de caractères JSON. Les caractères
   autorisés dépendent du gestionnaire de composants sous-jacent.

isolate (*optionnel*)

   Le nom de l'isolat sur lequel doit être instancié le composant.

   La valeur de ce champ est une chaîne de caractères JSON, sans caractères
   spéciaux ni d'espacement.

   .. note:: Dans les prochaines versions du *composer*, ce champ deviendra un
      tableau indiquant les isolats sur lesquels le composant pourra être
      instancié.

properties (*optionnel*)

   Un ensemble de propriétés spécifiques au composant à instancier.

   La valeur de ce champ est un objet JSON, ayant des chaînes de caractères
   pour clés et valeurs.

filters (*optionnel*)

   Un ensemble de d'associations d'identifiants de dépendance avec un filtre.
   Le contenu du filtre dépendant du gestionnaire de composants sous-jacent.
   Dans les cas d'iPOJO et d'iPOPO, les filtres seront au format LDAP.

   Un filtre vide signifie qu'il n'y a aucune limitation pour sélectionner le
   composant cible.

   La valeur de ce champ est un objet JSON, ayant des chaînes de caractères
   pour clés et valeurs.

wires (*optionnel*)

   Un ensemble de d'associations d'identifiants de dépendance avec un nom de
   composant cible.
   Le nom du composant peut être relatif (recherché dans les *composets* fils
   puis dans le *composet* père) ou absolu.

   La valeur de ce champ est un objet JSON, ayant des chaînes de caractères
   pour clés et valeurs.


Exemple
-------

Un exemple simple d'une composition ayant pour nom ``sample_app``, avec la
hiérarchie suivante :

* ``sample_app`` : la racine de la composition

  * ``hello-world-provider`` : un *composet*

    * ``hello-provider`` : un composant fournisseur de service

  * ``hello-world-consumer`` : un *composet*

    * ``hello-consumer`` : un composant consommateur

  * ``hello-provider`` : un composant fournisseur de service

.. todo:: Utiliser graphviz pour Sphinx..

.. code-block:: javascript
   :linenos:

   {
    "name":"sample_app",
    "composets":[
        {
            "name":"hello-world-provider",
            "components":[
                {
                    "name":"hello-provider",
                    "type":"hello-generator",
                    "isolate":"isolate-1",
                    "filters":{
                        "logger":""
                    },
                    "properties":{
                        "default-name":"Mr Anderson"
                    }
                }
            ]
        },
        {
            "name":"hello-world-consumer",
            "components":[
                {
                    "name":"hello-consumer",
                    "type":"hello-consumer-factory",
                    "isolate":"isolate-2",
                    "filters":{
                        "logger":""
                    },
                    "wires":{
                        "hello-svc":"hello-provider"
                    }
                }
            ]
        }
    ],
    "components":[
        {
            "name":"hello-provider",
            "type":"hello-generator",
            "isolate":"isolate-1",
            "filters":{
                "logger":""
            },
            "properties":{
                "default-name":"Mrs Robinson"
            }
        }
    ]
   }


Les deux fournisseurs de services ont le même nom local, mais des noms complets
différents :

* ``sample_app.hello-provider``
* ``sample_app.hello-world-provider.hello-provider``

Le consommateur sera lié prioritairement au composant le plus proche,
c'est-à-dire son *cousin*, ``sample_app.hello-world-provider.hello-provider``.

.. todo:: À vérifier (au cas où...)


Format SCA
==========

Une composition SCA est décrite par un ensemble de fichiers XML portant
l'extension *.composite*. Les liens entre les fichiers se font à travers
les espaces de noms XML.

Le standard SCA a été défini l'`OSOA <http://www.osoa.org/>`_ puis par
l'`OASIS <http://www.oasis-open.org/>`_.

.. note:: La spécification du modèle SCA **était** disponible sur le site de
   l'OSOA jusque février 2012.
   À l'heure de la rédaction de ce document, le site de
   l'`OSOA <http://www.osoa.org/>`_ a disparu et redirige automatiquement vers
   le site de l'OASIS, qui est encore incomplet.

La spécification SCA est disponible sur le site de l'OASIS :
`OASIS Service Component Architecture <http://www.oasis-open.org/committees/tc_home.php?wg_abbrev=sca-assembly>`_

Le convertisseur SCA vers PSEM2M Composer est capable de gérer une composition
hiérarchisée, c'est-à-dire contenant des *composites*, ainsi que de résoudre
les liens décrits par des promotions de services et de références.

Extensions SCA PSEM2M
---------------------

Les extensions SCA sont définies dans l'espace de nom XML
``http://www.psem2m.org/ns/psem2m-sca``.

Actuellement, un seul élément a été ajouté aux types du standard SCA :

``implementation.psem2m``

   Indique que l'implémentation du composant SCA décrit est un composant que
   doit gérer PSEM2M.

   Les attributs de cette extension sont :

   +------------------------+--------+-------------------------------------+
   | Attribut               | Type   | Description                         |
   +========================+========+=====================================+
   | type (**obligatoire**) | string | Indique le type du composant à      |
   |                        |        | instancier (la fabrique dans le cas |
   |                        |        | d'iPOJO ou iPOPO)                   |
   +------------------------+--------+-------------------------------------+
   | isolate (*optionnel*)  | string | Indique dans quel isolat devra être |
   |                        |        | instancié le composant              |
   +------------------------+--------+-------------------------------------+


Exemple
-------

Les extraits suivants montrent la racine d'une composition SCA et un fichier
décrivant l'un des *composites*.

Le composant *entry* est implémenté par un *composite*, et ses références
``next`` et ``second`` doivent être reliées à ``normal`` et ``fallback``,
respectivement.

Dans le cas présenté, il n'est pas nécessaire d'utiliser les extensions SCA dans
le fichier racine, car il ne décrit aucun composant implémenté autrement que par
un composite standard.

À l'inverse, le *composite* ``entry`` décrit deux composants :

* ``entry-point`` est implémenté par un composant PSEM2M de type ``test-entry``
  et doit s'exécuter sur l'isolat 2.
  Le composant est configuré à l'aide des propriétés SCA (``nbIterations``).

* ``obi-wan_Kenobi`` est également implémenté par un composant PSEM2M,
  de type ``fall-back``, et doit s'exécuter sur l'isolat 1.

Les liens entre composants sont définis par des références (*csa:reference*) ou
des liens directs (*csa:wire*) vers des services exportés par des composants
ou promus pour être visibles sur le *composite* parent.

Fichier racine *application.composite* :

.. code-block:: xml
   :linenos:
   
   <?xml version="1.0" encoding="UTF-8"?>
   <csa:composite xmlns:csa="http://docs.oasis-open.org/ns/opencsa/sca/200912"
    xmlns:entry="app/entry" xmlns:erp-caller="app/erp-caller"
    xmlns:fallback="app/fallback"
    xmlns:normal="app/normal"
    name="application"
    targetNamespace="http://eclipse.org/sca/src/application">
     <csa:component name="entry">
       <csa:implementation.composite name="entry:entry"/>
       <csa:service name="IComponent"/>
       <csa:reference name="next"/>
       <csa:reference name="second"/>
     </csa:component>
     <csa:component name="normal">
       <csa:implementation.composite name="normal:normal"/>
       <csa:service name="IComponent"/>
       <csa:reference name="next"/>
     </csa:component>
     <csa:component name="fallback">
       <csa:implementation.composite name="fallback:fallback"/>
       <csa:service name="IComponent"/>
     </csa:component>
     <csa:component name="erp-caller">
       <csa:implementation.composite name="erp-caller:erp-caller"/>
       <csa:service name="IComponent"/>
     </csa:component>
     <csa:service name="IComponent" promote="entry/IComponent"/>
     <csa:wire source="entry/next" target="normal/IComponent" />
     <csa:wire source="entry/second" target="fallback/IComponent" />
     <csa:wire source="normal/next" target="erp-caller/IComponent" />
   </csa:composite>


Fichier *entry.composite* :

.. code-block:: xml
   :linenos:
   
   <?xml version="1.0" encoding="UTF-8"?>
   <csa:composite xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:csa="http://docs.oasis-open.org/ns/opencsa/sca/200912"
    xmlns:psem2m="http://www.psem2m.org/ns/psem2m-sca"
    name="entry"
    targetNamespace="app/entry">
     <csa:component name="entry-point">
       <csa:service name="IComponent">
         <csa:interface.java interface="org.psem2m.composer.test.api.IComponent"/>
       </csa:service>
       <csa:reference name="next"/>
       <csa:reference name="logger"/>
       <csa:property name="nbIterations" value="20"/>
       <psem2m:implementation.psem2m type="test-entry" isolate="isolate-2"/>
     </csa:component>
     <csa:service name="IComponent" promote="entry-point/IComponent"/>
     <csa:component name="obi-wan_Kenobi">
       <csa:service name="IComponent">
         <csa:interface.java interface="org.psem2m.composer.test.api.IComponent"/>
       </csa:service>
       <csa:reference name="next"/>
       <csa:reference name="second"/>
       <psem2m:implementation.psem2m type="fall-back" isolate="isolate-1"/>
     </csa:component>
     <csa:reference name="next" multiplicity="1..1" promote="obi-wan_Kenobi/next"/>
     <csa:reference name="second" multiplicity="1..1" promote="obi-wan_Kenobi/second"/>
     <csa:wire source="entry-point/next" target="obi-wan_Kenobi/IComponent"/>
   </csa:composite>


Utilisation du Composer
***********************

Chargement d'une composition
============================

Service de configuration
------------------------

Le chargement d'une composition s'effectue en dehors du Composer Core, à l'aide
des implémentations de la spécification
``org.psem2m.composer.config.IComposerConfigHandler``, ayant l'API suivante :

+---------------------+-------------------------------------------------------+
| Méthode             | Description                                           |
+=====================+=======================================================+
| canHandle(*String*) | Teste si le nom de fichier donné peut être chargé     |
+---------------------+-------------------------------------------------------+
| load(*String*)      | Retourne la composition décrite dans le fichier donné |
+---------------------+-------------------------------------------------------+


Exemple
-------

Pour rappel, les services du Composer Core ne sont implémentés qu'en Java, et ne
sont accessibles que dans l'isolat dans lequel ils sont installés.

L'exemple suivant permet de charger un fichier indiqué dans la variable
``aFileName``, du moment qu'un chargeur puisse le gérer.

.. code-block:: java
   :linenos:
   
   IComposerConfigHandler configReader = null;
   for (final IComposerConfigHandler reader : pConfigReaders) {
       if (reader.canHandle(aFileName)) {
           // Un lecteur de configuration a été trouvé pour ce fichier
           configReader = reader;
           break;
       }
   }

   if (configReader == null) {
       /* Aucun lecteur trouvé pour ce fichier */
       return;
   }

   // Chargement de la composition
   final ComponentsSetBean compoSet = configReader.load(aFileName);
   if (compoSet == null) {
       /* Erreur à la lecture du fichier */
       return;
   }

   // Enregistrement de la composition 
   instantiateComponentsSet(compoSet);


Cycle de vie d'une composition
==============================

La prémière étape pour utiliser le Composer est de récupérer une référence vers
le service exposé par Composer Core. Celui a pour spécification
``org.psem2m.composer.IComposer`` (voir la JavaDoc pour avoir une description
complète de l'API du service).
Le service n'est pas exporté et n'est donc accessible que dans l'isolat où est
il est déployé. Un module tiers peut être développé facilement pour diffuser
le service à tous les isolats d'une instance PSEM2M.

.. todo:: Expliquer rapidement comment utiliser le service Composer pour :

   * démarrage de composition
   * arrêt de composition (pas encore fait)
   * mise à jour de composition (pas encore fait)
