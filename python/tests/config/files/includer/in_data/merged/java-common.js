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
		"name" : "org.cohorte.utilities"
	}, {
		"name" : "org.psem2m.isolates.base"
	}, {
		"name" : "org.apache.felix.ipojo"
	}, {
		"name" : "org.apache.felix.ipojo.handler.jmx"
	}, {
		"name" : "org.psem2m.isolates.loggers"
	}, {
		"name" : "org.psem2m.status.storage"
	}, {
		"name" : "org.apache.felix.gogo.runtime"
	}, {
		"name" : "org.apache.felix.gogo.shell"
	}, {
		"name" : "org.apache.felix.gogo.command"
	}, {
		"name" : "org.apache.felix.ipojo.gogo"
	}, {
		"name" : "org.cohorte.shell.osgi"
	}, {
		"name" : "org.cohorte.shell.remote"
	}, {
		"name" : "org.cohorte.isolates.discovery.local"
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
	}, {
		// Remote shell
		"factory" : "cohorte-remote-shell-factory",
		"name" : "cohorte-remote-shell"
	} ],

	/*
	 * Framework properties
	 */
	"properties" : {
		"gosh.args" : "--nointeractive --noshutdown"
	}
}
