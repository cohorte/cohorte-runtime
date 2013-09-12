Démo "August 2013"
##################

Le noeud "Central"
******************

Liens symboliques
=================

Les liens à créer dans le dossier :

.. code-block:: bash
   ${workspace_loc:/platforms}/small-install/demo-august2013/central/

+----------------------+------------------------------------------------------+
| Lien                 | Cible                                                |
+======================+======================================================+
| base                 | ../../../base-demo-august2013                        |
+----------------------+------------------------------------------------------+
| cohorte              | ../../../../trunk/python/cohorte.python/cohorte      |
+----------------------+------------------------------------------------------+
| demo.july2012.python | ../../../../demos/demo-july2012/demo.july2012.python |
+----------------------+------------------------------------------------------+
| home                 | ../../../psem2m.home                                 |
+----------------------+------------------------------------------------------+

Soit les commands suivantes :

.. code-block:: bash

   ln -s ../../../base-demo-august2013 base
   ln -s ../../../../trunk/python/cohorte.python/cohorte cohorte
   ln -s ../../../../demos/demo-july2012/demo.july2012.python demo.july2012.python
   ln -s ../../../psem2m.home home


Pré-requis
==========

* Les bundles OSGi doivent être présents dans le dossier pointé par *base*
* jPype1-py3 et iPOPO, dernières versions (pour Python 3):

  .. code-block:: bash

     # !! Attention !! pip peut s'appeler pip-3.3/... selon la version installée
     sudo pip install --upgrade https://github.com/tcalmant/ipopo/archive/dev.zip
     sudo pip install --upgrade https://github.com/tcalmant/jpype-py3/archive/dev.zip


Démarrage
=========

.. code-block:: bash

   ./run.sh --color --start-monitor


Le noeud Raspberry
******************

Liens symboliques
=================

Les liens à créer dans le dossier :

.. code-block:: bash
   ${workspace_loc:/platforms}/small-install/demo-august2013/raspberry/
   
+----------------------+------------------------------------------------------+
| Lien                 | Cible                                                |
+======================+======================================================+
| cohorte              | ../../../../trunk/python/cohorte.python/cohorte      |
+----------------------+------------------------------------------------------+
| demo.july2012.python | ../../../../demos/demo-july2012/demo.july2012.python |
+----------------------+------------------------------------------------------+
| pelix                | <repos-git>/ipopo/pelix                              |
+----------------------+------------------------------------------------------+

Soit les commands suivantes :

.. code-block:: bash

   ln -s ../../../../trunk/python/cohorte.python/cohorte cohorte
   ln -s ../../../../demos/demo-july2012/demo.july2012.python demo.july2012.python

Si iPOPO a déjà été installé avec pip sur le raspberry, il n'est pas nécessaire
de créer un lien symbolique, mais il faut utiliser la commande :

.. code-block:: bash

   sudo pip install --upgrade https://github.com/tcalmant/ipopo/archive/dev.zip


Démarrage
=========

.. code-block:: bash

   ./run.sh --color


Distribution
************

.. code-block:: bash

   zip -r central central
   zip -r raspberry raspberry
