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

	/* Top Composer components */
	{
		"name" : "cohorte.composer.node.composer"
	}, {
		"name" : "cohorte.composer.node.distributor"
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
