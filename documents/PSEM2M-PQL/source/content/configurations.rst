.. Gestion des configuration

Gestion des configuration
#########################

Configuration matérielle
************************

L'équipe de développement dispose de trois machines :

- 2 machines sous Linux (Ubuntu 10.04)
- 1 machine sous Mac OS

Afin de pouvoir effectuer le développement dans les meilleures conditions,
s'ajoutent à ce matérielle des machines virtuelles :

- 1 machine virtuelle sous Linux (Ubuntu 10.10)
- 1 machine virtuelle sous Windows XP.

Les machines virtuelles ne sont pas des machines de développement et permettent
de simuler les environnement cibles.


Configuration logicielle
************************


Documentation
=============

La documentation est réalisée à l'aide de Sphinx et du plug-in ReST Editor pour
Eclipse, sans contrainte quant à la machine utilisée.

OpenOffice peut être utilisé pour éditer les documents qui n'ont pas été
convertis au format reStructuredText.


Développement et tests
======================

Le développement principal est effectué sur une machine Linux (Ubuntu 10.04) et
sur une machine Mac.
Les tests unitaires ont lieu sur les machines de développement, tous les autres
tests sont réalisés sur les machines virtuelles.


Forge
=====

Chaque module du projet est défini comme un sous-projet sur la forge.
Selon l'importance du module, il peut avoir son propre dépôt de gestionnaire de
version ou utiliser celui de son projet père.

La forge utilise le gestionnaire de version Git et repose donc sur la notion
de branches.

Tout le code développé avant la premier diffusion est inscrit dans la branche
**master**.
Ensuite, le code stable diffusé est inscrit dans la **master**, le code
de corrections de bugs dans la branche **hotfixes** et le code de développement
dans la branche **develop**.

Le code source est enregistré dans le dossier */trunk/*, la documentation dans
le dossier */documents/*.
