/**
 * Common Java configuration: Signals
 */
{
	/*
	 * Java Signals bundles
	 */
	"bundles" : [ {
		"name" : "org.psem2m.signals.api"
	}, {
		"name" : "org.psem2m.signals.base"
	}, {
		"name" : "org.psem2m.signals.directory"
	} ],

	/*
	 * Components
	 */
	"composition" : [
	/* Signals Base components */
	{
		"factory" : "psem2m-signals-broadcaster-factory",
		"name" : "psem2m-signals-broadcaster"
	}, {
		"factory" : "psem2m-signals-receiver-factory",
		"name" : "psem2m-signals-receiver"
	},

	/* Signals Directory */
	{
		"factory" : "psem2m-signals-directory-factory",
		"name" : "psem2m-signals-directory"
	}, {
		"factory" : "psem2m-signals-directory-updater-factory",
		"name" : "psem2m-signals-directory-updater"
	} ]
}
