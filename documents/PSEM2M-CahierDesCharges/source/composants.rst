.. comment:



Les composants de la plateforme
*******************************

Chaque isolat embarquent un jeux de bundles de en fonction de son rôle :

pour le rôle « master »:

org.psem2m.isolates.master.manager

org.psem2m.isolates.master.monitor

pour le rôle « slave »:

org.psem2m.isolates.slave.manager

org.psem2m.isolates.slave.agent

pour le rôle « forker »:

org.psem2m.isolates.forker

pour tous les isolats :

org.psem2m.isolates.config

org.psem2m.isolates.provisioner

org.psem2m.isolates.diagnoser

org.psem2m.isolates.broadcaster


Le bundle master.manager
========================

Ce composant à la charge de déterminer le « graphe » d'isolats à lancer et ensuite à surveiller en fonction de la description du logiciel qui lui aura été retourné par le service de configuration.


Le bundle master.monitor
========================

Une fois un isolat lancé, le composant master.manager lui confie le soins de le surveiller en lui fournissant la liste des métriques à prendre en compte (consommation de ressources, débit et/ou absence de réseau).

Le composant master.monitor établi le lien avec les isolat en utilisant le protocole JMX.

Lorsqu'il détecte une impossibilité de fonctionnement, voir un arrêt d'un isolat, il averti le master.manager en diffusant un message adéquat via le service de broadcast.


Le bundle slave.manager
=======================


Le bundle slave.agent
=====================


Les services de base
====================

Ils sont présents dans tous les isolats.


Le service de configuration
---------------------------

C'est un service très simple d'accès à un dictionnaire publié par le bundle isolates.config.

Les paramètres sont soit des valeurs discrètes, soit des documents XML ou JSON.

Les paramètres appartiennent explicitement à une famille (ex.
« psem2m.ISvcTrace ») ou à la famille par défaut « psem2m.all »

Le stockage des paramètres est un ensemble de fichier :

- psem2m-config[-paramfamily].properties.xml

- psem2m-config[-paramfamily]_paramIdX.xml

- psem2m-config[-paramfamily]_paramIdY.js

Les paramètres sont décrits dans des dictionnaire sous la forme de schémas xsd.

Au démarrage, le service de configuration valide tous les fichiers « .properties.xml » et « .xml »

- psem2m-config[-paramfamily].properties.xsd

- psem2m-config[-paramfamily]_paramIdX.xsd

Les paramètres (ID=VALUE) sont stockés dans un format XML dans les fichiers « .properties.xml » pour pouvoir être validés simplement avec un schéma xsd.

D'autre stockages peuvent être envisagés sur la base d'une désignation de la localisation des fichiers par une url.

La configuration du service de configuration (une URL) est une simple variable d'environnement disponible dans chacun des isolat d'un serveur d'exécution.

La log de l'initialisation du service de configuration envoi ses lignes dans la sortie standard de l'isolat courant.

Toutes les erreurs d'accès sont consignées dans un fichier d'activité et lèvent une exception

L'interface de service de configuration "org.psem2m.service.ISvcConfig" présente les méthodes :

- boolean isParamExists(IP2mParamId aParamId);

- Object getParam(IP2mParamId aParamId);

- List<Object> getParams(IP2mParamFilter aFilter);

- String getParamStr(IP2mParamId aParamId);

- Date getParamDate(IP2mParamId aParamId);

- Long getParamNum(IP2mParamId aParamId);

- Boolean getParamBool(IParamId aParamId);

- Document getParamXml(IParamId aParamId);

- JSONObject getParamJson(IParamId aParamId);

Pour chacune des méthodes d'accès (get), on aura la déclinaison :

- XXX getParamXxx(String aParamName);

- XXX getParamXxx(String aParamFamily, String aParamName);


Le service de diffusion d'évènements
------------------------------------

Chaque isolat charge et démarre le bundle isolates.broadcaster

Ce bundle publie un service de diffusion et d'écoute d'évènements pour permettre de s'y inscrire.

- BroadcastEvent(IP2mEvent aEvent) *synchrone*

- SendEvent(IP2mEvent aEvent) *asynchrone*

- registerListener(IP2mEventListener aListener)

- unregisterListener(IP2EventListener aListener)


Le service de console interactive
---------------------------------

Chaque isolat démarre un service interactif utilisable avec un terminal tty.

C'est une capacité de base du framework OSGI qui est activé simplement par la valorisation des paramètres :

osgi.console 

