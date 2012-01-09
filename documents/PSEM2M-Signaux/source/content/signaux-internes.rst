.. Sémantique des signaux internes

Sémantique des signaux internes
###############################

Les signaux sont utilisés en interne à plusieurs niveaux :

* Gestion des isolats
* Gestion des services distants

Gestion des isolats
*******************

Les signaux utilisés pour la gestion des isolats sont les suivants :

* */psem2m/isolate/status* : émis par un isolat pour indiquer un changement
  d'état.
  La donnée associée est un ``IsolateStatus``, décrit plus loin.

* */psem2m/isolate/stop* : surveillé par le *Slave Agent* de chaque isolat, ce
  signal engendre l'arrêt de l'isolat qui le reçoit, *via* un appel à
  ``ISvcAgent.killIsolate()``.

* */psem2m/platform/stop* : surveillé par le *Monitor Core* de chaque moniteur.
  Celui qui le reçoit arrête de démarrer des isolats et demande l'arrêt des
  autres moniteurs, des isolats puis du forker.

  Le forker est arrêté en dernier afin de pouvoir tuer les isolats qui ne
  répondent plus.


État des isolats
================

L'état des isolats est transmis dans signaux à l'aide d'un objet de type
``IsolateStatus``.

Cet objet contient les informations suivantes :

* l'identifiant de l'isolat à l'origine du signal, ce qui permet de transmettre
  le signal à travers une chaîne d'isolats (notamment en passant par le
  *forker*)

* le statut de l'isolat, une valeur entière, correspondant aux étapes du cycle
  de vie d'un isolat :

  * Émis par le *bootstrap*, et relayés par le *forker* :

    * **STATE_READ_CONF** (0) : la configuration de base a été lue correctement
      par le *bootstrap*.

    * **STATE_FRAMEWORK_LOADED** (1) : le framework OSGi a pu être instancié.

    * **STATE_BUNDLES_INSTALLED** (2) : les bundles de base ont tous pu être
      installés.

    * **STATE_FRAMEWORK_STARTED** (3) : le framework OSGi a pu être démarré.

    * **STATE_BUNDLES_STARTED** (4) : les bundles de base ont tous pu être
      démarrés.

    * **STATE_FRAMEWORK_STOPPING** (99) : le *bootstrap* arrête le framework
      OSGi.

    * **STATE_FRAMEWORK_STOPPED** (100) : le *bootstrap* est arrêté.

  * Émis par le *Slave Agent*, via le *bootstrap* et donc le *forker* :

    * **STATE_AGENT_DONE** (10) : tous les bundles non-optionnels de l'isolat
      ont été correctement installés et démarrés.

    * **STATE_AGENT_STOPPED** (90) : le composant *Slave Agent* a été invalidé.

  * En cas d'arrêt d'un isolat sur un échec, émis par le *Slave Agent* ou par
    le *forker*.

    * **STATE_FAILURE** (-1) : l'isolat est mort sur un échec.

* une information sur la progression du lancement de l'isolat. Cette information
  n'est là que pour pondérer le statut indiqué.

