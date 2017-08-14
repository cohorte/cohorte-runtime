/**
 * Common component configuration for the XMPP transport
 */
{
	/*
	 * XMPP core component
	 */
	"composition" : [ {
		"factory" : "herald-xmpp-transport-factory",
		"name" : "herald-xmpp-transport",
		"properties" : {
			"xmpp.server" : "localhost",
			"xmpp.port" : "5222",			
            "xmpp.user.jid" : null,
            "xmpp.user.password" : null
		}
	} ]
}
