.. Configuration de la forge

Configuration de la forge
#########################


Présentation de la forge
************************

La forge est gérée par Redmine, couplé à l'outil d'intégration continue Hudson,
fonctionnant sur deux serveurs (logiciels) distinct.
Le gestionnaire de version utilisé est Subversion.

Un proxy Apache a été mis en place, afin que les outils soient accessible via un
port unique.

Des préfixes ont été ajoutés dans les URLs utilisées par les outils pour que le
proxy puisse fonctionner correctement.

- Jenkins : la configuration a été faite selon les indications de :
  `<http://www.zzorn.net/2009/11/setting-up-hudson-on-port-80-on-debian.html>`_

- Redmine a été configuré pour interagir avec Hudson via un plug-ins

Redmine a été installé manuellement dans le dossier ``/opt/redmine``.
Il utilise une base de données mysql.

Jenkins a été installé par paquet, en tant que remplacement de l'installation
précédente d'Hudson.
Ses données sont stockées dans le dossier ``/var/lib/jenkins``.

Le dépôt Subversion se trouve dans ``/var/svn``, le dépôt Git dans ``/var/git``.

Jenkins et Git
**************

Le plug-in Git pour Jenkins s'installe simplement depuis le catalogue
d'extensions de Jenkins.
