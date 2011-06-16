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
| -c,          | ./monitor.conf    | Fichier de configuration principale à   |
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

     Redémarre la plateforme complète.

* ``IIsolate startIsolate(String aIsolateId, boolean aForceRestart)``

     Démarre l'isolat indiqué, forçant ou non son redémarrage si celui est
     déjà en cours d'exécution.
     Retourne le nouvel isolat, nul en cas de problème.

* ``boolean stopIsolate(String aIsolateId)``

     Arrête l'isolat indiqué. Renvoie faux en cas de problème.

* ``IIsolate getIsolate(String aIsolateId)``

     Renvoie l'isolat indiqué, ce qui permet d'avoir accès à son état et à
     le contrôler.

* ``String[] getRunningIsolates()``

     Renvoie une vue de la liste des identifiants des isolats étant dans l'état
     **RUNNING**.

* ``String[] getPossibleIsolates()``

     Renvoie une vue de la liste complète des identifiants des isolats ayant
     été correctement décrit par le fichier de configuration.


Routage des message
*******************

Les routes de messages sont définies dans un fichier de configuration.

Pour chaque isolat, la configuration spécifie les routes acceptées
(consommation) et les départs de routes (production).

La gestion du routage est faite en interne par le moniteur.
Afin de conserver une certaine cohérence de la plateforme, seuls des accès en
lectures sont autorisés :

* ``IRoute[] getActiveRoutes()``

     Renvoie une vue de l'ensemble des routes actives
