{

    "name": "CohorteIotPack",
    "root": {
    	
    	/* import here (in root) the content of the root json objects contained in the files */
    	"import-files" : [ ],
    	
        "name": "CohorteIotPack-composition",
        
        /* your component descriptions here */
        "components": [

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
