/**
 * Boot configuration for Pelix isolates
 */
{
	/**
	 * Import the common configuration for Python isolates
	 */
	"import-files" : "boot-common-py.js",

	/**
	 * Isolate bundles
	 */
	"bundles" : [ {
		"name" : "cohorte.boot.loaders.pelix_inner"
	} ],

	/**
	 * Components
	 */
	"composition" : [
	/* Loader instantiation */
	{
		"factory" : "cohorte-loader-pelix-factory",
		"name" : "cohorte-loader-pelix"
	} ]
}
