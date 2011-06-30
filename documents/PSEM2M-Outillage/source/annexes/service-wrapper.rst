.. Service wrapper

Service Wrapper
###############


Version du projet indépendante d'Eclipse
****************************************

Pour ne plus dépendre du mécanisme d'exécution d'Eclipse, il faut que les
fichiers suivants soient dans la Target Platform :

- org.eclipse.core.runtime_3.6.0.v20100505.jar

- org.eclipse.equinox.preferences_3.3.0.v20100503.jar

- org.eclipse.core.contenttype_3.4.100.v20100505-1235.jar

- org.eclipse.equinox.app_1.3.0.v20100512.jar

La hiérarchie de la plateforme indépendante est la suivante :

- plug-ins : contient les bundles pouvant être chargés par Equinox

- configuration : contient le fichier *config.ini* décrivant le comportement
  d'Equinox pour cette plateforme.

Le fichier *config.ini* peut être basé sur celui que génère Eclipse lors des
exécutions de l'espace de travail.
Il doit cependant inclure des propriétés supplémentaires pour se comporter
correctement :

.. literalinclude:: /_static/eclipse-config.ini
   :language: ini
   :linenos:

Les désignations des répertoires et des fichiers est relatives et prend en
compte le fait que ce fichier est lu depuis le répertoire *configuration*.

La ligne de commande pour exécuter le projet, lancée depuis la racine de la
plateforme est alors :

.. code-block:: bash

   $ java -cp plug-ins/\* org.eclipse.core.runtime.adaptor.EclipseStarter \
      -consoleLog -console -configuration configuration


Configuration de Tanuki Java Service Wrapper
********************************************

On ajoute les fichiers suivants dans la hiérarchie de base :

- bin : répertoire contenant le wrapper exécutable, dépendant de la plateforme
  d'exécution

  - psem2m : fichier script pour Linux

  - wrapper : le wrapper exécutable, dépendant de la plateforme d'exécution

- configuration

  - wrapper.conf : configuration du wrapper

- lib : répertoire contenant les dépendances du wrapper

  - libwrapper.so : bibliothèque native dépendante du système

  - wrapper.jar : liaison Java pour cette bibliothèque

- logs : dossier contenant la journalisation du service -- enregistrement de ses
  sorties standards

Le fichier de configuration *wrapper.conf* est basé sur le modèle disponible
dans le source de Java Service Wrapper : ``src/conf/wrapper.conf.in``

Voici un exemple de fichier *wrapper.conf* :

.. literalinclude:: /_static/tanuki-wrapper.ini
   :language: ini
   :linenos:


Pour tester le wrapper, il faut se placer dans le dossier bin et exécuter la
commande :

.. code-block:: bash

   $ ./wrapper -c ../configuration/wrapper.conf


Configuration de Yet Another Java Service Wrapper
*************************************************

On ajoute les fichiers suivants dans la hiérarchie de base :

- bat : contenant les scripts d'utilisation de YAJSW

- conf : dossier de configuration par défaut

  - wrapper.conf : fichier de configuration du wrapper

- lib : contenant les dépendances du wrapper

  - À copier depuis la distribution de YAJSW

- scripts : contenant des scripts gérant le wrapper

  - À copier depuis la distribution de YAJSW

- wrapper.jar : à copier depuis la distribution de YAJSW

- wrapperApp.jar : à copier depuis la distribution de YAJSW

YAJSW est capable de générer un fichier *wrapper.conf* à l'aide du script
``bin/genConfig``, prenant en paramètre le PID de l'application à wrapper.

Cette technique semble fonctionner de manière satisfaisante, mais tous les
paramètres ne sont pas trouvés.

On peut s'inspirer du fichier de configuration de JSW pour écrire celui YAJSW,
ce dernier se voulant compatible avec le premier.

Exemple de fichier *wrapper.conf* :

.. literalinclude:: /_static/yajsw-wrapper.conf
   :language: ini
   :linenos:
