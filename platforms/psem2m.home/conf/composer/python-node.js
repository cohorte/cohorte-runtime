/**
 * Start configuration for a Node Composer (Python version)
 */
{
	/*
	 * Composer bundles
	 */
	"bundles" : [
	/* Parser */
	{
		"name" : "cohorte.composer.parser"
	},

	/* Node Composer components */
	{
		"name" : "cohorte.composer.node.finder"
	}, {
		"name" : "cohorte.composer.node.composer"
	}, {
		"name" : "cohorte.composer.node.distributor"
	}, {
		"name" : "cohorte.composer.node.status"
	}, {
		"name" : "cohorte.composer.node.commander"
	}, {
		"name" : "cohorte.composer.node.top_store"
	},
	
	/* Shell commands */
	{
		"name" : "cohorte.shell.composer_node"
	},

	/* Distributor criteria */
	{
		"name" : "cohorte.composer.node.criteria.distance.configuration"
	}, {
		"name" : "cohorte.composer.node.criteria.reliability.crashing"
	}, {
		"name" : "cohorte.composer.node.criteria.reliability.timer"
	} ]

/* All components of the Composer are automatically instantiated */
}
