{
	"name" : "demo-august2013-cohorte",
	"root" : {
		"name" : "demo-august2013",
		"components" : [ {
			/**
			 * Python sensor
			 */
			"name" : "temper-python-python",
			"factory" : "demo-temperature-fake-factory",
			// Force isolation
			"isolate" : "temper.python",
			"properties" : {
				"temper.value.min" : -5,
				"temper.value.max" : 45
			}
		}, {
			/**
			 * Raspberry Pi sensor
			 */
			"name" : "temper-python-raspi",
			"factory" : "demo-temperature-fake-factory",
			// Force placement
			"isolate" : "temper.raspi",
			"node" : "raspberry"
		}, {
			/**
			 * Java sensor
			 */
			"name" : "temper-java",
			"factory" : "java-fake-temp-factory",
			"isolate" : "temper.java"
		}, {
			/**
			 * Aggregator component
			 */
			"name" : "aggregator",
			"factory" : "demo-sensor-aggregator-factory",
			"language" : "python",
			"isolate" : "stratus.aggregator",
			"properties" : {
				"poll.delta" : 1
			}
		}, {
			/**
			 * Aggregator web UI
			 */
			"name" : "aggregator-UI",
			"factory" : "demo-sensor-aggregator-ui-factory",
			"language" : "python",
			// Force the isolate name: allows to force the HTTP port in
			// the isolate configuration
			"isolate" : "aggregator.ui",
			"properties" : {
				"servlet.path" : "/sensors"
			},
			"wires" : {
				"_aggregator" : "aggregator"
			}
		} ]
	}
}