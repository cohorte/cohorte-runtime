.. Implémentation iPOPO

Agent basé sur iPOPO (Python)
#############################

Instanciation de composants avec iPOPO
**************************************

Le projet iPOPO (injected Plain Old Python Object), initié par isandlaTech, est
un modèle de composants orientés services pour Python.
Il reprend une partie des principes de iPOJO, du monde Java.

IPOPO est utilisable dans toute application Python exécutées avec un
interpréteur respectant les normes 2.6 ou 3.0 et supérieures.

Pour qu'une application fonctionne avec ce modèle de composants, elle doit être
basée sur Pelix (en hommage à Felix), une plateforme de développement orienté
services pour Python inspirée d'OSGi.


Méthode 1 : Création des composants à la volée
==============================================

.. todo:: Ajouter tutoriel sur le site iPOPO et un lien ici

L'idée est de manipuler une classe à l'exécution, en décrivant le composant
programmatiquement.

L'avantage est de pouvoir profiter des mécanismes d'injection d'iPOPO sur une
classe qui n'est pas initialement prévue pour.

Le principal inconvénient est de devoir décrire le composant dans le fichier
de configuration de la composition, ce qui ne peut pas être fait sans un minimum
de connaissance sur la classe utilisée, notamment son nom et le nom de ses
champs.
De plus, certaines classes ne peuvent être manipulées après leur définition,
notamment si elles utilisent le champ particulier ``__slots__``, décrit dans
la documentation officielle de Python
(`__slots__ <http://docs.python.org/reference/datamodel.html?highlight=__slots__#slots>`_).


Méthode 2 : Instanciation d'un composant
========================================

Cette méthode se base sur la création d'instance de composants iPOPO à partir
d'une classe ``Factory`` enregistrée par l'utilisation d'un décorateur
``@ComponentFactory`` soit lors de la définition de la classe, soit
programmatiquement.
L'instanciation programmatique d'un composant se fait à travers le service
fourni par iPOPO.

De plus, la description des dépendances d'un composant peut être modifiée
lors de l'instantiation, en yajoutant une propriété ``requires.filter``.
De cette manière, les filtres utilisés pour sélectionner les services à injecter
dans le composant peuvent être surchargés.

De cette manière, nous nous basons toujours sur les mécanismes d'iPOPO pour
gérer les liens entre composants et les dépendances à des services, tout en
s'assurant d'avoir une chaîne de composition qui correspond à la configuration.


Implémentation choisie
**********************

L'implémentation choisie se base sur la méthode 2.
En effet, cette technique permet de ne traiter que les composants dont les
*factories* ont été correctement traitées par iPOPO, mais assure que le
composant instancié aura le comportement attendu, sans effets de bord.
