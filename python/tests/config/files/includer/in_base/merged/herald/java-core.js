/**
 * Configuration for Herald
 */
{
	/*
	 * Herald core bundles
	 */
	"bundles" : [ {
		"name" : "org.cohorte.herald.api"
	}, {
		"name" : "org.cohorte.herald.core"
	}, { 
		"name" : "org.cohorte.herald.shell"
	}, 
	/* Herald HTTP transport bundle is embeeded to core
	 * starting from 1.2.0 version of Cohorte.
	 * It is used by the Local Discovery (except multicast)
	 */
	{
		"name" : "org.cohorte.herald.http"
	} ]	
}
