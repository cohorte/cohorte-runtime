.. Implémentation iPOJO

Agent basé sur iPOJO
####################

Instanciation de composants avec iPOJO
**************************************

Le projet iPOJO est un sous-projet de Felix permettant de transformer des
classes Java classiques (POJO, Plain Old Java Object) en un composant iPOJO
utilisable dans une plateforme OSGi.

La particularité d'iPOJO est de modifier la représentation binaire de la classe
afin d'y injecter des méthodes de contrôles de cycle de vie et d'assignations;
Ces méthodes seront appelées par le service iPOJO présent dans la plateforme
OSGi d'exécution.


Méthode 1 : Création des composants à la volée
==============================================

Cette technique est décrite sur le site d'iPOJO, à l'adresse
`<http://felix.apache.org/site/apache-felix-ipojo-api.html>`_.

L'idée est de manipuler une classe à l'exécution, en décrivant le composant
programmatiquement.

L'avantage est de pouvoir profiter des mécanismes d'iPOJO sur une classe qui
n'est pas prévue pour.

Le principal inconvénient est de devoir décrire le composant dans le fichier
de configuration de la composition, ce qui ne peut pas être fait sans un minimum
de connaissance sur la classe utilisée, notamment son nom et le nom de ses
champs.
De plus, un certain nombre de traitements peuvent être effectués par le
constructeur de la classe, et donc en dehors du cycle de vie géré par iPOJO.


Méthode 2 : Instanciation d'un composant
========================================

Cette méthode se base sur la création d'instance de composants iPOJO à partir
d'un service ``Factory``, décrite dans *OSGi in Action* [#OSGi]_.
Chaque service ``Factory`` est associé à la définition d'un composant et permet
d'en récupérer les méta-données et d'en démarrer une instance.

Dans notre cas, nous devons modifier le comportement du *handler* **Requires**
afin d'indiquer expliquement quel composant fourni le service demandé.

Ceci peut se faire en indiquant l'une des propriétés suivantes lors de la
création d'un composant à partir d'une ``Factory`` :

  * ``requires.filter``, fournie sous la forme d'un tableau de ``String``
    alternant identifiant de champ et filtre LDAP.

  * ``requires.from``, fournie sous la forme d'un dictionnaire ``Properties``,
    chaque clé (``String``) étant associée à un nom d'instance de composant
    iPOJO (``String``). Il est également possible de fournir un tableau de
    ``String``, les indices pairs étant les clés et les impairs les valeurs.


De cette manière, nous nous basons toujours sur les mécanismes d'iPOJO pour
gérer les liens entre composants et les dépendances à des services, tout en
s'assurant d'avoir une chaîne de composition qui correspond à la configuration.


Implémentation choisie
**********************

L'implémentation choisie se base sur la méthode 2.
En effet, cette technique permet de ne traiter que les composants basés sur
iPOJO, mais assure justement que le composant instancié aura le comportement
attendu d'un composant iPOJO, sans effets de bord.

Un patch, appliqué, a été transmis au projet iPOJO afin que le *handler*
**Temporal** réagisse comme le *handler* **Requires**.

.. note::

   Selon Clément Escoffier, créateur du projet iPOJO, les *handlers* 
   **Requires** et **Temporal** devraient fusionner dans une future version
   d'iPOJO (2.x)


.. only:: html

   .. rubric:: Références

.. [#OSGi] OSGi in Action, R. S. Hall, K. Pauls, S. McCulloch et D. Savage,
   ed. Manning 2011
