/**
 * Start configuration for the monitor (Java configuration read by Python)
 */
{
	/*
	 * OSGi stuffs
	 */
	"import-files" : [ "java-osgi.js", "java-common-ui.js" ],

	/*
	 * Framework properties
	 */
	"properties" : {
		"osgi.shell.telnet.port" : "6100",
		// HTTP port
		"org.osgi.service.http.port" : 9100
	},

	/*
	 * Framework informations
	 */
	"osgi_jar" : "org.apache.felix.framework-4.0.3.jar"
}
