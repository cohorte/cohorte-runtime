.. Structure de la plateforme

.. |home| replace:: **PSEM2M_HOME**
.. |base| replace:: **PSEM2M_BASE**


Structure de la plateforme
##########################

La structure de la plateforme PSEM2M est semblable à celle de Catalina (Tomcat).
Le principe est de donner à l'administrateur la possibilité de partager le noyau
de la plateforme entre différentes instances de la plateforme.
Ceci permet notamment de gagner de l'espace disque, de partager des éléments de
configuration et assure en partie l'utilisation d'interfaces de même version en
cas de dialogues entre les instances de plateforme.

L'administrateur devra ainsi définir les variables d'environnement suivantes :

* |home|, au niveau du système, indiquant où se trouve le noyau de la plateforme.
  Cette variable doit obligatoirement être définie pour garantir le bon
  fonctionnement de PSEM2M.
  En cas d'abscence, le répertoire d'exécution courant du script de démarrage
  est considéré comme étant |home|.


* |base|, au niveau de l'application, indiquant où trouver les informations
  spécifiques à une instance de la plateforme.
  Cette variable est facultative : si l'administrateur ne souhaite lancer qu'une
  instance de la plateforme et que |base| n'est pas définie, |home| sera
  utilisée à la place.


Contenu de |home|
*****************

Le contenu du répertoire |home| doit être semblable à celui-ci :

::

   $PSEM2M_HOME
   |-- bin
   |-- conf
   |-- repo
   `-- scripts


Les dossiers présents sont définis comme suit :

bin
   Ce dossier contient les scripts de contrôle de la plateforme (démarrage,
   arrêt, statut, mise à jour, ...).
   Il doit forcément se trouver sous |home|.

conf
   Il s'agit du répertoire contenant la configuration globale de la plateforme.
   Dans le cas où |base| n'est pas définie, il contient également la définition
   des isolats à lancer.

   Sous Unix, si ce dossier n'existe pas ou est incomplet, la plateforme
   cherchera à compléter sa configuration avec les dossiers ``/etc/psem2m`` puis
   ``/etc/default/psem2m``.

repo
   Le dépôt principal de la plateforme contient les bundles communs fournis par
   PSEM2M :

   * le *bootstrap*, démarrant les isolats
   * les plateformes OSGi supportées (Felix, Equinox, ...)
   * les bundles requis pour le fonctionnement de la plateforme (iPOJO, ...)
   * les bundles de contrôle PSEM2M
   * les bundles utilitaires PSEM2M

   L'administrateur peut y ajouter / mettre à jour les bundles communs à tous
   les isolats.
   La mise à jour ne sera effectuée qu'au redémarrage des isolats, ou si la
   commande *update-framework* est utilisée.

   Ce dossier doit forcément être placé sous |home| et contenir le *bootstrap*,
   un framework OSGi et les bundles requis par la plateforme.

scripts
   Ce dossier contient des fichiers scripts modèles permettant d'exécuter PSEM2M
   au démarrage de la machine, à l'aide d'``init`` ou de ``systemd``.

   Il n'est pas nécessaire d'avoir ce dossier pour faire fonctionner la
   plateforme, il n'est présent qu'à titre de modèle.


Contenu de |base|
*****************

Le contenu du répertoire |base| doit être semblable à celui-ci :

::

   $PSEM2M_BASE
   |-- conf
   |-- deploy
   |-- logs
   `-- repo

Les dossiers présents sont définis comme suit :

conf
   Contient la configuration de cette instance de plateforme ainsi que la
   définition des isolats à lancer.

   Cette configuration doit être présente pour l'instance puisse être exécutée
   correctement.

deploy
   Ce répertoire, facultatif, est scruté en permanence par l'outil *FileInstall*
   s'il est activé.

   Tous les bundles ajoutés dans ce dossier seront automatiquement installés et,
   si possible, démarrés.
   À l'inverse, les bundles supprimés seront automatiquement désinstallés de la
   plateforme.

logs
   Répertoire par défaut de stockage des journaux de la plateforme, ce dossier
   est facultatif et n'est créé qu'au besoin.
   L'administrateur peut définir un autre répertoire de journalisation dans le
   fichier de configuration.

repo
   Ce répertoire, facultatif, correspond au dépôt *local* de l'instance de la
   plateforme.

   Il est utilisé en priorité pour rechercher un bundle lors du démarrage d'un
   isolat.
   En cas d'échec, le bundle est ensuite recherché dans le dépôt principal de la
   plateforme.
   De cette manière, une instance de plateforme peut être créée afin de tester
   une nouvelle version d'un bundle.

