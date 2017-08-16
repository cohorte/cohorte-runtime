/**
 * Configuration for Herald
 */
{
	/*
	 * Herald core bundles
	 */
	"bundles" : [ {
		"name" : "herald.core"
	}, {
		"name" : "herald.directory"
	}, {
		"name" : "herald.shell"
	}, {
		"name" : "herald.remote.discovery"
	}, {
		"name" : "herald.remote.herald_jabsorbrpc"
	}, 
	/* Starting from 1.2.0 version of Cohorte */
	{
		"name" : "herald.transports.http.directory"
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
	} ]
}
