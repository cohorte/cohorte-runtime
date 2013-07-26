.. jPype - Python

Partie Python
#############

Version utilisée
****************

La version de jPype qui a été convertie à Python 3 n'est pas la version
original de Steve Ménard [#jpype-sf]_, mais celle de Luis Nell [#jpype-nell]_.

Cette dernière apporte déjà un certain nombre de modifications dans le code
Python, notamment :

* l'amélioration de la recherche d'installation de Java sous Linux et MacOS X
* le remplacement du module déprécié ``sets`` par le type Python ``set``

.. [#jpype-sf] http://jpype.sourceforge.net/
.. [#jpype-nell] https://github.com/originell/jpype


Amélioration de setup.py
************************

La recherche de JDK pour Linux a été modifiée : plutôt que de chercher des
répertoires pré-définis, le script cherche un dossier ``include`` dans les
sous-dossiers de ``/usr/lib/jvm`` et ``/usr/java``, dossiers d'installation de
plus en plus communs pour les JRE et JDK des différentes distributions Linux.

.. code-block:: python

   def __find_jdk(self, parent):
        """
        Tries to find a JDK folder in the first-level children of the
        given folder
        
        :param parent: A parent folder
        :return: The first found JDK, or None
        """
        for folder in os.listdir(parent):
            # Construct the full path
            java_home = os.path.join(parent, folder)

            # Lower-case content tests
            folder = folder.lower()

            # Consider it's a JDK if it has an 'include' folder
            # and if the folder name contains 'jdk' or 'java'
            if os.path.isdir(java_home) \
            and ('jdk' in folder or 'java' in folder):
                include_path = os.path.join(java_home, 'include')
                if os.path.exists(include_path):
                    # Match
                    return java_home

        return None
     
   def setupLinux(self):
      # ...
      # Known places where we might find a JDK
      possible_install_dirs = ('/usr/lib/jvm', '/usr/java')

      for install in possible_install_dirs:
         if os.path.isdir(install):
            home = self.__find_jdk(install)
            if home:
               # Match
               self.javaHome = home
               print('Using JDK at {0}'.format(home))
               break

            else:
                # No JDK found: Abandon...
                sys.exit(1)
      # ...


Utilisation de 2to3
*******************

La conversion des modules de Python 2 vers Python 3 a été faite majoritairement
à la main, en se basant sur les modifications proposées par l'utilitaire
``2to3``.

Les modifications ont principalement touché :

* Normalisation des indentations : utilisation d'espaces uniquement
* Remplacement du mot clé ``print`` par la méthode du même nom.
* Renommage de modules (``_winreg`` devient ``winreg``, ...)
* Utilisation des imports relatifs Python 3, plus explicites :

  * ``import common`` devient ``from . import common``
  * ``from common import *`` devient ``from .common import *``

  L'utilitaire ``2to3`` ne repère pas tous les cas possibles, notamment si
  un import relatif est mélangé avec un import classique sur une seule ligne :
  ``import sys, common``.
* Remplacement des tests ``operator.isSequenceType(var)`` par
  ``isinstance(var, collection.Sequence)``
* Remplacement du module ``new``, supprimé dans Python 3.
  La création d'un nouveau type se fait en appelant directement ``type(...)``.
* Changement de signature des itérateurs, passant de ``next()`` à ``__next__()``
* Suppression des tests différenciant ``str`` et ``unicode``
* Suppression des utilisations explicite d'Unicode (``u'b'``, ``unicode()``)
* Suppression des références au type ``long``, renommé ``int``
