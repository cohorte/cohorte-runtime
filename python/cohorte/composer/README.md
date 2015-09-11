# Cohorte Composer

The Cohorte Composer manages compositions, *i.e.* sets of components. It is split into 3 main levels:

* the *Top Composer* reads composition files and computes the distribution of components over multiple nodes (machines running a monitor isolate). It sends the instantiation orders to each Node Composer service. The *Top Composer* is unique in an application and must be present when a *Node Composer* appears; it is optional the rest of the time.
* a *Node Composer* runs in each monitor isolate. It receives a list of components to instantiate and distributes them into various isolates (processes). When the required isolates are ready (loaded with cohorte.boot.boot and a loader), the *Node Composer* sends its orders to the *Isolate Composers*;
* an *Isolate Composer* run in each applicative isolate, i.e. all isolates started with cohorte.boot.boot except monitor isolates. It converts each description of component to one that can be used by the model of the component. 

The *Isolate Composer* is based on agents, which interface it with the underlying component models. Two agents exist for now: one for [iPOPO](https://ipopo.coderxpress.net) (Python) and one for [iPOJO](http://felix.apache.org/documentation/subprojects/apache-felix-ipojo.html) (Java).

## Model

The composition model used in Cohorte Composer was designed according to the requirements of iPOJO and iPOPO to instantiate a component. Nevertheless, this model aims to be directly reusable for other service-oriented components models, like OSGi Blueprint, ...

The classes defining the objects used in Cohorte Composer are defined in ``cohorte.composer.beans`` as follows.

### Component

The ``RawComponent`` class represents a component instance. It has the following members:

| Member             | Description                                                          |
|--------------------|----------------------------------------------------------------------|
| ``factory``        | Name of the component factory (type)                                 |
| ``name``           | Name of the component instance                                       |
| ``properties``     | Initial properties of the component instance                         |
| ``bundle_name``    | Name of the bundle providing the factory                             |
| ``bundle_version`` | Version of the bundle providing the factory                          |
| ``language``       | Implementation language of the factory                               |
| ``isolate``        | Name of the isolate which must host the component instance           |
| ``node``           | Name of the node where the component must be instantiate             |
| ``filters``        | A dictionary of LDAP filters associated to component requirement IDs |
| ``wire``           | Associates a component requirement ID with a component instance      |

### Composite

The notion of composite only appears in the *Top Composer*, when it parses a composition file. It doesn't exist in the runtime model and only acts as a namespace in the description of the composition.
The ``RawComposite`` class has the following members:

| Member         | Description                                                                  |
|----------------|------------------------------------------------------------------------------|
| ``name``       | Name of the composite                                                        |
| ``parent``     | Reference to the parent composite                                            |
| ``components`` | Dictionary containing the components in this composite (Name: Component)     |
| ``composites`` | Dictionary containing the sub-composites of this composite (Name: Composite) |

### Composition

| Member       | Description                                        |
|--------------|----------------------------------------------------|
| ``name``     | Name of the composition                            |
| ``filename`` | Name of the composition file (if any)              |
| ``root``     | Reference to the root composite of the composition |
