.. Documentation de l'implémentation de forkall dans le noyau
.. highlight:: c

.. |fork| replace:: ``fork()``
.. |forkit| replace:: ``forkit()``
.. |forkall| replace:: ``forkAll()``

.. _forkall-kernel:

Implémentation de |forkall| en appel système (v1)
#################################################

Algorithme
**********

L'algorithme utilisé ici reprend les bases de celui de |forkit|, décrit dans la
section :ref:`forkit-algo`.

Le processus original complet, threads compris, doit être stoppé avant un appel
à |forkall|.

Pour cela, on peut utiliser la commande système ou la fonction ``kill()`` sur
n'importe quel élément du groupe.
En effet, si elle est utilisée sur le processus père ou sur un de ses threads
afin de lancer un signal **SIGSTOP** ou **SIGCONT**, le gestionnaire de signaux
par défaut se charge de le distribuer à tous les membres du groupe.

.. note:: On appellera *image* le processus qui sert de base à la création du
   nouveau processus.

Une fois le groupe stoppé, l'implémentation de |forkall| doit :

#. Récupérer les informations sur l'appelant via la macro *current*.

#. Récupérer les informations sur l'image avec ``find_task_by_vpid()``.

#. Sauvegarder l'état des registres de l'image.
   Cette méthode est dépendante de la plateforme, mais elle évite de corrompre
   l'état de la tâche dupliquée.

#. Changer le contexte : on force la macro *current* à renvoyer un pointeur vers
   l'image quand l'appelant devrait avoir la main.

#. Appeler le cœur de |fork|, ``do_fork()``, avec les drapeaux **CLONE_PARENT**
   et **CLONE_STOPPED**.
   Ainsi, le processus fils n'est pas ajouté à la file d'ordonnancement.

#. Sauvegarder et supprimer les informations sur les signaux du processus, sinon
   les ``do_fork()`` appliqués au threads échoueront : on ne peut pas dupliquer
   une tâche ayant des signaux en attente.

#. Récupérer les informations sur le fils, toujours avec
   ``find_task_by_vpid()``, cette fois appliqué au processus dupliqué.

#. Changer le contexte : la macro *current* doit renvoyer un pointeur vers
   le processus dupliqué, plutôt que vers l'image.

#. Pour chaque thread de l'image, en utilisant la macro *while_each_thread* :

   #. Sauvegarder son état, ses registres.

   #. Appeler ``do_fork()``, avec les drapeaux de clonage indiqués dans la
      section :ref:`thread_clone_flags`.

   #. Forcer l'état du nouveau thread à correspondre à son modèle
      (état, registres...)

#. Restaurer l'état des registre et le statut du processus fils.

#. Restaurer ses informations sur les signaux.

#. Changer le contexte : la macro *current* reprend son comportement original.

#. Retourner le PID du processus fils.



.. _thread_clone_flags:

Clonage des threads
===================

Le clonage des threads utilise également la méthode *do_fork()*, mais en
indiquant un certain nombre de drapeaux :

+-------------------+---------------------------------------------------------+
| Drapeau           | Description                                             |
+===================+=========================================================+
| **CLONE_FILES**   | Le père et le fils partagent la même table des          |
|                   | descripteurs de fichier                                 |
+-------------------+---------------------------------------------------------+
| **CLONE_FS**      | Le père et le fils partagent les mêmes informations sur |
|                   | le système de fichiers (chroot, chdir, ...)             |
+-------------------+---------------------------------------------------------+
| **CLONE_SIGHAND** | Le père et le fils partagent les gestionnaires de       |
|                   | signaux                                                 |
+-------------------+---------------------------------------------------------+
| **CLONE_SYSVSEM** | Le père et le fils partagent les compteurs des          |
|                   | sémaphores SystemV                                      |
+-------------------+---------------------------------------------------------+
| **CLONE_THREAD**  | Création d'un thread (processus léger)                  |
+-------------------+---------------------------------------------------------+
| **CLONE_VM**      | Le père et le fils s'exécutent dans le même espace      |
|                   | mémoire                                                 |
+-------------------+---------------------------------------------------------+


Nous avons utilisé les drapeaux passés en paramètres de l'appel de ``clone()``
par l'implémentation de ``pthread_create()`` de la gLibC.

.. note:: La libC utilise le drapeau **CLONE_SIGNAL**, défini en interne et
   ayant pour valeur **CLONE_SIGHAND | CLONE_THREAD**.

