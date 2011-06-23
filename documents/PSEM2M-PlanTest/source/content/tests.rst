.. Définition des tests

Définition des tests
####################

Tests unitaires
***************

Cette section décrit les tests *unitaires* appliqués à chaque composant de la
plateforme.

Ces tests seront effectués à l'aide de junit4osgi (voir :ref:`outils-test`) dans
les frameworks OSGi Felix et Equinox.


Moniteur
========

**Reconfiguration à chaud**
---------------------------

*Objectif*
^^^^^^^^^^

   Valider le comportement du moniteur lors d'une reconfiguration à chaud.

*Technique*
^^^^^^^^^^^

   #. Éditer un fichier de configuration
   #. Démarrer le moniteur
   #. Afficher la configuration à l'écran
   #. Comparer la trace au contenu du fichier
   #. Éditer le fichier de configuration
   #. Afficher la configuration à l'écran
   #. Comparer la trace au contenu du fichier
   #. Valider les modifications

*Critères de complétion*
^^^^^^^^^^^^^^^^^^^^^^^^

   Le test sera considéré réussi si :

   * Le moniteur doit lire correctement la configuration d'origine
     (premier affichage)
   * Les modifications apportées au fichier de configuration doivent être
     appliquées

*Commentaire*
^^^^^^^^^^^^^

   L'affichage et la comparaison des traces peut se faire dans un bundle
   utilitaire développé spécifiquement.


**Surveillance des isolats**
----------------------------

*Objectif*
^^^^^^^^^^

   Valider la mise à jour de l'état des isolats en fonction des évènements
   reçus.

*Technique*
^^^^^^^^^^^

   #. Démarrer le moniteur
   #. Émettre un évènement indiquant le démarrage réussi d'un isolat
   #. Calculer le temps de prise en charge de la notification
   #. Valider l'état de l'isolat vu par le moniteur
   #. Attendre un temps aléatoire (entre 1 et 5 secondes)
   #. Émettre un évènement indiquant l'arrêt d'un isolat
   #. Calculer le temps de prise en charge de la notification
   #. Valider l'état de l'isolat vu par le moniteur

*Critères de complétion*
^^^^^^^^^^^^^^^^^^^^^^^^

   Le test sera considéré réussi si :

   * Le moniteur affiche toujours une valeur correcte
   * Le traitement des notifications est inférieur à la seconde

*Commentaire*
^^^^^^^^^^^^^

   Les évènements seront générés par un bundle développé spécifiquement et
   simulant les signaux qui seront reçus depuis les isolats ou le forker.
   Le temps de prise en charge sera calculer par un chronomètre déclenché lors
   de la réception de la notification et arrêté à la fin de son traitement.


Forker
======

**Démarrage d'un isolat**
-------------------------

*Objectif*
^^^^^^^^^^

   Valider l'exécution d'un isolat Java ou OSGi par le forker.

*Technique*
^^^^^^^^^^^

   #. Préparer un fichier de configuration décrivant deux isolats :

      * Un isolat Java
      * Un isolat OSGi (Felix ou Equinox)

   #. Démarrer le forker
   #. Indiquer au forker de lancer l'isolat Java
   #. Valider son exécution
   #. Indiquer au forker de lancer l'isolat OSGi
   #. Valider son exécution

*Critères de complétion*
^^^^^^^^^^^^^^^^^^^^^^^^

   Le test sera considéré réussi si :

   * Les isolats doivent avoir été exécutés correctement : leur présence est
     avérée par un outil développé spécifiquement ou manuellement avec l'outil
     *htop*.

*Commentaire*
^^^^^^^^^^^^^

   Les isolats lancés doivent avoir exécution suffisamment longue pour pouvoir
   être détectés par les outils utilisés, c'est-à-dire un peu plus d'une
   seconde.


**Arrêt radical d'un isolat**
-----------------------------

**Objectif**
^^^^^^^^^^^^

   Valider l'arrêt garanti d'un isolat par la manière forte en utilisant les
   outils du système d'exploitation.

