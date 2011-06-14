.. Risques potentiels

Risques potentiels
==================


Tableau récapitulatif
---------------------

+--------------------------------------------+-----------------------------+-------------+
| Facteur                                    | Risque                      | Probabilité |
+============================================+=============================+=============+
| Perte de données                           | Dépassement de délai        | Faible      |
+--------------------------------------------+-----------------------------+-------------+
| Fausse piste en phase de recherche         | Dépassement de délai        | Forte       |
+--------------------------------------------+-----------------------------+-------------+
| Mauvaise estimation de la durée des tâches | Dépassement de délai        | Moyenne     |
+--------------------------------------------+-----------------------------+-------------+
| Développement d'outils                     | Dépassement de délai        | Forte       |
+--------------------------------------------+-----------------------------+-------------+
| Méconnaissance technologique               | Dépassement de délai        | Moyenne     |
+--------------------------------------------+-----------------------------+-------------+
| Contraintes de l'OS cible                  | Fonctionnalités incomplètes | Moyenne     |
+--------------------------------------------+-----------------------------+-------------+
| Fonctionnalités incompatibles              | Fonctionnalités incomplètes | Faible      |
+--------------------------------------------+-----------------------------+-------------+


Risque 1 : Dépassement des délais fixés
---------------------------------------

Ces délais ne sont qu'une valeur indicative pour le moment et peuvent varier en
fonction des avancées des phases de recherche.

Facteur 1 : Perte de données
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Il y a toujours un risque de perdre les données du développement en cours, d'une
manière ou d'une autre.

* Mesures préventives :

   * Utilisation d'un gestionnaire de version
   * Sauvegarde des données sur un serveur distant

* Mesures curatives :

   * Reprise à partir d'une sauvegarde


Facteur 2 : Fausses pistes dans les phases de recherche
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Étant donné que la qualité du projet sera principalement basée sur la rapidité
d'exécution et sur l'utilisation de la mémoire. Pour optimiser ces paramètres,
nous devons étudier un certains nombre d'options, certaines d'entre elles
pouvant être des impasses, d'autres pouvant entrainer de nouvelles recherches.

* Mesures préventives :

   * Limiter la durée des phases de recherches
   * Chercher des avis extérieurs pour connaître la faisabilité des pistes étudiées

* Mesures curatives :

   * Stopper la phase de recherche en cours
   * Modifier le planning


Facteur 3 : Mauvaises estimations de la durée des tâches
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Ce facteur est lié aux phases de recherches car ce sont elles qui donneront une
approximation du temps nécessaire au développement correspondant à leurs
résultats. Il peut également être un optimisme ou un pessimisme trop important
de la part des membres du projet estimant ces durées.

* Mesures préventives :

   * Mettre à jour le planning à chaque fin de tâche ou sous-tâche

* Mesures curatives :

   * Réorganiser le planning du projet
   * Modifier la priorité de certaines fonctionnalités


Facteur 4 : Développement d'outils extra-projet
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Il est possible que l'équipe de développement consacre du temps pour développer
des outils afin de l'assister dans ses tâches, aussi bien en période de
conception qu'en période de développement voire de tests.

* Mesures préventives :

   * Trouver un maximum d'informations et d'outils sur les technologies qui seront
     utilisées après leur phase de validation
   * Prévoir à l'avance les outils qui devront être codés par l'équipe

* Mesures curatives :

   * Demander l'assistance de la communauté de la technologie traitée
   * Ne développer que des outils simples, ne répondant qu'à un besoin précis
     (mais prévoir la ré-utilisabilité du code)


Facteur 5 : Méconnaissance d'une technologie par des membres de l'équipe
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Le nombre de technologies étant assez important, il est fort probable que toutes
ne soient pas connues par certains membres.

* Mesures préventives :

   * Maintenir un tableau des connaissances : qui sait quoi et à quel degré
   * Répartir le développement des modules  selon les connaissances de chaque
     membre
   * Maintenir des documents didactiques sur les technologies apprises par un
     membre pour que les autres puissent s'y référer

* Mesures curatives :

   * Demander l'assistance de la communauté de la technologie traitée
   * Prendre le temps de former l'équipe



Risque 2 : Impossibilité d'implémenter certaines fonctionnalités
----------------------------------------------------------------

Le but du projet est d'isoler des applications et d'être capable de les
redémarrer en cas de défaillance. Ces tâches ne peuvent pas être effectuées de
manière à la fois portable et efficace.


Facteur 1 : Contraintes du système d'exploitation cible
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Le projet est prévu pour fonctionner sur les systèmes Windows, Linux et Mac.
Une version est également prévue pour Android. Windows n'étant pas un système
Unix, son développement devra être surveillé de près afin de vérifier si
certaines fonctions sont faisables sans trop de développement supplémentaire.
De même, le développement sur Android aura sa part de spécificité, des outils
comme Zygote pouvant même être utilisés à la place de modules du projet.


* Mesures préventives :

   * Se focaliser sur le développement Unix en respectant les standards POSIX
   * Prévoir des phases de documentation sur l'implémentation des méthodes
     utilisées sur les systèmes qui ne sont pas totalement compatibles POSIX.

* Mesures curatives :

   * Trouver des implémentations des méthodes manquantes (Cygwin pour Windows)
   * Utiliser des méthodes spécifiques au système (CreateProcess /
     ProcessBuilder au lieu de fork)
   * Revue du fonctionnement de l'application sur certains systèmes
     (ProcessBuilder au lieu de fork)


Facteur 2 : Fonctionnalité non compatible avec les outils choisis
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

L'une des fonctionnalités du projet est la surveillance des isolats en cours
d'exécution. Dans le cas d'un framework Felix, ceci peut se faire par le
développement d'un bundle sonde; dans le cas d'une application Java SE par
l'utilisation de JMX; … Cependant, il est possible que ces sondes ne soient pas
compatibles avec l'application isolées. D'autres fonctionnalités comme le
redémarrage automatique d'isolat ou libération de ressources peut être
indisponible à un instant donné.


* Mesures préventives :

   * Définir clairement les domaines de fonctionnement des sondes utilisées
   * Ne pas faire confiance au système hôte (libérer un maximum de ressources
     allouées avant de tuer un isolat)

* Mesures curatives :

   * Gérer finement la gestion des ressources
   * Interdire l'utilisation de certains outils dans les isolats
