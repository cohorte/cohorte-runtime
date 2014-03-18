{
	"name" : "validation-crashers",
	"root" : {
		"name" : "validation-root",
		"components" : [
		/* Components */
		{
			"name" : "Component-A",
			"factory" : "ghost-provider-factory",
			"properties" : {
				"specification" : "demo.A"
			}
		}, {
			"name" : "Component-B",
			"factory" : "ghost-provider-factory",
			"properties" : {
				"specification" : "demo.B"
			}
		},

		/* Nemesis */
		{
			"name" : "Nemesis-A",
			"factory" : "ghost-nemesis-factory",
			"properties" : {
				"nemesis" : "demo.A"
			}
		}, {
			"name" : "Nemesis-B",
			"factory" : "ghost-nemesis-factory",
			"properties" : {
				"nemesis" : "demo.B"
			}
		},

		/* Slimer */
		{
			"name" : "Slimer",
			"factory" : "ghost-slimer-factory"
		} ]
	}
}