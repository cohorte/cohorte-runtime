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
		"name" : "pelix.remote.dispatcher"
	}, {
		"name" : "pelix.remote.registry"
	}, {
		"name" : "pelix.services.eventadmin"
	}, {
		"name" : "pelix.shell.eventadmin"
	}, {
		"name" : "cohorte.shell.agent"
	}, {
		"name" : "cohorte.shell.signals"
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
	"composition" : [
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
		"factory" : "cohorte-remote-discovery-signals-factory",
		"name" : "cohorte-remote-discovery-signals"
	}, {
		"factory" : "cohorte-jabsorbrpc-importer-factory",
		"name" : "cohorte-jabsorbrpc-importer"
	}, {
		"factory" : "cohorte-jabsorbrpc-exporter-factory",
		"name" : "cohorte-jabsorbrpc-exporter"
	},

	/* Event Admin */
	{
		"factory" : "pelix-services-eventadmin-factory",
		"name" : "pelix-services-eventadmin"
	} ]
}
