.. Documentation sur ce qui a été appris pendant le développement

.. |clone| replace:: ``clone()``
.. |fork| replace:: ``fork()``
.. |join| replace:: ``pthread_join()``

.. _xp-acquise:

Expérience acquise
##################

.. _kernel-task:

Les processus vus par le noyau 2.6
**********************************

Tout ce qui est exécuté dans l'espace utilisateur (processus, threads) est
représenté par une tâche dans le noyau : une structure de données
``task_struct`` (définie dans le fichier *sched.h*).

Cette structure contient notamment les informations sur :

* ``state`` : l'état de la tâche,
* ``stack`` : un pointeur vers sa pile,

* ``parent``, ``real_parent`` : un pointeur son processus père,
* ``children`` : la liste de ses processus fils,
* ``sibling`` : la liste de ses *tâches* sœurs (équivalent à
  ``parent->children``),
* ``tasks`` : la liste de ses *tâches* filles (threads),

* ``thread`` : les informations sur le fil d'exécution
  (état des registres, ...),

* ``fs`` : un pointeur vers une structure décrivant le système de fichiers à
  utiliser pour cette tâche, permettant le ``chroot`` et la gestion de
  références sur les points de montages pour les déclarer utilisés ou non
* ``files`` : un pointeur vers la table des descripteurs de fichiers ouverts
  (voir la section :ref:`filedes`),

* ``signal`` : un pointeur vers une structure contenant les informations sur les
  signaux commune aux threads d'un processus,
* ``sighand`` :  un pointeur vers une structure décrivant les gestionnaires de
  signaux,
* ``pending`` : l'ensemble des signaux en attente,

* des paramètres d'ordonnancement (priorité, cgroup, ...),
* son PID, représentée par une structure dans le noyau, ...

Cette structure est évidemment sujette à des modifications fréquentes, car elle
regroupe toutes les informations sur une tâche et subit donc les ajouts et
suppressions de fonctionnalités du noyau (sécurité, ordonnancement, ...).

La macro ``current`` renvoie un pointeur vers la structure de la tâche en cours
d'exécution.


.. _filedes:

Gestion des descripteurs de fichiers
====================================

La structure ``task_struct`` contient un champ ``files`` de type
``files_struct``, défini dans le fichier *fdtable.h*.

Son champ le plus intéressant est ``fdt``, un pointeur vers une structure
``fdtable``, contenant la table des descripteurs.
L'entier descripteur de fichier de l'espace utilisateur (0 pour *stdout*, ...)
correspond à l'indice d'une entrée dans cette table.
Chaque entrée est un pointeur vers une structure ``file``, du fichier *fs.h*,
pouvant être partagée entre les processus : c'est notamment le cas lors d'un
|fork|.

Les champs les plus importants sont :

* ``f_path`` : le chemin du fichier, représenté par un point de montage et
  une suite de noeuds sur celui-ci,
* ``f_op`` : une structure de données indiquant quelle méthode utiliser pour les
  opérations de base (ouverture, lecture, écriture, ...) : cette structure est
  préparée par le pilote ayant créé la structure,
* ``f_pos`` : la position actuelle dans le fichier,
* ``f_count`` : le nombre de références sur cette structure
  (*garbage collecting*)
* ``private_data`` : un pointeur vers une structure de données personnalisée
  pour les pilotes en ayant besoin (*sockets*, *tty*, ...)

On peut remarquer que le champ ``f_pos`` est donc partagé entre les processus
utilisant cette structure : ceci a apparemment été choisi pour simplifier la
synchronisation des entrées/sorties, particulièrement sur les terminaux.

Dans le cas des sockets, le champ ``private_data`` est utilisé pour lier un
descripteur de fichier à la structure du module correspond, tout comme le champ
``f_op``.
Le point de montage utilisé dans ``f_path`` est virtuel, défini par le module
gérant les sockets.


Références
==========

Pour plus d'informations, voir :

* `<http://www.informit.com/articles/article.aspx?p=370047>`_
* `<http://oreilly.com/catalog/linuxkernel/chapter/ch10.html>`_


Étapes d'un appel système
*************************

Ce qui suit correspond à l'architecture x86, des variations apparaissent
évidemment sur les autres architectures.


Étape 0 : la préparation du noyau
=================================

Les appels systèmes sont définis *en dur* dans le code source du noyau.
Chaque appel système est associé à un entier : il s'agit de la position de
l'appel dans un tableau de référence.
Il ne peut pas y avoir de trou dans ce tableau, et il n'est pas visible à
l'extérieur du noyau, même pas depuis les modules.

