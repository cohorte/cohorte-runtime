.. Description des fonctionnalités

Fonctionnalités attendues
#########################

Programme autonome
******************

Le projet PSEM2M Compiler devra pouvoir être exécuté de manière autonome par un
outil d'intégration continue tel que Jenkins.

L'outil pouvant être installé sur la machine de compilation, un script peut
préparer l'environnement d'exécution du cœur de PSEM2M Compiler.
Par exemple, si le projet est écrit en Java, le script peut générer la liste
des paramètres à passer au programme *java*, notamment le *classpath*.


Préparation de la *Target Platform*
***********************************

L'outil devra commencer le traitement d'une arborescence en détectant les
archives JAR composant la *Target Platform* utilisée dans l'environnement de
développement.

Ces archives devront être installées dans le dépôt local Maven en suivant un
ensemble de règles de noms d'artefacts Maven afin qu'ils ne puissent pas être
confondus avec des éléments présents sur les dépôts Maven en ligne.


Génération de fichiers projets Maven "purs"
*******************************************

Le but de PSEM2M Compiler est de générer des fichiers *pom.xml*, c'est-à-dire
des fichiers projets **Maven**, à partir d'un ensemble de projets Eclipse.

Contrairement à **Tycho**, il ne s'agit pas de faire un plug-in pour **Maven**,
mais bien de générer des projets utilisant les mécanismes de dépendances et les
plug-ins **Maven** existant.
De cette manière, nous pourrons compiler n'importe quel *bundle* développé avec
Eclipse PDE, sans pour autant dépendre d'Equinox.

La génération des fichiers *pom.xml* sera basée sur le contenu des fichiers
*.project* et *.classpath* de chaque projet Eclipse indiqué.

Un graphe de dépendance devra être généré lors du parcours des projets à
préparer, afin de déterminer quelles dépendances indiquer dans le fichier
*pom.xml*.


Le cas iPOJO
************

L'outil PSEM2M Compiler devra être capable de gérer le cas particulier des
projets utilisant iPOJO : leur manipulation devra être effectuée à l'aide du
plug-in iPOJO pour **Maven**.

Un projet Eclipse sera considéré comme nécessitant une manipulation iPOJO si
son fichier *.project* indique la nature
``org.ow2.chameleon.eclipse.ipojo.iPojoNature``, utilisée par le plug-in
**iPOJO Nature** pour Eclipse.
