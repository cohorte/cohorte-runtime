.. Description des services de signaux

Services PSEM2M Signals
#######################

.. note:: Les implémentations Python et Java sont fondamentalement différentes.

   * la version Python est une implémentation directe de la spécification par
     dessus le service HTTP
   * la version Java utilise des fournisseurs pour séparer la logique de
     sélection de cible, de la transmission effective des données

Annuaire
********

Contenu
=======

L'annuaire conserve les accès des isolats connus par l'isolat courant.
Chaque isolat est associé à un couple nœud-port, et à chaque nœud est associée
une adresse IP ou un nom d'hôte.

Cette abstraction par un identifiant de nœud a été utilisée afin de permettre
à un nœud de changer d'adresse en cours d'exécution.

Groupes
-------

L'annuaire défini deux types de groupes d'isolats:

* les groupes personnalisés, définis dans la configuration de l'isolat.
  Ces groupes permettent de communiquer avec un sous-ensemble particulier
  d'isolats

* les groupes de l'annuaire, calculés à chaque fois par l'annuaire lui-même en
  fonction des identifiants d'isolats. Ces groupes sont les suivants :

  +------------+-----------------------------------------------------------+
  | Groupe     | Cibles                                                    |
  +============+===========================================================+
  | ALL        | Tous les isolats, y compris l'actuel                      |
  +------------+-----------------------------------------------------------+
  | OTHERS     | Tous les isolats, sauf l'actuel                           |
  +------------+-----------------------------------------------------------+
  | CURRENT    | L'isolat actuel                                           |
  +------------+-----------------------------------------------------------+
  | NEIGHBOURS | Tous les isolats du même nœud que l'actuel, sauf l'actuel |
  +------------+-----------------------------------------------------------+
  | MONITORS   | Tous les moniteurs, sauf l'isolat actuel                  |
  +------------+-----------------------------------------------------------+
  | FORKERS    | Tous les *forkers*, sauf l'isolat actuel                  |
  +------------+-----------------------------------------------------------+
  | ISOLATES   | Tous les isolats, sauf l'actuel et les *forkers*          |
  +------------+-----------------------------------------------------------+

  Ces groupes sont définis dans l'énumération ``ISignalDirectory.EBaseGroup``.


Construction
============

L'annuaire possède un ensemble de méthodes permettant d'y ajouter et supprimer
des entrées.

L'implémentation PSEM2M utilise les méthodes *sendTo* (voir les méthodes de
l'émetteur) pour qu'un isolat demande à son *forker* une copie de son annuaire,
avant d'émettre un signal d'enregistrer à tous les membres de cet annuaire.
De même, dès qu'un *forker* est détecté par un moniteur, ce dernier invite le
*forker* en question à mettre à jour son annuaire.

À chaque signal d'enregistrement ou de mise à jour, l'annuaire met à jour
l'adresse associée au nœud de l'émetteur par celle détectée par le récepteur
de signaux.


Émetteur
********

Pour plus de simplicité, il est recommandé de n'avoir qu'un émetteur par isolat,
celui-ci pouvant se baser sur plusieurs fournisseurs.

L'implémentation de l'émetteur peut envoyer directement les signaux ou se baser
sur des fournisseurs.

Méthodes
========

Un émetteur doit implémenter l'interface ``ISignalBroadcaster``, définissant
plusieurs ensembles méthodes d'envoi de signal.

Il existe quatre catégories d'envoi de signaux en Java, trois en Python :

+---------+-------------------------------------------------------------------+
| Préfixe | Description                                                       |
+=========+===================================================================+
| send*   | Envoi bloquant d'un signal. La méthode retourne le résultat de    |
|         | chaque listeners de chaque isolat                                 |
+---------+-------------------------------------------------------------------+
| post*   | Envoi d'un signal dans un thread à part. Retourne un objet        |
|         | ``Future`` permettant d'obtenir plus tard le même résultat que    |
|         | ``send``                                                          |
+---------+-------------------------------------------------------------------+
| fire*   | Envoi d'un signal bloquant, mais n'attendant pas les résultats.   |
|         | Retourne la liste des isolats ayant reçu le signal.               |
|         | **ATTENTION:** même si un isolat a reçu le signal, il est         |
|         | possible qu'aucun listener n'y soit associé.                      |
+---------+-------------------------------------------------------------------+
| stack*  | Mise en attente d'un signal. Le signal sera émis dès qu'un        |
|         | *provider* compatible sera présent. Il est mis en échec si un     |
|         | timeout est levé avant l'émission. Un listener donné en paramètre |
|         | est rappelé en cas de succès ou d'échec de l'envoi.               |
|         | **ATTENTION:** ce type d'envoi n'est pas géré en Python           |
+---------+-------------------------------------------------------------------+

Pour chaque catégorie, il est possible d'émettre vers quatre types de cibles :

