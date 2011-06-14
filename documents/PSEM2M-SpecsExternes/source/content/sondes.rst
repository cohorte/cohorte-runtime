.. Spécification des sondes

Spécification des sondes
========================

.. todo:: API de configuration / interrogation. Protocole réseau / interface JMX

Sondes JMX
----------

Ces sondes peuvent être déployées sous la forme de bundle dans un framework
OSGi, ou en tant que modules additionnels pour les applications les gérant.  

Sondes spécifiques
------------------

Ces sondes peuvent être des bibliothèques se calant entre l'isolat le système
d'exploitation.

Elles pourraient ainsi créé un thread à part, écoutant une socket
d'administration, prêt à répondre aux requêtes du moniteur même si le processus
isolé n'a pas de point d'entrée disponible.
