.. Interfaces du Forker
.. highlight:: java

Interfaces du forker
####################

Toutes les méthodes décrites ici sont accessibles *via* les services distants
OSGi. Elles sont utilisées par le moniteur.

Gestion de la configuration
***************************

Le moniteur a la responsabilité de transmettre la configuration de la plateforme
au forker. Il s'agit ici de la configuration globale de la plateforme et des
paramètres communs aux isolats.

* ``void setConfiguration(IPlatformConfiguration aPlatformConfiguration)``

  Met à jour la configuration globale de la plateforme, ne comprenant pas la
  définition des différents isolats.


Gestion des isolats
*******************

La gestion des isolats tient en trois méthodes :

* ``void startIsolate(IIsolateConfiguration aIsolateConfiguration) throws IOException, InvalidParameterException, Exception``

  Démarre l'isolat selon la configuration passée en paramètre.
  Lève une IOException si l'exécutable de l'isolat n'est pas trouvé; une
  InvalidParameterException si l'isolat est déjà en cours d'exécution; ou bien
  une Exception si l'implémentation du forker spécifique au type d'isolat n'a
  pas fonctionné correctement.

* ``void stopIsolate(String aIsolateId)``

  Tente de tuer l'isolat ayant l'identifiant indiqué en paramètre.
  Le forker tente de tuer *proprement* le processus isolé (via JMX, via un
  signal, ...); puis, en cas d'échec, tente de le tuer en utilisant des méthodes
  dépendantes du système d'exploitation.

* ``EProcessState ping(String aIsolateId)``

  Effectue une requête sur une sonde de l'isolat indiqué ayant l'identifiant
  indiqué en paramètre.
  Le résultat peut être :

  * ALIVE : l'isolat est fonctionnel
  * DEAD : l'isolat est mort ou n'a pas été démarré
  * STUCK : le processus de l'isolat est présent mais ne répond pas ou répond
    mal.
