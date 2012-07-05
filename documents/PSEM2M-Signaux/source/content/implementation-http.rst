.. Implémentation HTTP / JSON

Implémentation: HTTP / JSON
###########################

.. note:: Cette implémentation est celle préconisée pour un fonctionnement
   optimal de PSEM2M.


Protocole
*********

L'implémentation de référence de PSEM2M est basée sur HTTP:

* les requêtes utilisent toujours la méthode POST
* l'URI indique le nom du signal transmis,
* les en-têtes suivants sont utilisés pour ajouter de l'information :

  * ``Content-Type``: indique le type de contenu de la requête.
    Seul ``application/json`` devrait être utilisé, même pour sans contenu.

  * ``psem2m-mode``: indique le mode de transmission, comme indiqué dans
    :ref:`modes`.

Le contenu du signal est inscrit dans le corps de la requête POST.


Format de données
=================

Les données échangées sont au format JSON/Jabsorb.
Pour chaque objet JSON, un champ ``javaClass`` est ajouté afin d'indiquer
comment le re-charger dans le monde Java.

Dans le cas des listes et des maps, l'objet Jabsorb contient l'entrée
``javaClass`` ainsi qu'une entrée, respectivement ``list`` ou ``map``, contenant
les entrées de la liste ou de la map.
Cette indirection permet notamment d'éviter de considérer ``javaClass`` comme
une entrée de la map décrite.

* En Java, la conversion est effectuée par Jabsorb, au travers d'un service de
  sérialisation (voir :ref:`specif-java`).

* En Python, deux méthodes utilitaires, ``from_jabsorb`` et ``to_jabsorb``,
  permettent de transformer un objet JSON standard en objet compatible avec
  Jabsorb.


Échanges
========

Les signaux fonctionnent toujours sur le principe requête-réponse, ce qui
correspond au protocole HTTP.

L'émetteur envoie une requête à un récepteur qui, selon le mode de
transmission, renverra :

* une réponse immédiate de réception (code 200) en mode FORGET
* une indication de présence (code 200) ou d'absence (404) d'abonné pour le
  signal reçu, en mode ACK
* le résultat des abonnés (code 200) en mode SEND.

Dans tous les cas, si une erreur a lieu côté serveur, un code erreur supérieur
ou égale à 500 sera renvoyé.

Les transmissions en mode asynchrone sont simulées par l'utilisation du mode
FORGET: les émetteurs n'attendent pas la fin du traitement demandé, mais
attendent tout de même que des isolats cibles reçoivent le signal.


.. _specif-java:

Implémentation Java
*******************

Description
===========

L'implémentation Java de PSEM2M Signals diffère de celle en Python du fait de
l'utilisation de fournisseurs de transport et de sérialisation.
Cette solution a été choisie pour simplifier le démarrage de la plateforme
PSEM2M Java.
En effet, PSEM2M Signals nécessite au moins un service de serveur HTTP et un
serializer JSON, alors PSEM2M Signals peut être requis en interne avant que
ceux-ci ne soient disponibles.

De fait, PSEM2M Signals Java est capable de fonctionner en mode *hors-ligne*,
ne traitant que les signaux internes à l'isolat.
Les services passent en mode *en ligne* dès qu'un fournisseur de transport est
activé.

De la même manière, le serializer Java est disponible dès le démarrage de
PSEM2M Signals, tandis que la version JSON/Jabsorb n'est disponible que bien
plus tard.
Il est donc souvent nécessaire d'indiquer qu'un composant dépend de PSEM2M
Signals avec la propriété *en ligne* activée.

Outils utilisés
===============

L'implémentation de l'émetteur utilise la classe ``java.net.HttpURLConnection``,
fournie avec la JVM.
De cette manière, il est certain que cette classe sera disponible sur la majeure
partie des JVM, y compris Android.

Le récepteur se base quant à lui sur le service HTTP du standard OSGi.
L'implémentation actuellement utilisée de ce service est celle du projet Apache
Felix, basée sur le serveur Jetty.


.. _specif-python:

Implémentation Python
*********************

Description
===========

Les capacités de serveur HTTP et de parsing JSON étant présentes très tôt dans
le démarrage d'un isolat Python, l'implémentation de PSEM2M Signals dans ce
langage est beaucoup moins dynamique et se base directement sur le service HTTP
disponible.

Ainsi, l'émetteur, le récepteur et l'annuaire PSEM2M Signals sont chacun
implémentés par un composant ne dépendant pas de fournisseurs.

Outils utilisés
===============

Le module ``http.client`` de la bibliothèque standard Python est utilisé
par l'émetteur.

Le récepteur est basé sur un service HTTP pour le framework pseudo-OSGi Pelix,
développé par isandlaTech.
Ce service se base lui-même sur le module ``http.server`` de la bibliothèque
standard Python.
