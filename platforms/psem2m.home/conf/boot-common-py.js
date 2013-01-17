/**
 * Common boot configuration for full-Python isolates
 */
{
	/*
	 * Import the common configuration
	 */
	"import-file" : "boot-common.js",

	/*
	 * Bundles
	 */
	"bundles" : [ {
		"name" : "pelix.http.basic"
	}, {
		"name" : "cohorte.signals.http"
	}, {
		"name" : "cohorte.debug.servlet"
	} ],

	/*
	 * Components
	 */
	"composition" : [ {
		"factory" : "ipopo-remote-shell-factory",
		"name" : "pelix-remote-shell",
		"properties" : {
			"pelix.shell.port" : 9000
		}
	}, {
		"factory" : "pelix.http.service.basic.factory",
		"name" : "pelix-http-service",
		"properties" : {
			// Use the first port available
			"pelix.http.port" : 0
		}
	},

	/* Debug */
	{
		"factory" : "cohorte-debug-servlet-factory",
		"properties" : {
			"pelix.http.path" : "/cohorte-debug"
		}
	},

	/* Signals components */
	{
		"factory" : "cohorte-signals-receiver-http-factory",
		"name" : "cohorte-signals-receiver-http"
	} ]
}
