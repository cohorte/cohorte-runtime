/**
 * Common Java configuration: Remote Services
 */
{
	/*
	 * Java Remote Services bundles
	 */
	"bundles" : [ {
		"name" : "org.psem2m.remote.api"
	}, {
		"name" : "org.psem2m.remote.core"
	}, {
		"name" : "org.psem2m.remote.broadcaster"
	}, {
		"name" : "org.psem2m.remote.repository"
	} ],

	/*
	 * Components
	 */
	"composition" : [
	/* Importer (RSI), Exporter (RSE) */
	{
		"factory" : "psem2m-remote-exporter-factory",
		"name" : "psem2m-remote-exporter"
	}, {
		"factory" : "psem2m-remote-importer-factory",
		"name" : "psem2m-remote-importer"
	},

	/* Broadcaster (RSB) */
	{
		"factory" : "psem2m-remote-rsb-factory",
		"name" : "psem2m-remote-rsb"
	}, {
		"factory" : "psem2m-remote-rsb-signals-factory",
		"name" : "psem2m-remote-rsb-signals"
	},

	/* Repository (RSR) */
	{
		"factory" : "psem2m-remote-rsr-factory",
		"name" : "psem2m-remote-rsr"
	} ]
}
