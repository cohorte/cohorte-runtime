.. Gestion de projet avec Eclipse, Maven et Tycho

Gestion de projet avec Eclipse, Maven et Tycho
##############################################


Gestion de projet avec Eclipse PDE
**********************************

La gestion des dépendances du projet se fait en passant par Eclipse, en
modifiant le fichier Manifest lui correspondant.

Ces dépendances doivent être des bundles OSGi ou des Features Eclipse -- un
regroupement de plug-ins Eclipse -- présentent dans le dossier plug-ins de la
Target Platform du projet.


Installation de Maven 3
***********************

La version utilisée lors de l'édition de ce document est la 3.0.0.

Cette installation doit être utilisée en remplacement de Maven 2 afin d'être
certain d'utiliser cette version et donc être capable d'utiliser le plug-in
Tycho.

La principale modification de Maven 3 pouvant entrainer des problèmes de
lecture de fichiers POM de Maven 2 est une lecture plus stricte des fichiers et
un comportement différent vis à vis des plug-ins et dépendances dont le numéro
de version a été omis.

Le plug-in Maven pour Eclipse est compatible avec Maven 3.


Création du POM parent -- Installation implicite de Tycho
*********************************************************

Comme tout plug-in Maven disponible sur les dépôts publics, il suffit d'indiquer
dans un fichier POM qu'on utilise ce plug-in pour qu'il soit automatiquement
installé -- dans le dossier dépôt local -- lors de sa première utilisation.

Le fichier POM parent doit indiquer le chemin relatif vers les modules qui
devront être compilés. L'ordre de ces modules importe peu car Maven se chargera
de trouver un ordre en fonction de leurs inter-dépendances.
Il contient également la liste des plug-ins et des dépendances partagées.

Grâce à l'utilisation de Tycho, les dépendances indiquées dans ce fichier ne
sont que celles utilisées lors des tests effectués automatiquement par Maven à
la compilation et qui n'ont pas à être utilisées par le bundle en production.

Exemple de POM parent :

.. literalinclude:: /_static/pom-tycho-parent.xml
   :language: xml
   :linenos:


Exemple de POM enfant
*********************

L'utilisation de Tycho permet de ne pas avoir à s'occuper des dépendances et
l'utilisation d'un POM parent nous permet de ne pas avoir à indiquer à chaque
fois qu'on utilise le plug-in Tycho.

Pour créer un plug-in Eclipse, le packaging du projet doit être
``eclipse-plug-in``.

Exemple de POM enfant :

.. literalinclude:: /_static/pom-tycho-child.xml
   :language: xml
   :linenos:


Autre(s) fonctionnalité(s) de Tycho
***********************************

Si on part d'un espace de travail Eclipse PDE, sans jamais avoir créé de fichier
POM, on peut laisser Tycho s'en charger.
Pour cela, il faut ouvrir un terminal dans le dossier de l'espace de travail et
lancer la commande :

.. code-block:: bash

   $ mvn org.sonatype.tycho:maven-tycho-plug-in:generate-poms \
      -DgroupId=org.psem2m


Les projets Maven auront alors le groupId indiqué et le nom de projet Eclipse
comme artifactId.
Le POM parent aura le nom de l'espace de travail comme artifactId et sera
présent à la racine de celui-ci.

On peut appeler cette commande :

- sans aucun fichier POM : Tycho créera tous les fichiers comme indiqué
  ci-dessus.

- avec un fichier POM parent à la racine de l'espace de travail : Tycho en
  changera l'artifactId et créera les POM des modules indiqués
  uniquement -- comportement précis à vérifier.

- avec un fichier POM parent déporté : Tycho créera les POM des modules indiqués
  uniquement, mais avec un parent erroné -- sans chemin relatif -- et lèvera une
  exception car les modules enfants ne peuvent pas trouver leur parent.
