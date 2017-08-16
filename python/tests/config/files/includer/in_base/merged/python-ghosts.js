/**
 * Ghost components (Python version)
 */
{
	/*
	 * Ghost bundles
	 */
	"bundles" : [ {
		"name" : "pelix.services.configadmin"
	}, {
		"name" : "pelix.services.mqtt"
	}, {
		"name" : "pelix.shell.configadmin"
	}, {
		"name" : "demo.ghost.starter"
	}, {
		"name" : "demo.ghost.provider"
	}, {
		"name" : "demo.ghost.crasher"
	}, {
		"name" : "demo.ghost.nemesis"
	}, {
		"name" : "demo.ghost.slimer"
	} ],

	/*
	 * Components
	 */
	"composition" : [
    /* Ghosts */
    {
		"factory" : "ghost-crasher-factory",
		"name" : "crasher"
	} ]
}
