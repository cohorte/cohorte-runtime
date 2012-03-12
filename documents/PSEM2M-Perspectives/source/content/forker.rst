.. Forker

Évolution du Forker
###################

Le *forker* est l'outil permettant de démarrer et de tuer des isolats d'une
instance de la plateforme PSEM2M, sur ordre d'un moniteur.
Il ne doit pas embarquer de logique complexe en dehors de la gestion des erreurs
lors de l'initialisation et de l'arrêt d'un isolat.

Actuellement, il se présente sous la forme d'un isolat particulier, isolé des
messages des isolats, développé en Java.

Implémentations possibles
*************************

Isolat Java
===========

.. note:: Implémentation actuelle

L'isolat Forker Java exporte un service ``IForker`` via *Remote OSGi* vers le(s)
moniteur(s).

Le service recherche le chemin vers l'interpréteur Java indiqué dans la variable
d'environnement **JAVA_HOME** ou, à défaut, celui qui a servi à démarrer
l'isolat forker.

Avantages
---------

* Cohérence avec PSEM2M Java : un isolat parmi les autres, géré par le moniteur,
  avec les mêmes outils que les autres

* Couple Moniteur / Forker : si l'un meurt, l'autre peut le re-démarrer

* Re-direction des entrées/sorties des isolats : les E/S des isolats peuvent
  être redirigées vers un fichier de journalisation ou vers un console

* Isolat toujours actif : l'isolat a un cycle de vie équivalent à l'instance de
  PSEM2M. Le service ``IForker`` peut ainsi conserver un lien avec les isolats
  qu'il a lancé pour vérifier leur état d'exécution

* Portabilité basique : le forker se base sur les mécanismes standards Java pour
  exécuter un processus.


Inconvénients
-------------

* Isolat complet : le forker est un isolat comme les autres et doit donc avoir
  le *bootstrap*, un framework OSGi, la couche PSEM2M Base (Signaux,
  Remote Services, ...) et enfin le service IForker.

* Faible utilisation : le forker est passif, il n'est plus utile après le
  démarrage d'un isolat en dehors de la redirection des E/S

* Pas de support système : les mécanismes Java masquent les phases de démarrage
  des processus et peuvent potentiellement masquer des états incohérents
  (gels, ...).


Isolat Python
=============

L'idée est de transformer l'isolat Java en isolat Python, en supprimant les
paquets (et *bundles*) inutiles (API, journalisation spécialisée, ...).
Le forker aura accès aux mécanismes bas niveau du système d'exploitation hôte
pour démarrer, gérer et tuer les isolats de l'instance de PSEM2M.

Étant toujours un isolat à part entière, le comportement de cette implémentation
vis-à-vis du moniteur sera proche de celle écrite en Java
(Signaux, Remote Services...).


Avantages
---------

* Cohérence avec PSEM2M Python : un isolat parmi les autres, géré par le
  moniteur, avec les mêmes outils que les autres

* Couple Moniteur / Forker : si l'un meurt, l'autre peut le re-démarrer

* Isolat toujours actif : l'isolat a un cycle de vie équivalent à l'instance de
  PSEM2M. Le service ``IForker`` peut ainsi conserver un lien avec les isolats
  qu'il a lancé pour vérifier leur état d'exécution

* Re-direction des entrées/sorties des isolats : les E/S des isolats peuvent
  être redirigées vers un fichier de journalisation ou vers un console

* Accès aux mécanismes de l'OS hôte : le forker peut contrôler finement
  l'état des isolats. Les mécanismes de démarrage des isolats peuvent aussi
  être plus poussés :

  * ``fork`` pour démarrer un nouvel isolat Python depuis un modèle
  * ``fork`` + *JNI* : duplication d'un modèle et démarrage d'une machine
    virtuelle Java dans le nouvel isolat.


Inconvénients
-------------

* Isolat complet : le forker reste un isolat complet, même s'il consomme moins
  qu'un isolat Java

