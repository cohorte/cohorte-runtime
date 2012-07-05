.. Définition de PSEM2M Signals

Définition de PSEM2M Signals
############################

PSEM2M Signals représente un ensemble de composants et de services permettant
de faire communiquer facilement des isolats PSEM2M, quelque soit leur
répartition sur le réseau et leur langage de développement.


Terminologie
************

PSEM2M Signals utilise la même terminologie que la plateforme PSEM2M.

+----------------------+-------------------------------------------------------+
| Terme                | Définition                                            |
+======================+=======================================================+
| Identifiant d'isolat | Identifiant d'un isolat. Il doit être unique pour une |
|                      | application PSEM2M.                                   |
+----------------------+-------------------------------------------------------+
| Nœud                 | Identifiant utilisé pour nommer hôte d'un isolat.     |
|                      | Ce nom peut être différent de celui la machine.       |
|                      | Un nœud est associé à une seule machine.              |
|                      | Une machine peut avoir plusieurs nœuds.               |
|                      | Le nom du nœud est donné par le nœud du forker.       |
+----------------------+-------------------------------------------------------+
| Hôte                 | Nom d'hôte ou adresse IP (v4 ou v6) d'une machine     |
|                      | ou d'un nœud                                          |
+----------------------+-------------------------------------------------------+
| Port                 | Port d'accès au récepteur PSEM2M Signals              |
+----------------------+-------------------------------------------------------+

Chaque isolat peut être associé à un groupe, d'où les termes suivants :

+---------------------+------------------------------------------------------+
| Terme               | Définition                                           |
+=====================+======================================================+
| Groupe d'annuaire   | Groupe d'isolats calculés par l'annuaire en fonction |
|                     | de leurs identifiants                                |
+---------------------+------------------------------------------------------+
| Groupe personnalisé | Groupe d'isolats construit selon la configuration.   |
|                     | Un isolat peut appartenir à plusieurs groupes.       |
+---------------------+------------------------------------------------------+


Fonctionnement
**************

Description
===========

PSEM2M Signals est basé sur 3 principaux éléments :

* l'annuaire, contenant les accès aux différents isolats
* le récepteur, permettant de recevoir les signaux et les diffuser aux abonnés
* l'émetteur, permettant de diffuser les signaux aux isolats.

Ces éléments sont décrits plus en détails plus loin dans ce document.


Annuaire dynamique
------------------

L'annuaire de PSEM2M Signals dispose d'un composant particulier,
*Directory Updater*, permettant l'inscription dynamique d'isolats lorsque
ceux ci émettent un signal d'enregistrement, ainsi que le transfert de
l'annuaire d'un isolat à un autre.

De cette manière, l'annuaire ne se base sur aucun fichier de configuration et
se construit au fur et à mesure.
De plus, l'annuaire est totalement décentralisé, ce qui permet de supporter
facilement la disparition d'un isolat central.


Transmissions d'un signal
=========================

Les détails de l'implémentation actuelle de PSEM2M Signals sont décrits à la
fin de ce document.

Définition
----------

Un signal est un paquet transmis à un récepteur de signaux d'un isolat par
un émetteur.

Il porte au moins les informations suivantes :

* Un nom de signal ayant le format d'une URI absolue.
* Un mode de signalisation, indiquant comment le récepteur doit répondre au
  signal
* Un contenu de premier niveau, donnant des informations sur l'émetteur du
  signal
* Un contenu de second niveau, des données supplémentaires pouvant être
  utilisées par les abonnés.

.. _modes:

Mode
----

Il est nécessaire d'indiquer un mode de transmission pour que le récepteur
sache comment gérer un signal et quand renvoyer une réponse.

Il existe trois modes de transmissions :

* ``MODE_SEND``: le traitement est synchrone, le résultat est l'aggrégatation
  des résultats de tous les listeners.
  Utilisé par les méthodes ``send*`` et ``post*``.

* ``MODE_FORGET``: le traitement est asynchrone, le résultat est vide.
  Utilisé par les méthodes ``fire*``.

* ``MODE_ACK``: le traitement est asynchrone, le résultat indique si au moins
  un listener va gérer le signal.
  Non utilisé.

Les méthodes sont décrites plus loin dans ce document.


Contenu
-------

Le format de transmission des données dépend de l'implémentation utilisée.
La version préconisée est basée sur JSON, avec des informations sur les types
de données transmis comme le fait *Jabsorb*.

Le contenu de premier niveau des signaux est normalisé par l'interface Java
``ISignalData`` et contient au moins :

* l'identifiant de l'émetteur
* le nœud hôte de l'émetteur
* la date d'émission du signal

L'adresse de l'émetteur, nécessaire pour être compatible avec ``ISignalData``,
peut être laissée vide pendant la transmission et être inscrite par le
récepteur, utilisant l'adresse indiquée dans les trames réseau.

Un contenu de second niveau peut être ajouté au signal, les types autorisés
dépendent de l'implémentation utilisée.
L'implémentation préconisée se base sur Jabsorb en Java, ce qui signifie que
les objets transmis doivent avoir un *getter* et un *setter* pour chacune de
leurs propriétés.
