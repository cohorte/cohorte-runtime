/**
 * Start configuration for the monitor (Python version)
 */
{
	"import-files" : [ "python-common-http.js",
	                   "composer/python-top.js",
	                   "composer/python-node.js"],

	/*
	 * Monitor bundles
	 */
	"bundles" : [
	/* Monitor core */
	{
		"name" : "cohorte.monitor.core"
	}, {
		"name" : "cohorte.monitor.status"
	}, /* {
		"name" : "cohorte.forker.aggregator"
	}, */ {
		"name" : "cohorte.monitor.node_starter"
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
	/* Common components */
	{
		"name" : "pelix-http-service",
		"properties" : {
			// Standard forker HTTP port
			"pelix.http.port" : 8010
		}
	}, {
		"factory" : "ipopo-remote-shell-factory",
		"name" : "pelix-remote-shell-monitor",
		"properties" : {
			// Standard forker remote shell port
			"pelix.shell.port" : 8011
		}
	},

	/* Configuration of monitor components */
	{
		"factory" : "cohorte-monitor-core-factory",
		"name" : "cohorte-monitor-core"
	}, {
		"factory" : "cohorte-monitor-status-factory",
		"name" : "cohorte-monitor-status"
	}, {
		"factory" : "cohorte-forker-aggregator-factory",
		"name" : "cohorte-forker-aggregator",
		"properties" : {
			"multicast.port" : 42001
		}
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
	} ]
}
