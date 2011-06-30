.. Documentation de la 2e version du forkit
.. highlight:: c

.. |fork| replace:: *fork()*
.. |forkit| replace:: *forkit()*

Implémentation du |forkit| - Deuxième version
=============================================

Principe
--------

Le principe de cette implémentation du |forkit| est d'encadrer un appel du cœur
de |fork| plutot que d'en faire une modification.
Ainsi, nous profitons des mises à jour du |fork| sans avoir à modifier notre 
code.

De plus, nous réduisons notre empreinte dans le noyau : nous modifions moins de 
fichiers car nous n'avons plus besoin de rendre visible toutes les fonctions
utilisées par |fork|.

Enfin, cette méthode nous permet d'être beaucoup moins dépendant de 
l'architecture utilisée, sans pour autant en être totalement indépendant.


.. _forkit-algo:

Algorithme de |forkit|
----------------------

Nous avons créé un fichier **forkit.c** comportant une méthode *do_forkit()*,
afin de ne pas avoir à modifier la déclaration de l'appel système écrit lors de
la première implémentation.

Le contenu de la méthode consiste à :

#. Récupérer les informations sur le processus appelant le |forkit|, en 
   utilisant la macro *current*.

#. Récupérer les informations sur le futur père, en utilisant la méthode
   *find_task_by_vpid()* avec le PID fourni en paramètre.

#. Sauvegarder l'état du registre **eax** du père.
   Ainsi, nous pourrons réduire les effets de bords du |fork|, qui force ce
   registre à 0 pour le fils.
   
   **Attention :** cette étape dépend de la plateforme cible.

#. Changer le contexte : nous forçons la valeur de retour de la macro *current*
   afin qu'elle renvoie un pointeur vers le furure père et non vers l'appelant.

#. Appeler le coeur de |fork|, c'est-à-dire *do_fork()*.
   Cette méthode renvoie le **PID** du processus fils.

#. Récupérer les informations sur le fils.
   Pour cela nous utilisons encore une fois *find_task_by_vpid()*, avec le PID
   du fils.

#. Restaurer l'état des registres et le statut du fils.
   Nous forçons la valeur du registre *eax* pour avoir le même état que dans
   le processus père.
   Encore une fois, cette étape dépend de la plateforme cible.

#. Changer le context : nous arrêtons de forcer la valeur de retour de
   *current*, qui retrouve son comportement original.

#. Retourner le *pid* du fils : le processus appelant connait alors le PID
   du processus fils.s


Évaluation du résultat
----------------------

L'évaluation du résultat se fait légèrement différemment par rapport à la 
première  implémentation (voir :ref:`forkit-eval-1`).

En effet, c'est désormais à l'appelant de gérer l'arrêt et le redémarrage des
processus père et fils, à l'aide de *kill(SIGSTOP)* et *kill(SIGCONT)*.

De plus, nous introduisons une pause de 50 ms (*usleep(50)*) afin de laisser le
temps au système et au processus cible de gérer le signal **SIGSTOP** avant 
l'appel à |forkit|.

.. literalinclude:: /_static/tests/forkit_2.c
   :language: c
   :linenos:


Problèmes rencontrés
--------------------

Récupération de la valeur des registres
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Pour sauvegarder l'état d'une tâche avant l'appel à *do_fork* et le restaurer
ensuite, nous devons récupérer le contenu des registres processeur.

Les valeurs de ces registres sont enregistrés dans l'espace mémoire de la tâche,
à côté de la structure *task_struct*, avant le tas.

Au début du développement, nous avons utilisé une méthode spécifique à 
l'architecture x86 64 bits, en utilisant le code :

::

   regs = ((struct pt_regs *) (THREAD_SIZE + task_stack_page(task))) - 1;


Après quelques recherches, nous avons trouvé une macro permettant de masquer
cette opération et par conséquent d'être légèrement moins dépendant de la
plateforme cible :

::

   regs = task_pt_regs(task);

