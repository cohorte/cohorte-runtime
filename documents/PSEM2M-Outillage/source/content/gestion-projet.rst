.. Gestion de projet de bundle

Gestion de projet de bundle
###########################

Eclipse PDE - Maven 2
*********************

Pour gérer tout ce qui concerne un projet de bundle - gestion des dépendances,
compilation, etc - deux principaux outils s'offrent à nous :

* Eclipse PDE : outil graphique intégré à Eclipse SDK

* Maven : utilitaire bien connu, avec les plug-ins correspondant à la création
  de bundles

Suite aux différents essais effectués, l'utilisation d'Eclipse PDE paraît être
la plus pratique.

En effet, cet outil assiste énormément la création de bundles, la gestion de
leurs inter-dépendances et permet un débogage facile dans l'IDE Eclipse.

Cependant, la gestion des dépendances tierces doit s'effectuer à la main.

Maven est un outil de développement reconnu, imposant un standard de structure
de projet, assistant la gestion des dépendances et ses plug-ins permettent la
génération automatique des fichiers nécessaires à la création de bundles.

Cependant, les paramétrages à effectuer à la main, ou bien les actions
effectuées automatiquement par les plug-ins - section Input-Package du Manifest
d'un bundle, etc - peuvent être sources d'erreurs.

Le débogage peut se faire au travers d'Eclipse, après configuration.


PDE, Maven 3 et Tycho
*********************

Afin de résoudre le problème posé par l'utilisation conjointe d'Eclipse PDE et
de Maven, nous avons trouvé une solution permettant de faire un compromis : le
plug-in Tycho pour Maven 3.

Cette solution impose l'utilisation de Maven 3, encore en version bêta lors de
la rédaction de ce document mais étant apparemment suffisamment stable pour
être utilisé ici. Maven 3 est compatible Maven 2, en étant beaucoup plus
stricte sur le format des fichiers XML des projets.

Tycho permet compiler avec Maven un projet Eclipse PDE, en utilisant les
fichiers utilisés par Eclipse, notamment le fichier Manifest du bundle. Ainsi,
il n'y a pas besoin de dupliquer la liste des dépendances dans le fichier
projet Maven, pouvant être une source d'erreurs ou d'incompatibilités.

Le plug-in Tycho peut générer les fichiers projets Maven si besoin, mais
l'utilisation d'un fichier POM parent dans un projet de l'espace de travail -
afin d'être éditable facilement dans Eclipse - pose quelques problèmes à ce
niveau. Cette contrainte n'est pas énorme car un fichier POM pour ce type de
projet ne contient que la description du POM parent et le définition de base du
projet, sans indication de dépendance ni de plug-ins.
