.. Spécification des sondes
.. highlight:: java

Spécification des sondes
########################

Les sondes correspondent aux interfaces des isolats utilisés par le moniteur et
le forker pour tester l'état d'un isolat.


Sondes JMX
**********

Ces sondes peuvent être déployées sous la forme de bundle dans un framework
OSGi, ou en tant que modules additionnels pour les applications les gérant.

Les sondes utilisées pour tester l'état des frameworks OSGi seront toutes de
type JMX, inscrites par iPOJO.

Ces sondes utiliseront le *MBeanManager* et le protocole de transmission par
défaut de la plateforme utilisée.


Sonde de contrôle d'état
========================

Cette sonde doit être présente dans tout isolat utilisant JMX.
Il s'agit d'un simple *ping*, permettant de connaître l'état de l'isolat, s'il
peut répondre ou non.

Dans le cas des plateformes OSGi, cette sonde renvoie l'état visible de tous les
bundles installés :

* ``Map<String, Integer> getBundlesState()``

     Renvoie une association des identifiants des bundles installés et de leur
     état.

* ``String ping(String)``

     Renvoie la chaîne reçue en paramètre. Ceci permet de tester la qualité
     de la liaison.


Sondes spécifiques
******************

Ces sondes peuvent être des bibliothèques se calant entre l'isolat le système
d'exploitation.

Elles pourraient ainsi créé un thread à part, écoutant une socket
d'administration, prêtent à répondre aux requêtes du moniteur même si le
processus isolé n'a pas de point d'entrée disponible.

Ces sondes ne seront pas développées dans le cadre du projet PSEM2M, mais en
tant qu'outils additionnels, sur demande.
