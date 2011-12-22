.. Définition des services

Services de Remote Services
###########################

Définitions
***********

Un **service** est une instance d'un objet Java implémentant une certaine
interface auquelle sont associées des propriétés.
Un service est dit **exporté** dès lors qu'il est accessible depuis un autre
processus voire une autre machine.
Un service est dit **importé** dès lors que le registre OSGi le considère
présent alors que l'objet fournissant ce service se trouve dans un autre
processus voire sur une autre machine.s

On appelle **End point** le point de connexion qui permet d'utiliser un
service exporté.

On appelle **Proxy** un objet Java généré à l'exécution et implémentant
l'interface du service à importé. Il appelle automatiquement le *end point* du
service importé.
C'est cet objet proxy qui est enregistré dans le registre central OSGi, se
faisant passer pour le service distant avec lequel il communique.


Architecture
************

L'architecture choisie pour gérer les services distants se base sur celle
utilisée pour implémenter le mécanisme de signaux.
Ainsi, PSEM2M Remote Services se découpe en deux types de services :

* les composants de base, se chargeant de découvrir les services à exporter
  et ceux à importer.

* les fournisseurs, ou *providers*, appelés par l'implémentation de base pour
  créer les proxies et les *end points* correspondant aux services à importer
  et exporter.


Composants de base
==================

Remote Services Broadcaster (RSB)
---------------------------------

Ce composant se base sur les signaux pour gérer les évènements concernant les
services distants et les requêtes des autres isolats sur l'état actuel des
exports de l'isolat courant.

Il fournit également le service ``IRemoteServiceBroadcaster`` permettant
d'envoyer une requête de mise à jour de l'état des services à importer, ainsi
que d'envoyer des notifications sur l'état des services exportés.


Remote Services Exporter (RSE)
------------------------------

Ce composant écoute les évènements de la plateforme OSGi et attend l'arrivée
de nouveaux services.
Lorsque cet évènement arrive, le RSE analyse les propriétés des nouveaux
services et exporte tous ceux qui ont les propriétés suivantes :

* ``psem2m.service.export`` : le service est exporté si cette propriété
  spécifique à PSEM2M Remote Services vaut ``true``.
  Le service **ne sera pas exporté** si cette propriété vaut ``false``.
  En cas d'absence, le comportement standard OSGi est suivi.

* ``service.exported.interfaces`` : indique les interfaces à exporter, vaut
  ``*`` si toutes les interfaces d'un service doivent être exportées.

* ``service.exported.configs`` : indique quelles configurations d'export sont
  supportées, vaut ``*`` s'il n'y a pas de contrainte.
  Cette propriété permet notamment de filter les fournisseurs pouvant exporter
  ce service.

Au moins l'une des deux propriétés ``service.exported.interfaces`` ou
``service.exported.configs`` doit être présente pour qu'un service soit exporté.

Le RSE se base sur les fournisseurs pour créer les *end points* correspondant
aux services à exporter.


Remote Services Importer (RSI)
------------------------------

Le RSI se base sur les fournisseurs pour créer les proxies correspondant aux
services à importer.
Il est appelé par le RSB (Broadcaster) lorsqu'un signal de service distant
a été reçu.

Quand un service est importé, ses propriétés sont modifiées. Pour éviter une
boucle d'export de services, les propriétés d'export des services sont
transformées en propriétés d'import.

Ainsi, ``service.exported.interfaces`` devient ``service.imported.interfaces``
et ``service.exported.configs`` devient ``service.imported.configs``.
La propriété ``psem2m.service.export`` n'est pas modifiée, car elle ne provoque
pas l'export du service à elle seule.


Remote Services Repository (RSR)
--------------------------------

Enfin, le composant RSR conserve la liste des *end points* exportés par le
RSE.
C'est lui qui est appelé par le RSB lorsqu'un signal demandant l'état des
services exportés est traité.


Fournisseurs
============

Classes d'informations
----------------------

Les composants de base et les fournisseurs dialoguent avec deux classes
utilitaires :

* ``EndpointDescription``, qui contient les propriétés permettant d'accéder à
  un *end point* (protocole, hôte, port, ...).

* ``RemoteServiceEvent``, qui signale un évènement sur un service
  (apparition, disparition, changement de propriétés, ...)

* ``RemoteServiceRegistration``, contenue dans les ``RemoteServiceEvent``, qui
  contient toutes les informations sur un service exporté : ses *end points*,
  ses interfaces, ...


Remote Services Endpoint Handlers
---------------------------------

Les *end point handlers* sont des services permettant de créer et supprimer des
points connexions vers une instance de service à exporter.

Ces services implémentent l'interface ``IEndpointHandler`` avec les méthodes
suivantes :

* ``EndpointDescription[] createEndpoint(String, ServiceReference)`` : crée un
  *end point* avec le nom indiqué vers le service passé en second paramètre.
  Le fournisseur peut créer plusieurs *end points* pour un même service, ce qui
  peut être notamment utile dans les cas où plusieurs interfaces sont exportées
  par le service, tandis qu'un *end point* ne peut donner accès qu'à l'une
  d'entre elle.

* ``boolean destroyEndpoint(ServiceReference)`` : supprime tous les *end points*
  correspondant au service passé en paramètre.
  Les points de connexion doivent être fermés et toutes les ressources associées
  libérées.

* ``EndpointDescription[] getEndpoints(ServiceReference)`` : retourne la liste
  des *end points* existants pour le service passé en paramètre.


Remote Services Client Handlers
-------------------------------

Les *client handlers* sont des services permettant de créer et supprimer des
proxies vers des points de connexions créés par les *end point handlers*.

Ces services implémentent l'interface ``IRemoteServiceClientHandler`` avec
les méthodes suivantes :

* ``Object getRemoteProxy(RemoteServiceEvent) throws ClassNotFoundException`` :
  appelée par le RSI quand un signal de service distant indiquant l'apparition
  d'un nouveau service a été reçu. Toutes les informations nécessaires à la
  création du proxy sont présentes dans l'objet ``RemoteServiceEvent`` passé
  en paramètre.
  Cette méthode peut générer une exception si l'interface exposée par le proxy
  est introuvalble.

* ``void destroyProxy(Object)`` : détruit le proxy passé en paramètre et
  libère les ressources associées.


Signaux de Remote Services
**************************

Le *Remote Services Broadcaster* (RSB) est utilisé par le
*Remote Services Exporter* pour signaler qu'un service est désormais exporté
par l'isolat courant.
À leur reception, ces signaux sont pris en charge par le
*Remote Service Importer* (RSI) afin de tenter d'importer les services distants
dans l'isolat ayant reçu le message.

Les signaux transmis sont les suivants :

* */psem2m/remote-service-broadcaster/remote-event* : notification d'un
  évènement de service : export ou arrêt d'export d'un service externe.
  La donnée associée est un objet ou un tableau de ``RemoteServiceEvent``.

* */psem2m/remote-service-broadcaster/request-endpoints* : signal utilisé pour
  demander à un isolat quels services il exporte.
  L'isolat recevant ce message répond par un tableau d'objets
  ``RemoteServiceEvent`` dans un signal
  */psem2m/remote-service-broadcaster/remote-event*.