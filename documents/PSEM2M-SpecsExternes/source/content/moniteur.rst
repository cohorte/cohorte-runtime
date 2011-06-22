.. Interfaces du moniteur
.. highlight:: java


Interfaces du moniteur
######################

Paramètres d'exécution
**********************

Le moniteur doit accepter les paramètres suivants :

.. tabularcolumns:: |p{3cm}|p{3cm}|p{9cm}|

+--------------+-------------------+-----------------------------------------+
| Paramètre    | Valeur par défaut | Description                             |
+==============+===================+=========================================+
| -c,          | ./monitor.xml     | Fichier de configuration principale à   |
| --config     |                   | utiliser                                |
+--------------+-------------------+-----------------------------------------+
| -f,          | Faux              | Si ce paramètre est indiqué, le         |
| --foreground |                   | moniteur est lancé en mode premier-plan |
|              |                   | et non en mode démon / service          |
+--------------+-------------------+-----------------------------------------+


Re-configuration en cours d'exécution
*************************************

Étant donné la criticité du moniteur, on ne peut pas se permettre de forcer
l'arrêt et le redémarrage de celui-ci pour mettre à jour sa configuration.

Pour cette raison, le moniteur est capable de relire sa configuration et
exécuter les traitements nécessaires à son application (arrêt d'isolats,
création d'images, ...) sur ordre de l'administrateur.

Cet ordre peut être émis à l'aide d'une opération JMX :

* ``boolean reloadConfiguration(String aPath, boolean aForce)``

   Force la relecture du fichier de configuration par le moniteur, en lui
   indiquant s'il faut ou non tuer les isolats ayant disparu de la
   configuration.

   Si le fichier de configuration n'existe pas, aucune action n'est effectuée.

Gestion des isolats
*******************

La gestion des isolats est effectuée en interne, par le moniteur.
Celui-ci lit les fichiers de configurations afin d'obtenir la description de
l'ensemble des isolats et les démarre à l'aide du *forker*.

Le moniteur offre un certain nombre d'opérations utilisables via JMX afin de
pouvoir interagir sur les isolats :

* ``boolean restartPlatform(boolean aForce)``

     Redémarre la plateforme complète. Le paramètre permet de forcer l'arrêt des
     isolats toujours en cours de fonctionnement.

* ``boolean startIsolate(String aIsolateId, boolean aForceRestart)``

     Ordonne au forker de démarrer l'isolat indiqué, forçant ou non son
     redémarrage si celui est déjà en cours d'exécution.

* ``boolean stopIsolate(String aIsolateId)``

     Arrête l'isolat indiqué. Renvoie faux en cas de problème.

* ``IIsolateConfiguration getIsolate(String aIsolateId)``

     Renvoie la configuration de l'isolat indiqué.

* ``... getRunningIsolates()``

     Renvoie une vue de la liste des identifiants des isolats ayant été démarré
     par ce moniteur et étant considéré en cours d'exécution, c'est-à-dire un
     isolat qui :

     * a émis les notifications de démarrage (base OK, bundles OK, ...)
     * répond correspond à un ping sur une de ses sondes.

     Le résultat est une liste d'informations contenant l'identifiant de chaque
     isolat.

* ``Collection<IIsolateConfiguration> getPossibleIsolates()``

     Renvoie une vue de la liste complète des configurations des isolats ayant
     été correctement décrit par le fichier de configuration.


Ces méthodes ne seront pas disponibles par service OSGi *distant* : il n'est pas
nécessaire que les isolats puissent accéder directement au moniteur.


Écoute des isolats
******************

Le moniteur fournit une méthode JMX, utilisée par les sondes JMX des isolats
pour indiquer leur état de démarrage.
Quand un isolat démarre, il utilise cette méthode pour indiquer quelle étape il
vient de démarrer.

* ``void updateIsolateState(String aIsolateId, int aCurrentStep, int aTotalSteps)``

   Cette méthode est utilisée par les isolats via JMX pour notifier le moniteur
   qu'ils ont avancé à une certaine étape.

   Le premier paramètre indique l'isolat démarrant, le deuxième indique la
   nouvelle étape de progression et le dernier indique le nombre d'étapes de
   progressions attendues.

Généralement, le démarrage d'un isolat se fait en deux étapes :

#. Démarrage des bundles communs aux isolats (bibliothèques communes, sondes, ...)
#. Démarrage des bundles spécifiques à l'isolat


Routage des message
*******************

.. note:: Cette section sera à compléter lorsque les exigences correspondantes
   auront été clairement mises à jour.

Les routes de messages sont définies dans un fichier de configuration.

Pour chaque isolat, la configuration spécifie les routes acceptées
(consommation) et les départs de routes (production).

La gestion du routage est faite en interne par le moniteur.
Afin de conserver une certaine cohérence de la plateforme, seuls des accès en
lectures sont autorisés :

* ``IRoute[] getActiveRoutes()``

     Renvoie une vue de l'ensemble des routes actives
