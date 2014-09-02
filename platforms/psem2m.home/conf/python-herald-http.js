/**
 * Configuration for Herald HTTP transport
 */
{
	/*
	 * Herald HTTP transport bundles
	 */
	"bundles" : [ {
		"name" : "herald.transports.http.directory"
	}, {
		"name" : "herald.transports.http.discovery_multicast"
	}, {
		"name" : "herald.transports.http.servlet"
	}, {
		"name" : "herald.transports.http.transport"
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
