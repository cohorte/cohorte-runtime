.. Description de l'implémentation

Implémentations disponibles
###########################

Implémentation JSON-RPC
***********************

Actuellement, seule une implémentation des fournisseurs se basant sur JSON-RPC
est disponible.

Description de l'implémentation
===============================

Ces fournisseurs se basent sur la bibliothèque Jabsorb, remaniée afin de mieux
correspondre aux contraintes de PSEM2M.

Le *end point handler* se base sur le ``JSONRPCBridge`` de Jabsorb, une
*servlet* devant être hébergée par un service de serveur HTTP.
Le fournisseur se base sur le premier service implémentant ``HTTPService``
trouvé dans le registre central OSGi.

Le *client handler* se base sur une ``HttpUrlConnection`` pour dialoguer avec
les *end points*. Un client ne dépend donc pas de services particulier.


Contraintes
===========

Les contraintes sont ici liées à Jabsorb : la conversion des paramètres et des
valeurs de retour du monde Java au monde JSON et vice versa est laissée à cette
bibliothèque.

Jabsorb se base sur des *Serializers*, des classes qui indiquent si elles
peuvent convertir un objet ou non.

Les *Serializers* suivant sont disponibles :

* ``ArraySerializer`` : traitant les tableaux
* ``BeanSerializer`` : traitant les beans, des classes ayant un *getter* et un
  *setter* publics pour chacun de ses membres.
* ``BooleanSerializer`` : traitant les booléens
* ``DateSerializer`` : traitant les objets Date
* ``DictionnarySerializer`` : traitant les collections implémentant
  ``Dictionnary``
* ``EnumSerializer`` : traitant les énumérations (classe créée par isandlaTech)
* ``ListSerializer`` : traitant les collections implémentant ``List``
* ``MapSerializer`` : traitant les collections implémentant ``Map``
* ``NumberSerializer`` : traitant les objets implémentant ``Number``
  (``Integer``, ``Long``, ...)
* ``PrimitiveSerializer`` : traitant les types primitifs
* ``SetSerializer`` : traitant les collections implémentant ``Set``
* ``StringSerializer`` : traitant les chaînes de caractères


Ainsi, tous les objets transmis par appel d'un service distant doit pouvoir
être géré par un de ces *serializers*.


Perspectives
============

Pour permettre plus de souplesse dans les données à transférer, il serait utile
d'étendre la bibliothèque Jabsorb pour traiter les objets des types suivant :

* ``Serializable`` : transmettant une version *base64* du binaire sérializé
* ``JSONable`` : exposant deux méthode, ``toJSON()`` et ``fromJSON`` permettant
  à une classe de définir explicitement le ``JSONObject`` représentant son
  instance.
