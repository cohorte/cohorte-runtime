.. Description du fork Linux

.. |fork| replace:: *fork()*

Description du |fork| Linux
###########################

Représentation d'un processus
*****************************

Lorsque nous travaillons dans l'espace utilisateur, nous sommes capables
d'identifier un processus à l'aide de son **PID** (Process ID), représenté sous
la forme d'un entier long.

Dans le noyau, les processus sont identifiés au travers de structures C de type
*pid*.
Le PID connu dans l'espace utilisateur correspond à l'une des valeurs du champ
*nr* de la structure *pid*.
En effet, sous Linux, un processus peut avoir plusieurs PID, en fonction de
l'espace de nommage utilisé.
Nous n'utilisons pas cette notion d'espace de nommage dans notre code.


La structure *pid* est définie dans le fichier **include/linux/pid.h**.
Il est interessant de noter que cette structure dispose d'un champ *tasks*,
contenant la liste de toutes les tâches utilisant ce PID.s


La macro *current*
******************

Afin de repérer quel processus est en cours d'exécution, le noyau Linux fourni
une macro appelée *current*, dépendante de la plateforme, qui retourne un
pointeur vers la structure *task_struct* de la tâche en cours.

Cette macro est définie dans le fichier **current.h** et est utilisée très
souvent dans tout le code, aussi dans le noyau que dans les modules.

Selon l'architecture cible, cette macro retourne une valeur stockée en mémoire
ou dans un registre processeur.

Elle n'est modifiée que par l'ordonnanceur, dans la méthode de changement de
contexte *switch_to*.


Opérations post-fork
********************

À la fin de l'appel de la méthode *do_fork*, le père retrouve son état
d'origine, tandis que celui de son nouveau fils est modifié :

* son registre *eax* est forcé à 0, d'où la valeur de retour de |fork| dans
  un programme en espace utilisateur.

* le drapeau **TIF_FORK** est ajouté à sa tâche, côté noyau.


Le fils est ensuite ajouté dans la *run queue* de la CPU du père
Il est alors connu du système et est prêt à être selectionné par l'ordonnanceur.

On obtient alors le comportement suivant :

#. La tâche taguée est repérée par l'ordonnanceur lors de sa première
   prise en charge, au travers de la méthode changement de contexte *switch_to*.

#. C'est cette méthode qui appelle alors une macro dépendante de la plateforme,
   un code assembleur brut dans le cas des x86.

#. Ce code prépare les registres du processeur avant d'appeler une méthode
   *ret_from_fork* définie dans le fichier *kernel/entry.S* de l'architecture
   cible.

#. Il est important de noter de *ret_from_fork* se termine par un saut à
   *ret_from_syscall*, qui se trouve dans la méthode *system_call* du même
   fichier : on effectue donc un saut long entre méthodes du noyau...


Déclarations utiles
*******************

.. warning:: Les lignes indiquées ici ne sont pas forcément homogènes et
   correspondent soit au noyau 2.6.35.10, soit au noyau 2.6.37.

Nous recommandons l'utilisation d'un outil spécialisé dans la recherche de
références dans le noyau Linux, tel que
`Tomoyo cross-references <http://tomoyo.sourceforge.jp/cgi-bin/lxr/ident>`_.


**Déclaration des structures / macros intéressantes :**

.. tabularcolumns:: |p{5cm}|p{8cm}|p{2cm}|

+-----------------+----------------------------------------------------+-------------+
| Elément         | Fichier                                            | Ligne       |
+=================+====================================================+=============+
| current         | Dépend de l'architecture (include/ asm/ current.h) |             |
+-----------------+----------------------------------------------------+-------------+
| SYSCALL_DEFINE1 | include/ linux/ syscalls.h                         | 192         |
+-----------------+----------------------------------------------------+-------------+
| signal_struct   | include/ linux/ sched.h                            | 523 / 519   |
+-----------------+----------------------------------------------------+-------------+
| task_struct     | include/ linux/ sched.h                            | 1167 / 1182 |
+-----------------+----------------------------------------------------+-------------+
| rq              | kernel/ sched.c                                    | 448         |
+-----------------+----------------------------------------------------+-------------+
| cfs_rq          | kernel/ sched.c                                    | 312         |
+-----------------+----------------------------------------------------+-------------+


**Position des méthodes utiles à la compréhension du |fork| de Linux 2.6 :**

.. tabularcolumns:: |p{3cm}|p{6cm}|p{2cm}|p{4cm}|

+-------------------+---------------------------------------------------------+-------------+---------------------------+
| Méthode           | Fichier                                                 | Ligne       | Description               |
+===================+=========================================================+=============+===========================+
| sys_clone         | linux-src/ arch/ um/ sys-x86_64/ syscalls.c             | 82          |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| sys_clone         | linux-src/ arch/ um/ sys-i386/ syscalls.c               | 22          |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| sys_clone2        | linux-src/ arch/ ia64/ kernel/ entry.S                  | 109         | Appel différent sur IA64  |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| do_clone          | glibc-src/ nptl/ sysdeps/ pthread/ createthread.c       | 50          |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| do_fork           | linux-src/ kernel/ fork.c                               | 1384        | Le vrai gestion de fork() |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| __libc_fork       | glibc-src/ nptl/ sysdeps/ unix/ sysv/ linux/ fork.c     | 53          |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| ARCH_FORK         | glibc-src/ nptl/ sysdeps/ unix/ sysv/ linux/ sh/ fork.c | 25          |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| __clone           | glibc-src/ sysdeps/ unix/ sysv/ linux/ i386/ clone.S    | 49          |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| copy_process      | linux-src/ kernel/ fork.c                               | 972         | Le cœur du fork()         |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| copy_thread       | Appel dans copy_process                                 | 1159        | La copie des informations |
|                   |                                                         |             | du processus dupliqué     |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
|                   | => arch/ x86/ kernel/ process_32.c                      | 195         |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
|                   | => arch/ x86/ kernel/ process_64.c                      | 259         |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| wake_up_new_task  | kernel/ sched.c                                         | 2514 / 2800 |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| get_cpu           | include/ linux/ smp.h                                   | 177         |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| task_rq_lock      | kernel/ sched.c                                         | 955 / 958   |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| task_cpu          | include/ linux/ sched.h                                 | 2412        |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| task_rq_unlock    | kernel/ sched.c                                         | 976 / 979   |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
| sched_info_queued | kernel/ sched_stat.h                                    | 220         |                           |
+-------------------+---------------------------------------------------------+-------------+---------------------------+
