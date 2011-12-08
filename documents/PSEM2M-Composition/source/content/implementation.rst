.. Implémentation(s) possible(s)

Implémentation avec iPOJO
#########################

Architecture attendue
*********************

Pour être le plus efficace possible, l'implémentation du système de composition
devra être séparée en trois modules :

* le module **API**, contenant toutes les constantes, les interfaces et les
  *beans* nécessaires au fonctionnement du système de composition, y compris à
  travers les *Remote Services*,

* le module **Components Library**, contenant les implémentations de composants
  fournis avec PSEM2M.

* le module **Composer**, démarrant un composant iPOJO qui se charge de créer
  les compositions.


Méthode 1 : Création des composants à la volée
**********************************************

Cette technique est décrite sur le site d'iPOJO, à l'adresse
`<http://felix.apache.org/site/apache-felix-ipojo-api.html>`_.

L'idée est de *pojoizer* une classe à l'exécution, en décrivant un composant
programmatiquement.

L'avantage est de pouvoir profiter des mécanismes d'iPOJO sur une classe qui
n'est pas prévue comme tel à la base.

Le principal inconvénient est de devoir décrire le composant dans le fichier
de configuration de la composition, ce qui ne peut pas être fait sans un minimum
de connaissance sur la classe utilisée, notamment son nom et le nom de ses
champs.


Méthode 2 : Instanciation d'un composant
****************************************

Cette méthode se base sur la création d'instance de composants iPOJO à partir
d'un service ``Factory``, décrite dans *OSGi in Action* [#OSGi]_.
Chaque service ``Factory`` est associé à la définition d'un composant et permet
d'en récupérer les méta-données et d'en démarrer une instance.

Dans notre cas, nous devons modifier le comportement du *handler* **Requires**
afin d'indiquer expliquement quel composant fourni le service demandé.

Ceci peut se faire en indiquant l'une des propriétés suivantes lors de la
création d'un composant à partir d'une``Factory`` :

  * ``requires.filter``, fournie sous la forme d'un tableau de ``String``
    alternant identifiant de champ et filtre LDAP.

  * ``requires.from``, fournie sous la forme d'un dictionnaire ``Properties``,
    chaque clé (``String``) étant associée à un nom d'instance de composant
    iPOJO (``String``).


De cette manière, nous nous basons toujours sur les mécanismes d'iPOJO pour
gérer les liens entre composants et les dépendances à des services, tout en
s'assurant d'avoir une chaîne de composition qui correspond à la configuration.

.. only:: html

   .. rubric:: Références

.. [#OSGi] OSGi in Action, R. S. Hall, K. Pauls, S. McCulloch et D. Savage,
   ed. Manning 2011
