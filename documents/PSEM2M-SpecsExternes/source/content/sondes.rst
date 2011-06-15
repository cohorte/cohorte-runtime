.. Spécification des sondes

Spécification des sondes
########################


Sondes JMX
**********

Ces sondes peuvent être déployées sous la forme de bundle dans un framework
OSGi, ou en tant que modules additionnels pour les applications les gérant.

Les sondes utilisées pour tester l'état des frameworks OSGi seront toutes de
type JMX, inscrites par iPOJO.

Dans un premier temps, elles utiliseront le *MBeanManager* et le protocole de
transmission par défaut de la plateforme utilisée.


Sondes spécifiques
******************

Ces sondes peuvent être des bibliothèques se calant entre l'isolat le système
d'exploitation.

Elles pourraient ainsi créé un thread à part, écoutant une socket
d'administration, prêtent à répondre aux requêtes du moniteur même si le
processus isolé n'a pas de point d'entrée disponible.

Ces sondes ne seront pas développées dans le cadre du projet PSEM2M, mais en
tant qu'outils additionnels, sur demande.
