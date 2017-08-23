{

    "name": "CohorteIotPack",
    "root": {
    	
    	/* import here (in root) the content of the root json objects contained in the files */
    	"import-files" : [ ],
    	
        "name": "CohorteIotPack-composition",
        
        /* your component descriptions here */
        "components": [
          
            /* *************************** tests **********************/
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
            {
            	"name" : "Cohorte-CCpntDefaultTestExecuter",
                "factory" : "Cohorte-CCpntDefaultTestExecuter-Factory",
                "isolate" : "aggregator"
            },
            {
            	"name" : "Cohorte-CCpntConsoleTestListener",
                "factory" : "Cohorte-CCpntConsoleTestListener-Factory",
                "isolate" : "aggregator"
            },
            {
            	"name" : "Cohorte-CCpntTestCommand",
                "factory" : "Cohorte-CCpntTestCommand-Factory",
                "isolate" : "aggregator"
            },
            {
            	"name": "IotPack-Tests-CCpntRolesTest",
            	"factory": "IotPack-Tests-CCpntRolesTest-Factory",
            	"isolate": "aggregator"
            }
           
          
        ]
    }
}
