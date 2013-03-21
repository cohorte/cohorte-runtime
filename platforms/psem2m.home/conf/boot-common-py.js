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
		"name" : "pelix.remote.dispatcher"
	}, {
		"name" : "pelix.remote.registry"
	}, {
		"name" : "cohorte.signals.http"
	}, {
		"name" : "cohorte.signals.directory"
	}, {
		"name" : "cohorte.signals.directory_updater"
	}, {
		"name" : "cohorte.remote.jabsorb_rpc"
	}, {
		"name" : "cohorte.remote.signals_discovery"
	}, {
		"name" : "cohorte.debug.servlet"
	} ],

	/*
	 * Components
	 */
	"composition" : [ {
		"factory" : "pelix.http.service.basic.factory",
		"name" : "pelix-http-service",
		"properties" : {
			// Use the IPv6 stack by default
			"pelix.http.address" : "::",

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
		"factory" : "pelix-remote-dispatcher-factory",
		"name" : "pelix-remote-dispatcher"
	}, {
		"factory" : "pelix-remote-imports-registry-factory",
		"name" : "pelix-remote-imports-registry"
	}, {
		"factory" : "cohorte-remote-discovery-signals-factory",
		"name" : "cohorte-remote-discovery-signals"
	}, {
		"factory" : "cohorte-jabsorbrpc-importer-factory",
		"name" : "cohorte-jabsorbrpc-importer"
	}, {
		"factory" : "cohorte-jabsorbrpc-exporter-factory",
		"name" : "cohorte-jabsorbrpc-exporter"
	} ]
}
