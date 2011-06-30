.. Documentation de la 1ere version du forkit
.. highlight:: c

.. |fork| replace:: *fork()*
.. |forkit| replace:: *forkit()*

Implémentation du |forkit| - Première version
=============================================

Principe
--------

Le principe de cette première version de l'appel système |forkit| est de copier
et modifier le code source du |fork| afin qu'il permette de dupliquer un
processus autre que l'appelant.


Modification du noyau - Partie 1
--------------------------------

Ajout des méthodes
^^^^^^^^^^^^^^^^^^

La méthode |forkit| agit exactement comme la méthode |fork|, mais remplace 
toute utilisation de la macro  *current* par un paramètre indiquant la tâche à 
utiliser comme source.

Ce travail est important en terme de quantité : quasiment toutes les fonctions
du fichier **fork.c** utilisent cette marco

Dans cette première version, nous ne prendrons pas *parent_process* en paramètre
de |forkit| : il ne s'agira que d'une copie de la valeur de *current*.
Nous aurons ainsi exactement le même comportement que |fork|, ce qui nous
permettra de valider plus facilement notre implémentation.


Pour ajouter la méthode |forkit| :

#. on copie le fichier **fork.c** en **forkit.c**

#. on remplace le code de *do_fork()* par celui de *do_forkit()*

#. on remplace toutes les occurrences de *current* par un argument
   *parent_process* dans chaque méthode de **forkit.c**.
   Étant donné que nous sommes en C, nous ajoutons le suffixe *_2* à 
   toutes les méthodes que l'on modifie. 

#. on supprime les variables globales et les méthodes non modifiées, sous peine 
   d'avoir des erreurs de duplication lors de compilation.
   
   **Attention**, ce nettoyage implique des modification du fichier **fork.c**
   (suppression du modifieur ``static``) afin de pouvoir accéder à toutes
   les variables et méthodes nécessaires à |forkit| :
   
   #. on transforme les définitions de ces méthodes en déclarations externes
   
   #. on transforme les définitions et déclarations de variables globales non
      statiques en déclarations externes.
   
   #. même chose pour les variables globales statiques, en ayant pris soin de 
      les rendre non statiques dans les fichiers où elles sont déclarées.


.. note:: Variables globales statiques ou externes en C :

   * une variable globale *static* n'est accessible que dans le fichier où elle
     est déclarée et définie.
   * une variable globale *extern* est la déclaration d'une variable globale
     **non statique** définie à un autre endroit (dans le même fichier ou un
     autre)


Ajout de l'appel système
^^^^^^^^^^^^^^^^^^^^^^^^

