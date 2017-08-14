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

	/* Debug instruments */
	{
		"name" : "cohorte.composer.node.history"
	}, /*{
		"name" : "cohorte.instruments.servlet"
	}, {
		"name" : "cohorte.instruments.node_composer"
	}, */

	/* Voting system */
	{
		"name" : "cohorte.vote.core"
	}, {
		"name" : "cohorte.vote.approbation"
	}, /* {
		"name" : "cohorte.vote.cartoonist"
	}, */ {
		"name" : "cohorte.vote.servlet"
	}, {
		"name" : "cohorte.vote.dummy_store"
	},

	/* Distributor criteria */
	{
		"name" : "cohorte.composer.node.criteria.distance.configuration"
	}, {
		"name" : "cohorte.composer.node.criteria.distance.history"
	} ]

/* All components of the Composer are automatically instantiated */
}
