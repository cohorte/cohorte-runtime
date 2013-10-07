/**
 * Start configuration for a Top Composer (Python version)
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
		"name" : "cohorte.composer.top.composer"
	}, {
		"name" : "cohorte.composer.top.distributor"
	}, {
		"name" : "cohorte.composer.top.status"
	}, {
		"name" : "cohorte.composer.top.commander"
	},

	/* Distributor criteria */
	{
		"name" : "cohorte.composer.criteria.distance.configuration"
	},

	/* Debug shell commands */
	{
		"name" : "cohorte.shell.debug_composer"
	} ]

/* All components of the Top Composer are automatically instantiated */
}
