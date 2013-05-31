/**
 * Start configuration for the monitor (Java configuration read by Python)
 */
{
	/*
	 * OSGi stuffs
	 */
	"import-files" : [ "java-osgi.js", "java-common.js" ],

	/*
	 * Bundles
	 */
	"bundles" : [ {
		"name" : "org.cohorte.composer.api"
	}, {
		"name" : "org.cohorte.composer.agent"
	} ],

	/*
	 * Components
	 */
	"composition" : [
	/* Composer agent */
	{
		"factory" : "cohorte-composer-agent-ipojo-factory",
		"name" : "cohorte-composer-agent-ipojo"
	} ],

	/*
	 * Framework properties
	 */
	"properties" : {
		// Remote shell port
		"osgi.shell.telnet.port" : 0,
		// HTTP port
		"org.osgi.service.http.port" : 0
	},

	/*
	 * Framework informations
	 */
	"osgi_jar" : "org.apache.felix.framework-4.0.3.jar"
}
