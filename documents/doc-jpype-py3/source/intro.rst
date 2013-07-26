.. Introduction

Introduction
############

Travaux
*******

Ce document décrit les étapes effectuées afin de pouvoir utiliser jPype avec
un interpréteur Python 3.

Ces travaux ont été effectués par isandlaTech [#isandlatech]_,
dans le cadre de l'évolution de sa plate-forme Cohorte.
Ils ont été motivés par la possibilité d'un déploiement de Cohorte dans le
projet Predis [#predis]_ de l'équipe G2ELab [#g2elab]_, sous le giron de
Grenoble INP [#inpg]_ et de l'Université Joseph Fourier Grenoble I [#ujf]_.

Le développement a été effectué sous Linux (Ubuntu 12.04), pour l'interpréteur
CPython 3.2.

.. [#isandlatech] http://isandlatech.com/
.. [#predis] http://www.g2elab.grenoble-inp.fr/plateformes/plateforme-predis-196107.kjsp
.. [#g2elab] http://www.g2elab.grenoble-inp.fr/index.jsp
.. [#inpg] http://www.grenoble-inp.fr/
.. [#ujf] http://www.ujf-grenoble.fr/


jPype
*****

jPype est une bibliothèque permettant d'exécuter une machine virtuelle Java
(JVM) dans un interpréteur Python.

Cette bibliothèque est composée :

* d'un ensemble de modules écrits en Python, correspondant à l'API publique
  de jPype,
* d'un module écrit en C, interfaçant le monde Python et le monde Java.

Le module C utilise l'API C de Python [#c-api]_ pour passer du monde Python au
monde C, et l'API Java Native Interface (JNI) [#JNI]_ pour passer du monde C au
monde Java.

.. [#c-api] http://docs.python.org/3/c-api/
.. [#JNI] http://docs.oracle.com/javase/7/docs/technotes/guides/jni/
