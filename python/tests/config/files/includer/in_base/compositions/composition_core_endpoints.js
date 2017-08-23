{
	/*
	 * contain all core component for cohorte_iot_pack 
	 */

    "name": "CohorteIotPack",
    "root": {
    	
       
        /* your component descriptions here */
        "components": [

			/* *************************** RestApi ************************* */	
            {
            	"name": "IotPack-Aggregator-RestApi-CCpntAggregates",
				"factory": "IotPack-Aggregator-RestApi-CCpntAggregates-Factory",
				"isolate": "aggregator"
            },
        	{
				"name": "IotPack-Aggregator-RestApi-CCpntItems",
				"factory": "IotPack-Aggregator-RestApi-CCpntItems-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntModules",
				"factory": "IotPack-Aggregator-RestApi-CCpntModules-Factory",
				"isolate": "aggregator"
			},
            {
				"name": "IotPack-Aggregator-RestApi-CCpntFeatures",
				"factory": "IotPack-Aggregator-RestApi-CCpntFeatures-Factory",
				"isolate": "aggregator"
			},
            {
            	"name" : "IotPack-Aggregator-RestApi-CCpntAPIKeys",
            	"factory" : "IotPack-Aggregator-RestApi-CCpntAPIKeys-Factory",
            	"isolate" : "aggregator"
            },
			{
				"name": "IotPack-Aggregator-RestApi-CCpntRoot",
				"factory": "IotPack-Aggregator-RestApi-CCpntRoot-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntAuthenticationFilter",
				"factory": "IotPack-Aggregator-RestApi-CCpntAuthenticationFilter-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntCORSResponseFilter",
				"factory": "IotPack-Aggregator-RestApi-CCpntCORSResponseFilter-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntDebugMeFilter",
				"factory": "IotPack-Aggregator-RestApi-CCpntDebugMeFilter-Factory",
				"isolate": "aggregator"
			},
			{
				"name" : "IotPack-Aggregator-RestApi-CCpntRenewFilter",
				"factory": "IotPack-Aggregator-RestApi-CCpntRenewFilter-Factory",
				"isolate": "aggregator"
			},                  
			{
				"name": "IotPack-Aggregator-RestApi-CCpntAuthentication",
				"factory": "IotPack-Aggregator-RestApi-CCpntAuthentication-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntConfigurations",
				"factory": "IotPack-Aggregator-RestApi-CCpntConfigurations-Factory",
				"isolate": "aggregator"
			},	
			{
				"name": "IotPack-Aggregator-RestApi-CCpntDevices",
				"factory": "IotPack-Aggregator-RestApi-CCpntDevices-Factory",
				"isolate": "aggregator"
			},	
			{
				"name": "IotPack-Aggregator-RestApi-CCpntDeviceSubsets",
				"factory": "IotPack-Aggregator-RestApi-CCpntDeviceSubsets-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntDeviceSensors",
				"factory": "IotPack-Aggregator-RestApi-CCpntDeviceSensors-Factory",
				"isolate": "aggregator"
			},			
			{
				"name": "IotPack-Aggregator-RestApi-CCpntDeviceSessions",
				"factory": "IotPack-Aggregator-RestApi-CCpntDeviceSessions-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntMeasures",
				"factory": "IotPack-Aggregator-RestApi-CCpntMeasures-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntOrganizations",
				"factory": "IotPack-Aggregator-RestApi-CCpntOrganizations-Factory",
				"isolate": "aggregator"
			},	
			{
				"name" : "IotPack-Aggregator-RestApi-CCpntOrganizationFeatures",
				"factory" : "IotPack-Aggregator-RestApi-CCpntOrganizationFeatures-Factory",
				"isolate" : "aggregator"
			},
			{
				"name" : "IotPack-Aggregator-RestApi-CCpntRoles",
				"factory" : "IotPack-Aggregator-RestApi-CCpntRoles-Factory",
				"aggregator" : "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntSearch",
				"factory": "IotPack-Aggregator-RestApi-CCpntSearch-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntSensorMeasures",
				"factory": "IotPack-Aggregator-RestApi-CCpntSensorMeasures-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntSensors",
				"factory": "IotPack-Aggregator-RestApi-CCpntSensors-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntSessionMeasures",
				"factory": "IotPack-Aggregator-RestApi-CCpntSessionMeasures-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntSessions",
				"factory": "IotPack-Aggregator-RestApi-CCpntSessions-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntSites",
				"factory": "IotPack-Aggregator-RestApi-CCpntSites-Factory",
				"isolate": "aggregator"
			},	
			{
				"name": "IotPack-Aggregator-RestApi-CCpntStats",
				"factory": "IotPack-Aggregator-RestApi-CCpntStats-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntBadges",
				"factory": "IotPack-Aggregator-RestApi-CCpntBadges-Factory",
				"isolate": "aggregator"
			},
			{
				"name": "IotPack-Aggregator-RestApi-CCpntAccounts",
				"factory": "IotPack-Aggregator-RestApi-CCpntAccounts-Factory",
				"isolate": "aggregator"
			},			
			/* *************************** WebConsole ********************** */
            {
			    "name": "IotPack-Aggregator-WebConsole-CCpntMainServlet",
			    "factory": "IotPack-Aggregator-WebConsole-CCpntMainServlet-Factory",
			    "isolate": "aggregator"                
			},
            {
			    "name": "IotPack-Aggregator-WebConsole-CCpntWebConsole",
			    "factory": "IotPack-Aggregator-WebConsole-CCpntWebConsole-Factory",
			    "isolate": "aggregator"                
			}
        ]
    }
}
