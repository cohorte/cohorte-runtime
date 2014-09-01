/**
 * Configuration for Herald
 */
{
	"import-files" : [ "python-common-http.js" ],

	/*
	 * Herald bundles
	 */
	"bundles" : [ {
		"name" : "herald.core"
	}, {
		"name" : "herald.directory"
	}, {
		"name" : "herald.shell"
	}, {
		"name" : "herald.transports.http.directory"
	}, {
		"name" : "herald.transports.http.discovery_multicast"
	}, {
		"name" : "herald.transports.http.servlet"
	}, {
		"name" : "herald.transports.http.transport"
	}, {
		"name" : "herald.remote.discovery"
	}, {
		"name" : "herald.remote.herald_rpc"
	} ],

	/*
	 * Components
	 */
	"composition" : [ {
		"factory" : "herald-http-servlet-factory",
		"name" : "herald-http-servlet"
	}, {
		"factory" : "herald-http-discovery-multicast-factory",
		"name" : "herald-http-discovery-multicast"
	} ]
}
