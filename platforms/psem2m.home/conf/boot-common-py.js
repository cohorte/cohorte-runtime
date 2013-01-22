/**
 * Common boot configuration for full-Python isolates
 */
{
	/*
	 * Import the common configuration
	 */
	"import-files" : "boot-common.js",

	/*
	 * Bundles
	 */
	"bundles" : [ {
		"name" : "pelix.http.basic"
	}, {
		"name" : "cohorte.signals.http"
	}, {
		"name" : "cohorte.signals.directory"
	}, {
		"name" : "cohorte.signals.directory_updater"
	}, {
		"name" : "cohorte.signals.remoteservices"
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
	}, {
		"factory" : "cohorte-signals-sender-http-factory",
		"name" : "cohorte-signals-sender-http"
	}, {
		"factory" : "cohorte-signals-directory-factory",
		"name" : "cohorte-signals-directory"
	}, {
		"factory" : "cohorte-signals-directory-updater-factory",
		"name" : "cohorte-signals-directory-updater"
	},

	/* Remote services */
	{
		"factory" : "cohorte-remote-exporter-factory",
		"name" : "cohorte-remote-exporter"
	}, {
		"factory" : "cohorte-remote-importer-factory",
		"name" : "cohorte-remote-importer"
	} ]
}
