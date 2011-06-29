.. Composants de services OSGi

Composants de services OSGi : Declarative Services contre iPOJO
###############################################################

Les deux outils ont à peu près les mêmes possibilités.

Declarative Service (OSGi)
**************************

Declarative Services laisse la possibilité d'utiliser le POJO dans un projet
sans iPOJO, car il force le développeur à utiliser des méthodes de binding pour
chaque nouveau service trouvé. Ces méthodes peuvent être réutilisées afin de
simuler le mécanisme de Declarative Services dans un projet sans enregistrement
dynamique de services. Elles permettent également un traitement fin de
l'apparition de services, par exemple en autorisant la configuration d'un
service dès qu'il est connu du bundle, mais augmentent la quantité de code à
produire.

De plus, on peut contrôler l'ordre de signalisation des dépendances est celui de
leur déclaration dans le fichier XML de description de composant.

Enfin, Maven est capable de gérer ce genre de bundle, il suffit de placer
correctement le fichier de description XML dans le dossier de ressources
OSGI-INF ; Eclipse PDE gère directement ce type de bundle.

Il est à noter que les Declarative Services sont un standard depuis OSGi R4.


iPOJO (Apache)
**************

Côté iPOJO, tout est automatisé une fois le fichier XML correctement renseigné.
Les références vers les services sont injectées et peut être utilisées
directement dans le code POJO. On peut cependant débrailler ce comportement Le
contrôle tout automatique de iPOJO peut-être très pratique mais aussi
insuffisamment fin dans la gestion de l'apparition de services, selon
l'utilisation souhaitée.

Enfin, le plug-in iPOJO de Maven est disponible pour faciliter la compilation
d'un bundle de ce type ; un plug-in existe pour Eclipse.

Dans les deux cas, les dépendances au framework OSGi ne sont pas obligatoires,
ce qui simplifie légèrement la configuration du projet.

Après différents tests, Declarative Services semble être le plus pratique et le
plus fin des deux outils. Le choix final dépendra des dépendances des autres
outils à utiliser.