Cette macro est définie dans le fichier **processor.h**.


Processus fils zombie
^^^^^^^^^^^^^^^^^^^^^

|forkit| quand le père est en cours de fonctionnnement...
"""""""""""""""""""""""""""""""""""""""""""""""""""""""""

Si nous supprimons l'étape *arrêter le père*, le processus fils est bien créé,
mais le système le considère zombie.
On remarquera également dans les traces que le processsus fils a pourtant bien
été lancé car il s'arrête à cause d'un *segfault*.

L'arrêt du père se faisait à l'aide de *__set_task_state(parent, TASK_STOPPED)*,
or cet appel ne peut garantir un arrêt immédiat de la tâche cible.
En effet, cette tâche ne sera stoppée que lorsque l'ordonnanceur sera de nouveau
appelé pour sélectionner les tâches en cours d'exécution.

C'est pour cela que nous avons choisi de faire stopper manuellement le processus
père en espace utilisateur, avant l'appel à |forkit|.


À la fin de l'exécution du fils
"""""""""""""""""""""""""""""""

Si le fils fonctionne correctement après un |forkit|, celui-ci devient zombie
lorsqu'il termine son exécution.

Le processus père n'étant pas prévu pour être dupliqué, il n'attend pas le
retour de son fils : ce dernier devient donc zombie, car aucun processus ne
s'est chargé de libérer ses ressources et signaler son arrêt au système.

La solution pour éviter ce problème consiste à indiquer que le père du processus
issu de |forkit|, est son *grand-père* (le père de son père).

De cette manière, en considérant que le *grand-père* est le processus ayant 
appelé la méthode |forkit|, celui-ci s'attend au retour d'un *petit-fils*, et
permet sa terminaison complète en attendant sa valeur de retour à l'aide d'un
*wait()*.

Cette modification est effectuée en ajoutant **CLONE_PARENT** aux drapeaux de
clonage passés à *do_fork()*.

Plus d'informations sur les processus zombies ici : [#wiki_zombie]_.


Gel de la machine
^^^^^^^^^^^^^^^^^

Si nous arrêtons le processus père avant le changement de contexte, la machine
gèle, sans *kernel panic*.

La raison exacte de ce problème n'a pas été trouvée, mais il peut s'agir soit
d'un verrouillage mortel, soit d'un appel manquant à l'ordonnanceur, stoppant
ce dernier.


Opérations post-|fork|
^^^^^^^^^^^^^^^^^^^^^^

À chaque cycle, l'ordonnanceur détermine si le processus sélectionné est issu 
d'un |fork| et effectue une opération particulière dans ce cas.

Il retourne notamment vers l'espace utilisateur en considérant qu'il s'agit 
d'un retour depuis un appel système (avec interruption ``int 80h`` sur x86) et
modifie donc l'état du processus fils.

Pour éviter ceci, nous supprimons le drapeau **_TIF_FORK** des drapeaux du 
processus fils, celui-ci étant ajouté lors de la copie du thread appelant, dans
la méthode *copy_thread()*.

De cette manière, le processus fils n'a pas conscience d'avoir été forké.


Cache de la libc
^^^^^^^^^^^^^^^^

Afin d'éviter d'effectuer inutilement des appels systèmes et donc des 
changements de contexte, la libc se charge de mettre en cache un maximum 
d'informations constantes sur les processus, notamment leur PID.

Lorsque l'on appelle un |fork| en passant par la libc, celle-ci est consciente
que des modifications ont lieu sur le processus et met donc à jour le cache.

Dans notre cas, nous passons directement par un appel système, sans avertir la
libc de nos modifications.
Ainsi, la méthode *getpid()* renvoie la même valeur pour le père et ses fils 
issus d'un |forkit| car le cache n'a pas été mis à jour.


Références
----------

.. [#wiki_zombie] Processus zombie (Wikipedia) : `<http://fr.wikipedia.org/wiki/Processus_zombie>`_ 
