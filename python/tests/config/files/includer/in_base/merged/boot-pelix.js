/**
 * Boot configuration for Pelix isolates
 */
{
	

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
