.. Cycle de vie du service

Cycle de vie du service
#######################

L'exécution du service peut se faire de deux manières principales : en utilisant
un script shell, ou en utilisant un service wrapper Java.

L'utilisation d'un script shell entraîne une forte dépendance vis à vis du
système -- syntaxe, conventions de nommage, etc -- sans permettre de contrôler
simplement l'état réel du service.

Un service wrapper permet de créer de manière transparente un service définit
selon les normes du système d'exploitation sur lequel il devra s'exécuter.

Trois principales options sont disponibles pour créer un service système en
Java :

- Tanuki Java Service Wrapper (commercial, GPL ou Silver Egg Technology License)

- Yet Another Java Service Wrapper (LGPL)

- OW2 Java Service (LGPL)

Le Java Service d'OW2 ne fonctionne que sous Windows et n'a pas été testé pour
l'instant.

Tanuki Java Service Wrapper (JSW) et Yet Another Java Service Wrapper (YAJSW)
sont à peu près compatibles l'un et l'autre, le premier étant le plus stable,
le second étant plus complet.
Les deux sont compatibles Windows et Linux, YAJSW supportant en plus le système
de services de Mac OS.

La différence principale se trouve au niveau de la gestion des démarrage et
arrêt du service :

- YAJSW rend la main au système dès que le wrapper a démarré et tue le service
  via un signal SIGKILL.
  Le service doit gérer ce genre de signal -- via un shutdown hook -- pour se
  terminer proprement.

- JSW a plusieurs modes d'exécution et donne la possibilité au service de
  s'inscrire à un gestionnaire d'évènement, lui permettant d'indiquer lui-même
  son état au système.
  JSW peut se comporter comme YAJSW, ou plus finement si on lui indique une
  classe pouvant gérer le démarrage et l'arrêt du service.
