.. Implémentation des fournisseurs de signaux

Fournisseurs de signaux
#######################

Définition
**********

La méthode recommandée pour implémenter le mécanisme de signaux dans la
plate-forme PSEM2M est de n'utiliser qu'un service d'émission et qu'un service
de réception, et de leur permettre d'utiliser des services fournisseurs de
signaux.

Chaque implémentation de fournisseur doit offrir au moins deux services :

* Un service d'émission de signaux, implémentant ``ISignalBroadcastProvider``
* Un service de réception de signaux, implémentant ``ISignalReceptionProvider``

Les interfaces des providers correspondent à celles des services les utilisant,
mais n'ont pas de notion de transmission locale d'un signal.

La détermination des paramètres d'émission d'un signal sont à la charge du
fournisseur utilisé.

Les deux services sont fortement couplés : ils doivent comprendre le même
protocole pour transmettre et recevoir des signaux d'un isolat à l'autre.

Selon les capacités attendues de la plateforme, les signaux peuvent être
limitées à des communications inter-processus ou peuvent nécessiter un
traitement particulier pour être transmis sur un réseau.


Implémentation disponible
*************************

Actuellement, seule une implémentation utilisant le protocole HTTP est
disponible pour la plate-forme PSEM2M.

Réception d'un signal
=====================

Le principe est d'inscrire une *servlet* à un serveur HTTP pour recevoir des
signaux.
Celle-ci est inscrite sous l'alias **/psem2m-signal-receiver**, défini dans
``IHttpSignalsConstant``.

Lorsqu'une requête de type **POST** est reçue, le chemin de l'URL succédant à
l'alias de la *servlet* est considéré comme étant le nom du signal reçu.

Le corps de la requête **POST** est lu à l'aide d'un ``ObjectInputStream``.
Si le corps de la requête est valide et qu'il s'agit d'un objet
``HttpSignalData``, celui-ci est mis à jour afin de contenir le nom d'hôte du
client.
Si le corps de la requête est vide, un objet vide est créé.

Enfin, le ou les services de réception de signaux inscrits à ce fournisseur sont
notifiés.

Émission d'un signal
====================

L'émission des signaux est effectuée avec le client HTTP intégré à la JRE
(``HTTPUrlConnection``).
Celui-ci est configuré afin d'envoyer un requête **POST** au récepteur.

L'URL utilisée pour se connecter au récepteur d'un isolat est forgée à partir
du service de configuration (``IIsolateDescr.getAccessUrl()``).

Un objet ``HttpSignalData`` est préparé pour contenir les données associées au
signal, puis ajouté au corps de la requête à l'aide d'un ``ObjectOutputStream``.
