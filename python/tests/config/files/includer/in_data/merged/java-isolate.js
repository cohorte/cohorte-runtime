/**
 * Start configuration for the monitor (Java configuration read by Python)
 */
{
	/*
	 * OSGi stuffs
	 */
	"import-files" : [ "java-osgi.js", "java-common.js",
	                   "composer/java-isolate.js"],

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
	"osgi_name" : "org.apache.felix.framework"
}
