.. Description du forkAll

.. |fork| replace:: ``fork()``
.. |forkit| replace:: ``forkit()``
.. |forkall| replace:: ``forkAll()``

Documentation du |forkall|
==========================

Description
-----------

Le principe est de simuler la méthode |forkall| présente sur OpenSolaris, à
savoir un |fork| dupliquant tous les threads du processus appelant et non pas
uniquement le thread effectuant l'appel système.

L'implémentation que nous tentons de développer doit être capable d'effectuer
le |forkall| sur un processus "distant", c'est-à-dire un processus autre que
l'appelant.


Méthodes d'implémentation
-------------------------

Via un module / pilote Linux
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Cette technique est en cours de développement sur le noyau 2.6.35, testé sur
une Ubuntu 10.10.

Le problème de cette technique est que nous devons implémenter intégralement 
la méthode |fork| à l'extérieur du noyau, en s'inspirant de l'original.
Ceci implique la copie toutes les méthodes qui ne sont pas exportées par le
noyau, or celles-ci sont nombreuses (environ une centaine de  méthodes à
importer), ce qui engendre d'autres problèmes :

* le module doit contenir une copie des définitions de structures présentes dans
  le noyau utilisé, ce qui implique que le code du module doit être mis à jour à
  chaque nouvelle version du noyau.
 
* le module doit contenir une copie des méthodes non exportées du noyau, et ne
  profite donc pas automatiquement des nouvelles techniques fournies par le
  noyau.
  Le comportement du |forkall| peut alors devenir très différent de celui du
  |fork|, notamment en omettant la mise à jour de certains champs.

* les méthodes copiées depuis le noyau peuvent travailler sur des variables
  globales devant être redéfinies dans le module, afin qu'il puisse être
  compilé.
  On peut citer l'exemple de l'ordonnanceur, qui utilise une file d'attente par
  CPU, déclarée dans le fichier *sched.c*.
  La re-déclaration de ces files peut impliquer l'utilisation d'un autre
  ordonnanceur par le module, voire faire planter l'ordonnanceur utilisé par le
  noyau en lui demandant des traitements sur une tâche qu'il ne gère pas.   


De fait, cette technique a été mise de côté au profit de l'implémentation d'un
nouvel appel système, directement dans le noyau.


Via un patch du noyau Linux
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Cette technique est basée sur l'implémentation de l'appel système |forkit|.

Le principe est d'ajouter à cet appel une logique de copie de tous les threads 
de la tâche mère, alors qu'ils sont à l'arrêt.
De cette manière, nous aurions une duplication complète du processus cible.

Cette implémentation est expliquée en détail dans la section 
:ref:`forkall-kernel`.
