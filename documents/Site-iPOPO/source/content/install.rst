.. Installation

Installation
############

Pre-requisites
**************

The distribution contains iPOPO and Pelix packages, which are fully runnable
with any Python 2.7 to Python 3.2 compatible interpreter.
If the back-port of the `importlib <http://pypi.python.org/pypi/importlib>`_ is
installed, iPOPO can also be run on a Python 2.6 compatible interpreter.

iPOPO has been tested on :

* `CPython <http://python.org/download/>`_ 2.6, 2.7, 3.1 and 3.2
* `Pypy <http://pypy.org/>`_ 1.8

Feel free to report other interpreters which can run iPOPO.

Due to syntax changes, it can't be run on a Python 2.5 interpreter
(e.g. `Jython <http://www.jython.org/>`_).


Set up iPOPO
************

The installation process is based on Python setup tools.

#. Download iPOPO v0.2 `here <http://ipopo.coderxpress.net/dl/ipopo-0.2.zip>`_
#. Extract the content and go into *ipopo-dist* directory
#. Run the *setup.py* file :

   ``python setup.py install``

#. Test if the installation is correct :

   .. code-block:: python

      $ python
      >>> import psem2m.ipopo
      >>>

#. Start playing with iPOPO with the :ref:`samples`.

.. _unittests:

Unit tests
**********

Unit tests are in a different distribution file :
`unit tests <http://ipopo.coderxpress.net/dl/ipopo-0.2-tests.zip>`_.

To apply the tests, just run the following commands. Printed errors and warnings
are results of the validation of exceptions.


.. code-block:: bash
   
   $ export PYTHONPATH=.
   $ python tests/ldapfilter_test.py
   ..............
   ----------------------------------------------------------------------
   Ran 14 tests in 0.002s

   OK
   
   $ python tests/pelix_test.py
   ...ERROR:pelix.main:Error calling the activator
   Traceback (most recent call last):
     File "/psem2m/services/pelix.py", line 234, in start
       starter(self.__context)
     File "/tests/simple_bundle.py", line 35, in start
       raise Exception("Some exception")
   Exception: Some exception
   ERROR:pelix.main:Error calling the activator
   Traceback (most recent call last):
     File "/psem2m/services/pelix.py", line 266, in stop
       stopper(self.__context)
     File "/tests/simple_bundle.py", line 49, in stop
       raise Exception("Some exception")
   Exception: Some exception
   .............ERROR:pelix.main:Invalid service listener filter
   Traceback (most recent call last):
     File "/psem2m/services/pelix.py", line 484, in add_service_listener
       ldapfilter.get_ldap_filter(ldap_filter)
     File "/psem2m/ldapfilter.py", line 771, in get_ldap_filter
       return _parse_LDAP(ldap_filter)
     File "/psem2m/ldapfilter.py", line 745, in _parse_LDAP
       raise ValueError("Invalid filter string")
   ValueError: Invalid filter string
   =================================
   ----------------------------------------------------------------------
   Ran 23 tests in 0.009s
   
   OK

   $ python tests/ipopo_test.py
   ....DEBUG:ipopo.core:componentB: Missing requirement for field service
   ..DEBUG:ipopo.core:componentB: Missing requirement for field service
   .DEBUG:ipopo.core:componentB: Missing requirement for field service
   .
   ----------------------------------------------------------------------
   Ran 8 tests in 0.015s

   OK
