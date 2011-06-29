.. Gestion des dépendances

Gestion des dépendances...
##########################

La gestion des dépendances peut se faire de différentes manières :


... en utilisant en priorité Maven
**********************************

Comme dit plus haut, Maven est un outil permettant la gestion des dépendances
d'un projet, capable de récupérer les fichiers nécessaires au bon fonctionnement
de l'application sur des dépôts, publics ou non.

Durant la phase de développement, les dernières versions disponibles des
dépendances peuvent être téléchargées, pour s'assurer d'avoir des bibliothèques
récentes et fonctionnelles lors de l'empaquetage du projet.

Ceci impose la mise à jour à la main du projet Eclipse correspondant pour
pouvoir exécuter le bundle dans l'IDE.

Un problème possible est que ce mode fonctionnement impose au client de
permettre à Maven d'accéder à ces dépôts, et que les dépendances décrites soient
toujours présentes et valides lors du déploiement du projet.


... en utilisant en priorité Eclipse
************************************

Les bundles peuvent être exécutés directement dans Eclipse, mais cette technique
impose la mise à jour manuelle du fichier Maven.


... en créant des bundles spécifiques aux dépendances
*****************************************************

Les dépendances peuvent être téléchargées via Maven ou à la main.
Elles seront ensuite transformées en bundle via Maven.
Ces bundles pourront alors être inclus directement dans la Target Platform du
projet Eclipse ou bien utilisés directement par Maven.

Dans le cas de la compilation des bundles par Maven, il faudra être capable de
repérer les bundles de dépendance venant d'être installés dans le dépôt local
face aux librairies originales.
Pour cela, il faudra marquer ces bundles d'une manière ou d'une autre --
remplacement du numéro de version, ajout d'un classifier, ...

La mise à jour cet espace de travail pourra être effectuée via l'appel d'un
fichier Maven parent, soit automatiquement, soit manuellement.


... en utilisant Eclipse, Maven et Tycho
****************************************

Comme dit plus haut, le plug-in Tycho permet de ne gérer qu'une seule fois les
dépendances du projet : dans le Manifest du bundle, géré par Eclipse ou à la
main.

Toutes ces dépendances doivent alors être présentes dans la Target Platform
-- indiquée par propriété Maven à Tycho, devant être la même que celle utilisée
par Eclipse -- et sous la forme de bundle afin de pouvoir être chargées par
Equinox.

Cette technique semble être la plus efficace car elle permet de travailler
uniquement avec Eclipse tout en permettant de compiler l'espace de travail
complet avec Maven.
