/**
 * Start configuration for the monitor (Python version)
 */
{
	"import-files" : [ "python-common-http.js" ],

	/*
	 * Monitor bundles
	 */
	"bundles" : [
	/* Monitor core */
	{
		"name" : "cohorte.monitor.core"
	}, {
		"name" : "cohorte.monitor.status"
	}, {
		"name" : "cohorte.forker.aggregator"
	},

	/* Composer */
	{
		"name" : "cohorte.composer.loader"
	}, {
		"name" : "cohorte.composer.parser"
	}, {
		"name" : "cohorte.composer.core.executor"
	}, {
		"name" : "cohorte.composer.core.queue"
	}, {
		"name" : "cohorte.composer.core.rating"
	}, {
		"name" : "cohorte.composer.core.ruleengine"
	}, {
		"name" : "cohorte.composer.core.status"
	},

	/* Repositories */
	{
		"name" : "cohorte.repositories.java.bundles"
	}, {
		"name" : "cohorte.repositories.java.ipojo"
	}, {
		"name" : "cohorte.repositories.python.modules"
	}, {
		"name" : "cohorte.repositories.python.ipopo"
	} ],

	/*
	 * Components
	 */
	"composition" : [
	/* Configuration of monitor components */
	{
		"factory" : "cohorte-monitor-core-factory",
		"name" : "cohorte-monitor-core"
	}, {
		"factory" : "cohorte-monitor-status-factory",
		"name" : "cohorte-monitor-status"
	}, {
		"factory" : "cohorte-forker-aggregator-factory",
		"name" : "cohorte-forker-aggregator"
	},

	/* Repositories */
	{
		"factory" : "cohorte-repository-artifacts-java-factory",
		"name" : "cohorte-repository-artifacts-java"
	}, {
		"factory" : "cohorte-repository-factories-ipojo-factory",
		"name" : "cohorte-repository-factories-ipojo"
	}, {
		"factory" : "cohorte-repository-artifacts-python-factory",
		"name" : "cohorte-repository-artifacts-python"
	}, {
		"factory" : "cohorte-repository-factories-ipopo-factory",
		"name" : "cohorte-repository-factories-ipopo"
	},

	/* Composition loading */
	{
		"factory" : "cohorte-composer-default-checker-factory",
		"name" : "cohorte-composer-default-checker"
	}, {
		"factory" : "cohorte-composer-loader-factory",
		"name" : "cohorte-composer-loader"
	}, {
		"factory" : "cohorte-composer-parser-factory",
		"name" : "cohorte-composer-parser"
	},

	/* Composer core engine */
	{
		"factory" : "cohorte-composer-core-executor-factory",
		"name" : "cohorte-composer-core-executor"
	}, {
		"factory" : "cohorte-composer-core-queue-factory",
		"name" : "cohorte-composer-core-queue"
	}, {
		"factory" : "cohorte-composer-rating-factory",
		"name" : "cohorte-composer-rating"
	}, {
		"factory" : "cohorte-composer-compatibility-factory",
		"name" : "cohorte-composer-compatibility"
	}, {
		"factory" : "cohorte-composer-core-rules-factory",
		"name" : "cohorte-composer-core-rules"
	}, {
		"factory" : "cohorte-composer-core-status-factory",
		"name" : "cohorte-composer-core-status"
	} ]
}
