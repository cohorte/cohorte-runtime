/**
 * Common bundles for OSGi isolates
 */
{
	/*
	 * Java bundles
	 */
	"bundles" : [ {
		// OSGi Compendium
		"name" : "osgi.cmpn"
	}, {
		"name" : "org.psem2m.isolates.constants"
	}, {
		"name" : "org.cohorte.pyboot.config"
	}, {
		"name" : "org.psem2m.isolates.utilities"
	}, {
		"name" : "org.psem2m.isolates.base"
	}, {
		"name" : "org.apache.felix.ipojo"
	}, {
		"name" : "org.apache.felix.ipojo.handler.jmx"
	}, {
		"name" : "org.psem2m.isolates.config"
	}, {
		"name" : "org.psem2m.isolates.loggers"
	}, {
		"name" : "org.psem2m.status.storage"
	}, {
		"name" : "org.apache.felix.shell"
	}, {
		"name" : "org.apache.felix.shell.remote"
	}, {
		"name" : "org.apache.felix.ipojo.arch"
	} ],

	/*
	 * Components
	 */
	"composition" : [ {
		// Python boot bridge : configuration parser
		"factory" : "cohorte-pyboot-configuration-parser-factory",
		"name" : "cohorte-pyboot-configuration-parser"
	}, {
		// Python boot bridge : boot configuration
		"factory" : "cohorte-pyboot-configuration-start-factory",
		"name" : "cohorte-pyboot-configuration-start"
	} ]
}