if set to a non-null value, the OSGi console (if installed) is enabled.
This is handy for investigating the state of the system.
If the value is a suitable integer, it is interpreted as the port on which the console listens and directs its output to the given port.
If the value is not a suitable interger (including the empty string) then the console will listen to System.in and direct its output to System.out.

osgi.console.class

the class name of the console to run if requested 

Chaque composant peut installer des commandes utilisable dans cette console.


Le service de visualisation et pilotage interactif
--------------------------------------------------

Chaque isolat « master » démarre un service interactif utilisable avec un browser Web

Chaque composant peut inscrire des pages auprès de ce service.

Exemple :

- Visualisation du monitoring des isolats (master.monitor)

- Visualisation de la configuration du logiciel (master.manager)

- Visualisation des paramètre de lancement( chemin,...) (config)


Les services de diagnostique
============================

Ce sont des services permettant de contrôler l'instrumentation du code à des fin de suivi d'activité et de diagnostique.

Ces deux services sont complémentaires : les « logs » permettent d'avoir une vue à postériori et les « traces » permettent d'avoir une vue immédiate sur le fonctionnement des logiciels bâtis avec la plate-forme.

Ces deux services peuvent être mis en cascade permettant ainsi de n'avoir qu'un code d'instrumentation à maintenir.

Ces deux services sont initialisés avec des valeurs de paramètres demandés au service de configuration.


les logs
--------

Le service de log permet d'activer dynamiquement des traitements d'instrumentation du code, et de rediriger les flots de lignes produites vers des fichiers.

Le service de log s'appui sur le package de base" java.util.logging".
Les capacité de base du package de la JVM son encapsulées dans un package "org.psem2m.util.logging" dans lequel sont formalisés : la gestion des fichiers « tournants », le formatage des lignes et des filtres par package.

L'interface de service de log "psem2m.service.ISvcLog" présente les méthodes :

- void setLoggerFactory(ILoggerfactory aFactory);

  - pour permettre de changer de factory (ex: mockito)

- void openLogger(String aLoggerId) qui retourne une instance de Ilogger.

- void setLevel(String aLoggerId ,Level, aLevel);

- void setSubLevel(String aLoggerId ,String aPackageName, Level aLevel);

- void setTraceOn(String aLoggerId ,boolean aOnOff)

  - une méthode pour activer ou non la trace des lignes loguées.
    L'activation de la trace d'un ILogger conduit à ouvrir un canal dans la ITrace courante et ensuite a rediriger tous les appels de log vers le ITracer associé.

Par défaut chaque ILogger correspond à 10 fichiers tournants de 10 Mo dont le nom est le "LogId" et le chemin est fixé par la configuration du bundle de log

L'interface "org.psem2m.shared.commons.ILogger" présente les méthodes:

- void log(Object aWho,Level aLevel, CharSequence aWhat, Object... aArgs);

  - cette méthode est déclinée en logInfo(), logSevere(), logWarning(), logDebug(),

- void seLogLevel(Level aLevel);

- boolean isLogLevelOn(Level aLevel);

- boolean isLogInfoOn();

- boolean isLogDebugOn();

- void setSubLogLevel(String aPackageName, Level aLevel);

- void setSubLogLevel(Class<?> aPackageClass, Level aLevel);

- void setTraceOn(boolean aOnOff);


les traces
----------

Le service de trace permet d'activer dynamique des traitements d'instrumentation du code, et de rediriger les flots de lignes produites vers des outils de visualisation distant (tty, serveur spécialisé,...)

L'interface de service de trace "psem2m.service.ISvcTrace" présente les méthodes :

- void openTraceCnx(URL aUrl)

  - pour ouvrir et fermer la trace vers un outil de visualisation (par défaut un serveur spécialisé "traceserver://localhost:1815") . cette méthode ouvre une connexion entre la ITrace courante et un serveur de trace

  - Les url acceptées sont de la forme « traceserver://host:port », « tty://localhost:port »

- void setTracerFactory(ITracerfactory aFactory);

  - pour permettre de changer de factory (ex: mockito)

- ITracer openTracer(String aName)

  - ouvre un canal dans la ITrace courante.
    Retourne un ITracer.
    Pour info : dans le serveur de visualisation de trace, les lignes des différents ITracer apparaissent sous différentes couleurs.

L'interface "org.psem2m.shared.commons.ITracer" présente les méthodes :

- void trace(Object aWho,Level aLevel, CharSequence aWhat, CharSequence aLine);

- void setLevel(Level, aLevel); 

- boolean isTraceLevelOn(Level aLevel);

- boolean isTraceDebugOn();

- boolean isTraceInfoOn();

