/**
 * Configuration for Herald HTTP transport
 */
{	
	/*
	 * Components
	 */
	"composition" : [ {
		"factory" : "herald-http-discovery-multicast-starter-factory",
		"name" : "herald-http-discovery-multicast-starter"
	} ], 
	/*
	 * Avoid discovering local peers using multicast
	 * when Local Discovery is used 
	 * (starting from 1.2.0 version of Cohorte)
	 */
	"properties" : {		
		"herald.discover.local.peers" : false
	}
}
