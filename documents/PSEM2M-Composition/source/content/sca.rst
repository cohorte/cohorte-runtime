.. Comparatif avec SCA

Compatibilité avec SCA
######################

Capacités communes ou proches
*****************************

Liens entre composants
======================

Dans le modèle SCA, on lie :

* une référence, dont le nom est celui du membre de la classe Java implémentant
  le composant sources
* un service, dont le nom est utilisé pour retrouver son instance si le
  composant cible est implémenté par un composite


Dans le modèle PSEM2M, on associe :

* une source, dont le nom est l'identifiant de la référence iPOJO ou le nom du
  membre de la classe implémentant le composant
* une cible, dont la valeur est le nom du composant exportant le service attendu


Propriétés des composants
=========================

Il est possible de valuer des membres d'un composant si ceux-ci sont déclarés
comme étant des propriétés, à l'aide d'une annotation *@Property* ou
équivalente.


Autowire SCA
============

*Autowire* est une capacité de SCA permettant de ne pas indiquer explicitement
à quel service doit être lié une référence.

Il s'agit du comportement de PSEM2M, via iPOJO, quand aucune indication n'est
donnée pour un membre d'un composant ni dans *wire*, ni dans *filters*.


.. note:: 

   PSEM2M Composer se base sur des fournisseurs de contrôle des composants.
   C'est à eux de gérer les injections de dépendances.
   Pour le moment, seul un fournisseur de composants iPOJO est disponible,
   laissant iPOJO gérer les références.


Validation des liens
====================

La validation des liens entre composants est disponible pour les modèles :

* Dans le cas de SCA, il est possible de charger une représentation du modèle
  et de valider qu'une référence est bien liée à un service correctement
  défini, y compris si celui-ci à été promu, à condition qu'elle ne soit pas
  marquée comme étant résolue à l'exécution.

* Dans le cas de PSEM2M Composer, cette validation est possible pour tous les
  liens décrits avec l'option *wires*.
  Les liens décrits avec l'options *filters* ou non décrits, quant à eux, ne
  peuvent être validés qu'à l'exécution lors de la résolution des références.

On voit que les modèles ont deux niveaux de validation possibles, la base
statique de la composition pouvant être validée par simple lecture, la base
dynamique ne pouvant l'être qu'une fois les services de l'environnement
d'exécution mis en place.


Capacités spécifiques à PSEM2M Composer
***************************************

Modèle *à plat*
===============

Le modèle de PSEM2M Composer se base sur les composants. Les *composets* ne
sont là que pour regrouper des ensembles de composants et les placer dans un
espace de nommage.

.. note:: 

   La gestion des promotions de service, présentée plus bas comme une
   spécificité de SCA, devrait être implémentée dans PSEM2M Composer sans
   modifier ce modèle à plat.
   En effet, la promotion n'entrainera pas de hierarchisation des compositions,
   mais un ajout de propriétés sur les services des composants.


Filtres sur les références
==========================

Au lieu d'être liés à des services définis explicitement, les références de
PSEM2M Composer peuvent décrire leur cible à l'aide d'un filtre LDAP sur les
propriétés du service attendu.


.. note::

   Le format de filtre, ici LDAP, est le seul actuellement utilisé. Il est
   possible que ce format devienne dépendant de l'agent devant démarrer
   le composant et plus particulièrement de son langage d'implémentation
   (cas des agents Python, ...) et des capacités de sa plateforme de gestion
   des composants.


Export automatique de service
=============================

Les services exportés par les composants sont automatiquement exportés par
l'agent de PSEM2M Composer.
De cette manière, les composants peuvent se parler même s'ils sont dans des
processus différents voire sur des machines différentes.

En SCA, cela reviendrait à placer manuellement l'annotation *@Remotable* sur
toutes les interfaces de services.


Capacités spécifiques à SCA
***************************

Promotion
=========

Un service d'un composant peut être exporté vers le composite parent avec une
promotion.
Il en va de même avec les références.

Les promotions permettent d'avoir un modèle SCA récursif, considérant qu'un
composant peut être implémenté par un composite.


