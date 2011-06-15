.. Architecture

Architecture du projet
######################

.. todo:: À décrire, schémas à l'appui

.. todo:: À reprendre, vues logiques, physiques, scénarios

Plateforme globale
******************

.. image:: /_static/diagrams/global.png
   :width: 5cm

Comme indiqué dans le cahier des charges, la plateforme est composée :

* d'un moniteur, instance unique, se chargeant du cycle de vie des isolats et
  de leurs liaisons,
* d'un forker, instance unique, démarrant et tuant les isolats sur ordre du
  moniteur,
* d'un ensemble d'isolats.

Les liaisons entre isolats sont appelées routes. Elles sont gérées par le
moniteur, suivant les fichiers de configuration, et non par les isolats eux
mêmes.

Moniteur
********

Le moniteur est composé de :

* un gestionnaire d'isolats, se chargeant de la surveillance des isolats
  démarrés.

* un gestionnaire de routes, calculant et effectuant les liaisons entre isolats.


.. todo:: Insérer vue logique


Forker
******

Le forker est composé de :

* un démarreur de processus, dépendant de la plateforme d'exécution.
* un gestionnaire de processus, se chargeant de la surveillance des processus
  lancés.

Le gestionnaire de processus est la partie dépendante du système d'exploitation
utilisée par le gestionnaire d'isolats du moniteur.

.. todo:: Insérer vue logique


Isolat
******

Chaque isolat dispose au moins d'une sonde permettant de contrôler son état,
décrite dans la section *Sondes* du document de spécifications externes.
