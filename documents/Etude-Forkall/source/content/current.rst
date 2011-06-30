.. État actuel du projet

.. |forkall| replace:: ``forkAll()``

État actuel du projet
#####################

Le projet est actuellement arrêté, ce depuis le début du mois de Mai 2011.

En l'état, l'implémentation de |forkall| permet de dupliquer tout un processus,
threads enfant compris, en faisant de plus une copie profonde de la table des
fichiers.
Nous sommes donc capable de nous retrouver avec deux processus théoriquement
viables.
Cependant, le processus résultant de la duplication meurt d'une faute de
segmentation dès qu'il est démarré.


Blocage
*******

Le principal problème vient des appels systèmes en cours sur le processus
à dupliquer.
Même quand il est en attente d'un évènement, un processus est en réalité en
attente sur un futex (voir :ref:`xp-futexes`), c'est-à-dire en cours d'appel
système.
Quand un appel système se termine, la mémoire utilisée pour retrouver la pile
et revenir dans le processus est celle avec laquelle l'appel a été initié,
ce qui est logique.

Après un |forkall|, l'appel système du processus père peut se terminer sans
problème : il est dans un état standard.

Au contraire, celui du processus fils essaiera de remonter dans l'espace
utilisateur en utilisant une zone mémoire qui ne lui appartient plus (ce qui
provoque un segfault), mais surtout dont le contenu ne lui correspond pas.
En effet, le père ayant certainement modifié sa pile après son retour d'appel
système, les informations sur l'état du processus au moment de l'appel auront
disparu.


Références
**********

Pour plus d'information sur ce qui a été retenu du projet, voir la section
:ref:`xp-acquise`.