.. note:: 

   La gestion de la promotion de service fait partie des perspectives
   d'évolution de PSEM2M.
   
   Il est également prévu de pouvoir décrire un lien d'une référence vers
   un composet plutot que vers un composant, pour pouvoir décrire un modèle
   récursif. La spécification d'un tel lien n'est pas encore prête.


Politiques
==========

SCA permet de définir des politiques de sécurité sur les composants.
Voir la description *policySets* dans le standard SCA.


.. note:: 

   La gestion des politiques n'est pas prévue dans PSEM2M Composer.


Binding explicite
=================

Par défaut, quand le *binding* n'est pas explicitement indiqué, le runtime SCA
se charge d'exporter les services de ses composants pour qu'ils puissent
dialoguer entre eux même s'ils sont dans des processus ou sur des machines
différentes.
PSEM2M Composer a également cette capacité avec l'export automatique de service.

Cependant, un composant SCA peut également avoir une configuration spécifique,
lui permettant d'exportant un service selon une certaine configuration.
Il est recommandé de fournir un fichier WSDL décrivant cet export.


.. note:: 

   Ce comportement peut être émulé dans PSEM2M Composer en utilisant les
   propriétés du composant à exporter. En ajoutant les propriétés correspondant
   aux Remote Services, et plus particulièrement au fournisseur correspondant
   au binding souhaité.
   Actuellement, seul le fournisseur JSON-RPC est disponible.
   Un founisseur de Web Services SOAP fait partie des perspectives d'évolution
   de PSEM2M.


*Aspect* d'un composant
=======================

Un composant peut avoir une description aspect, donnant des informations à son
conteneur :

* *OneWay* : l'opération ne renvoie pas de valeur, son appel peut ne pas être
  bloquant
* *Scope* : défini dans la section suivante
* *Callback* : définit une interface bi-directionnelle

Ces aspects et leurs valeurs dépendent de l'implémentation et du conteneur SCA
les exécutant.


*Scope* d'un composant
----------------------

Le scope permet de définir le cycle d'utilisation d'un composant, se rapprochant
du cycle de vie des EJB en J2EE.

Il peut être :

* **COMPOSITE** : le composant est un singleton créé la première fois que son
  service est appelé, et dure jusqu'à la fin de l'application

* **CONVERSATION** : l'état d'une instance doit être conservé entre un appel de
  début et un appel de fin de conversation.

* **REQUEST** : le composant est actif tant que le thread traitant **une**
  requête d'un client est actif.

* **STATELESS** (par défaut) : le composant n'a pas d'état, son instance peut
  être réutilisé directement par un autre client. Plusieurs instances peuvent
  être lancées pour gérer plusieurs clients en parallèle.


.. note:: 

   Les composants utilisés dans PSEM2M Composer n'ont pas la notion de session.
   On peut les considérer comme des composants de *scope* **COMPOSITE**.
   Composer devrait être capable d'héberger des composants SCA **COMPOSITE** et
   **STATELESS**, après une conversion telle que décrite dans la section
   suivante.


Conversion automatisée des classes Java
***************************************

Un projet possible de PSEM2M serait de convertir automatiquement des classes
Java possédant des annotations SCA en composants iPOJO.

Pour cela, il faudrait utiliser la table de conversion suivante, en ignorant
les annotations non gérées.

.. tabularcolumns:: |p{3cm}|p{3cm}|p{8cm}|


+------------------------------+-----------------------------------+--------------------------------------------------+
| Annotation SCA               | Annotation iPOJO                  | Commentaires                                     |
+==============================+===================================+==================================================+
| @Service(Class[] interfaces, | @Provides(Class[] specifications) | En SCA, *value* est utilisée si une seule        |
| Class value)                 |                                   | interface est publiée, à la place d'*interfaces* |
+------------------------------+-----------------------------------+--------------------------------------------------+
| @Reference(String name,      | @Requires(String id,              | Opposition de description de référence           |
| boolean required)            | boolean optional)                 | optionnelle. Même comportement autrement         |
+------------------------------+-----------------------------------+--------------------------------------------------+
| @Property(String name,       | @Property(String name,            | Propriété de service, la version                 |
| boolean required)            | boolean mandatory)                | iPOJO peut avoir une valeur par défaut.          |
+------------------------------+-----------------------------------+--------------------------------------------------+
| @Init                        | @Validate                         | Méthode appelée quand le composant est démarré   |
+------------------------------+-----------------------------------+--------------------------------------------------+
| @Destroy                     | @Invalidate                       | Méthode appelée quand le composant est terminé   |
+------------------------------+-----------------------------------+--------------------------------------------------+


