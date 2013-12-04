/**
 * Boot configuration for Forkers
 */
{
	/*
	 * Import the common configuration for Python isolates, and the Node
	 * Composer
	 */
	"import-files" : [ "boot-common-py.js", "python-common-http.js",
			"composer/python-node.js", "boot-forker.js" ],

	/*
	 * Components
	 */
	"composition" : [
	/* Configuration of common components */
	{
		"name" : "pelix-http-service",
		"properties" : {
			// Standard forker HTTP port
			"pelix.http.port" : 8020
		}
	}, {
		"name" : "pelix-remote-shell",
		"properties" : {
			// Standard forker remote shell port
			"pelix.shell.port" : 8021
		}
	} ]
}
