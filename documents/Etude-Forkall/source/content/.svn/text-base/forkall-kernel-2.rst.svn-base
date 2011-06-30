.. Documentation de l'implémentation de forkall dans le noyau
.. highlight:: c

.. |fork| replace:: ``fork()``
.. |forkit| replace:: ``forkit()``
.. |forkall| replace:: ``forkAll()``
.. |current| replace:: ``get_current()``
.. |join| replace:: ``pthread_join()``
.. |segfault| replace:: *segmentation fault*


.. _forkall-kernel-2:

Implémentation de |forkall| en appel système (v2)
=================================================

Algorithme
----------

On considère le processus père (le modèle) stoppée à l'aide d'un signal
**SIGSTOP** par le programme appelant la méthode |forkall| (le moniteur).
Le moniteur appelle |forkall| en indiquant le PID du processus père,
correspondant à la tâche principale du processus.

.. figure:: /_static/images/diag-forkall-1.png
   :alt: État initial : moniteur + modèle
   :align: center

   État initial


Lignes 219 à 241 :
   Le |forkall| va commencer par sauvegarder les informations sur le modèle non
   dupliquées par l'appel système |fork|, puis forcer la méthode noyau |current|
   à renvoyer les informations sur le modèle quand le moniteur a la main.
   Parmi ces informations, on retrouve la valeur du registre *eax* (x86), qui
   sera  écrasé par le résultat de |fork|; ainsi que l'état exacte de la tâche,
   avant qu'elle soit déclarée stoppée (état **TASK_STOPPED**).

Lignes 246 à 255 :
   S'en suit le |fork| de la tâche principale : s'il échoue, |forkall| échoue, 
   sinon on recherche le pointeur vers la nouvelle tâche à partir du PID indiqué
   en retour de |fork|.

.. figure:: /_static/images/diag-forkall-2.png
   :alt: Premier fork : moniteur + modèle + fils (sans threads)
   :align: center

   État après la duplication du processus (thread principal)


Lignes 256 à 301 :
   On stoppe *immédiatement* la tâche fille. On ne peut pas garantir cet aspect
   immédiat car il peut y avoir une certaine latence lors de la recherche de la
   tâche à partir de son PID durant laquelle l'ordonnanceur peut démarrer
   l'exécution de cette nouvelle tâche.
   L'implémentation précédent utilisait le drapeau **CLONE_STOPPED** lors de 
   l'appel à |fork|, mais celui-ci générait un comportement totalement erratique
   au lancement des nouvelles tâches.

   Les lignes suivantes (264-265) supprime le signal en attente **SIGSTOP**. Il
   est en effet impossible de créer un thread, et plus généralement d'appeler
   |fork|, si un signal est en attente de traitement.
   Ceci est apparemment une sécurité pour éviter de dupliquer une tâche recevant
   un signal de terminaison (**SIGKILL**, etc : voir le commentaire dans le
   fichier *fork.c*, dans la méthode ``copy_process()``).

   On se charge alors de remettre en place les informations sauvegardées avant
   le |fork|.

   Le bloc démarrant à la ligne 290 se charge de faire une duplication profonde
   de la table des descripteurs des fichiers ouverts, en dupliquant les
   structures internes (``struct file*``).
   Cette action permet d'avoir des positions indépendantes dans les fichiers,
   les lectures d'une tâche ne déplaçant plus le pointeur de l'autre.


Lignes 306 à 365 :
   Il s'agit ici de la boucle de duplication des threads du modèle afin
   qu'ils s'exécutent dans la tâche fille.

   Pour chaque thread du modèle :
      * on commence par sauvegarder son état,
      * on supprime les signaux en attente, interdisant le |fork|,
      * on appelle la méthode |fork| avec les drapeaux utilisés dans la version
        gLibC de ``pthread_create()``,
      * en cas de réussite, on stoppe le nouveau thread et on restaure ses
        informations selon son modèle.

   On remarquera la suppression du drapeau **CLONE_STOPPED** comme indiqué plus
   haut, mais aussi du drapeau **CLONE_SETTLS**.
   Ce dernier demande la création d'une nouvelle zone *Thread Local Storage*,
   contenant des données spécifiques à un thread (voir les méthodes
   ``pthread_set/getspecific()`` et ``pthread_key_*()``).
   Dans notre cas, nous utilisons la même zone mémoire pour le thread modèle et
   sa copie, en considérant que le mode *copy-on-write* utilisé pour le |fork|
   de Linux est appliqué sur cette zone.
   Dans le cas contraire, nous devrons trouver un moyen d'allouer et dupliquer
   cette zone dans la copie, afin d'avoir un état identique à celui du thread
   modèle.


