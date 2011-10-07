.. Description des services de signaux

Services de signalisation
#########################

Les services de signalisation sont répartis dans trois grands groupes : les
émetteurs, les récepteurs et les fournisseurs.

Les fournisseurs sont décrits dans la section suivante.

Définition d'un signal
**********************

Un signal est un paquet transmis à un recepteur de signaux d'un isolat, avec
les propriétés suivantes :

* Le nom du signal, **obligatoire**, ayant le format d'une URI absolue.

  Exemple: /psem2m/isolate/status

* Une donnée associée au signal, **recommandée**, décrivant l'émetteur du signal
  et un contenu.

  Si cette donnée est manquante, un instance vide doit être créée, afin de ne
  pas transmettre une valeur nulle aux abonnées aux signaux.

  Le contenu associé à la donné peut être nul et doit implémenter l'interface
  Serializable.

  La donnée associée au signal doit implémenter l'interface ``ISignalData``.


Des implémentations plus riches de ces paquets sont possibles, notamment pour
stocker plus d'informations sur l'émetteur d'un signal.


Émetteur
********

Pour plus de simplicité, il est recommandé de n'avoir qu'un émetteur par isolat,
celui-ci pouvant se baser sur des fournisseurs.

Interface
=========

Un émetteur doit implémenter l'interface ``ISignalBroadcaster``, définissant
deux méthodes d'envoi de signal :

* ``void sendData(EEmitterTargets aTargets, String aSignalName, Serializable aData)``

    Émet un signal à l'ensemble d'isolats défini par *aTargets* à partir d'un
    nouveau thread.
    Il n'y a pas moyen de savoir si l'envoi a réussi ou non.


* ``boolean sendData(String aIsolateId, String aSignalName, Serializable aData)``

   Émet un signal à l'isolate indiqué, en attendant la confirmation de l'envoi.
   Retourne *Vrai* si l'envoi a réussi.


Dans les deux cas, le nom du signal doit être valide, la donnée associée peut
être nulle.

L'implémentation de l'émetteur peut envoyer directement les signaux ou se baser
sur des fournisseurs.


Cibles de signaux
=================

La méthode ``sendData(String, String, Serializable)`` envoie le message à
l'isolat indiqué, en fonction de la configuration de la plateforme
(port, protocole, ...).


La méthode ``sendData(EEmitterTargets, String, Serializable)`` se base sur la
notion d'ensembles de cibles :

* **LOCAL**, pour émettre un signal dans l'isolat courant, sans passer par les
  fournisseurs.
  La transmission du signal doit être disponible même si l'isolat ne peut pas
  communiquer avec des éléments externes (autres processus, ...).

* **MONITORS**, cible les moniteurs de la plate-forme.
  L'isolat courant et le *forker* ne sont pas pris en compte.

* **FORKER** cible le *forker*
  Seule cette valeur permet d'envoyer un signal au *forker*.

* **ISOLATES** cible tous les isolats autres que ceux internes à la plate-forme.
  L'isolat courant n'est pas pris en compte.

* **ALL** combine les cibles **MONITORS** et **ISOLATES**.
  L'isolat courant n'est pas pris en compte.


Récepteur
*********

Pour plus de simplicité, il est recommandé de n'avoir qu'un récepteur par
isolat, celui-ci pouvant se baser sur des fournisseurs.

Interface
=========

Un récepteur doit implémenter l'interface ``ISignalReceiver``, définissant les
métodes suivantes :

* ``void localReception(String aSignalName, ISignalData aData)``

  Cette méthode est utilisée par un émetteur de signal quand la cible **LOCAL**
  est utilisée.
  Cette transmission en direct permet d'assurer la réception du signal.


* ``registerListener(String aSignalName, ISignalListener aListener)``

  Cette méthode enregistre un abonné au signal indiqué. Le nom du signal peut
  contenir des *jokers* ('\*' et '?') afin que l'abonné soit inscrit à un
  ensemble de signaux.
  Un abonné peut être inscrit à plusieurs signaux en utilisant les *jokers* ou
  en appelant plusieurs fois cette méthode.


* ``unregisterListener(String aSignalName, ISignalListener aListener)``

   Cette méthode retire un abonné à la liste des inscrits à un signal. Le nom
   du signal doit être le même que celui utilisé lors de l'inscription (avec
   les même *jokers*, ...).
   L'abonné conserve ses inscriptions aux autres signaux.


Lors de la réception d'un signal, le récepteur doit utiliser un thread séparé
pour notifier les abonnés.

De cette manière, le récepteur peut se mettre en attente d'un nouveau signal
alors que le traitement des précédents est toujours en cours.


Interface des abonnées
======================

Un abonné à un signal doit implémenter l'interface ``ISignalListener``,
définissant une méthode :

* ``void handleReceivedSignal(String aSignalName, ISignalData aSignalData)``

  Cette méthode est appelée par le récepteur à chaque fois qu'un signal est
  reçu.

  Le nom du signal est celui du signal reçu et ne comprend donc pas de *joker*.
  La donnée associée au signal n'est jamais nulle, mais peut être incomplète.

