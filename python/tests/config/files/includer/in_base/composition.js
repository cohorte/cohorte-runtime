{

    "name": "CohorteIotPack",
    "root": {
    	
    	/* import here (in root) the content of the root json objects contained in the files */
    	
        "name": "CohorteIotPack-composition",
        
        /* your component descriptions here */
        "components": [
            /* *************************** Cohorte ************************* */                                                     
            {
            	"name" : "Cohorte-CCpntPropertiesConfiguration",
                "factory" : "Cohorte-CCpntPropertiesConfiguration-Factory",
                "isolate" : "aggregator"
            },
            /* *************************** Internals *********************** */
          
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntFeaturesDirsSvc",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntFeaturesDirsSvc-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntModelHelpers",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntModelHelpers-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntJWTHandler",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntJWTHandler-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntCoreBootstrap",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntCoreBootstrap-Factory",
            	"isolate" : "aggregator"
            },         
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntIoTPackActivityLogger",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntIoTPackActivityLogger-Factory",
            	"isolate" : "aggregator"
            },   
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntTasksExecutor",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntTasksExecutor-Factory",
            	"isolate" : "aggregator"
            },
            /*
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntMemoryConstrainedCache",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntMemoryConstrainedCache-Factory",
            	"isolate" : "aggregator"
            },
            */
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntSystemRealm",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntSystemRealm-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntAccountsRealm",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntAccountsRealm-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntSubSystemsRealm",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntSubSystemsRealm-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntFeaturesRealm",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntFeaturesRealm-Factory",
            	"isolate" : "aggregator"
            },
            
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntDefaultSessionManager",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntDefaultSessionManager-Factory",
            	"isolate" : "aggregator"
            },                        
            
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntCoreStorageListener",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntCoreStorageListener-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntInternalSystem",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntInternalSystem-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Internal-CCpntSecurityManager",
            	"factory" : "IotPack-Aggregator-Core-Internal-CCpntSecurityManager-Factory",
            	"isolate" : "aggregator"
            },
            {
				"name" : "IotPack-Aggregator-Core-Internal-CCpntThreadContext",
				"factory" : "IotPack-Aggregator-Core-Internal-CCpntThreadContext-Factory",
				"isolate" : "aggregator"
			},
            /* *************************** Storage ************************* */		
			{
                "name": "IotPack-Aggregator-Storage-CCpntMongodbStorage",
                "factory": "IotPack-Aggregator-Storage-CCpntMongodbStorage-Factory",
                "isolate": "aggregator"                
            },            
            /* *************************** Managers *********************** */
            {
            	"name" : "IotPack-Aggregator-Core-Managers-CCpntAggregatesManager",
            	"factory" : "IotPack-Aggregator-Core-Managers-CCpntAggregatesManager-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Managers-CCpntItemsManager",
            	"factory" : "IotPack-Aggregator-Core-Managers-CCpntItemsManager-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Managers-CCpntModulesManager",
            	"factory" : "IotPack-Aggregator-Core-Managers-CCpntModulesManager-Factory",
            	"isolate" : "aggregator"
            },
            {
                  "name" : "IotPack-Aggregator-Core-Managers-CCpntFeaturesFactoriesManager",
                  "factory" : "IotPack-Aggregator-Core-Managers-CCpntFeaturesFactoriesManager-Factory",
                  "isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Managers-CCpntFeaturesManager",
            	"factory" : "IotPack-Aggregator-Core-Managers-CCpntFeaturesManager-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Managers-CCpntAPIKeysManager",
            	"factory" : "IotPack-Aggregator-Core-Managers-CCpntAPIKeysManager-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Managers-CCpntAuthenticationManager",
            	"factory" : "IotPack-Aggregator-Core-Managers-CCpntAuthenticationManager-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntConfigurationsManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntConfigurationsManager-Factory",
            	"isolate": "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntDevicesManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntDevicesManager-Factory",
            	"isolate": "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntDeviceSubsetsManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntDeviceSubsetsManager-Factory",
            	"isolate": "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntLoginsManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntLoginsManager-Factory",
            	"isolate": "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntMeasuresManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntMeasuresManager-Factory",
            	"isolate": "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntOrganizationsManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntOrganizationsManager-Factory",
            	"isolate": "aggregator"
            },
            {
            	"name" : "IotPack-Aggregator-Core-Managers-CCpntRolesManager",
            	"factory" : "IotPack-Aggregator-Core-Managers-CCpntRolesManager-Factory",
            	"isolate" : "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntSearchManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntSearchManager-Factory",
            	"isolate": "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntSensorsManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntSensorsManager-Factory",
            	"isolate": "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntSessionsManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntSessionsManager-Factory",
            	"isolate": "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntSitesManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntSitesManager-Factory",
            	"isolate": "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntStatsManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntStatsManager-Factory",
            	"isolate": "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntBadgesManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntBadgesManager-Factory",
            	"isolate": "aggregator"
            },
            {
            	"name": "IotPack-Aggregator-Core-Managers-CCpntAccountsManager",
            	"factory": "IotPack-Aggregator-Core-Managers-CCpntAccountsManager-Factory",
            	"isolate": "aggregator"
            },
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
			},
			/* *************************** Commands ********************** */
            {
				"name": "IotPack-Aggregator-Core-Internal-CCpntShellCommandsModels",
				"factory": "IotPack-Aggregator-Core-Internal-CCpntShellCommandsModels-Factory",
				"isolate": "aggregator"
			},
			{
                "name" : "IotPack-Aggregator-Core-Internal-CCpntShellCommands",
                "factory" : "IotPack-Aggregator-Core-Internal-CCpntShellCommands-Factory",
                "isolate" : "aggregator"
            },
        	{
                "name" : "IotPack-Aggregator-Core-Internal-CCpntAggregateCommands",
                "factory" : "IotPack-Aggregator-Core-Internal-CCpntAggregateCommands-Factory",
                "isolate" : "aggregator"
            },
            {
                "name" : "IotPack-Aggregator-Core-Internal-CCpntFeaturesCommands",
                "factory" : "IotPack-Aggregator-Core-Internal-CCpntFeaturesCommands-Factory",
                "isolate" : "aggregator"
            },
            {
                "name" : "IotPack-Aggregator-Core-Internal-CCpntManagersCommands",
                "factory" : "IotPack-Aggregator-Core-Internal-CCpntManagersCommands-Factory",
                "isolate" : "aggregator"
            },
        
            
            /* *************************** tests ********************** */
             /* to uncomment only on working desk in eclipse- 
			{
                "name" : "test_cohorte_iot_core_logging_conditions-CCpntSubSystemStuff",
                "factory" : "test-cohorte-iot-core-logging-conditions-CCpntSubSystemStuff-Factory",
                "isolate" : "aggregator"
            },
            {
			"name" : "test-cohorte-iot-thread-context-CCpntThreadContextTester",
			"factory" : "test-cohorte-iot-thread-context-CCpntThreadContextTester-Factory",
			"isolate" : "aggregator"
            },    
            {
                "name" : "IotPack-Aggregator-Core-Internal-CCpntLogChannelCommands",
                "factory" : "IotPack-Aggregator-Core-Internal-CCpntLogChannelCommands-Factory",
                "isolate" : "aggregator"
            },
            
            */
            
			/* *************************** Features ********************** */

            {
				"name" : "IotPack-Aggregator-Features-CCpntSystemFeatureFactory",
				"factory" : "IotPack-Aggregator-Features-CCpntSystemFeatureFactory-Factory",
				"isolate" : "aggregator"
			},

			/* ************ Data integration ************ */
			
			{
				"name": "cohorte-iot-message-puller",
				"factory": "cohorte-iot-message-puller-factory",
				"isolate": "aggregator"
			},
			{
				"name": "cohorte-iot-orange-lora-message-fetcher",
				"factory": "cohorte-iot-orange-lora-message-fetcher-factory",
				"isolate": "aggregator"
			},
			{
				"name": "cohorte-iot-raw-message-receiver",
				"factory": "cohorte-iot-raw-message-receiver-factory",
				"isolate": "aggregator"
			},
			{
				"name": "cohorte-iot-raw-data-interpreter",
				"factory": "cohorte-iot-raw-data-interpreter-factory",
				"isolate": "aggregator"
			}	
                  /*					
			,			
			{
				"name" : "IotPack-Demo-Meteo-CCpntMeteoReceiver",
				"factory": "IotPack-Demo-Meteo-CCpntMeteoReceiver-Factory",
				"isolate" : "aggregator"
				
			}*/
					
        ]
    }
}
