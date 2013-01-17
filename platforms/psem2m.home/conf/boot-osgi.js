/**
 * Boot configuration for the monitor
 */
{
	/*
	 * Import the common configuration for Java isolates
	 */
	"import-file" : "boot-java.js",

	/*
	 * Bundles
	 */
	"bundles" : [ {
		"name" : "cohorte.boot.loaders.osgi_inner"
	} ],

	/*
	 * Components
	 */
	"composition" : [
	/* Loader instantiation */
	{
		"factory" : "cohorte-loader-java-factory",
		"name" : "cohorte-loader-java"
	} ]
}
