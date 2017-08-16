/**
 * Common boot configuration for full-Python isolates
 */
{

	/*
	 * Bundles
	 */
	"bundles" : [ {
	    "$include":"file://boot-common.js#bundles"
	},{
		"name" : "pelix.remote.dispatcher"
	}, {
		"name" : "pelix.remote.registry"
	}, /* {
		"name" : "pelix.remote.transport.jabsorb_rpc"
	}, */ {
		"name" : "cohorte.shell.agent"
	}, /* {
		"name" : "cohorte.shell.signals"
	}, {
		"name" : "cohorte.signals.http"
	}, {
		"name" : "cohorte.signals.directory"
	}, {
		"name" : "cohorte.signals.directory_updater"
	}, {
		"name" : "cohorte.remote.signals_discovery"
	}, */ {
		"name" : "cohorte.debug.servlet"
	}, {
		"name" : "debug.agent"
	} ],

	/*
	 * Components
	 */
	"composition" : [
	/* Debug */
	{
	    "$include":"file://boot-common.js#composition"
	},
	{
		"factory" : "cohorte-debug-servlet-factory",
		"name": "cohorte-debug-servlet",
		"properties" : {
			"pelix.http.path" : "/cohorte-debug"
		}
	} 
	,
	{
		"factory" : "cohorte-debug-agent-factory",
		"name": "cohorte-debug-agent"
	}
	/* Signals components */
	/*
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
	*/

	/* Remote services */
	/*
	{
		"factory" : "cohorte-remote-discovery-signals-factory",
		"name" : "cohorte-remote-discovery-signals"
	},
	*/
        /*
	{
		"factory" : "pelix-jabsorbrpc-importer-factory",
		"name" : "pelix-jabsorbrpc-importer"
	}, {
		"factory" : "pelix-jabsorbrpc-exporter-factory",
		"name" : "pelix-jabsorbrpc-exporter"
	} */ ]
}
