/**
 * Common Java configuration: HTTP / JSON bundles
 */
{
	/*
	 * Pelix HTTP bundles
	 */
	"bundles" : [
	/* Pelix HTTP Bundle */
	{
		"name" : "pelix.http.basic"
	} ],

	/*
	 * , { "name" : "cohorte.shell.signals" }, { "name" : "cohorte.signals.http" }, {
	 * "name" : "cohorte.signals.directory" }, { "name" :
	 * "cohorte.signals.directory_updater" }
	 */

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
	} ]

/*
 * Signals components { "factory" : "cohorte-signals-receiver-http-factory",
 * "name" : "cohorte-signals-receiver-http" }, { "factory" :
 * "cohorte-signals-sender-http-factory", "name" : "cohorte-signals-sender-http" }, {
 * "factory" : "cohorte-signals-directory-factory", "name" :
 * "cohorte-signals-directory" }, { "factory" :
 * "cohorte-signals-directory-updater-factory", "name" :
 * "cohorte-signals-directory-updater" }
 */
}
