.. Définition d'une composition

Définition de PSEM2M Composer
#############################

Description
***********

Une composition est la description d'un ensemble de composants, pouvant
être regroupés en *components sets* - ou *composets*.
Chaque composant possède un certain nombre de propriétés permettant de
mettre en place des liens vers ses dépendances ou permettant d'influencer son
comportement.


PSEM2M Composer est un outil se chargeant de mettre en place une composition
décrite dans un modèle de composition, défini dans la section
:ref:`modele-compo`.


Un des grands principes de PSEM2M Composer est de séparer la résolution des
compositions, gérée par le compositeur, et l'instanciation des composants, prise
en charge par les agents.
Afin de permettre une implémentation la plus libre possible de ces éléments, le
dialogue entre le compositeur et les agents se fait à l'aide de signaux.
De cette manière, les agents peuvent être développés hors OSGi, voire être
implémentés dans des langages différents.


Le compositeur
==============

C'est ce service qui va analyser et résoudre un modèle de composition.
Le compositeur commence toujours par demander aux agents quels composants ils
peuvent traiter.
En fonction de ces réponses et des propriétés de chaque composant, il choisit
quel agent doit prendre en charge son instanciation.

De plus, il est à l'écoute des signaux de composition afin de connaître l'état
réel des capacités d'instanciation des isolats et de l'apparition ou disparition
des composants qu'il doit gérer.

Le compositeur n'a aucune notion de l'implémentation des agents avec lesquels
il communique et ne fait que leur transmettre des tableaux de chaînes de
caractères ou des ensembles de descriptions de composants en se basant sur le
mécanisme de signaux.


Les agents
==========

Les agents ont pour charge de répondre aux requêtes du compositeur et
d'instancier les composants qui leurs sont demandés.

L'implémentation des agents est libre. Seuls les signaux indiqués dans la
section :ref:`signaux-compo` doivent être impérativement pris en compte.

Tout ce qui concerne la gestion des références entre chaque composants et leur
cycle de vie est à la discretion de l'implémentation de l'agent.


.. _modele-compo:

Modèle de composition
*********************

Ce modèle décrit un ensemble de *composets* contenant des sous-*composets* et
des composants.


*Components Sets*
=================

Les *composets*, ou groupes de composants,  ont les propriétés suivantes :

.. tabularcolumns:: |p{2.5cm}|p{3.5cm}|p{9cm}|

+---------------+---------------------------+--------------------------------+
| Propriété     | Type                      | Description                    |
+===============+===========================+================================+
| Name          | String                    | Nom du *composet*              |
|               |                           | (**Obligatoire**)              |
+---------------+---------------------------+--------------------------------+
| Parent        | *composet*                | Lien vers le *composet* parent |
+---------------+---------------------------+--------------------------------+
| Components    | Map String -> *component* | Association du nom de chaque   |
|               |                           | composant fils avec sa         |
|               |                           | description                    |
+---------------+---------------------------+--------------------------------+
| ComponentSets | Map String -> *composet*  | Association du nom de chaque   |
|               |                           | *composet* fils avec           |
|               |                           | sa description                 |
+---------------+---------------------------+--------------------------------+


Les *composets* sont purement virtuels et ne servent qu'au compositeur.
Les agents n'ont pas connaissance de ces groupes.


*Components*
============

Chaque *component*, ou composant, peut avoir les propriétés suivantes :

.. tabularcolumns:: |p{2.5cm}|p{3.5cm}|p{9cm}|

+------------+----------------------+-----------------------------------------+
| Propriété  | Type                 | Description                             |
+============+======================+=========================================+
| Name       | String               | Nom du composant (**Obligatoire**)      |
+------------+----------------------+-----------------------------------------+
| ParentName | String               | Nom du *composet* parent                |
|            |                      | (**Obligatoire**)                       |
+------------+----------------------+-----------------------------------------+
| Type       | String               | Nom de l'isolat devant héberger le      |
|            |                      | composant                               |
+------------+----------------------+-----------------------------------------+
| Properties | Map String -> String | Dictionnare de propriétés               |
+------------+----------------------+-----------------------------------------+
| Filters    | Map String -> String | Association d'une référence à un filtre |
|            |                      | de sélection de **service** cible       |
+------------+----------------------+-----------------------------------------+
| Wires      | Map String -> String | Association d'une référence à           |
|            |                      | un nom de *composant* cible             |
+------------+----------------------+-----------------------------------------+

