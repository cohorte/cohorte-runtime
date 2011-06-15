.. Interfaces du moniteur

Interfaces du moniteur
######################

Paramètres d'exécution
**********************

Le moniteur doit accepter les paramètres suivants :

.. tabularcolumns:: |p{3cm}|p{3cm}|p{9cm}|

+--------------+-------------------+-----------------------------------------+
| Paramètre    | Valeur par défaut | Description                             |
+==============+===================+=========================================+
| -c,          | ./monitor.conf    | Fichier de configuration à utiliser     |
| --config     |                   |                                         |
+--------------+-------------------+-----------------------------------------+
| -f,          | Faux              | Si ce paramètre est indiqué, le         |
| --foreground |                   | moniteur est lancé en mode premier-plan |
|              |                   | et non en mode démon / service          |
+--------------+-------------------+-----------------------------------------+


Re-configuration en cours d'exécution
*************************************

Étant donné la criticité du moniteur, on ne peut pas se permettre de forcer
l'arrêt et le redémarrage de celui-ci pour mettre à jour sa configuration.

Pour cette raison, le moniteur est capable de relire sa configuration et
exécuter les traitements nécessaires à son application (arrêt d'isolats,
création d'images, ...) sur ordre de l'administrateur.

Cet ordre peut être émis à l'aide d'un paquet envoyé sur la socket
d'administration, contenant le message : **RELOAD_CONFIGURATION**.

Ce message pourra être accompagné du nom du fichier de configuration à utiliser.
Si celui-ci n'est pas présent, le fichier utilisé au démarrage du moniteur est
relu.

Le protocole utilisé sur cette socket est décrit dans la section
:ref:`protocole-moniteur`.