Lors de son initialisation, le noyau s'inscrit à certaines interruptions
processeur : dans notre il s'agit de l'interruption 80.


Étape 1 : l'appel par le processus
==================================

Le processus faisant un appel système va placer l'entier associé dans le
registre *eax* et les paramètres dans les registres *ebx*, *ecx*, *edx*, *esi*,
*edi* et *ebp*.
Le registre *esp* ne peut pas être utilisé car il est écrasé par le noyau
pendant le passage en *ring 0* (mode  noyau).
De ce fait, les appels systèmes sont limités à 6 paramètres, mais on peut très
bien n'indiquer qu'un paramètre pointant vers une structure contenant toutes les
informations nécessaires.

L'appelant va ensuite lancer l'interruption ``int $80``.


Étape 2 : le passage dans le noyau
==================================

Cette interruption correspond à un passage en mode noyau par le CPU, et à un
saut vers la fonction inscrite à l'étape 0.

Le noyau va alors sauvegarder l'état des registres puis exécuter la méthode
inscrite dans le tableau des appels à la position indiquée par le registre
*eax*, après avoir stocké ses arguments sur une pile côté noyau.
On ne peut pas parler de thread noyau à part entière, l'ordonnancement du noyau
se faisant en changeant la pile en cours d'utilisation.

La tâche appelante est toujours considérée en cours d'exécution : l'ordonnanceur
ne la sélectionnera donc pas lors des ordonnancement ayant lieu alors que
l'appel système est en cours.
Dans le cas des opérations sur les périphériques, l'état de la tâche est
modifiée en **TASK_INTERRUPTIBLE** ou **TASK_UNINTERRUPTIBLE** : celle-ci peut
alors être mise en attente par l'ordonnanceur, voire appeler directement la
méthode ``schedule()`` pour changer de tâche en attendant un réveil.

En indiquant **TASK_INTERRUPTIBLE**, la tâche peut recevoir et gérer des signaux
pendant l'appel système.
Avec **TASK_UNINTERRUPTIBLE**, les signaux sont ignorés durant l'appel, comme
c'est le cas pendant un |fork|.

Plus d'informations sur ces états sont disponibles à la section
:ref:`sig-interrupt`.


Étape 3 : le retour en espace utilisateur
=========================================

Le retour en espace utilisateur correspond à la fin de l'interruption,
c'est-à-dire à l'instruction ``iret``.

Avant d'appeler cette instruction, le noyau se charge de remettre les registres
à leur valeur initiale, excepté *eax* qui contient la valeur de retour de
l'appel.


.. _sig-interrupt:

Interruption d'appel
====================

Si un signal est reçu pendant l'exécution d'un appel système *simple*, comme une
pause, il est placé dans la file d'attente *sigpending* comme à l'accoutumée.
Quand l'appel système se termine, lors de l'exécution de ``ret_from_sys_call``,
un test est effectué sur cette file :

* si elle vide, l'appel se termine normalement
* si un signal est en attente, son gestionnaire est exécuté puis l'appel
  est considéré terminé.

Dans le cas d'un appel système *long*, comme l'attente d'un évènement extérieur,
l'appel est annulé et considéré interrompu (**EINTR**).

Un exemple montrant bien ce fonctionnement est l'attente sur un *mutex* :

.. literalinclude:: /_static/code/mutex-EINTR.c
   :language: c
   :linenos:


La méthode de test sur *sigpending* utilisée a pour déclaration :

.. literalinclude:: /_static/code/sigpending_state.c
   :language: c
   :linenos:

Les tâches marquées ininterruptibles renvoient 0 lors du premier test.

Références
==========

* `<http://www.linuxjournal.com/article/4048?page=0,1>`_
* `<http://www.win.tue.nl/~aeb/linux/lk/lk-4.html>`_
* `<http://stackoverflow.com/questions/2299566/internals-of-a-linux-system-call>`_
* `<http://book.opensourceproject.org.cn/kernel/kernelpri/index.html?page=opensource/0131181637/ch03lev1sec4.html>`_

.. _xp-futexes:

Les *futexes*
*************

Les *futexes* (Fast Userspace muTEX) sont des outils de gestion de verrous,
spécifiques à Linux 2.6.

Le principe d'un *futex* est d'être bloqué tant qu'une zone mémoire (un entier)
n'a pas été modifiée.
Ils sont utilisés par la GLIBC pour l'implémentation des sémaphores et autres
mécanismes de synchronisation.

Son application la plus visible est l'implémentation de |join| dans la GLIBC :

