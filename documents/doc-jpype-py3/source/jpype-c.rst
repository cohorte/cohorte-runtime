.. jPype - C++

Partie C++
##########

Le portage d'un module Python écrit en C de Python 2 vers Python 3 a été
effectué à l'aide des documents suivants :

* `Porting Extension Modules to Python 3 <http://docs.python.org/3/howto/cporting.html>`_
  (documentation officielle)
* `Migrating C extensions <http://python3porting.com/cextensions.html>`_
  (Porting to Python 3 - The Book Site)


Initialisation du module
************************

En Python 3, un module C s'enregistre lorsque l'interpréteur appelle la
méthode ``PyInit_<module>``.
Celle-ci retourne le ``PyObject`` représentant le module créé avec
``PyModule_Create``.

La méthode ``init_jpype()`` du fichier ``jpype_python.cpp`` a donc été
remplacée par :

.. code-block:: c++

   PyMODINIT_FUNC PyInit__jpype(void)
   {
        Py_Initialize();
        PyEval_InitThreads();

        static struct PyModuleDef moduledef = {
               PyModuleDef_HEAD_INIT,

               "_jpype",                          /* m_name */
               "jPype: Java <-> Python wrapper",  /* m_doc */
               -1,                                /* m_size */
               jpype_methods,                     /* m_methods */
               /* Default values ... */
        };
       
        PyObject* module = PyModule_Create(&moduledef);
        Py_INCREF(module);
        hostEnv = new PythonHostEnvironment();
       
        JPEnv::init(hostEnv);

        PyJPMonitor::initType(module);     
        PyJPMethod::initType(module); 
        PyJPBoundMethod::initType(module); 
        PyJPClass::initType(module);  
        PyJPField::initType(module);

        return module;
   }


Disparition de PyInt
********************

En Python 3, les types ``int`` et ``long`` ont été fusionnés : seule le type
``long`` subsiste.
En conséquence, toutes les méthodes préfixées par ``PyInt_`` ont été remplacées
par leur équivalent ``PyLong_``.

Afin de garder une certaine cohérence dans les types entiers *de base* de chaque
langage, il a été décidé que les entiers Python seront vus comme des entiers
``java.lang.Integer`` en Java.

Les modifications apportées touchent principalement le fichier
``pythonenv.cpp``.


Disparition de PyString
***********************

Alors qu'il était possible de mélanger ``str`` et ``unicode`` en Python 2, les
types ``bytes`` et ``str`` de Python 3 sont incompatibles.

JPype utilisant des méthodes utilitaires de la classe ``JPyString`` pour passer
des chaînes de caractères Python à leur représentation en C, très peu de code
a été impacté.
Les méthodes préfixées par ``PyString_`` ont été remplacées par leur
équivalent préfixé par ``PyBytes_``.

Étant donné la nature de Python 3, les chaînes de caractères construites depuis
C vers Python sont forcément de type ``str``, c'est-à-dire ``PyUnicode``.

Les méthodes ``JPyString::asString`` et ``JPyString::fromString`` ont été
modifiées afin de prendre en compte la conversion entre les types ``PyUnicode``
et ``PyBytes`` :

* ``PyUnicode`` vers ``PyBytes`` :

  .. code-block:: c++

     PyObject* bytes = PyUnicode_AsEncodedString(obj, "UTF-8", "strict");

* ``PyBytes`` vers ``PyUnicode`` :

  .. code-block:: c++

     PyObject* unicode = PyUnicode_FromEncodedObject(bytes, "UTF-8", "strict")


Ces modifications touchent principalement le fichier ``pythonenv.cpp``.


Utilisation de PyCapsule
************************

jPype utilise l'API PyCObject pour stocker certaines structures.
Cette API a été remplacée par PyCapsule à partir de Python 3.1.

