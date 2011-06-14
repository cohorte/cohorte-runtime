.. Processus qualité

Processus qualité
#################

Mécanismes de suivi et de contrôle
**********************************

Lors des deux premières phases, le contrôle est effectué par le chef de projet
se devant de valider la documentation produite par l'équipe.

Lors de la phase de conception, la pertinence de l'architecture proposée devra
être critiquée par l'équipe ADELE du LIG.

Lors des phases de développement, l'équipe principale devra faire une réunion
informel pour se mettre au courant des avancées de chacun de ses membres. Une
réunion plus formelle devra être effectuée toute les deux semaines afin de
décider sur quoi se focaliser lors du prochain *sprint SCRUM*.


Gestion de la documentation
***************************

Le chef de projet s'assure d'avoir un exemplaire de tous les documents produits.
Il assure aussi la diffusion de toutes les informations au sein du groupe lors
des revues.

Les documents doivent être fournis dans les formats suivants :

* source reStructuredText (.rst)

* Format HTML (site HTML statique généré par Sphinx)

* Format Adobe PDF (.pdf, généré par Sphinx)


Le nom des documents doit être de la forme :

   PSEME-NomDuDocument.format

Le responsable du document indique le passage d'une version à une autre.


Gestion de configuration
************************

Afin de garantir un environnement de travail cohérent, il a été décidé
d'utiliser l'outil de gestion de version Git. Ainsi l'équipe dispose d'une
version décentralisée du code, ainsi que d'un ou plusieurs dépôts principaux
accessibles depuis Internet.

Pour le projet PSEM2M, le dépôt central principal sera le serveur local
**chamechaude**.

Ce dépôt central principal hébergera également la forge du site, basée sur
Redmine et Hudson.

L'équipe utilise également l'outil Eclipse. Cet outil dispose d'un module
d'interfaçage avec Git sous forme d'un module d'extension.

Chaque membre de l'équipe peut donc utiliser cet outil pour récupérer les
dernières versions des sources et partager son travail.


* Les règles de mise à jour dans le dépôt Git central sont les suivantes :

   * le code de toute branche ne doit pas poser de problèmes de compilation,

   * le code de la branche **master** doit avoir passé l'ensemble des tests
     disponibles,

   * la documentation en cours de réalisation doit appartenir à la branche
     **develop**; celle validée doit être dans la branche **master**.

* Les règles de mise à disposition dans le dépôt Git local d'un développeur sont les suivantes :

   * le code de la branche **master** ne doit pas poser de problèmes de
     compilation.