*Technique*
^^^^^^^^^^^

   #. Configurer un isolat ne pouvant pas s'arrêter
   #. Démarrer le forker
   #. Démarrer l'isolat
   #. Attendre un temps aléatoire entre 2 et 5 secondes
   #. Indiquer au forker de tuer l'isolat
   #. Valider la mort de l'isolat

*Critères de complétion*
^^^^^^^^^^^^^^^^^^^^^^^^

   Le test sera considéré réussi si :

   * L'isolat est tué par le forker

*Commentaire*
^^^^^^^^^^^^^

   L'isolat utilisé peut être une simple application Java ayant une boucle
   infinie dans sa méthode ``main()``.

   La validation de l'arrivée et de la mort de l'isolat sera validée par un
   outil développé spécifiquement, scrutant l'état des processus du système.


Isolat
======

Auto-test par les sondes
------------------------

*Objectif*
^^^^^^^^^^

   Valider le fait qu'une sonde est capable de détecter si son isolat est dans
   un état stable ou non.

*Technique*
^^^^^^^^^^^

   #. Configurer un isolat
   #. Lancer l'isolat
   #. Attendre un temps aléatoire (1 à 5 secondes)
   #. Demander à la sonde de valider l'état de l'isolat
   #. Arrêter un bundle de l'isolat
   #. Attendre un temps aléatoire (1 à 5 secondes)
   #. Demander à la sonde de valider l'état de l'isolat

*Critères de complétion*
^^^^^^^^^^^^^^^^^^^^^^^^

   Le test sera considéré réussi si :

   * À la première requête, la sonde renvoie que l'état est correct
   * À la seconde requête, la sonde renvoie que l'état est incorrect

*Commentaire*
^^^^^^^^^^^^^

   Le service de configuration sera lancé dans l'isolat afin qu'il n'y ait pas
   besoin d'utiliser les services distants pour récupérer la configuration
   attendue pour celui-ci.


Tests d'intégration
*******************

Cette section décrit les tests permettant de valider les interactions entre les
composants de la plateforme PSEM2M.

Accès aux services distants
===========================

Appels du moniteur au forker
----------------------------

*Objectif*
^^^^^^^^^^

   Valider la communication entre le moniteur et le forker.

*Technique*
^^^^^^^^^^^

   #. Configurer un isolat ne pouvant pas s'arrêter
   #. Démarrer le moniteur
   #. Démarrer le forker
   #. Le moniteur configure le forker
   #. Le moniteur indique au forker de lancer l'isolat
   #. Valider le lancement de l'isolat
   #. Le moniteur attend un temps aléatoire (5 à 10 secondes)
   #. Le moniteur indique au forker de tuer l'isolat
   #. Valider la mort de l'isolat

*Critères de complétion*
^^^^^^^^^^^^^^^^^^^^^^^^

   Le test sera considéré réussi si :

   * L'isolat a bien été lancé
   * L'isolat a bien été tué ensuite

*Commentaire*
^^^^^^^^^^^^^

   On réutilisera les outils développés lors des tests unitaires afin d'avoir
   un isolat ne s'arretant pas de lui-même et de pouvoir scruter les processus
   du système.


Arrêt doux d'un isolat
----------------------

*Objectif*
^^^^^^^^^^

   Valider l'envoi d'un signal d'arrêt à un isolat.

*Technique*
^^^^^^^^^^^

   #. Configurer un isolat avec une sonde
   #. Démarrer le moniteur
   #. Démarrer le forker
   #. Le moniteur indique au forker de lancer l'isolat
   #. Valider le lancement de l'isolat
   #. Le moniteur attend un temps aléatoire (2 à 5 secondes)
   #. Le moniteur indique au forker de tuer l'isolat
   #. Le forker indique à la sonde de l'isolat de stopper.
   #. Valider l'arrêt de l'isolat

*Critères de complétion*
^^^^^^^^^^^^^^^^^^^^^^^^

   Le test sera considéré réussi si :

   * L'isolat a été lancé
   * L'isolat a été arrêté par la sonde ensuite.