Afin de simplifier l'écriture de modules pour Python 2.7 et 3.0, la
documentation Python fournit un fichier
`capsulethunk.h <http://hg.python.org/cpython/file/3.3/Doc/includes/capsulethunk.h>`_
simulant l'API PyCapsule en se basant sur PyCObject.

Tous les fichiers utilisant l'API PyCObject ont été modifiés afin d'utiliser
PyCapsule.

Les différences notables entre les APIs sont :

* PyCapsule associe obligatoirement un nom (``char*``) à la capsule, alors qu'il
  était possible d'associer une description (``void*``) à un PyCObject.

  Étant donné que jPype n'associait que des chaînes de caractères aux pointeurs
  stockés via PyCObject, la conversion a été assez simple.

* PyCapsule demande le nom associé au pointeur pour récupérer le pointeur
  lui-même.
  Une macro ``CAPSULE_EXTRACT`` a été définie pour simplifier la récupération du
  pointeur, dans le fichier ``jpype_python.h`` :

  .. code-block:: c++

     // Utility module to hide PyCObject_type API change
     #include "capsulethunk.h"
     #define CAPSULE_EXTRACT(obj) PyCapsule_GetPointer(obj, PyCapsule_GetName(obj))

* Les *destructeurs* associés au PyCapsule prennent en paramètre le ``PyObject``
  représentant la capsule. Il faut en extraire le pointeur, avec
  ``CAPSULE_EXTRACT``.
  Auparavant, le pointeur était passé directement en paramètre du destructeur.


Améliorations
*************

Exceptions
==========

La classe ``PythonException``, permettant de remonter une exception ayant eu
lieu dans le monde Python vers le monde Java, génère désormais un message
d'erreur contenant :

* le type de l'erreur, e.g. *TypeError*
* le texte associé à l'erreur, c'est-à-dire l'équivalent du code Python
  ``str(ex)``.

De cette manière, il est beaucoup plus facile de déterminer la raison de
l'exception.

* Fichier ``pythonenv.h``, ligne 30

  .. code-block:: c++


     /**
      * Exception wrapper for python-generated exceptions
      */
     class PythonException : public HostException
     {
     public :
          PythonException();
          PythonException(PythonException& ex);

          virtual ~PythonException();

          virtual string getMessage();

          bool isJavaException();
          PyObject* getJavaException();

     public :
          PyObject* m_ExceptionClass;
          PyObject* m_ExceptionValue;
     };


* Fichier ``pythonenv.cpp`` (ajout)

  .. code-block:: c++

     string PythonException::getMessage()
     {
          string message = "";

          // Exception class name
          PyObject* className = JPyObject::getAttrString(m_ExceptionClass, "__name__");
          message += JPyString::asString(className);
          Py_DECREF(className);

          // Exception value
          if(m_ExceptionValue)
          {
               // Convert the exception value to string
               PyObject* pyStrValue = PyObject_Str(m_ExceptionValue);
               if(pyStrValue)
               {
                    message += ": " + JPyString::asString(pyStrValue);
                    Py_DECREF(pyStrValue);
               }
          }

          return message;
     }

* Fichier ``jp_proxy.cpp``, ligne 123

  .. code-block:: c++

     // ...
     catch(HostException* ex)
     {
          JPEnv::getHost()->clearError();
          if (JPEnv::getHost()->isJavaException(ex))
          {
               JPCleaner cleaner;
               HostRef* javaExcRef = JPEnv::getHost()->getJavaException(ex);
               JPObject* javaExc = JPEnv::getHost()->asObject(javaExcRef);
               cleaner.add(javaExcRef);
               jobject obj = javaExc->getObject();
               cleaner.addLocal(obj);
               JPEnv::getJava()->Throw((jthrowable)obj);
          }
          else
          {
               // Prepare a message
               string message = "Python exception thrown: ";
               message += ex->getMessage();

               JPEnv::getJava()->ThrowNew(JPJni::s_RuntimeExceptionClass, message.c_str());
          }
     }
     // ...