Lecture d'une composition SCA
*****************************

Cette section décrit des méthodes possibles pour charger une composition SCA.

Le préfixe ``sca`` peut décrire l'un des espaces de nommage XML suivant, selon
la version du standard SCA utilisée :

.. tabularcolumns:: |p{4cm}|p{12cm}|

+---------+--------------------------------------------------+
| SCA 1.0 | http://www.osoa.org/xmlns/sca/1.0                |
+---------+--------------------------------------------------+
| SCA 1.1 | http://docs.oasis-open.org/ns/opencsa/sca/200912 |
+---------+--------------------------------------------------+

La dernière partie de l'espace de nommage pour SCA 1.1 contenant une date,
celle-ci peut varier en fonction de la période d'écriture du fichier source.


Comment *parser* une composition SCA ?
======================================

La racine d'une composition SCA est un domaine, dans lequel sont déployés les
*composites*.
Un domaine peut être lui-même considéré comme un *composite* logique.


Inclusion de *composites*
-------------------------

Dans les standard SCA, chaque *composite* est décrit dans un fichier XML qui
lui est propre.
Il est possible d'aggréger plusieurs fichiers *.composite* à l'aide de la balise
``<sca:include name="xml_ns:Name" />``; tous les composants des fichiers
inclus se retrouvent alors dans un même *composite* logique.


Gestion des extensions XML
--------------------------

Le standard SCA se base sur les capacités d'extension des schémas XML pour
autoriser la définition de nouveaux noeuds pour décrire de nouveaux types de :

* binding, ayant pour type de base ``sca:Binding``
* interface, ayant pour type de base ``sca:Interface``
* implémentation, ayant pour type de base ``sca:Implementation``


La lecture d'un fichier *.composite* doit se faire avec un *parser* supportant
le standard DOM niveau 3.

Celui-ci définit une méthode ``Element.getSchemaTypeInfo()``, renvoyant un objet
de type ``TypeInfo`` qui permet de tester si le type de l'élément traité hérite
d'un autre type XML, à l'aide de la méthode ``TypeInfo.isDerivedFrom()``.

La bibliothèque Xerces, par exemple, gère le standard DOM niveau 3.


Étapes de lecture
=================

Pour pouvoir lire une composition SCA, il est nécessaire de connaitre :

* les dossiers sources : ces dossiers contiennent les fichiers *.composite*
* le *composite* racine : c'est à partir de ce *composite* que sera chargé
  la représentation SCA.


La représentation d'une composition SCA doit être dynamique, principalement pour
gérer les extensions XML, et doit conserver un maximum d'informations
(type, *namespace* d'une balise ou d'un attribut...).

Pour gagner en simplicité de représentation et d'implémentation, il serait
intéressant de lire une réprésentation SCA en plusieurs étapes :

#. Lire les fichiers *.composite* tels quel et préparer un annuaire conservant
   le nom de chaque composite et celui des composants qu'il contient. Il peut
   également être utile d'y conserver la liste des références et services
   promus.

#. Résoudre les liens entre fichiers (références, services, ...)

#. Effectuer l'inclusion des composites (balise ``include``) et mettre à jour
   les liens entre les fichiers.
   Revenir à l'étape 2 si au moins une inclusion a été faite.

#. Hiérarchisation de la composition :

   Pour chaque composant du composite racine :

      * Si son implémentation est un composite : hiérarchiser ce composite et
        ses composants (récursion); remplacer ce composant par sa réprésentation
        en composite.

      * Sinon, conserver la réprésentation du composant

