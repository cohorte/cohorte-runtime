{

    "name": "CohorteIotPack",
    "root": {
    	
    	/* import here (in root) the content of the root json objects contained in the files */
    	"import-files" : [ ],
    	
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
         
			/* *************************** Features ********************** */

            {
				"name" : "IotPack-Aggregator-Features-CCpntSystemFeatureFactory",
				"factory" : "IotPack-Aggregator-Features-CCpntSystemFeatureFactory-Factory",
				"isolate" : "aggregator"
			}

					
        ]
    }
}
