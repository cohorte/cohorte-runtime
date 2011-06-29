.. Eclipse ECF

Annexe 3 -- Eclipse ECF
#######################

L'utilisation d'Eclipse ECF avec Declarative Services est très simple : il
suffit d'ajouter les bonnes dépendances et de faire une indication d'export de
service côté hôte.


Dépendances à utiliser
**********************

Ces dépendances sont à choisir en fonction des modes de diffusion et de
découverte que l'on veut utiliser.

On dépendra au moins de :

- org.eclipse.ecf : définition de base des ECF,

- org.eclipse.ecf.provider : contient les interfaces décrivant la diffusion et
  la découverte,

- org.eclipse.ecf.provider.discovery : s'occupe de découvrir et de répondre aux
  requêtes de découverte,

- org.eclipse.ecf.osgi.services.discovery : idem,

- org.eclipse.equinox.common : contient quelques classes nécessaires pour
  compiler avec ECF,

- org.eclipse.osgi.services : idem,

- org.eclipse.ecf.osgi.services.distribution : s'occupe d'inscrire ou de
  diffuser les services partagés


Méthodes de distribution
************************

Dans tous les cas, le bundle ``org.eclipse.ecf.osgi.services.distribution`` doit
être lancé avant tout provider, sous peine de recevoir une exception au
démarrage du framework.

Nous avons testé les méthodes de diffusions suivantes :

- R-OSGi : basé sur le travail d'ETH Zurich.
  Nous ne l'avons pas retenue du fait de quelques contraintes telles que la
  création d'un conteneur côté hôte et côté client.

- ECF generic server : serveur de base d'Equinox.
  Fonctionne correctement, mais l'exportation de plusieurs composants n'a pas
  été testée.

Pour utiliser R-OSGi, il faut dépendre de ``ch.ethz.iks.r_osgi.remote`` et
``org.eclipse.ecf.provider.r_osgi`` et utiliser un export de type
``ecf.r_osgi.peer``.
Le conteneur doit être créé manuellement, plus d'informations ici (en Russe) :
`<http://samolisov.blogspot.com/2009/10/ecf-remote-services-api.html>`_

Pour utiliser ECF, les plug-ins ``org.eclipse.ecf.provider.remoteservice`` et
``org.eclipse.ecf.remoteservice`` sont nécessaires.

D'autres "providers" sont indiqués à l'adresse :
`<http://wiki.eclipse.org/ECF_Providers#Remote_Services_.28org.eclipse.ecf.remoteservice.29>`_.


Méthodes de découverte
**********************

La méthode de découverte standard a été utilisée, mais l'exécution de l'hôte et
du client se faisant sur la même machine, sa version locale a due être ajoutée.

Le projet client dépend ainsi de :

- org.eclipse.ecf.provider.localdiscovery : découverte sur la machine locale
  (complète discovery),

- org.eclipse.ecf.osgi.services.discovery.local : idem (complète discovery)

D'autres "providers" de découverte sont indiqués à l'adresse :
`<http://wiki.eclipse.org/ECF_Providers#Discovery_.28org.eclipse.ecf.discovery.29>`_.


Configuration côté hôte
***********************

Dans le fichier décrivant un composant Declarative Services à exporter, il faut
ajouter des propriétés indiquant le type d'export -- voir les méthodes de
distribution -- et les propriétés nécessaires à celui-ci.

Exemple de composant avec le serveur générique ECF :

.. literalinclude:: /_static/ecf-host.xml
   :language: xml
   :linenos:


Configuration côté client
*************************

Côté client, on doit dépendre des mêmes outils ECF que côté serveur, à fin que
la découverte et le dialogue soient possible.

Au niveau de la configuration du composant client, rien ne change par rapport à
une utilisation "locale" de Declarative Services.

On peut cependant indiquer manuellement où se trouve le serveur contenant le
service à utiliser.
Ceci n'accélère pas la découverte du service.
Pour cela, on doit créer le fichier *services.xml* dans le dossier
*OSGI-INF/remote-service*, dont les propriétés décrites dépendent du modèle de
diffusion choisi.

Exemple de fichier *services.xml* indiquant un ecf.generic.server :

.. literalinclude:: /_static/ecf-client.xml
   :language: xml
   :linenos:
