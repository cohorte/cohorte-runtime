/**
 * Start configuration for basic isolates (Python version)
 */
{
	"import-files" : [ "python-common-http.js",
	                   "composer/python-isolate.js"],
	
	/*
	 * Monitor bundles
	 */
	"bundles" : [ {
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
	}]
}
