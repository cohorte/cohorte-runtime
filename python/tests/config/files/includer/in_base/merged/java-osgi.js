/**
 * Start configuration for the monitor (Java configuration read by Python)
 */
{
	/*
	 * Common files
	 */
	"import-files" : [ "java-common.js", "java-common-remote.js",
			"java-common-http.js", "herald/java-core.js",
			"herald/java-transport.js" ],

	/*
	 * Java bundles
	 */
	"bundles" : [ {
		"name" : "org.cohorte.pyboot.shell.agent"
	}, {
		"name" : "org.psem2m.isolates.slave.agent"
	} ],

	/*
	 * Components
	 */
	"composition" : [
	/* Configuration of common components */
	{
		// Slave Agent: provides JMX methods to handle bundles
		"factory" : "psem2m-slave-agent-core-factory",
		"name" : "psem2m-slave-agent-core"
	} ],

	/*
	 * Properties
	 */
	"properties" : {
		// HTTP port
		"org.osgi.service.http.port" : 9000
	}
}