*Commentaire*
^^^^^^^^^^^^^

   On réutilisera l'outil scrutant les processus du système.
   La sonde devra indiquer par une trace ou dans un journal qu'elle a reçu le
   message d'arrêt et indiqué au bundle principal de s'arrêter afin de valider
   le fait que l'isolat n'a pas été tué de manière radicale.


**Interrogation d'un isolat (ping)**
------------------------------------

*Objectif*
^^^^^^^^^^

   Valider l'utilisabilité du ping.

*Technique*
^^^^^^^^^^^

   #. Configurer un isolat
   #. Démarrer le moniteur
   #. Démarrer le forker
   #. Le moniteur indique au forker de démarrer l'isolat
   #. Le forker démarre l'isolat
   #. Valider le lancement de l'isolat
   #. Le forker attend un temps aléatoire (2 à 5 secondes)
   #. Le forker envoie un ping à l'isolat
   #. L'isolat renvoie la réponse au forker
   #. Calculer le temps de réponse
   #. Valider la réponse

*Critères de complétion*
^^^^^^^^^^^^^^^^^^^^^^^^

   Le test sera considéré réussi si :

   * L'isolat est lancé
   * Le temps de réponse est inférieur à 500 millisecondes
   * La réponse de l'isolat correspond à la requête du forker

*Commentaire*
^^^^^^^^^^^^^

   L'isolat devra indiquer dans un journal qu'il a reçu une requête de ping
   ainsi que son contenu.


Utilisation réciproque de services entre isolats
------------------------------------------------

*Objectif*
^^^^^^^^^^

   Valider les accès aux services des isolats par les isolats.

*Technique*
^^^^^^^^^^^

   #. Configurer deux isolats OSGi ayant tous deux :

      * un bundle fournissant un service connu et utilisé par l'autre isolat
      * un bundle se chargeant de l'inscription du premier dans les services
        exportés

   #. Démarrer le moniteur
   #. Démarrer le forker
   #. Le moniteur indique au forker de démarrer les isolats
   #. Valider le lancement des deux isolats
   #. Les isolats attendent chacun un temps aléatoire (2 à 5 secondes)
   #. Chaque isolat utilise le service de l'autre
   #. Valider le résultat des appels

*Critères de complétion*
^^^^^^^^^^^^^^^^^^^^^^^^

   Le test sera considéré réussi si :

   * les isolats sont lancés
   * les services ont bien été appelés

*Commentaire*
^^^^^^^^^^^^^

   L'export de service peut être mis en place en réutilisant les bundles de
   démonstration développés lors de la recherche d'outillage (iPOJO / CXF).

   Le service exporté peut fournir une méthode ``boolean hello(String aName)``,
   inscrivant dans le journal de l'isolat une ligne de type ``hello aName !`` et
   retournant vrai en cas de succès.


Vitesse de détection des isolats morts
--------------------------------------

*Objectif*
^^^^^^^^^^

   Valider le temps de réaction du moniteur face à la mort d'un isolat

*Technique*
^^^^^^^^^^^

   #. Configurer un isolat
   #. Démarrer le moniteur
   #. Démarrer le forker
   #. Le moniteur indique au forker de démarrer l'isolat
   #. Valider le lancement de l'isolat
   #. Valider l'état du moniteur
   #. Attendre un temps aléatoire (2 à 5 secondes)
   #. Tuer manuellement l'isolat
   #. Valider le nouvel état du moniteur

*Critères de complétion*
^^^^^^^^^^^^^^^^^^^^^^^^

   Le test sera considéré réussi si :

   * L'isolat a bien été lancé
   * Le moniteur a bien vu que l'isolat a été lancé
   * L'isolat a bien été tué manuellement
   * Le moniteur a bien vu que l'isolat a été tué


*Commentaire*
^^^^^^^^^^^^^

   Sans objet.


Vitesse de détection des isolats bloqués
----------------------------------------

*Objectif*
^^^^^^^^^^

   Valider le temps de réaction du moniteur face au blocage d'un isolat

