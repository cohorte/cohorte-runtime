/**
 * Configuration for Herald XMPP transport
 */
{
	/*
	 * Herald XMPP transport bundles
	 */
	"bundles" : [ {
		"name" : "herald.transports.xmpp.directory"
	}, {
		"name" : "herald.transports.xmpp.transport"
	} ],

	// Import the common component configuration
	"import-files" : "all-xmpp.js"
}
