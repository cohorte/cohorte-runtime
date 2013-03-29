/**
 * Start configuration for the monitor (Python version)
 */
{
	/*
	 * Monitor bundles
	 */
	"bundles" : [ {
		"name" : "cohorte.composer.agent"
	}, {
		"name" : "cohorte.monitor.agent"
	} ],

	/*
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
		"factory" : "cohorte-composer-agent-ipopo-factory",
		"name" : "cohorte-composer-agent-ipopo"
	} ]
}
