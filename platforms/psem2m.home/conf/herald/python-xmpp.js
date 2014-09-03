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

	/*
	 * Components
	 */
	"composition" : [ {
		"factory" : "herald-xmpp-transport-factory",
		"name" : "herald-xmpp-transport",
                "properties" : {
                    "xmpp.server" : "localhost",
                    "xmpp.port" : 5222,
                    "xmpp.monitor.jid" : "bot@phenomtwo3000",
                    "xmpp.room.jid" : "cohorte@conference.phenomtwo3000",
                    // FIXME: Should be given by the forker
                    "xmpp.monitor.key" : 42
                }
	} ]
}
