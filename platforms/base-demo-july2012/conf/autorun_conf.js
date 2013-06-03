{
	"name" : "demo-july2012-cohorte",
	"root" : {
		"name" : "demo-july2012",
		"components" : [ {
			/**
			 * Fake temperature sensors
			 */
			"name" : "temper-python-1",
			"type" : "demo-temperature-fake-factory",
			// Uncomment to force isolation
			// "isolate" : "demo.temper.1",
			"properties" : {
				"temper.value.min" : -5,
				"temper.value.max" : 45
			}
		}, {
			"name" : "temper-python-2",
			"type" : "demo-temperature-fake-factory"
			// Uncomment to force isolation
			// , "isolate" : "demo.temper.2"
		}, {
			"name" : "temper-java-1",
			"type" : "java-fake-temp-factory",
			// Give a name for the Java isolate
			"isolate" : "demo.temper.java"
		}, {
			/**
			 * Aggregator component
			 */
			"name" : "aggregator",
			"type" : "demo-sensor-aggregator-factory",
			"language" : "python",
			// "isolate" : "demo.stratus.aggregator",
			"properties" : {
				"poll.delta" : 1
			}
		}, {
			/**
			 * Aggregator web UI
			 */
			"name" : "aggregator-UI",
			"type" : "demo-sensor-aggregator-ui-factory",
			"language" : "python",
			// Force the isolate name: allows to force the HTTP port in
			// the isolate configuration
			"isolate" : "demo.aggregator.ui",
			"properties" : {
				"servlet.path" : "/sensors"
			},
			"wires" : {
				"_aggregator" : "aggregator"
			}
		} ]
	}
}