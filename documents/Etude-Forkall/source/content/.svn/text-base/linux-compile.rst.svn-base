.. Documentation sur la compilation de noyau Linux
.. highlight:: bash

.. |fork| replace:: *fork()*
.. |forkit| replace:: *forkit(pid_t)*
.. |forkall| replace:: *forkAll(pid_t)*


Compilation de noyau Linux (Ubuntu)
===================================

Les références utilisées pour effectuer cette compilation sont indiquées en fin
de document.

Introduction
------------

Ce document a pour but de montrer comment a été effectuée la compilation d'un
noyau Linux, configuré pour Ubuntu, afin d'ajouter les appels systèmes |forkit| 
et |forkall|.


Pour rappel, la méthode |forkit| correspond à un appel à |fork| sur un processus
autre que l'appelant, tandis que |forkall| duplique tous les threads du
processus ciblé.
Ces méthodes seront décrites en détail plus loin dans ce document.


Recompilation simple du noyau
-----------------------------

Cette partie a été réalisée selon la documentation du site 
Ubuntu-fr.org [#compile_lucid]_

Installation des outils de compilation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

La configuration des sources du noyau se fait à l'aide de l'outil ``fakeroot`` 
et par l'utilisation de ``make menuconfig``.

.. note:: La configuration par *menuconfig* nécessite *libncurses-dev*.

::

   # Installation des outils de compilation
   $ sudo apt-get install fakeroot kernel-wedge build-essential makedumpfile kernel-package
   # Installation des paquets de librairies recommandés
   $ sudo apt-get build-dep --no-install-recommends linux-image-$(uname -r)
   # Installation de ncurses pour la configuration
   $ sudo apt-get install libncurses-dev

Une fois ces outils installés, on est capable de configurer et compiler un 
noyau Linux.


Récupération des sources du noyau
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Le noyau à compiler sera celui de la version d'Ubuntu en cours d'utilisation.
À l'heure de la rédaction de ce document, nous utilisons Ubuntu 10.10, avec 
un noyau **2.6.35.10**. 

On récupère les sources de ce noyau dans un dossier utilisateur, afin de ne pas
avoir à faire toutes les opérations en mode super-utilisateur : ::

   # Création du répertoire qui contiendra les sources
   $ mkdir ~/src && cd ~/src
   # Téléchargement et décompression des sources du noyau actuel
   $ apt-get source linux-image-$(uname -r)
   $ cd linux-$(uname-r)

.. note:: Les sources du noyau pèsent environ 80 Mo


Ajout du fichier de configuration de base
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Afin de ne pas partir de zéro et pour correspondre au mieux au système déjà en
place, on copie la configuration du noyau en cours de fonctionnement pour 
préparer la compilation :

::

   $ cp /boot/config-`uname -r` .config

On peut maintenant personnaliser la configuration avec la commande :

::

   $ make menuconfig


.. note:: Il est recommandé de désactiver l'option *Kernel debugging* dans la
      section *Kernel hacking*, sous peine de générer un répertoire de
      compilation de plusieurs Giga-octets.


Compilation (1ère fois)
^^^^^^^^^^^^^^^^^^^^^^^

On commence par nettoyer le dossier de compilation, pour éviter la moindre 
erreur, puis on lance la compilation proprement dite :

::

   # Nettoyage
   $ fakeroot ./debian/rules clean
   # Compilation et génération des paquets .deb
   $ CONCURRENCY_LEVEL=4 skipabi=true fakeroot make-kpkg --initrd \
      --append-to-version=-test kernel-headers


* **CONCURRENCY_LEVEL** correspond au nombre de tâches parallèles utilisées 
  pour effectuer la compilation. Pour une vitesse optimale, il doit valoir
  le nombre de cœurs disponibles + 1.

* **skipabi** désactive les tests ABI, ce qui réduit le nombre d'erreurs 
  possibles au cours d'une compilation.

.. note:: Une compilation de noyau de cette manière prendre entre 10 minutes et
   1 heure (voire plus) selon les machines.

On obtient alors deux paquets .deb dans le répertoire parent, que l'on peut
installer en utilisant les commandes :

::

   $ sudo dpkg -i \
      # Installation du noyau
      linux-image-2.6.35.10-forkit-thomas_2.6.35.10-*.deb \
      # Installation des en-têtes C
      linux-headers-2.6.35.10-forkit-thomas_2.6.35.10-*.deb

Il ne reste alors plus qu'à redémarrer la machine sur le nouveau noyau.


Compilation (après modifications)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Le script de compilation se chargera de sélectionner les modules à re-compiler,
il n'est donc plus nécessaire de nettoyer les répertoires avant la compilation.

Comme précédemment, on utilise la commande suivante pour lancer la compilation :

::

   $ CONCURRENCY_LEVEL=4 skipabi=true fakeroot make-kpkg --initrd \
      --append-to-version=-test kernel-headers


Il ne faut pas hésiter à faire le tri dans les modules à compiler pour gagner du
temps : le noyau en lui-même met moins de 5 minutes à compiler, mais il y a
plus de 1900 modules sélectionnés dans la configuration par défaut du noyau
Ubuntu.


Références
----------

.. [#compile_lucid] Compiler un noyau Ubuntu 10.10 : `<http://doc.ubuntu-fr.org/tutoriel/compiler_kernel_ubuntu_lucid>`_ 
