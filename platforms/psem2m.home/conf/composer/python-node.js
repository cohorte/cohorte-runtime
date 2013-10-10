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
	},

	/* Distributor criteria */
	{
		"name" : "cohorte.composer.node.criteria.distance.configuration"
	},

	/* Debug shell commands */
	{
		"name" : "cohorte.shell.debug_composer"
	} ]

/* All components of the Top Composer are automatically instantiated */
}
