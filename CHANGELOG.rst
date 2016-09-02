Changes form the 1.0.3 to 1.1.0
-------------------------------

** New Features
    * Local Discovery 

** Improvements
    * Adding Forker's HTTP Port to OSGi java framework properties
    * Using Cohorte-Utilities 1.0.6

** Bug Fix
    * Java isolates are not started if no vm_args configuration was given [isandlatech/cohorte-platforms#79]
    * Normalize paths coming from PYTHONPATH #34

Changes form the 1.0.2 to 1.0.3
-------------------------------

** Bug Fix
    * OSGi framework user-provided extra packages are not considered [isandlaTech/cohorte-runtime#36]
    * Do not start Fragment bundles in OSGi [isandlaTech/cohorte-runtime#35]

Changes form the 1.0.1 to 1.0.2
-------------------------------

** Bug Fix
    * Cohorte cannot find required artifacts and stops starting the isolate [isandlaTech/cohorte-platforms#59]
    * Corrected top composer status [isandlaTech/cohorte-platforms#53]

Changes form the 1.0.0 to 1.0.1
-------------------------------

** Improvements
    * add "iPOJO Isolates" to cohorte-debug servlet
    * Using Cohorte-Utilities 1.0.3
    
** Bug Fix
    * AttributeError exception when stoping a node [isandlaTech/cohorte-platforms#35] (1)
    * Protecting cohorte agent at invalidation from exception caused by Herald which could have no transport bound.
