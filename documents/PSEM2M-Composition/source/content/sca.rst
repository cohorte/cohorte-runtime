.. Comparatif avec SCA

Comparatif avec SCA
###################

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


Capacités spécifiques à PSEM2M Composer
***************************************

Pas de notion de langage d'implémentation
=========================================

Le modèle PSEM2M Composer se base sur la notion de type de composants, ceux ci
étant considérés comme déployés ou prêt à l'être.

En effet, ce modèle se base sur les informations qui sont disponibles à
l'exécution, par les agents du Composer.
Il n'est pas possible de valider une composition PSEM2M sans analyser les
composants à mettre en place, par analyse du code en amont ou par reflexion.


Modèle *à plat*
===============

Le modèle de PSEM2M Composer se base sur les composants. Les *composets* ne
sont là que pour regrouper des ensembles de composants et les placer dans un
espace de nommage.


Filtres sur les références
==========================

Au lieu d'être liés à des services définis explicitement, les références de
PSEM2M Composer peuvent décrire leur cible à l'aide d'un filtre LDAP sur les
propriétés du service attendu.


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

Définition de l'implémentation
==============================

SCA peut être considéré comme un modèle de conception, les interfaces utilisées
et les classes d'implémentation des composants sont indiquées explicitement
dans le fichier de composition.

Ceci permet notamment de valider qu'une composition est *viable*, en vérifiant
que les références des composants sont de types compatibles avec le service
auquel ils sont liés.


Promotion
=========

Un service d'un composant peut être exporté vers le composite parent avec une
promotion.
Il en va de même avec les références.

Les promotions permettent d'avoir un modèle SCA récursif, considérant qu'un
composant peut être implémenté par un composite.


Politiques
==========

SCA permet de définir des politiques de sécurité sur les composants.
Voir la description *policySets* dans le standard SCA.


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

