{

    "name": "CohorteIotPack",
    "root": {
    	
    	/* import here (in root) the content of the root json objects contained in the files */
    	"import-files" : [ ],
    	
        "name": "CohorteIotPack-composition",
        
        /* your component descriptions here */
        "components": [
         
            /* *************************** Storage ************************* */		
			{
                "name": "IotPack-Aggregator-Storage-CCpntMongodbStorage",
                "factory": "IotPack-Aggregator-Storage-CCpntMongodbStorage-Factory",
                "isolate": "aggregator"                
            }         
          
        ]
    }
}
