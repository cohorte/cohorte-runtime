.. Périmètre d'intervention

Périmètre d'intervention
########################

Éléments testés
***************

Les tests porteront principalement sur le fonctionnement et les interactions
entre le moniteur et le forker. Il s'agira notamment de tester leurs capacités
de résistance à un environnement instable (mort du forker, ...).

Du côté des isolats, les tests porteront sur les sondes et sur les accès aux
services distants.
Le but de ces tests sera de valider les communications inter-isolats apportées
par PSEM2M, ainsi que les communications avec le moniteur.

Enfin, une partie des tests validera les services utilitaires (logger, lecteur
de configuration, ...).

Éléments exclus des tests
*************************

Les bundles tiers utilisés ne subiront pas de tests.
Tout bundle et/ou fonctionnalité des isolats qui n'a pas été défini comme un
service de PSEM2M ne sera pas testé (Remote Shell, ...).


Éxigences testées
*****************

Les exigences à tester ne sont pas forcément les mêmes pour chaque composants.
Celles-ci sont décrites en détails dans le cahier des charges.


Moniteur
========

Le moniteur devra respecter les exigences suivantes pour valider les
interactions avec le forker et le système :

* Disponibilité des méthodes JMX
* Accès aux services du forker

Il devra également valider les points suivants :

* Mise à jour de la configuration à chaud
* Connaissance du statut des isolats
* Détection rapide des isolats bloqués/morts


Selon les avancées du document de spécifications externes, nous devrons
également prévoir les tests suivants :

* Auto correction des routes à la mort d'un isolat


Forker
======

Les tests du forker valideront principalement ses interactions avec le système
d'exploitation (démarrage/arrêt d'isolat) et les tests de présence des isolats :

* Démarrage des isolats sur différents OS
* Application de la configuration de l'isolat lancé (environnement, arguments
  et bundles)
* Arrêt des isolats doux (demande à l'isolat) puis radical (demande au système)
* Ping des isolats


Isolat
======

Les isolats ne seront testés que sur les points suivants :

* Accès aux services du moniteur
* Réponse correcte à un ping
* Envoi des informations sur l'étape de démarrage en cours
* Auto-test de l'isolat par les sondes


Plateforme globale
==================

Nous testerons également l'empreinte de la plateforme dans son ensemble sur le
système. Ce test sera effectué avec des isolats n'ayant pas plus de bundles que
ceux nécessaires à la plateforme.

* Empreinte mémoire attendue / réelle

Enfin, nous testerons l'intégration de la plateforme dans le système
d'exploitation :

* Intégration avec le mécanisme de services du système d'exploitation

