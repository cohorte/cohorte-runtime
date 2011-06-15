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

* ``boolean reloadConfiguration(String aFileName, boolean aKillUselesses)``

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

* ``boolean restartPlatform(boolean force)``

     Redémarre la plateforme complète

* ``boolean restartIsolate(String aIsolateId)``

     Redémarre l'isolat indiqué

* ``int getIsolateState(String aIsolateId)``

     Renvoie l'état de l'isolat indiqué (OK, ne répond pas, non lancé)

* ``String[] getRunningIsolates()``

     Renvoie une vue de la liste des isolats ayant l'état OK


Routage des message
*******************

Les routes de messages sont définies dans un fichier de configuration.

Pour chaque isolat, la configuration spécifie les routes acceptées
(consommation) et les départs de routes (production).

La gestion du routage est faite en interne par le moniteur.
Afin de conserver une certaine cohérence de la plateforme, seuls des accès en
lectures sont autorisés :

* ``Route[] getActiveRoutes()``

     Renvoie une vue de l'ensemble des routes actives
