{
	
    "name": "CohorteIotPack",
    "root": {
    	
    	/* import here (in root) the content of the root json objects contained in the files */
    	"import-files" : [ ],
    	
        "name": "CohorteIotPack-composition",
        
        /* your component descriptions here */
        "components": [
          
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
            }        
     
					
        ]
    }
}
