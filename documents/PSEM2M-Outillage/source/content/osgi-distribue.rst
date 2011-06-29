.. Plateforme OSGI distribuée

Plateforme OSGI distribuée
##########################

Nous avons testés trois produits pour gérer une plateforme OSGi distribuée :
Apache CXF, Eclipse ECF et OW2 Rose.

Ces trois outils sont compatibles avec les Remote Services d'OSGi 4.2 et se
disent compatibles avec toutes les plateforme répondant à cette norme.
Celle-ci étant basée sur les propriétés des services à exporter, elle peut être
définie soit à l'enregistrement d'un service via un Activator, soit en tant que
propriétés de composants iPOJO ou Declarative Services.

À la rédaction de ce document, Rose est en cours de développement et est
absolument incapable de répondre à nos besoins.
Il n'est pas encore capable d'effectuer une découverte ni un import de service
distribué.

ECF a été testé avec son conteneur "ecf.generic.server" avec Declarative
Services, CFX avec sa distribution en un bundle avec iPOJO et Rose avec le
protocole JSON-RPC avec iPOJO.

ECF a été mis à jour en version 3.5 avec la sortie d'Eclipse Indigo.
Celle-ci apporte le support de framework OSGi autres qu'Equinox, notamment
Felix.
Cette nouvelle fonctionnalité n'a pas encore testée ni validée par nos soins.


Points communs : OSGi 4.2
*************************

Pour qu'un service soit exporté, celui-ci doit comporter au minimum deux
propriétés :

* service.exported.interfaces : liste des interfaces exportées par le service.
  Peut valoir le joker '*'

* service.exported.configs : liste des outils d'export, dépendant des outils
  présents dans le framework.
  Permet d'utiliser plusieurs protocoles / outils de distribution.

En général, d'autres propriétés sont nécessaires afin de configurer les outils
de distribution de services.

Pour ne pas utiliser d'outil de découverte de protocole, il est possible
d'indiquer les plateformes fournissant des services données via des fichiers de
configuration au format XML dans le dossier OSGI-INF/remote-service/ du bundle.


Apache CXF
**********

Cet outil semble le plus simple à utiliser et à faire fonctionner, notamment
grâce à une documentation bien remplie.
Il existe sous deux formats : un bundle embarquant toutes les dépendances ou un
ensemble de bundles à installer si besoin.

L'export de service se base sur Jetty, et la découverte -- facultative -- sur
ZooKeeper.

Les propriétés de service à utiliser sont :

- ``service.exported.configs = org.apache.cxf.ws`` -- Indiquant l'utilisation de
  l'export via CXF

- ``endpoint.id = http://localhost:9090/test`` -- L'adresse, le port et l'URI
  du point d'accès au service

  Cette propriété semble être la remplaçante de org.apache.cxf.ws.address

- Dans le cas d'iPOJO, il faut indiquer que les propriétés du composant sont
  propagées

CXF utilise un serveur Jetty pour exporter les services.
Il est capable d'utiliser une instance unique de ce serveur pour exporter
plusieurs services, mais il démarre un serveur "vide" au démarrage de son
bundle, sur le port HTTP indiqué dans les propriétés du framework.
Une configuration multi-bundles pourrait résoudre ce problème d'auto-démarrage,
mais ceci n'a pas été testé.

Pour utiliser la découverte, il faut :

- configurer et démarrer un serveur ZooKeeper

- inscrire l'adresse -- au minimum -- du serveur ZooKeeper dans le fichier
  ``./load/org.apache.cxf.dosgi.discovery.zookeeper.cfg`` du répertoire
  d'exécution du framework.
  Celui-ci sera lu par FileInstall pour être transmis à ConfigurationAdmin.
  Il peut contenir les propriétés :

  - ``zookeeper.host`` : nom d'hôte du serveur

  - ``zookeeper.port`` : port d'écoute du serveur

  - ``zookeeper.timeout`` : intervalle de test d'état du serveur

CXF découvrira le serveur dès qu'il sera lancé ou lorsque sa configuration sera
mise à jour.

Le principal avantage d'une découverte utilisant ZooKeeper est sa rapidité : il
faut moins de 5 secondes pour qu'un service exporté soit découvert et utilisé.

L'inconvénient est que cette technique repose sur un serveur pouvant disparaître.
Cet inconvénient semble pouvoir être mis de côté en utilisant la possibilité de
réplication de ZooKeeper, celle-ci n'ayant pas été testée.


Eclipse ECF
***********

ECF fonctionne en utilisant les "providers" démarrés dans le framework.
Ainsi, il peut utiliser n'importe quel protocole de découverte ou d'export,
du moment que le bundle correspondant est présent.
On peut citer à titre d'exemple la découverte par le protocole Bonjour ou
l'export par R-OSGi ou par XMPP.

Cette capacité est une grande faiblesse car il existe peu de documentation sur
le fonctionnement de ces outils, et ceux-ci ne sont pas vraiment centralisés.

Un autre inconvénient de cet outil est son incapacité à gérer plusieurs services
définis par Declarative Services ou iPOJO dans un même container : il faut créer
explicitement le container, avant que les composants soient démarrés.
On perd alors l'ntérêt de l'ordre de démarrage des bundles gérés par iPOJO ou
DS.

Les propriétés à utiliser sont :

- ``service.exported.configs = ecf.generic.server`` -- dépend des services
  utilisables par ECF présents sur le framework

- ``org.eclipse.ecf.containerFactoryArgs = ecftcp://localhost:38000/server`` --
  configuration du port et de l'URI d'export du service.

Certains exportateurs peuvent nécessiter des propriétés supplémentaires.

.. note:: Il est nécessaire d'avoir une version d'ECF >= 3.5 (Eclipse Indigo)
   pour pouvoir l'utiliser avec un framework OSGi autre qu'Equinox.


Rose
****

Rose est actuellement en cours de développement et est beaucoup trop incomplet
pour pouvoir être utilisé.
On notera notamment l'absence de découverte et d'import de services.

L'export de service est fonctionnel et se base sur des protocoles, comme
JSON/RPC, XML/RPC, JMS.

On retrouve les capacités de configuration d'ECF, mais tous les protocoles sont
connus, présents dans la branche de développement de Rose.

À terme, Rose devrait être un mélange d'ECF et de CXF, conservant le meilleur
des deux outils.