#. Résolution des liens directs entre références et services dans un annuaire
   à part, afin de ne pas modifier la réprésentation "pure" de la composition
   SCA.


La résolution des liens entre références et services doit prendre en compte les
*wires* de chaque *composite*, ainsi que de l'attribut *target* de chaque
référence.


Modèles de données
==================

Modèle de données
-----------------

Il est nécessaire de conserver toutes les informations des noeuds XML décrivant
les *composites* et les composants.
De cette manière, il est toujours possible de retrouver des attributs non
standards lors du traitement d'un noeud dont le type est une extension de SCA.

Les types à représenter sont décrits ci après.


Composite
^^^^^^^^^

.. tabularcolumns:: |p{3cm}|p{3cm}|p{9cm}|

+------------+-----------+---------------------------------------------+
| Attribut   | Type      | Description                                 |
+============+===========+=============================================+
| components | Liste     | Liste des composants du composite           |
+------------+-----------+---------------------------------------------+
| composites | Liste     | Liste des sous-composites                   |
+------------+-----------+---------------------------------------------+
| name       | String    | Nom du composite                            |
+------------+-----------+---------------------------------------------+
| parent     | Composite | Composite père (nul pour la racine)         |
+------------+-----------+---------------------------------------------+
| properties | Liste     | Liste des propriétés (valeurs par défaut)   |
+------------+-----------+---------------------------------------------+
| references | Liste     | Liste des références promues                |
+------------+-----------+---------------------------------------------+
| services   | Liste     | Liste des services promus                   |
+------------+-----------+---------------------------------------------+
| wires      | Liste     | Liste des liens définis au niveau composite |
+------------+-----------+---------------------------------------------+
| xmlElement | Element   | Element DOM décrivant le composant          |
+------------+-----------+---------------------------------------------+


Composant
^^^^^^^^^

.. tabularcolumns:: |p{3cm}|p{3cm}|p{9cm}|

+----------------+----------------+-------------------------------------------+
| Attribut       | Type           | Description                               |
+================+================+===========================================+
| implementation | Implementation | Implémentation du composant               |
+----------------+----------------+-------------------------------------------+
| name           | String         | Nom du composant                          |
+----------------+----------------+-------------------------------------------+
| parent         | Composite      | Composite père                            |
+----------------+----------------+-------------------------------------------+
| properties     | Liste          | Liste des propriétés (valeurs par défaut) |
+----------------+----------------+-------------------------------------------+
| references     | Liste          | Liste des références promues              |
+----------------+----------------+-------------------------------------------+
| services       | Liste          | Liste des services promus                 |
+----------------+----------------+-------------------------------------------+
| xmlElement     | Element        | Element DOM décrivant le composant        |
+----------------+----------------+-------------------------------------------+


Property
^^^^^^^^

La valeur d'une propriété peut être donnée dans le fichier XML, chargée depuis
un fichier ou par un composite parent.

.. tabularcolumns:: |p{3cm}|p{3cm}|p{9cm}|

+------------+---------+----------------------------------------+
| Attribut   | Type    | Description                            |
+============+=========+========================================+
| mustSupply | Boolean | Si vrai, la propriété doit être valuée |
|            |         | pour que le composant soit valide      |
+------------+---------+----------------------------------------+
| name       | String  | Nom de la propriété                    |
+------------+---------+----------------------------------------+
| value      | String  | Valeur brute (*raw*) de la propriété   |
+------------+---------+----------------------------------------+
| xmlElement | Element | Element DOM décrivant la propriété     |
+------------+---------+----------------------------------------+


Reference
^^^^^^^^^

.. tabularcolumns:: |p{3cm}|p{3cm}|p{9cm}|