#. Lors de la création du thread, la GLIBC appelle la méthode |clone| avec les
   drapeaux :

   * **CLONE_CHILD_SETTID** : le noyau doit inscrire le TID (Thread ID) dans
     la zone mémoire du fils.
   * **CLONE_CHILD_CLEARTID** : le noyau doit remettre à zéro cette zone
     mémoire à la mort du fils.


#. La méthode |join| pose un *futex* sur cette zone.

#. Quand le thread meurt, le noyau remet à zéro la zone mémoire conservant son
   TID : le *futex* de |join| est alors libéré.

Pour plus d'informations : `<http://en.wikipedia.org/wiki/Futex>`_.


Fonctionnement des threads sous Linux
*************************************

Historique
==========

La gestion des threads sous Linux a démarré avec le projet
`LinuxThreads <http://pauillac.inria.fr/~xleroy/linuxthreads/>`_, développé par
X. Leroy (INRIA) en 1996.
Il s'agit apparemment de la première implémentation des threads côté noyau sous
Linux, utilisant l'appel système |clone| pour créer les nouvelles tâches : des
processus partageant l'espace mémoire de leur père.
Elle est toujours utilisée sur les machines utilisant un noyau Linux 2.4.

Étant donnés les problèmes causés par cette implémentation, particulièrement le
fait que les threads ont un identifiant différent du père, gênant la gestion des
signaux, deux nouvelles implémentations ont été développées :

NGPT
   Next Generation POSIX Threads, par des développeurs IBM.
   Ce projet a été abandonné en 2003, lors de la sortie de la version stable de
   NPTL.

NPTL
   Native POSIX Thread Library, par des développeurs RedHat.
   Le principe est le même que pour LinuxThreads : on utilise l'appel |clone|
   pour créer une nouvelle entité.
   L'utilisation des *futexes* pour augmenter les performances implique d'avoir
   un noyau Linux 2.6.

   Pour plus d'informations : `<http://en.wikipedia.org/wiki/Nptl>`_.


Modèles de threading
====================

Modèle N:1
   Utilisé par GNU Portable Threads et utilisable sur les noyaux ne gérant pas
   le threading, ce modèle consiste à n'avoir qu'une entité ordonnançable par
   processus; un module en espace utilisateur (bibliothèque, ...) se chargeant
   alors de choisir  quel code exécuter (ordonnanceur en espace utilisateur).

   Exemple : une application à 4 threads correspond à 1 tâche dans le noyau,
   la biblitohèque gérant les threads se charge d'en exécuter 1 quand elle a
   la main.

Modèle 1:1
   Utilisé par LinuxThreads, NPTL et la plupart des implémentations, c'est le
   modèle le plus simple à concevoir.
   Le principe est d'associer une entité utilisable par l'ordonnanceur pour
   chaque thread de l'espace utilisateur.
   Dans le cas de Linux, il s'agit des tâches (*tasks*) décrites dans la section
   :ref:`kernel-task`.

   Exemple : une application à 4 threads correspond à 4 tâches dans le noyau,
   ordonnancées séparément.

Modèle N:M
   Le modèle le plus complexe.
   Comme dans le modèle N:1, une entité ordonnançable correspond à plusieurs
   threads, mais le bibliothèque gérant la partie espace utilisateur dispose
   de plusieurs de ces entités pour chaque application.
   D'après Wikipedia, il s'agit du modèle utilisé par Windows 7.

   Exemple : une application à 4 threads correspond à 3 tâches dans le noyau,
   pour chaque tâche ayant la main, la bibliothèque gérant les threads exécute
   1 thread.


Une description de ces modèles est disponible sur Wikipedia :
`<http://en.wikipedia.org/wiki/Thread_%28computer_science%29#Models>`_.


GLIBC et uClibC
===============

Sous Linux, il existe deux principales implémentations de la librairie standard
C :

GLIBC
   La GNU Library C est la version de la FSF (Free Software Foundation).
   Sa gestion des threads se base sur NPTL et leur synchronisation est gérée à
   l'aide des *futexes*, ce qui implique d'avoir un noyau Linux 2.6.


uClibC (ou µClibC)
   Il s'agit d'une version orientée vers le matériel embarqué, devant être
   utilisable sur des noyaux Linux 2.4 : elle se base donc sur LinuxThreads.
   Cette bibliothèque utilise les signaux pour synchroniser les threads, en
   utilisant la méthode ``sigsuspend()``.
   Les threads sont gérés par un *manager*, ayant pour tâche d'émettre les
   signaux de synchronisation, ainsi que de préparer la pile de chaque nouveau
   thread...

   La création des threads se base sur l'appel système |clone|.
