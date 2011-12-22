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


Propositions d'implémentations
******************************

L'utilisation du mécanisme de *serialisation* de Java permet de simplifier
énormément l'utilisation des signaux entre deux machines virtuelles.
Cependant, ce choix empêche d'émettre et de recevoir les signaux de programmes
écrits dans un autre langage, ne se basant pas sur la machine virtuelle Java.

Dans tous les cas, le contenu associé au signal doit fournit suffisamment
d'informations pour pouvoir être converti en un objet implémentant l'interface
``ISignalData``.


Version synchrone du service
============================

Dans la spécification actuelle, les signaux sont considérés comme des
transmissions asynchrone, sans garantie de réception des données.

Une implémentation synchrone, permettant d'attendre la réception des données
par la ou les cibles serait intéressant dans les cas de transmission d'ordre.

Il faudrait alors renommer les méthodes actuelles ``sendData`` des émetteurs en
``postData``, et définir une nouvelle sémantique pour les méthodes équivalentes
nommées ``sendData``.

* ``postData`` : les signaux sont émis immédiatement, sans attendre de réponse.
  L'appel à cette méthode retourne immédiatement après que le signal soit mis en
  file d'attente d'envoi.

* ``sendData`` : les signaux sont émis immédiatement, mais l'appel ne retourne
  pas avant que les destinataires aient répondu ou que l'envoi d'un signal ait
  échoué.


Modèle requête - réponse
========================

On peut diviser les cas d'usage du mécanisme des signaux en trois grandes
catégories :

* les signaux émis pour information, n'ayant pas forcément d'abonnés
* les signaux émis comme ordres ou requêtes
* les signaux émis en réponse à un ordre ou une requête

Pour des simplification d'écriture de code et de standardisation du comportement
des signaux, il serait intéressant de mettre en place une méthode du service
émetteur permettant d'émettre un message et d'attendre la réponse du ou des
destinataires.

Plusieurs méthodes d'implémentation sont possibles :

* Utilisation d'un domaine de signaux (URI) spécifique */request* et
  */response*.

* Appel d'une méthode spécifique pour traiter ce genre de signaux, renvoyant
  une donnée à émettre en réponse

* Conservation du modèle actuel avec ajout d'un *signal ID*, le traitement se
  faisant au niveau du service de réception.


Solution parallèle ou alternative ?
===================================

Cette section décrit quelques possibilités d'implémentations de fournisseurs de
signaux pouvant résoudre ce problème d'interopérabilité.

On distinguera deux types de solutions :

* les solutions alternatives : les fournisseurs ne supportent que le mode
  *ouvert*, permet d'émettre et de recevoir des signaux dans un ou plusieurs
  formats utilisables par des outils ne se basant pas sur la machine virtuelle
  Java,

* les solutions parallèles : les fournisseurs supportent à la fois le mode
  *Java*, utilisant la *serialisation*, et un ou plusieurs mode *ouvert*.


Service de conversion d'objets ISignalData
------------------------------------------

Afin de simplifier le travail des fournisseurs et de centraliser du code pouvant
s'avérer redondant, il serait judicieux de définir un service se chargeant de
convertir :

* un objet Java ``ISignalData`` dans le format attendu par le récepteur du
  signal
* le contenu d'un signal reçu en un objet Java ``ISignalData``


L'interface proposée pour un tel service est la suivante :

.. literalinclude:: ../_static/ISignalDataConverter.java
   :language: java
   :tab-width: 4
   :linenos:


Le service devra également avoir une propriété ``signal.data.convert.accepts``,
renvoyant un tableau d'objets String (``String[]``), indiquant les formats de
donnés qu'il est capable de traiter.

Solutions parallèles
--------------------

Extended HTTP Signals Provider
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Cette implémentation serait une extension du fournisseur HTTP basé sur
``ObjectInputStream`` et ``ObjectoutputStream``.

Le principe est d'utiliser le champ *Content-Type* du protocole HTTP pour
déterminer le format du contenu du signal.
Les principaux types de contenu à gérer sont :

* *application/octet-stream* : pour les objets Java sérialisés
* *application/json* : pour les signaux décrits en JSON
* *text/xml* : pour les signaux décrits en XML

L'utilisation d'un service de type ``ISignalDataConverter`` est fortement
recommandée, ceci garantissant notamment l'extensibilité des formats de données
utilisables.


Solutions alternatives
----------------------

Sans objet pour le moment.
