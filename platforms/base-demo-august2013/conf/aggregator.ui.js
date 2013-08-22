{
	"composition" : [ {
		/*
		 * Override the HTTP service configuration
		 */
		"factory" : "pelix.http.service.basic.factory",
		"name" : "pelix-http-service",
		"properties" : {
			"pelix.http.port" : 9500
		}
	} ]
}