+--------------+---------------+----------------------------------------------+
| Attribut     | Type          | Description                                  |
+==============+===============+==============================================+
| bindings     | Liste         | Liste des bindings possibles pour accèder au |
|              |               | service                                      |
+--------------+---------------+----------------------------------------------+
| interface    | Interface     | Interface du service référencé               |
+--------------+---------------+----------------------------------------------+
| multiplicity | EMultiplicity | Multiplicité de la référence                 |
+--------------+---------------+----------------------------------------------+
| name         | String        | Nom de la référence                          |
+--------------+---------------+----------------------------------------------+
| targets      | Liste         | Liste des cibles de la référence             |
+--------------+---------------+----------------------------------------------+
| xmlElement   | Element       | Element DOM décrivant le composite           |
+--------------+---------------+----------------------------------------------+

La multiplicité est valuée par une énumération ``EMultiplicity`` :

* ``0_1`` : *0..1*
* ``1_1`` : *1..1*
* ``0_n`` : *0..n*
* ``1_n`` : *1..n*


Service
^^^^^^^

.. tabularcolumns:: |p{3cm}|p{3cm}|p{9cm}|

+------------+-----------+----------------------------------------------------+
| Attribut   | Type      | Description                                        |
+============+===========+====================================================+
| bindings   | Liste     | Liste des bindings permettant d'accèder au service |
+------------+-----------+----------------------------------------------------+
| interface  | Interface | Interface du service                               |
+------------+-----------+----------------------------------------------------+
| name       | String    | Nom du service                                     |
+------------+-----------+----------------------------------------------------+
| xmlElement | Element   | Element DOM décrivant le composite                 |
+------------+-----------+----------------------------------------------------+


Binding
^^^^^^^

Représente un binding, décrivant l'accès à un service.
Peut être une extension du standard SCA.

.. tabularcolumns:: |p{3cm}|p{3cm}|p{9cm}|

+------------+---------+----------------------------------------+
| Attribut   | Type    | Description                            |
+============+=========+========================================+
| kind       | String  | Nom de l'élément DOM (binding.ws, ...) |
+------------+---------+----------------------------------------+
| xmlElement | Element | Element DOM décrivant le binding       |
+------------+---------+----------------------------------------+


Implementation
^^^^^^^^^^^^^^

Représente une implémentation de composant, peut être une extension du
standard SCA.

.. tabularcolumns:: |p{3cm}|p{3cm}|p{9cm}|

+------------+---------+-------------------------------------------------+
| Attribut   | Type    | Description                                     |
+============+=========+=================================================+
| kind       | String  | Nom de l'élément DOM (implementation.java, ...) |
+------------+---------+-------------------------------------------------+
| xmlElement | Element | Element DOM décrivant l'implémentation          |
+------------+---------+-------------------------------------------------+


Interface
^^^^^^^^^

Représente une interface de service, peut être une extension du standard SCA.

.. tabularcolumns:: |p{3cm}|p{3cm}|p{9cm}|

+------------+---------+--------------------------------------------+
| Attribut   | Type    | Description                                |
+============+=========+============================================+
| kind       | String  | Nom de l'élément DOM (interface.java, ...) |
+------------+---------+--------------------------------------------+
| xmlElement | Element | Element DOM décrivant l'interface          |
+------------+---------+--------------------------------------------+


Références
**********

SCA
===

* `SCA Resources  <http://osoa.org/display/Main/SCA+Resources>`_

  * `Introducing SCA <http://www.davidchappell.com/articles/Introducing_SCA.pdf>`_

* `Service Component Architecture Specifications <http://osoa.org/display/Main/Service+Component+Architecture+Specifications>`_

  * `SCA Assembly Model V1.00 <http://osoa.org/download/attachments/35/SCA_AssemblyModel_V100.pdf?version=1>`_
  * `SCA Java Common Annotations and APIs V1.00 <http://osoa.org/download/attachments/35/SCA_JavaAnnotationsAndAPIs_V100.pdf?version=1>`_


XML
===

* `Create a complex type using XML Schema inheritance <http://www.techrepublic.com/blog/programming-and-development/create-a-complex-type-using-xml-schema-inheritance/759>`_
* `XML DOM TypeInfo <http://xerces.apache.org/xerces2-j/javadocs/api/org/w3c/dom/TypeInfo.html>`_