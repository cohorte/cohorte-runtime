.. Contexte d'utilisation et but

Contexte d'utilisation
======================

But du projet
-------------

Le but du projet PSEM2M est de fournir les outillages de développement et
d'exécution nécessaires à la construction d'un serveur d'échange robuste.

Il se découpe en quatre parties :

#. Une partie système, dépendante du système d'exploitation hôte et pouvant être
   absente sur certains d'entre eux. 
   Elle permet un contrôle fin et efficace sur les processus gérés.

#. Une partie applicative indépendante, le moniteur, s'occupant du cycle de vie
   des processus gérés.

#. Des sondes à insérer dans les processus à gérer.
   Ils peuvent se présenter sous la forme de bundles OSGi ou de plugins
   d'application.

#. Un IDE, permettant la création, le développement et la configuration du
   serveur d'échange à générer.


Types d'utilisateurs
--------------------

Les utilisateurs principaux de PSEM2M seront principalement des développeurs
ayant une connaissance moyenne du langage de programmation Java.

La configuration de l'outillage sera gérée par des administrateurs système
et/ou réseau.


Contexte matériel
-----------------

Le projet cible principalement les serveurs, c'est-à-dire des machines ayant
une puissance de calcul moyenne mais une quantité de mémoire vive relativement
importante.


Il est prévu que le projet soit porté sur des périphériques embarqués à
puissance de calcul moyenne, tels que des *smartphones*.


Contexte logiciel
-----------------

Le projet est initialement prévu pour fonctionner sur les systèmes suivants :

* Linux >= 2.6.32
* Mac OS X
* Windows, famille NT, >= Windows 2000


Comme indiqué précédemment, il est possible que le projet soit porté sur du
matériel embarqué.

Dans la plupart des cas, il s'agira :

* de plateformes Linux *pures*, comme les micro-PC
* de plateformes Linux modifiées, comme les téléphones Android

Le portage vers des périphériques fonctionnant sous Windows CE ou iOS n'est pas
prévu. 