.. figure:: /_static/images/diag-forkall-3.png
   :alt: Forkall() complet : moniteur + modèle + fils (avec threads)
   :align: center

   État après la duplication des threads


Lignes 370 à 380 :
   Une fois tous les threads dupliqués, on peut réinscrire le signal **SIGSTOP**
   dans la file d'attente de la tâche fille, ainsi que de restaurer l'état réel
   de ces threads.
   Nous n'effectuons cette restauration qu'après avoir dupliqué tous les threads
   afin d'éviter qu'un dialogue entre deux threads échoue si l'un d'entre eux
   n'est pas encore créé.


Fin de la méthode :
   On se charge ici de remettre en état le modèle et la méthode |current|.


Problèmes rencontrés
--------------------

La méthode |forkall| n'est pour le moment pas utilisable du fait des problèmes
décrits ci-dessous.
Les tests de la méthode |forkall| ont été effectués sur un serveur
*Apache Tomcat 7* tel qu'il est disponible en téléchargement, sans configuration
préalable, avec les machines virtuelles Sun JDK 1.6 et OpenJDK-dbg 6. 


Le cas de la méthode |join|
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Le premier problème que nous avons rencontré lors de nos tests a été un arrêt
brutal du processus fils dû à la mise en attente du thread principal par un
|join| dans l'interpréteur Java.
En effet, lorsqu'on utilise la commande ``java`` pour exécuter un programme,
le thread principal configure la machine virtuelle puis lance le programme dans
un thread à part, puis se met en attente de la mort de ce dernier.

La méthode |join| de la gLibC se base un *futex* (Fast User-space mutex),
c'est-à-dire une mise en attente, côté noyau, de la modification d'une zone
mémoire précise : pour |join|, il s'agit de la zone contenant le TID du thread
attendu, modifiée à la mort de ce dernier car **CLONE_CHILD_CLEARTID** aura été
passé à la méthode ``clone()``, à la création du thread.

L'erreur obtenue est une |segfault|, ne laissant pas de trace dans les
journaux du noyau, mais semblant avoir lieu autour de l'instruction
``syscall (futex)`` appelée par la gLibC.


La solution utilisée a été de compléter la bibliothèque *libIsolator* afin 
qu'elle transforme le *futex* utilisé par |join| en un sémaphore, dans un
premier temps, puis en une attente active (boucle ``while``) régulée par une
attente d'une seconde (``sleep(1)``).

Dans le premier cas, nous avions toujours une |segfault|, les sémaphores se 
basant également sur l'appel système *futex*.
Dans le second cas, il arrive que nous ayons l'erreur, mais ce n'est pas
systématique.

Nous nous sommes alors rendu compte, après quelques essais, que le drapeau
**CLONE_STOPPED** pouvait être en cause de ces |segfault|; sa suppression lors
des appels à |fork| a réduit le nombre d'erreurs, mais ne les a pas totalement
supprimés.


La cas des appels systèmes bloquants
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Dans les cas où la tâche fille et ses threads ont réussi a être lancés, nous
pouvons voir les traces correspondant aux traitements post-fork, réalisés par
la bibliothèque *libIsolator* à la réception du message **SIGUSR1** envoyé
par le moniteur.

Une fois ce signal géré, nous obtenons de nouveau une |segfault| dans un ou
plusieurs threads fils.
Il s'agit alors d'erreurs causées pendant / par l'exécution d'un appel système,
généralement travaillant sur un périphérique (lecture/écriture dans un fichier,
une socket, ...).

Grâce aux *core dumps* générés par ces erreurs, nous avons pu voir deux types
de sources d'erreurs :

* les erreurs dues à un double retour : une faute de page non gérée pendant
  l'appel, généralement quand le noyau tente d'inscrire un résultat en mémoire,

* les erreurs dues à des erreurs de pile : *gdb* montre que l'interpréteur Java
  a correctement préparé son appel, mais que la pile n'est pas valide lorsque la
  méthode système est appelée (généralement ``write()`` dans *libIsolator*). 


Code source
-----------

.. literalinclude:: /_static/code/forkitall-v2.c
   :language: c
   :linenos:
