.. Outils SOA

Outils SOA
##########

SOA et OSGi
***********

Petite description des interactions possibles entre les frameworks SCA et OSGi :

* OSGI to SCA

  * Les services SCA sont exportés dans un registre OSGi (OSGi invoque SCA)

* SCA to OSGi

  * Les références SCA sont importées depuis un registre OSGi (SCA invoque OSGi)

* OSGI in SCA

  * Les composants SCA sont implémentés par des bundles OSGi

* OSGi for SCA

  * Applications SCA avec une dynamique OSGi (ajout/suppression de bundles OSGi)

* SCA for OSGi

  * Plusieurs langages d'implémentation supportés (composants SCA)
  * Plusieurs protocoles réseaux supportés (services SCA)

Plus d'informations sur ce diaporama (pages 8 à 16) :
`FraSCAti and OSGi <https://wiki.ow2.org/frascati/attach?page=Documents%2F2011-OUGF-FraSCAti-with-OSGi.pdf>`_.

Apache Tuscany
**************

Tuscany est un projet Apache découpé en trois catégories de sous-projets :

* Service Component Architecture (SCA)

  * Modélisation des interactions entre services
  * Versions Java et C++
  * Standard OASIS

* Service Data Object (SDO)

  * Modélisation des accès aux données
  * Accès uniforme aux données, quelque soit leur source
  * Versions Java, C++ et PHP
  * Standard OASIS

* Data Access Service

  * Simplification de la conversion entre les types de l'application et les
    types SDO.
  * Versions Java et C++

Il est à noter que Tuscany est utilisé par IBM en tant qu'extension possible
de WebSphere.

Seule les versions Java de ces projets sont prises en compte.


État actuel
===========

Tuscany peut fonctionner en mode *standalone* ou être embarquée dans un hôte.

Tuscany 1.x, actuellement la branche stable, peut être embarquée dans :

* Tomcat
* Geronimo (sans doc)
* WebSphere
* WebLogic
* Eclipse

La branche 2.x est en cours de développement et apporte le support d'une
exécution sous la forme d'un bundle OSGi, fonctionnant avec Felix et Equinox.
La documentation de la branche 2.x est encore faible, car en cours de
développement.

Interfaces supportées
---------------------

* Java
* WSDL


Implémentations supportées
--------------------------

Tuscany 1.x supporte les implémentations de composants suivantes :

* Java
* OSGi
* Spring
* J2EE Web Apps
* Scripts (JSR-223 : Groovy, JavaScript, JRuby, Jython, ...)
* BPEL


Protocoles supportés
--------------------

Les protocoles d'accès supportés par Tuscany sont :

* Ajax (Côté serveur)
* CORBA
* Erlang
* JMS
* JsonRPC (équivalent à Ajax)
* RMI
* SOAP
* HTTP (REST ?)
* EJB
* Atom
* RSS


Outils associés
===============

* Le plug-in Eclipse SCA Tools Platform a une extension permettant de gérer
  Tuscany (1.x et 2.x).


Références
==========

* `Apache Tuscany <http://tuscany.apache.org/>`_
* `Tuscany 2 dans Equinox/Felix <http://tuscany.apache.org/documentation-2x/running-tuscany-sca-2x-with-equinox-and-felix.html>`_


OW2 FraSCAti
************

FraSCAti est un projet OW2, développé uniquement en Java.

État actuel
===========

En évolution vers la version 3.5.

Interfaces supportées
---------------------

* Java
* En-têtes C
* WSDL
* UPnP


Implémentations supportées
--------------------------

* Java
* OSGi
* Scripts JSR-223
* Fscript
* Scala
* Fractal
* Web resources


Protocoles supportés
--------------------

* RMI
* SOAP
* REST
* JSON-RPC
* JNA
* UPnP


Outils associés
===============

* Le plug-in Eclipse SCA Tools Platform a une extension permettant de gérer
  FraSCAti.

* FraSCAti Explorer : outil de gestion d'applications FraSCAti.

* FraSCAti FScript : langage de script pour la reconfiguration d'applications
  FraSCAti.

* Monitoring via JMX et REST


Références
==========

* `FraSCAti <http://wiki.ow2.org/frascati/Wiki.jsp?page=FraSCAti>`_
* `FraSCAti and OSGi <https://wiki.ow2.org/frascati/attach?page=Documents%2F2011-OUGF-FraSCAti-with-OSGi.pdf>`_

Comparatif
**********

Lors d'une conférence fOSSa, FraSCAti a présenté un comparatif de performances
face à Tuscany en 2009
(`Slideshare SOA/SCA FraScAti <http://www.slideshare.net/fossaworkshops/soasca-frascati>`_, page 21 à 24).

L'objectivité des tests reste à montrer, mais il semblerait que FraSCAti soit
plus efficace que Tuscany sur un grand nombre de composants.