Il a été nécessaire de tester la validité du champs *files* de la structure de
données de la tâche du processus fils, car ``do_fork()`` ne fait aucune
vérification en ce sens et provoque alors un Kernel Panic si ce champ vaut 0.


État du processus fils après un |forkall|
*****************************************

Après l'exécution de |forkall|, le processus fils et ses threads sont à l'arrêt.

Conservation du signal **SIGSTOP** en attente
=============================================

Comme on l'a vu dans l'algorithme décrit plus haut, on remet l'état des signaux
en place à la fin de l'appel système.

Si on ne le fait pas, le fils plantera lorsqu'il recevra un signal **SIGCONT**,
en indiquant l'erreur 514, c'est-à-dire qu'il ne connait pas de gestionnaire
pour celui-ci.

Le fils a alors le signal **SIGSTOP** en file d'attente, tandis que le père
l'a dans sa liste des signaux bloquants.


Signal de fin d'exécution
=========================

On peut voir le numéro du signal que renverra le processus une fois terminé
dans le fichier */proc/self/status*, au champ **exit_status**.

Dans le cas du processus père, cette valeur sera 0 (ignoré), tandis qu'elle
vaut 17 (**SIGCHLD**) pour le fils.

C'est grâce à cette valeur que le père peut attendre la mort de ses fils via
une fonction du groupe ``wait()``.


Problèmes rencontrés
********************

Trouver les threads d'une tache
===============================

Pour effectuer un |forkall|, l'appelant ne fourni que le PID du processus cible.
Nous devons donc récupérer à partir de cette information la totalité des threads
qui ont été créé par ce processus ou par ses threads.

Le noyau propose une macro permettant de parcourir la liste des threads d'une
tâche : *while_each_thread(p, t)*, où *p* est la tâche à parcourir et *t* est
un pointeur qui pointera vers la structure d'information du thread à chaque
itération.


Informations supplémentaires
----------------------------

Nous avons appris que les threads sont liés à un *group_leader*, afin que les
threads puissent être gérés correctement si leur parent meurt.

Il est important de savoir que les threads et les processus sont traités de la
même manière par le noyau, c'est-à-dire en tant que tâches.
De fait, chaque thread doit avoir un **PID** qui lui est propre, c'est donc le
**PGID** (Process Group ID) qui permet de repérer les tâches d'un groupe de
threads.

Cette information est indiquée dans une révision de patch du noyau Linux
[#patch_setpgid_group_leader]_.


Création des threads après un ``do_fork()``
===========================================

Après l'appel à ``do_fork()``, avec le drapeau **CLONE_STOPPED**, le processus
fils a le signal **SIGSTOP** dans sa liste d'attente.
Il est impossible d'appeler à nouveau cette méthode dans qu'un signal est en
attente ou en cours de traitement par le processus père, auquel cas nous
obtenons l'erreur 513.

Nous avons donc supprimé ce signal de la liste d'attente, dupliqué les threads
puis nous l'avons réinscrit afin que son fonctionnement ne soit pas altéré.

Zone mémoire locale à un thread
===============================

Il ne faut pas oublier d'utiliser le drapeau **CLONE_SETTLS** lors de la
création des threads.
Celui-ci permet l'allocation par le noyau d'une zone mémoire spécifique à
chaque thread, la TLS (Thread Local Storage).

Sans cette zone mémoire, l'exécution d'un thread fera planter le processus
entier.

Plus d'informations sont disponibles ici : [#descr_tls]_.


Rubrique "fourre tout"
**********************

.. todo:: À trier / reformuler / ranger

.. note:: Un des deux points suivants (ou les deux) a permis de ne plus tuer
   le père quand le fils meurt après son démarrage.

* Registre FS : extra data segment, apparemment indique la taille de la mémoire
  valide accessible (en gros la taille des 2 pages de mémoire)
  Registre uniquement présent sur 64 bits.
  *Pourrait* correspondre à IP en 32 bits, qui n'est pas présent en 64 : à
  vérifier en regardant du code 32 bits.

  => Voir `<http://www.linux.it/~rubini/docs/ksys/ksys.html>`_, "why get_fs()"


* Traiter le cas des applications à interface graphique => arrêt du processus
  fils par X

Références
**********

.. [#patch_setpgid_group_leader] Patch de set_pgid : `<http://linux.derkeiler.com/Mailing-Lists/Kernel/2005-12/msg03260.html>`_
.. [#descr_tls] Définition de TLS : `<http://fr.wikipedia.org/wiki/Thread_local_storage>`_
