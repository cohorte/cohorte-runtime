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


Conversion automatisée
**********************

Un projet possible de PSEM2M serait de convertir automatiquement des classes
Java possédant des annotations SCA en composants iPOJO.

Pour cela, il faudrait utiliser la table de conversion suivante, en ignorant
les annotations non gérées.

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


Références
**********

* `SCA Resources  <http://osoa.org/display/Main/SCA+Resources>`_

  * `Introducing SCA <http://www.davidchappell.com/articles/Introducing_SCA.pdf>`_

* `Service Component Architecture Specifications <http://osoa.org/display/Main/Service+Component+Architecture+Specifications>`_

  * `SCA Assembly Model V1.00 <http://osoa.org/download/attachments/35/SCA_AssemblyModel_V100.pdf?version=1>`_
  * `SCA Java Common Annotations and APIs V1.00 <http://osoa.org/download/attachments/35/SCA_JavaAnnotationsAndAPIs_V100.pdf?version=1>`_
