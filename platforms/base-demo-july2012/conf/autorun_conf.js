{
	"name": "demo-july2012-cohorte",
	"root" : {
		"name" : "demo-july2012",
		"components" : [ {
			"name" : "temper-1",
			"type" : "demo-temperature-fake-factory",
			"language": "python",
			"isolate" : "demo.temper",
			"properties" : {
				"temper.value.min" : -5,
				"temper.value.max" : 45
			}
		}, {
			"name" : "temper-2",
			"type" : "demo-temperature-fake-factory",
			"language": "python",
			"isolate" : "demo.temper-2"
		}, {
			"name" : "temper-3",
			"type" : "java-fake-temp-factory",
			"language": "java",
			"isolate" : "demo.temper-java"
		}, {
			"name" : "aggregator",
			"type" : "demo-sensor-aggregator-factory",
			"language": "java",
			"isolate" : "demo.stratus.aggregator",
			"properties" : {
				"poll.delta" : 1
			}
		}, {
			"name" : "aggregator-UI",
			"type" : "demo-sensor-aggregator-ui-factory",
			"language": "java",
			"isolate" : "demo.stratus.aggregator",
			"properties" : {
				"servlet.path" : "/sensors"
			},
			"wires" : {
				"_aggregator" : "aggregator"
			}
		} ]
	}
}