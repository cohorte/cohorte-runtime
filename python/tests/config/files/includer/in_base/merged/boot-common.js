/**
 * Boot configuration common for all isolates
 */
{
	/*
	 * List of bundles
	 */
	"bundles" : [
	// The remote shell is common to all isolates
	{
		"name" : "pelix.shell.core"
	}, {
		"name" : "pelix.shell.ipopo"
	}, {
		"name" : "pelix.shell.remote"
	}, {
		"name" : "cohorte.local.discovery"
	} ],

	/*
	 * The boot components
	 */
	"composition" : [
	// The remote shell port should be overridden
	{
		"factory" : "ipopo-remote-shell-factory",
		"name" : "pelix-remote-shell",
		"properties" : {
			"pelix.shell.port" : 0
		}
	} ],
	
	/*
	 * The isolate properties
	 */
	"properties" : {
		"psem2m.compatible" : false
	}
}
