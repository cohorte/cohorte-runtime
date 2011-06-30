.. Documentation des opérations post-fork
.. highlight:: c

.. |fork| replace:: *fork()*
.. |forkit| replace:: *forkit()*
.. |forkall| replace:: *forkAll()*
.. |pfork| replace:: *child_post_fork()*

Opérations post-fork
====================

Description
-----------

Lorsque l'on effectue un |fork|, le fils obtient une copie des informations du
processus père, notamment une copie de la liste des *file descriptors (fd)*.
Les *fd* sont des entiers correspondant à un indice dans un tableau conservé par
le noyau.

De cette manière, le père et le fils utilise les mêmes pointeurs dans les
fichiers ouverts : quand le père lit un fichier, il modifie sa position et celle
de son fils dans ce fichier.

Pour éviter cela, le fils doit :

* soit fermer puis rouvrir chaque fichier puis se repositionner, dans ce cas
  on *peut* conserver les *fd* utilisés par le père;
* soit dupliquer chaque *fd* (via *dup()*) et fermer celui reçu depuis le père,
  dans ce cas, on aura forcément des *fd* différents de ceux du père.


Cas particulier : les sockets
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Dans le cas des sockets, nous pouvons être confronté à un problème de session
dans les protocoles utilisés.
Nous ne savons pas les gérer correctement pour le moment.

Dans le cas des sockets clients, nous conservons la connexion, ce qui permet 
un fonctionnement correcte pour la plupart des cas.

Dans le cas des sockets serveurs, nous fermons la connexion, en attendant de 
trouver un moyen viable de les traiter.

 
Présentation de libIsolator
---------------------------

La libIsolator est un bibliothèque partagée faisant la liaison entre les *fd*
vus par l'application et ceux connus par le système.
De fait, elle doit surcharger **toutes** les fonctions de la libC utilisant des
*fd* afin de faire la conversion de ceux utilisés par l'application à ceux
connus par le système.

Elle dispose également d'une méthode |pfork| se chargeant du travail de mise à
jour des *fd*, en utilisant la méthode *dup()*.

Pour lancer une application avec cette bibliothèque, on peut :

* soit re-compiler l'application, en indiquant libIsolator avant la libC,
* soit lancer une application déjà compilé en indiquant la variable
  d'environnement **LD_PRELOAD=/chemin/vers/libIsolator.so**.

Nous utilisons la technique **LD_PRELOAD** car elle est beaucoup plus souple et
elle permet l'utilisation des capacités de libIsolator sur des applications non
open-source.


Avantages / Inconvénients
^^^^^^^^^^^^^^^^^^^^^^^^^

* Les avantages :

  * Bibliothèque quasi-transparente : l'application n'a pas à être modifiée pour
    gérer un |fork|,

  * Permet un traitement fin des ouvertures de fichiers, de sockets, avec par
    exemple la capacité de modifier le nom du fichier ouvert en fonction d'un
    paramètre ou d'une variable d'environnement (l'identifiant de l'isolat par
    exemple)

* Les inconvénients :

  * Bibliothèque quasi-transparente : certaines méthodes ne peuvent pas être
    traitées correctement. Ainsi *man* n'a pas accès au *tty* comme il le 
    devrait et ne fonctionne pas correctement,

  * Nécessité de couvrir toutes les méthodes de la libC utilisant des *fd*, y
    compris celles faiblement documentées (*fopen64*, ...),

  * Inutilité face au applications liées statiquement à la libC (cas rare),

  * Comportement non garanti si l'application utilise une bibliothèque
    d'abstraction à la libC,

  * Incapacité à satisfaire les applications qui s'attendent à recevoir certains
    *fd* (écrits "en dur")


Autres pistes
^^^^^^^^^^^^^

Nous nous intéressons à l'implémentation des opérations post-fork sur les *fd*
dans le noyau, à la fin des méthodes |forkit| et |forkall|.

De cette manière, nous serions certain que le processus fils aurait des *fd*
prêts à l'emploi à son redémarrage.

Étant donné que nous ne pourrions jamais avoir un traitement aussi fin qu'avec
libIsolator, nous conserverions cette bibliothèque pour les opérations de plus
haut niveau, comme la gestion des sockets ou la mise à jour de *getpid()*. 


Appel de la méthode |pfork|
---------------------------

Nous utilisons un gestionnaire de signal **SIGUSR2** pour appeler cette méthode.
Celui-ci est inscrit à l'initialisation de la bibliothèque, avant l'exécution du
code du processus isolé.

Lorsqu'un signal est reçu, le processus et ses threads sont mis en pause.
Le gestionnaire se charge alors de tester si le PID de l'application est
toujours le même, en comparant sa valeur en cache et celle indiquée par le lien
symbolique */proc/self*.

Si les PID sont identiques, le signal n'a pas à être traité; sinon le
gestionnaire appelle la méthode |pfork|.


Qui appelle |pfork| ?
^^^^^^^^^^^^^^^^^^^^^

Actuellement, c'est à celui qui a appelé |forkit| de lancer le signal.
De cette manière, nous pouvons prévoir le choix du signal à émettre dans un 
fichier de configuration.

Cependant, nous étudions également l'ajout de **SIGUSR2** à la liste des signaux
en attente de traitement directement à la fin de la méthode |forkit|.
De cette manière, nous serions certains qu'il n'y a aucun délai entre la gestion
des signaux **SIGCONT** et **SIGUSR2**, c'est-à-dire qu'il n'y a pas de temps
mort pendant lequel le processus fils pourrait s'exécuter avec des *fd* 
invalides.


Ordre de traitement des signaux
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Les signaux *semblent* être appelés dans l'ordre décroissant de leur valeur.
Ainsi **SIGUSR2** (12) est géré avant **SIGUSR1** (10).
