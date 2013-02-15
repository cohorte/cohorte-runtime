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
		"name" : "cohorte.composer.agent"
	}, {
		"name" : "cohorte.monitor.agent"
	}, ],

	/**
	 * Components
	 */
	"composition" : [
	/* Isolate agent */
	{
		"factory" : "cohorte-isolate-agent-factory",
		"name" : "cohorte-isolate-agent"
	},
	/* Composer agent */
	{
		"factory" : "cohorte-composer-agent-factory",
		"name" : "cohorte-composer-agent"
	} ]
}