*Technique*
^^^^^^^^^^^

   #. Configurer un isolat
   #. Démarrer le moniteur
   #. Démarrer le forker
   #. Le moniteur indique au forker de démarrer l'isolat
   #. Valider le lancement de l'isolat
   #. Valider l'état du moniteur
   #. Attendre un temps aléatoire (2 à 5 secondes)
   #. Arrêter la sonde dans l'isolat
   #. Valider le nouvel état du moniteur

*Critères de complétion*
^^^^^^^^^^^^^^^^^^^^^^^^

   Le test sera considéré réussi si :

   * L'isolat a bien été lancé
   * Le moniteur a bien vu que l'isolat a été lancé
   * L'isolat a bien été tué manuellement
   * Le moniteur a bien vu que l'isolat a été tué

*Commentaire*
^^^^^^^^^^^^^

   L'arrêt de la sonde dans l'isolat lui empêche de répondre aux pings du
   forker.


Utilisabilité des méthodes JMX
==============================

*Objectif*
----------

   Valider la possibilité de contrôler le moniteur via son interface JMX.

*Technique*
-----------

   #. Configurer un isolat OSGi
   #. Démarrer le moniteur
   #. Démarrer le forker
   #. Le moniteur indique au forker de démarrer l'isolat
   #. Valider le lancement de l'isolat
   #. Attendre un temps aléatoire (2 à 5 secondes)
   #. Utiliser JMX pour indiquer au moniteur d'arrêter la plateforme
   #. Valider l'arrêt de l'isolat
   #. Attendre un temps aléatoire (2 à 5 secondes)
   #. Utiliser JMX pour indiquer au moniteur de démarrer la plateforme
   #. Valider le lancement de l'isolat

*Critères de complétion*
------------------------

   Le test sera considéré réussi si :

   * Les appels JMX ont abouti
   * L'isolat a été lancé
   * L'isolat a été arrêté avec la plateforme
   * L'isolat a été relancé avec la plateforme

*Commentaire*
-------------

   Les appels JMX de tests peuvent être exécutés depuis VisualVM
   (voir :ref:`outils-test`).


Tests système
*************

Cette section décrit les tests permettant de contrôler l'empreinte de la
plateforme et de valider son intégration avec les mécanismes de services du
système hôte.


Évaluation de l'empreinte système
=================================

*Objectif*
----------

   Vérifier que l'empreinte de la plateforme PSEM2M sur le système n'est pas
   visible.

*Technique*
-----------

   #. Configurer un isolat OSGi sans bundle particulier
   #. Démarrer le moniteur
   #. Démarrer le forker
   #. Le moniteur indique au forker de démarrer l'isolat
   #. Attendre un temps aléatoire (entre 5 et 10 secondes)
   #. Utiliser un outil capturant l'empreinte de la plateforme


*Critères de complétion*
------------------------

   Le test sera considéré réussi si :

   * La plateforme a été correctement démarrée (isolat compris)
   * L'empreinte mémoire a été récupérée


*Commentaire*
-------------

   L'évaluation du résultat du test permet d'avoir une vision des ressources
   nécessaires à la plateforme.
   C'est de manière subjective que l'équipe de testeurs décide si cette
   empreinte est trop importante ou non pour considérer le test comme validé.


Intégration aux services du système
===================================

*Objectif*
----------

   Valider la possibilité d'exécuter et d'arrêter la plateforme en utilisant le
   mécanisme de services du système d'exploitation.

*Technique*
-----------

   #. Installer le service correspondant à la plateforme
   #. Utiliser les outils du système pour démarrer la plateforme
   #. Valider le lancement du moniteur et du forker
   #. Utiliser les outils du système pour arrêter la plateforme
   #. Valider l'arrêt du moniteur et du forker


*Critères de complétion*
------------------------

   Le test sera considéré réussi si :

   * Le moniteur et le forker ont bien été démarré par le système
   * Le moniteur et le forker ont bien été arrêtés par le système ensuite

*Commentaire*
-------------

   L'interface entre le mécanisme de service du système et la plateforme est
   effectué par un *wrapper*.
   Nous utiliserons dans ce test le *wrapper* sélectionné lors de la recherche
   préliminaire de l'outillage.