* Faible utilisation : le forker est passif, il n'est plus utile après le
  démarrage d'un isolat en dehors de la redirection des E/S

* Différences Windows / POSIX : il sera nécessaire de développer au moins deux
  versions du forker en fonction de l'OS hôte.


Service natif
=============

Par service natif, on entend un service implémenté selon les standards du
système d'exploitation hôte :

* Linux : scripts Linux Standard Base, ayant déjà deux grandes familles

  * *init.d* (System V) : scripts appelés avec des paramètres standardisés
    (start, stop, status, ...). Standard actuel, en passe d'être remplacé.
    Étant un héritage d'Unix, ce standard devrait être retrouvé sur la plupart
    des plateformes cibles.

  * *systemd* (ou *systemctl*) : description des services et de leurs
    dépendances dans des fichiers particuliers, référençant des scripts à
    exécuter en fonction des services actifs.

* Windows : Un exécutable (avec ou sans paramètres) ou une DLL est enregistré
  en tant que service.

* Autres systèmes : à définir


Avantages
---------

* Mécanismes standards de l'OS cibles : les administrateurs apprécieront

* Service minimal : le forker doit pouvoir recevoir des ordres, répondre aux
  moniteurs et travailler sur les processus, sans avoir à être un isolat PSEM2M
  complet

* Gestion des utilisateurs possibles : le forker est exécuter avec un compte
  utilisateur particulier, et peut dans certains cas démarrer les isolats dans
  l'environnement d'un autre compte :

  * Avec un forker *root* (non recommandé)
  * Avec un forker par compte isolat (mais plusieurs instances de forkers)

* Selon le langage d'implémentation : accès aux fonctionnalités bas niveau du
  système hôte

* Couple Moniteur / Forker : si l'un meurt, l'autre peut le re-démarrer


Inconvénients
-------------

* Ré-implémentation de Remote Services et autres outils en dehors PSEM2M

* Code non portable entre familles d'OS, voire entre versions d'un même OS

* Problèmes de sécurité : le forker doit être exécuté avec un minimum de droits,
  ce qui devient contraignant pour l'exécution d'isolats


Service emballé
===============

L'idée est d'avoir l'équivalent d'un service natif, mais en utilisant un outil
tiers pour intéragir avec le système hôte.

Avantages
---------

* Mêmes avantages que le service natif

* Pas besoin de gérer les interfaces avec le système hôte (simplification)


Inconvénients
-------------

* Liaison forte à un outil tiers pour le forker, module critique de PSEM2M

* Aucune garantie sur la sûreté ni la fiabilité du outil tiers utilisé


Fusion avec le moniteur
=======================

Dernière proposition en date, la fusion du moniteur et du forker semble être la
plus efficace et la plus simple à mettre en œuvre, mais supprime la surveillance
respective des moniteurs et du forker.

Le forker peut être implémenté comme module inséré dans le moniteur (Java,
Python, ...) ou comme une bibliothèque appelée depuis le moniteur (fichier JAR,
JNI, JNA, module Python, ctypes, ...).


Avantages
---------

* Le moniteur a tout l'outillage nécessaire pour démarrer, gérer et tuer les
  isolats

* Mise à jour à chaud toujours possible (module ou bibliothèque)

* Pas d'isolat ni de service démon

* L'implémentation en bibliothèque peut être faite dans le même langage ou dans
  un langage différent du reste de la plateforme (Python, C, Java, ...)


Inconvénients
-------------

* Perte du couple moniteur / forker : pour assurer le fonctionnement de PSEM2M,
  il faudra au moins deux moniteurs, l'un redémarrant l'autre en cas d'erreur.

* Problèmes d'interropérabilité possibles si l'implémentation du forker est
  faite dans un langage différent du moniteur (Java <-> Python, Java <-> C, ...)


Adhérences au système d'exploitation
************************************

Récapitulatif
*************
