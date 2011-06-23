.. Ressources disponibles pour les tests

Ressources
##########

Ressources humaines
*******************

L'équipe de test correspond à l'équipe principale du projet dans son
intégralité :

* Thomas Calmant
* Olivier Gattaz

Cette équipe pourrait être accompagnée de beta-testeurs, employés d'entreprises
pouvant être intéressées par le projet.


Ressources matérielles
**********************

Les tests pour les environnements Linux et Mac OS seront effectués sur des
machines physiques.

Les tests pour les environnements Windows seront effectués sur des machines
virtuelles, exécutées par Oracle VirtualBox.

Ressources logicielles
**********************

.. _outils-test:

Outils de tests
***************

Les outils utilisés pour effectuer les tests seront :

**junit4osgi**

   Un bundle OSGi permettant d'exécuter des tests JUnit 3 dans un framework
   OSGi, notamment Felix.

**Eclipse JUnit PDE Test**

   Une *Run configuration* d'Eclipse permettant d'exécuter des tests JUnit 3
   et 4 dans Equinox et d'en visualiser les résultats.

**Oracle VisualVM**

   Un outil fournit avec le JDK ou sur le site d'Oracle permettant de visualiser
   l'état des machines virtuelles Java en cours d'exécution.
   Un module d'extension permet d'accéder aux *MBeans* (JMX) et à utiliser
   directement leurs opérations.

**htop**

   Pour visualiser l'état et la consommation des processus isolés sous Linux.

**Outils spécialisés**

   Des outils de tests devront être développés afin de simuler des processus
   externes (WebService distant, etc).
   Ces outils seront réalisés en Python et/ou en Java.


Outils de rapport
*****************

Les rapports de tests seront écrits sous la forme d'un document ReST mis à jour
à chaque session de validation.

Les tests ayant échoués ou ayant indiquer la présence d'une anomalie devront
être suivis d'un rapport de bug sur la forge du projet [#forge]_.

.. only:: not(latex)

   .. rubric:: Références
   
.. [#forge] `Forge isandlaTech <http://forge.isandlatech.com:3080/redmine>`_
