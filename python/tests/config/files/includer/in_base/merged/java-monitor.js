/**
 * Start configuration for the monitor (Java configuration read by Python)
 */
{
	/*
	 * OSGi stuffs
	 */
	"import-files" : [ "java-osgi.js", "java-common-ui.js" ],

	/*
	 * Java bundles
	 */
	"bundles" : [ {
		"name" : "org.cohorte.monitor.api"
	}, {
		"name" : "org.cohorte.composer.api"
	}, {
		"name" : "org.psem2m.forker.api"
	}, {
		"name" : "org.cohorte.composer.core"
	}, {
		"name" : "org.cohorte.monitor"
	}, {
		"name" : "org.psem2m.forkers.aggregator"
	} ],

	/*
	 * Components
	 */
	"composition" : [
	/* Configuration of monitor components */
	{
		"factory" : "cohorte-platform-controller-factory",
		"name" : "cohorte-platform-controller"
	}, {
		"factory" : "cohorte-monitor-status-factory",
		"name" : "cohorte-monitor-status"
	}, {
		"factory" : "cohorte-platform-static-isolates-factory",
		"name" : "cohorte-platform-static-isolates"
	}
	/* Configuration of composer components */
	],

	/*
	 * Framework properties
	 */
	"properties" : {
		"osgi.shell.telnet.port" : "6000"
	},

	/*
	 * Framework informations
	 */
	"osgi_name" : "org.apache.felix.framework"
}
