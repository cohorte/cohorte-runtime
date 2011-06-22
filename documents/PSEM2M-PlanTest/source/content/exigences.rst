.. Liste des exigences à tester

Exigences à tester
##################

Les exigences à tester ne sont pas forcément les mêmes pour chaque composants.
Celles-ci sont décrites en détails dans le cahier des charges.


Exigences communes
******************

.. todo:: À compléter


Moniteur
********

* Disponibilité des méthodes JMX
* Accès aux services du forker

* Mise à jour de la configuration à chaud
* Connaissance du statut des isolats
* Détection rapide des isolats bloqués/morts

À prévoir selon les avancées du document de spécifications externes :

* Auto correction des routes à la mort d'un isolat


Forker
******

* Démarrage des isolats sur différents OS
* Application de la configuration de l'isolat lancé (environnement, arguments
  et bundles)
* Arrêt des isolats doux puis radical
* Ping des isolats


Isolat
******

* Accès aux services du moniteur
* Réponse correcte à un ping
* Envoi des informations sur l'état de démarrage
* Auto-test de l'isolat par les sondes


Plateforme globale
******************

* Empreinte mémoire attendue / réelle
* Intégration avec le mécanisme de services du système d'exploitation