Cette partie a été faite en suivant les instructions de [#add_syscall_1]_.
Étant donné qu'il s'agit d'un test, seule l'architecture x86_64 est prise en
compte.


#. on ajoute la déclaration du numéro d'appel système dans
   **arch/x86/include/asm/unistd_64.h** : ::

      #define __NR_forkit 300
      __SYSCALL(__NR_forkit, stub_forkit)

   On copie le mécanisme de déclaration de |fork| en indiquant *stub_forkit*
   comme il est indiqué *stub_fork*.


#. on définit *sys_forkit* dans **arch/x86/include/asm/syscalls.h** : ::

      int sys_forkit(struct pt_regs *);


#. on déclare la fonction *sys_forkit* dans le fichier 
   **arch/x86/kernel/process.c** : ::

      // Définition du cœur de forkit
      extern long do_forkit(unsigned long clone_flags,
         unsigned long stack_start,
         struct pt_regs *regs,
         unsigned long stack_size,
         int __user *parent_tidptr,
         int __user *child_tidptr);
      
      int sys_forkit(struct pt_regs *regs)
      {
         return do_forkit(SIGCHLD, regs->sp, regs, 0, NULL, NULL);
      }

#. on ajoute le fichier **forkit.c** au fichier **kernel/Makefile** : ::

      obj-y += forkit.o


Modification du noyau - Partie 2
--------------------------------

Modification des conventions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Après quelques lectures, nous avons pu apercevoir quelques particularité de 
l'appel système |fork|, notamment le fait de ne pas enregistrer l'appel dans
le fichier **linux/syscalls.h** et de ne pas utiliser un nom standard,
*stub_fork*, pour être déclaré dans **asm/unistd.h**.

Nous avons donc modifié les fichiers comme suit :

+--------------------------------------+-------+-------------------------------------+---------------------------------------------------+
| Fichier                              | Ligne | Ancien                              | Nouveau                                           |
+======================================+=======+=====================================+===================================================+
| arch/ x86/ include/ asm/ unistd_64.h | 668   | __SYSCALL(__NR_forkit, stub_forkit) | __SYSCALL(__NR_forkit, sys_forkit)                |
+--------------------------------------+-------+-------------------------------------+---------------------------------------------------+
| include/ linux/ syscalls.h           | 830   | (inexistant)                        | // asmlinkage long sys_forkit(unsigned long pid); |
+--------------------------------------+-------+-------------------------------------+---------------------------------------------------+

La dernière modification prépare l'emplacement où devra être défini l'appel 
système lorsque son implémentation sera indépendante de la plateforme.
De cette manière, il sera accessible depuis les fichiers d'en-tête du noyau et
donc pour toutes les applications à compiler sur ce noyau.


Ajout d'un paramètre à l'appel système
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Le but est de remplacer ``parent_process = current`` par un passage de paramètre
à l'appel système.

Parmi les choses à savoir à ce sujet, sur processeur de type x86 :

* lors d'un appel système, le numéro d'appel est placé dans le registre **eax**

* les paramètres sont placés dans les registres suivants (**ebx**, **ecx**, ...)
  Côté utilisateur, la méthode *syscall()* s'en charge.
  Côté noyau, nous n'avons pas à nous soucier des paramètres, ils sont
  apparemment automatiquement lus.

* les registres du processeur sont accessibles en lecture côté noyau via
  l'argument de type ``struct pt_regs``, dont les champs de type long portent le
  nom du registre correspondant.


On choisit de modifier la signature actuelle de la méthode *do_forkit* dans le
fichier **forkit.c** en ajoutant le paramètre *pid* :

::

   long do_forkit(
      unsigned long pid,
      unsigned long clone_flags,
      unsigned long stack_start,
      struct pt_regs* regs,
      unsigned long stack_size,
      int __user *parent_tidptr,
      int __user *child_tidptr)
   {
      // ...
   }


Nous devons alors mettre à jour les autres fichiers :

+-------------------------------------+-------+---------------------------------------+----------------------------------------------------------+
| Fichier                             | Ligne | Ancien                                | Nouveau                                                  |
+=====================================+=======+=======================================+==========================================================+
| arch/ x86/ include/ asm/ syscalls.h | 25    | int sys_forkit(struct pt_regs \*);    | int sys_forkit(unsigned long pid, struct pt_regs\*);     |
+-------------------------------------+-------+---------------------------------------+----------------------------------------------------------+
| arch/ x86/ kernel/ process.c        | 245   | int sys_forkit(struct pt_regs \*regs) | int sys_forkit(unsigned long pid, struct pt_regs\* regs) |
+-------------------------------------+-------+---------------------------------------+----------------------------------------------------------+

.. _forkit-eval-1:

Évaluation du résultat
^^^^^^^^^^^^^^^^^^^^^^

On utilise un programme appelant directement l'appel système *sys_forkit* pour
tester le fonctionnement du programme.
Étant donné que nous n'avons pas modifié tous les headers nécessaires, nous
appelons *sys_forkit* via son numéro d'appel, à savoir **300**, en lui passant
un paramètre.

.. literalinclude:: /_static/tests/forkit_1.c
   :language: c
   :linenos:


Référence
---------

.. [#add_syscall_1]  Ajouter un appel système au noyau 2.6 : `<http://courses.cs.vt.edu/~cs3204/spring2004/Projects/2/InstructionsToAddSystemCallToLinux_rev00.pdf>`_
.. [#add_syscall_2]  Ajouter un appel système au noyau 2.6 : `<http://www.csee.umbc.edu/courses/undergraduate/CMSC421/fall02/burt/projects/howto_add_systemcall.html>`_
