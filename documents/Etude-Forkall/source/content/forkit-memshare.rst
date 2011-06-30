.. Documentation des isolats à partage de segments de code

========================================
Lancement d'isolats par mémoire partagée
========================================

Principe
--------

Le principe est de démarrer les isolats à partir d'une image d'application à 
l'arrêt préalablement chargée en mémoire.

L'objectif est de rester le plus possible en espace utilisateur afin de :

* éviter une intrusion trop forte dans le code du noyau : il n'est plus 
  nécessaire d'ajouter un appel système.

* éviter un nombre important de changements de contexte : il ne s'agit plus
  que d'une copie mémoire et d'un saut long.


Vue schématique
^^^^^^^^^^^^^^^

Ci-dessous un schéma décrivant l'algorithme que nous comptons utiliser pour
implémenter ce principe :

.. figure:: /_static/images/algo_mem_share.png
   
   Vue schématique de l'algorithme

#. Le moniteur démarre, se configure et ouvre un tube de communication, à l'aide
   de *pipe()*.

#. Le moniteur se duplique à l'aide de *vfork()*, en vue  d'exécuter un
   programme cible avec *execl()*.
   Le processus père se met en attente avec *wait()*, le processus fils devient
   une image.

#. L'image démarre et se configure : c'est le chargement du *bootstrap*.

#. Elle recherche les informations la concernant dans sa zone mémoire. La 
   structure *task_struct* que nous souhaitons utiliser se trouve avant le tas
   dans la zone mémoire du processus.

#. Une fois configurée, elle envoie un signal **SIGUSR1** au moniteur en 
   utilisant la méthode *kill()*.
   À partir d'ici, l'ordre des opérations n'est pas prévisible :
   
   * Elle écrit dans le tube les informations lui correspondant (*task_struct*, 
     etc.)

   * Elle utilise la méthode *pause()* pour s'arrêter, en attente d'un signal.

   * Le processus père est interrompu pour gérer le signal **SIGUSR1**.

   * Il récupère les informations concernant l'émetteur du signal et lit les
     paramètres qui ont été écrits dans le tube.
   
   Le processus père lance alors un isolat :

#. Il prépare les variables qui informeront le processus fils de son état,
   c'est-à-dire le fait qu'il soit un isolat et quelle configuration devra être
   appliquée.

#. Il se duplique à l'aide de *fork()*.
   Le processus père se met en attente d'ordre, le processus fils devient un
   isolat.

#. L'isolat démarre et se configure selon les variables qui lui sont 
   accessibles afin d'avoir un état proche de celui de l'image (pile comprise)

#. Il effectue le saut dans le segment de code de l'image, juste avant
   l'exécution de *pause()*. Il est alors en attente d'un signal.

#. Le père annule la pause de l'isolat en lui envoyant un signal **SIGCONT**.

La manière dont le père décide à quel moment il effectue le dernier point reste 
à définir.
Il faut en effet qu'il soit certain que l'isolat est en attente d'un signal,
car s'il émet **SIGCONT** trop tôt, ce dernier ne sortira jamais de son attente.


Tests de faisabilité
--------------------

Adresses lisibles en espace utilisateur
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Le premier point à tester est la possibilité pour un programme de connaître 
l'emplacement et la structure de sa zone mémoire.

Pour cela, nous avons utilisé le programme ci-dessous :

.. literalinclude:: /_static/tests/addresses.c
   :language: c
   :linenos:

La première boucle (commentée) permet d'accéder à la partie *statique* du 
processus, à savoir l'en-tête ELF, les variables globales assignées ou non, etc.
La seconde boucle lit les données accessibles autour de la pile.


Ce programme génère notamment des sorties de la forme : ::

    i : 0x7fff641f7be0 ; ptr : 0x60103c ; ptr2 : 0x601038 ; main : 0x400564

Comme on peut le voir, *ptr*, *ptr2* et *main* ont des adresse relatives à la
position actuelle du code en mémoire, tandis que *i* semble avoir une position
absolue.


Pour pouvoir nous approcher du résultat escompté, nous devons être en mesure de
connaître l'adresse absolue d'une fonction, comme *main*, et donc de connaître
l'adresse de base du processus en mémoire.

Pour cela, nous devrions trouver cette information dans les champs *mm* ou
*active_mm* de la structure *task_struct* côté noyau.
Celle-ci serait présente dans la zone mémoire du processus (voir 
[#process_descr]_), et serait donc potentiellement lisible.

Malheureusement, cette structure dépend fortement de la configuration de 
compilation du noyau et serait donc difficile à décrire dans l'espace 
utilisateur.
De plus, les utilisations de *task_struct* lors de l'implémentation de 
*forkit()* montrent que les champs à utiliser sont souvent nuls, ce qui 
compromet énormément notre raisonnement.
Nous pourrions cependant utiliser d'autres champs permettant de calculer les
écarts entre adresses, comme le pointeur vers le début de la pile, *stack*.

Enfin, il ne faut pas oublier que les accès en mémoire depuis l'espace 
utilisateur sont contrôlés et provoquent l'arrêt immédiat du processus par une
**segmentation fault** en cas de lecture en zone interdite.


Communications inter-processus
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Il s'agit plus ici d'un rappel que d'une étude de faisabilité.
Le but est de savoir s'il est possible d'obtenir suffisamment d'informations et
d'avoir suffisamment de contrôle pour arriver à nos fins.

Pour rappel, le moniteur doit être capable de récupérer le PID du processus qui
a émis un signal.

Nous avons donc utilisé la fonction *sigaction()* au lieu de  *signal()*, cette 
dernière étant trop simplifiée et ne donnant aucune information.
En effet, lorsqu'un signal est géré, *sigaction()* dispose d'un paramètre
supplémentaire de type *siginfo*, contenant notamment le PID et l'UID de
l'émetteur, ainsi que des informations sur le contexte du récepteur lors de son 
interruption.

Ci-dessous une version simplifiée du moniteur :

.. literalinclude:: /_static/tests/sigaction.c
   :language: c
   :linenos:



Références
----------

.. [#process_descr] Process descriptors handling : `<http://book.opensourceproject.org.cn/kernel/kernel3rd/opensource/0596005652/understandlk-chp-3-sect-2.html>`_
