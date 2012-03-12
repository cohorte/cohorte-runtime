.. Évolutions attendues

Évolutions attendues
####################

.. todo:: À trier / mettre en forme

* Support de plusieurs moniteurs

  * Pour le moment la plateforme n'utilise qu'un moniteur
  * Pour gérer plusieurs moniteurs, il faut un système d'élection du moniteur
    principal et de suppression des ordres doublons

* Support multi-hôtes amélioré

  * Le multi-hôte se base actuellement sur l'entrée *host* de la configuration
    des isolats.
  * Le moniteur ne démarre pas les isolats dont l'hôte est différent de celui
    indiqué dans ses propriétés système.

* Amélioration de l'API Signals

  * Implémentation du ACK
  * API send / post
  * Mécanisme de routage des signaux ? avec TTL ?

* Mise à jour de la documentation

  * Certains projets ont évolué, pas leurs documents

* Tests unitaires

  * Seuls Pelix, iPOPO (Python) et quelques projets Java (Logger, Tracer)
    ont des tests unitaires
  * Première étape : mise en place d'une plateforme de test intra-OSGi
    (attente d'un forker Python ?)

* Jabsorb NG : extension ou remplacement ?

* Spécification des *Cerveaux*

  * Il faut d'abord regrouper de la littérature sur les modèles autonomiques
    et leur mise en place
  * Il faut également voir comment mettre en place l'éther, probablement basé
    sur les réseaux P2P

* Pelix / iPOPO

  * Compléter les tests unitaires (recherche du 100% de couverture de code et
    de branches)
  * Injection de proxy *nullable* ?
  * Verrouillage des références injectés dans les méthodes en cours d'exécution
    (à la iPOJO)
  * Validation du projet par transformation d'un framework Python en composants
    iPOPO ?
