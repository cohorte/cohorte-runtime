/**
 * Start configuration for the monitor (Java configuration read by Python)
 */
{
	/*
	 * OSGi stuffs
	 */
	"import-file": "java-osgi.js",
	
	/*
	 * Java bundles
	 */
	"bundles" : [  ],

	/*
	 * Components
	 */
	"composition" : [
	/* Configuration of common components */
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
	"osgi_jar" : "org.apache.felix.framework-4.0.3.jar"
}
