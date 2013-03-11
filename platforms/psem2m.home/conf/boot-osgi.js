/**
 * Boot configuration for the monitor
 */
{
	/*
	 * Import the common configuration for Java isolates
	 */
	"import-files" : "boot-java.js",

	/*
	 * Bundles
	 */
	"bundles" : [ {
		"name" : "cohorte.boot.loaders.osgi_inner"
	}, {
		"name" : "cohorte.repositories.java.bundles"
	} ],

	/*
	 * Components
	 */
	"composition" : [
	/* Loader instantiation */
	{
		"factory" : "cohorte-loader-java-factory",
		"name" : "cohorte-loader-java"
	}, {
		"factory" : "cohorte-repository-artifacts-java-factory",
		"name" : "cohorte-repository-artifacts-java"
	} ]
}
