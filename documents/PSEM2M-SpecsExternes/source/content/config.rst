.. Description des fichiers de configuration

Fichiers de configuration
=========================

Il existera au moins deux fichiers de configurations :

* le premier concernera le comportement d'exécution de la plateforme PSEM2M, 
  plus particulièrement celui du moniteur.

* le second concernera la description des isolats, leur configuration commune
  et leur configuration propre.


Ces fichiers devraient être au format XML, permettant ainsi d'être édité
manuellement ou à l'aide d'un éditeur spécifique.

Le principal intérêt de ce format est de pouvoir valider le fichier à l'aide
d'un schéma.
Ceci permettra notamment à un administrateur de tester son fichier de
configuration sans avoir à l'appliquer à une instance de la plateforme.


Configuration de la plateforme
------------------------------

Dans ce fichier, on doit être capable de définir les comportements du moniteur,
à savoir :

* le délai de détection des isolats crashés ou stoppés
* le nombre de tentatives de redémarrage d'un isolat
* les modes de sondage à utiliser (JMX, ping, ...) pour détecter l'état d'un
  isolat et leur paramètres propres
* port d'écoute de la socket d'administration

Configuration des isolats
-------------------------

Théoriquement, tout processus peut être isolé; cependant, nous ne garantissons
que l'isolement de machines virtuelles Java et plus particulièrement les 
frameworks OSGi. 

Ce fichier doit contenir une configuration par défaut des isolats, ainsi qu'une
description de chaque isolat et leur configuration propre.

Configuration par défaut
^^^^^^^^^^^^^^^^^^^^^^^^

Dans ce nœud, l'administrateur doit indiquer les valeurs de configuration à 
utiliser si elles ne sont pas précisées dans la section correspondant à un 
isolat.

L'administrateur doit pouvoir y définir optionnellement :

* les droits d'exécution de l'isolat (utilisateur, groupe, ...) 
* les variables d'environnement (PATH, HOME, ...)
* les arguments à passer à l'isolat

Si un de ces paramètre est redéfini pour un isolat, sa valeur par défaut est
ignorée.


Configuration d'un isolat
^^^^^^^^^^^^^^^^^^^^^^^^^

Pour chaque isolat, il faut définir le processus à exécuter.

L'administrateur devra ici indiquer obligatoirement :

* le nom de l'exécutable correspondant à l'isolat

Si plusieurs isolats sont basés sur le même exécutable, ayant les mêmes
paramètres (arguments, environnement, ...), alors le moniteur ne créera qu'une
seule image de base de cet exécutable.
Au contraire, si deux isolats utilisent le même exécutable avec des paramètres
différents, le moniteur exécutera une instance

Il pourra également indiquer :

* les droits d'exécution de l'isolat 
* les variables d'environnement (PATH, HOME, ...)
* les arguments à passer à l'isolat
* le nom d'un exécutable à lancer *avant* le démarrage de l'isolat et ses
  paramètres
* le nom d'un exécutable à lancer *après* la mort de l'isolat et ses paramètres
