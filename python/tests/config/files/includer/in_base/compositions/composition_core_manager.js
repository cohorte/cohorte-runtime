{

    "name": "CohorteIotPack",
    "root": {
    	
    	/* import here (in root) the content of the root json objects contained in the files */
    	"import-files" : [ ],
    	
        "name": "CohorteIotPack-composition",
        
        /* your component descriptions here */
        "components": [
          
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
            }
		
					
        ]
    }
}
