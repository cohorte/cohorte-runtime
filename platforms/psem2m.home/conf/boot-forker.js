/**
 * Boot configuration for Forkers
 */
{
	/*
	 * Import the common configuration for Python isolates
	 */
	"import-file" : "boot-common-py.js",

	/*
	 * Forker bundles
	 */
	"bundles" : [ {
		"name" : "cohorte.forker.core"
	}, {
		"name" : "cohorte.forker.broker"
	}, {
		"name" : "cohorte.forker.state"
	}, {
		"name" : "cohorte.forker.state_updater"
	} ],

	/*
	 * Components
	 */
	"composition" : [
	/* Configuration of common components */
	{
		"name" : "pelix-http-service",
		"properties" : {
			// Standard forker HTTP port
			"pelix.http.port" : 8000
		}
	}, {
		"name" : "pelix-remote-shell",
		"properties" : {
			// Standard forker remote shell port
			"pelix.shell.port" : 8001
		}
	},
	/* Forker specific components */
	{
		"factory" : "cohorte-forker-factory",
		"name" : "cohorte-forker"
	}, {
		"factory" : "cohorte-forker-broker-factory",
		"name" : "cohorte-forker-broker"
	}, {
		"factory" : "cohorte-forker-state-factory",
		"name" : "cohorte-forker-state"
	}, {
		"factory" : "cohorte-forker-state-updater-factory",
		"name" : "cohorte-forker-state-updater"
	} ]
}
