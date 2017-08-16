/**
 * Configuration for Herald HTTP transport
 */
{
	/*
	 * Herald HTTP transport bundles
	 */
	"bundles" : [ {		
		"name" : "herald.transports.http.discovery_multicast"
	} ],

	/*
	 * Components
	 */
	"composition" : [ {
		"factory" : "herald-http-discovery-multicast-factory",
		"name" : "herald-http-discovery-multicast",
		"properties": {
			/* do not allow the discovery of local peers
			   (from the same node). This is related to the use
			   of the Local Discovery on cohorte. */
			"discover.local.peers": false
		}
	} ]
}
