/**
 * Start configuration for a Isolate Composer (Python version)
 */
{
	/*
	 * Composer bundles
	 */
	"bundles" : [
	/* Isolate Composer components */
	{
		"name" : "cohorte.composer.isolate.composer"
	}, {
		"name" : "cohorte.composer.isolate.status"
	}, {
		"name" : "cohorte.composer.isolate.agents.ipopo"
	},

	/* Debug shell commands */
	{
		"name" : "cohorte.shell.debug_composer"
	} ]

/* All components of the Top Composer are automatically instantiated */
}
