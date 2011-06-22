.. Description des fichiers de configuration

Fichiers de configuration
#########################

Il existera au moins un fichier de configurations, contenant la configuration de
la plateforme et la définition des isolats.

Ce fichier sera au format XML, permettant ainsi d'être édité aussi bien
manuellement qu'à l'aide d'un éditeur spécifique.

Le principal intérêt de ce format est de pouvoir valider le fichier à l'aide
d'un schéma.
Ceci permettra notamment à un administrateur de tester son fichier de
configuration sans avoir à l'appliquer à une instance de la plateforme.

.. todo:: Insérer schéma du fichier

Ce fichier sera lu par le service de configuration du moniteur uniquement. Les
autres services de configuration y accèderont par service distant

Configuration de la plateforme
******************************

Dans ce fichier, on doit être capable de définir les comportements du moniteur,
à savoir :

* le délai de détection des isolats crashés ou stoppés
* le nombre de tentatives de redémarrage d'un isolat
* les modes de sondage à utiliser (JMX, ping, ...) pour détecter l'état d'un
  isolat et leurs paramètres propres
* port d'écoute de la socket d'administration.

Configuration des isolats
*************************

Théoriquement, tout processus peut être isolé; cependant, nous ne garantissons
que l'isolement de machines virtuelles Java et plus particulièrement les
frameworks OSGi.
De fait, la configuration ne permet de définir que des isolats Java et des
isolats des frameworks officiellement supportés.


Configuration par défaut
========================

Dans ce nœud, l'administrateur doit indiquer les valeurs de configuration à
utiliser si elles ne sont pas précisées dans la section correspondant à un
isolat.

L'administrateur doit pouvoir y définir optionnellement :

* les droits d'exécution de l'isolat (utilisateur, groupe, ...)
* les variables d'environnement (PATH, HOME, ...)
* les arguments à passer à l'isolat

Si un de ces paramètre est redéfini pour un isolat, sa valeur par défaut est
ignorée.


Configuration d'un isolat
=========================

Pour chaque isolat, il faut définir le processus à exécuter.

L'administrateur devra ici indiquer obligatoirement :

* l'identifiant de l'isolat (unique)
* le type d'isolat :

  * *java* : une application Java simple
  * *felix* : une instance du framework OSGi Felix
  * *equinox* : une instance du framework OSGi Equinox
  * d'autres types d'isolats pourront apparaître.

Si le framework OSGi indiqué n'est pas disponible, le lancement échoue. Il n'y
a pas de tentative d'utilisation d'un autre framework.

L'administrateur pourra également indiquer :

* les droits d'exécution de l'isolat
* les variables d'environnement (PATH, HOME, ...)
* les arguments à passer à l'isolat
