.. Perspectives de PSEM2M Composer

Perspectives
############

Fonctionnement global
*********************

Le modèle de PSEM2M Composer a été conçu pour une architecture de démonstration
particulière. Certains points doivent être revus et corrigés.


Renommages de certains termes
=============================

Certaines constantes n'ont pas été modifiées depuis la première implémentation
de PSEM2M Composer, alors appelé PSEM2M Composition.

Certains noms de constantes, de variables et certaines valeurs sont encore
basées sur l'ancienne terminologie.
De cette manière, les noms seront correctement standardisés et permettront
d'éviter toute assimilation avec le standard SCA.

Le principal terme à reprendre est ``composite`` à transformer en ``composet``
ou ``ComponentsSet``.


Gestion de plusieurs agents par isolat
======================================

Pour le moment, les signaux ont pour unité atomique l'isolat, selon la
définition de la plateforme PSEM2M.
Cette unité devrait devenir l'agent, principalement pour être capable d'avoir
plusieurs agents dans un même isolat.

Les modifications à apporter seront alors les suivantes :

* Les données des signaux devront alors être étendues pour contenir
  l'identifiant de l'agent cible dans l'isolat. Ceci peut modifier le type
  des données transportées par le signal

* Les agents doivent tester si l'identifiant transporté dans les données du
  signal leur correspond avant d'effectuer un traitement

* Le compositeur doit gérer les agents comme un sous-niveau de l'isolat avec :

  * un *bean* agent : contenant l'identifiant de l'agent et le nom de son
    isolat parent,
  * une map isolat -> beans agents : indiquant les agents correspondant à un
    isolat (pour gérer facilement la perte d'un isolat),
  * une map agent -> capacité : indiquant les types de composants que peut gérer
    un agent.


Implémentations d'agents
************************

Actuellement, seul un agent reposant sur iPOJO a été implémenté.

Plusieurs nouveaux agents devraient être développés pour les standards ou
langages suivants.


Gestion de nouveaux langages
============================

* Javascript : un agent basé sur `H-Ubu <http://akquinet.github.com/hubu/>`_
  pourrait intéresser certaines entreprises.

* Python : dans le cadre du développement d'un isolat hybride ou intégralement
  développé en Python, il faudra être capable de décrire un composant en
  Python et de le gérer de manière transparente avec le compositeur.


Gestion de standards / technologies OSGi
========================================

* OSGi Declarative Services (SCR) : concurrent d'iPOJO, mais ne se basant pas
  sur la manipulation, il faut étudier son fonctionnement comment décrire
  dynamiquement des instances composants

* OSGi Blueprint : standard OSGi basé sur SpringDM, il s'agira d'une évolution
  ou de la base de développement de l'agent SpringDM

* SpringDM : notamment utilisé avec Virgo, il faut étudier son fonctionnement
  et comment décrire dynamiquement des instances composants


.. important:: Il sera certainement nécessaire de modifier les informations
   contenues dans le modèle de composition pour que de nouveaux agents puissent
   être développés.
   