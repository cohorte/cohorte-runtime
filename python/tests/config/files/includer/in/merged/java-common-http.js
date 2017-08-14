/**
 * Common Java configuration: HTTP / JSON bundles
 */
{
	/*
	 * Java HTTP bundles
	 */
	"bundles" : [
	/* Apache Felix HTTP Bundle */
	{
		"name" : "org.apache.felix.http.jetty"
	},
	{
		"name" : "org.apache.felix.http.servlet-api"
	},

	/* JSON Support */
	{
		"name" : "org.jabsorb.ng"
	} /* {
		"name" : "org.psem2m.signals.serializer.json"
	}, {
		"name" : "org.cohorte.remote.jabsorbrpc"
	} */ ],

	/*
	 * Components
	 */
	"composition" : [
	/* HTTP Signals */
	/*
	{
		"factory" : "psem2m-signals-sender-http-factory",
		"name" : "psem2m-signals-sender-http"
	}, {
		"factory" : "psem2m-signals-receiver-http-factory",
		"name" : "psem2m-signals-receiver-http"
	},
	*/

	/* JSON Serializer */
	/*
	{
		"factory" : "psem2m-signals-data-json-factory",
		"name" : "psem2m-signals-data-json"
	}
	*/ ],

	/*
	 * Properties
	 */
	"properties" : {
		// Activate Jetty
		"org.apache.felix.http.jettyEnabled" : "true"
	}
}