.. raw:: latex

   \bigskip


.. important::

   Si une référence est décrite à la fois dans *Wires* et dans *Filters*, ce
   sera l'association décrite dans **Wires** qui sera prise en compte lors de
   la préparation des références du composant.


Les descriptions des composants sont transmises dans les signaux du compositeur
vers ses agents, afin que ces derniers puissent les instancier.

De fait, ces descriptions doivent être transmises dans un format utilisable
par l'agent et transmissibles par le système de signaux.


.. _signaux-compo:

Signaux de composition
**********************

Comme indiqué plus haut, les dialogues entre le compositeur et les agents
se base sur le mécanisme des signaux.

Toute implémentation d'un compositeur ou d'un agent doit répondre aux signaux
suivants :


.. |pca| replace:: /psem2m-composer-agent

.. tabularcolumns:: |p{4cm}|p{1.6cm}|p{1.8cm}|p{8.6cm}|

+---------------------------------------+-------------------------+-------------+-------------------------------------------------------------------------------+
| Signal                                | Données                 | Émetteur    | Description                                                                   |
+=======================================+=========================+=============+===============================================================================+
| |pca|/request/can-handle-components   | Tableau de *components* | Compositeur | Demande si l'agent peut instancier les composants donnés                      |
+---------------------------------------+-------------------------+-------------+-------------------------------------------------------------------------------+
| |pca|/response/can-handle-components  | Tableau de *components* | Agent       | Réponse contenant le sous-ensemble des composants que l'agent peut instancier |
+---------------------------------------+-------------------------+-------------+-------------------------------------------------------------------------------+
| |pca|/request/instantiate-components  | Tableau de *components* | Compositeur | Demande à l'agent d'instancier les composants donnés                          |
+---------------------------------------+-------------------------+-------------+-------------------------------------------------------------------------------+
| |pca|/response/instantiate-components | Map                     | Agent       | Résultat de l'instanciation :                                                 |
|                                       |                         |             |                                                                               |
|                                       |                         |             | * composite : Nom du *composet* racine (String)                               |
|                                       |                         |             | * instantiated : Noms des composants instanciés (String[])                    |
|                                       |                         |             | * failed : Noms des composants en éched (String[])                            |
+---------------------------------------+-------------------------+-------------+-------------------------------------------------------------------------------+
| |pca|/request/stop-components         | Tableau de *components* | Compositeur | Demande à l'agent de détruire les composants donnés                           |
+---------------------------------------+-------------------------+-------------+-------------------------------------------------------------------------------+
| |pca|/component-changed               | Map                     | Agent       | L'agent notifie le changement d'état d'un composant                           |
|                                       |                         |             |                                                                               |
|                                       |                         |             | * name : Nom du composant (String)                                            |
|                                       |                         |             | * state : Nouvel état (ECompositionEvent : START, STOP, REMOVE)               |
+---------------------------------------+-------------------------+-------------+-------------------------------------------------------------------------------+
| |pca|/factory-state/added             | Tableau de Strings      | Agent       | L'agent indique qu'il peut gérer de nouveaux types de composants              |
+---------------------------------------+-------------------------+-------------+-------------------------------------------------------------------------------+
| |pca|/factory-state/removed           | Tableau de Strings      | Agent       | L'agent indique qu'il ne peut plus gérer certains types de composants         |
+---------------------------------------+-------------------------+-------------+-------------------------------------------------------------------------------+
| |pca|/factory-state/all-gone          | Aucune                  | Agent       | L'agent indique qu'il ne peut plus gérer de composants (arrêt de l'agent)     |
+---------------------------------------+-------------------------+-------------+-------------------------------------------------------------------------------+

