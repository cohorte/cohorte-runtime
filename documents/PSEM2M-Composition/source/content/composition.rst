.. Définition d'une composition

Définition d'une composition
############################

Principe
********

L'idée est de rendre générique deux types de traitements :

* les traitements en lecture seule, avec une interface ``IGetData``.
* les traitements en lecture et écriture, avec une interface ``ISetData``.

La généricité est assurée en considérant que les données traitées sont des
arbres de dictionnaires (``Map``) ayant pour valeurs des chaînes de caractères
(``String``).


Définition
**********

Une composition, ou chaîne de traitement, est définie comme une succession de
composants exposant une interface de traitement (``IGetData`` ou ``ISetData``).

Le chaînage consiste à appeler le composant suivant et à retourner la valeur
résultat après traitement.

Le composant suivant est un membre ayant pour type la même interface de
traitement que le composant actuel, injecté par iPOJO à l'aide du *handler*
**Requires**.
Si plusieurs successeurs doivent être visibles, par exemple dans le cas d'un
composant échangeur (*switch*), chacun d'entre eux devra avoir identifiant
unique, indiqué dans la configuration du *handler* **Requires**.


Capacités attendues
*******************

* Le système de composition de la plateforme PSEM2M devra pouvoir gérer un
  ensemble de compositions définies dans un fichier de configuration ou par des
  objets Java spécifiques.
  La gestion des liaisons sera laissée à iPOJO, et son mécanisme de dépendances.

* Les compositions devront pouvoir être créées et supprimées à l'exécution, leur
  mise à jour n'est pas encore considérée.

* Une chaîne de traitement devra pouvoir passer par plusieurs isolats.
