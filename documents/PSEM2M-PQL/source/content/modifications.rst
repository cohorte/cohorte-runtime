.. Gestion des modifications

Gestion des modifications
#########################


Origine des modifications
*************************

Les modifications peuvent avoir plusieurs origines :

- un rapport d'anomalie
- une demande d'évolution
- une proposition de modification par patch

Un rapport d'anomalie peut être émis par l'équipe de développement ou par un
utilisateur.
Il représente une non conformité du projet par rapport à ses spécifications ou
aux indications de son manuel développeur ou utilisateur.
Ces erreurs sont généralement décelées pendant la phase de qualification.

Une évolution est une requête formelle de la part de l'équipe de développement
ou d'un utilisateur visant à ajouter, modifier ou supprimer une fonctionnalité
ou un comportement.

Une modification par patch est une requête de modification ou d'ajout de
fonctionnalité à laquelle est associée tout ou partie du code source lui
correspondant.

Ces demandes et rapports doivent être enregistrées dans la section
correspondante de la forge.


Procédure et organisation
*************************

Les requêtes de modifications postées sur la forge doivent être étudiées par le
chef de projet.

Chaque requête peut être :

- Acceptée : l'équipe de développement doit alors appliquer les modifications
  en respectant les règles du PQL,
- Refusée : la requête est conservée sur la forge, mais son développement n'a
  pas lieu.

Dans le cas des modifications avec patch, ce dernier devra être modifié pour
répondre aux normes de codage et au PQL.
