.. Plateforme OSGi cible

Plateforme OSGi cible
#####################

Le projet a pour but d'être totalement conforme OSGi, c'est-à-dire d'être
capable de fonctionner sur n'importe quelle plateforme répondant aux
spécifications OSGi 4.2.

Pour vérifier cette validité, nous créé deux "Target Platforms" Eclipse, l'une
reposant sur Equinox, l'autre reposant sur Felix.

Pour que Felix soit utilisable directement sous Eclipse, nous utilisons le
plug-in "felix-eclipse" disponible ici :
`Felix-Eclipse <https://code.google.com/p/felix-eclipse/>`_.

Ce plug-in permet d'utiliser Felix de manière transparente et d'utiliser le
débogage point à point inclus dans Eclipse.

Cette création de deux *target plateforms* à révélé quelques points auxquels
nous devrons faire attention :

* Le nommage des bundles est différent d'un framework à l'autre.
  Ainsi Declarative Services est appelé Felix SCR sous Felix et Equinox DS sous
  Equinox.

  Dans le cas de l'implémentation de standards (OSGi ou autre), il est
  préférable d'utiliser la clause Import-Package du Manifest plutôt que
  Require-Bundle.

* Certaines fonctionnalités ne sont pas compatibles d'un framework à l'autre
  (notamment ECF, lié à Equinox).