+-------------------+---------------------------------------------------------+
| Cible             | Description                                             |
+===================+=========================================================+
| Accès Isolat      | La cible est décrite par un couple adresse/port         |
|                   | permettant de communiquer avec récepteur de signaux.    |
|                   | Cette méthode est utile pour communiquer avec un isolat |
|                   | absent de l'annuaire                                    |
+-------------------+---------------------------------------------------------+
| Isolat(s)         | Les cibles sont décrites un ou plusieurs identifiants   |
|                   | d'isolats. L'annuaire est utilisé pour résoudre leurs   |
|                   | accès                                                   |
+-------------------+---------------------------------------------------------+
| Groupe(s)         | Les cibles sont décrites par un ou plusieurs noms de    |
|                   | groupes personnalisés.                                  |
|                   | **ATTENTION:** pour le moment, ces groupes ne sont pas  |
|                   | peuplés                                                 |
+-------------------+---------------------------------------------------------+
| Groupe d'annuaire | Les cibles sont décrites par un groupe standard,        |
|                   | calculé par l'annuaire. Il est possible d'exclure des   |
|                   | identifiants d'isolats de ce groupe lors de l'émission  |
+-------------------+---------------------------------------------------------+


Méthodes Python/Java
--------------------

Les méthodes ne sont pas nommées de la même manière en Python et en Java:

* en Python, seuls deux noms existent pour chaque méthode, les cibles étant
  décrites par les paramètres,
* en Java, il existe une méthode par groupe et par cible.

+------------+------------+-----------------------------------------------+
| Nom Python | Nom Java   | Description                                   |
+============+============+===============================================+
| send_to    | sendTo     | Émission vers un accès                        |
+------------+------------+-----------------------------------------------+
| post_to    | postTo     | Émission vers un accès                        |
+------------+------------+-----------------------------------------------+
| fire_to    | fireTo     | Émission vers un accès                        |
+------------+------------+-----------------------------------------------+
| send       | send       | Émission vers un ou plusieurs isolats         |
+------------+------------+-----------------------------------------------+
| post       | post       | Émission vers un ou plusieurs isolats         |
+------------+------------+-----------------------------------------------+
| fire       | fire       | Émission vers un ou plusieurs isolats         |
+------------+------------+-----------------------------------------------+
| n/a        | stack      | Mise en attente vers un ou plusieurs isolats  |
+------------+------------+-----------------------------------------------+
| send       | sendGroup  | Émission vers un ou plusieurs groupes,        |
|            |            | ou vers un groupe de l'annuaire               |
+------------+------------+-----------------------------------------------+
| post       | postGroup  | Émission vers un ou plusieurs groupes,        |
|            |            | ou vers un groupe de l'annuaire               |
+------------+------------+-----------------------------------------------+
| fire       | fireGroup  | Émission vers un ou plusieurs groupes,        |
|            |            | ou vers un groupe de l'annuaire               |
+------------+------------+-----------------------------------------------+
| n/a        | stackGroup | Mise en attente vers un ou plusieurs groupes, |
|            |            | ou vers un groupe de l'annuaire               |
+------------+------------+-----------------------------------------------+


Récepteur
*********

Pour plus de simplicité, il est recommandé de n'avoir qu'un récepteur par
isolat, celui-ci pouvant se baser sur des fournisseurs.

Désormais, les abonnées peuvent répondre directement en retournant une valeur
après le traitement du signal.
Le récepteur a pour charge d'aggréger les résultats de tous les abonnés
d'un signal et de retourner cet ensemble à l'émetteur par le flux qu'il a
ouvert.


Interface
=========

Un récepteur doit implémenter l'interface ``ISignalReceiver``, définissant les
méthodes suivantes :

* ``HostAccess getAccessInfo()``

  Cette méthode retourne l'accès (couple adresse-port) à utiliser pour
  communiquer avec ce récepteur.
  Le port est le plus important, étant donné que l'adresse peut être calculé
  par les isolats cibles.
  L'implémentation peut retourner *null* s'il n'est pas prévu que ce récepteur
  soit utilisé à distance.

* ``SignalResult handleReceivedSignal(String aSignalName, ISignalData aData, String aMode)``

  Cette méthode est appelée par le *provider* de réception.
  Le mode indique comment le traitement du signal et la valeur de retour doivent
  être gérés (synchrone, asynchrone, ...).
  Les modes actuellement disponibles sont décrits dans la sections :ref:`modes`.

* ``SignalResult localReception(String aSignalName, ISignalData aData)``

  Cette méthode est utilisée par un émetteur de signal quand la cible est
  l'isolat actuel.
  Cette transmission en direct permet d'assurer la réception du signal même en
  cas d'absence de *provider*.


* ``void registerListener(String aSignalName, ISignalListener aListener)``

  Cette méthode enregistre un abonné au signal indiqué. Le nom du signal peut
  contenir des *jokers* ('\*' et '?') afin que l'abonné soit inscrit à un
  ensemble de signaux.
  Un abonné peut être inscrit à plusieurs signaux en utilisant les *jokers* ou
  en appelant plusieurs fois cette méthode.


* ``void unregisterListener(String aSignalName, ISignalListener aListener)``

  Cette méthode retire un abonné à la liste des inscrits à un signal. Le nom
  du signal doit être le même que celui utilisé lors de l'inscription (avec
  les même *jokers*, ...).
  L'abonné conserve ses inscriptions aux autres signaux.


Interface des abonnés
=====================

Un abonné à un signal doit implémenter l'interface ``ISignalListener``,
définissant une méthode :

* ``Object handleReceivedSignal(String aSignalName, ISignalData aSignalData)``

  Cette méthode est appelée par le récepteur à chaque fois qu'un signal est
  reçu.

  Le nom du signal est celui du signal reçu et ne comprend donc pas de *joker*.
  La donnée associée au signal n'est jamais nulle, mais peut être incomplète.

  Si la valeur de retour est non nulle, elle sera ajoutée à la liste des
  résultats à retourner à l'émetteur.
