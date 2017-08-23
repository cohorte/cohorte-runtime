/**
 * Boot configuration for Forkers
 */
{
	/*
	 * Import the common configuration for Python isolates, and the Node
	 * Composer
	 */
	"import-files" : [ "boot-common-py.js", "python-common-http.js",
			"composer/python-node.js" ],

	"bundles" : [
	/*
	 * Forker bundles
	 */
	{
		"name" : "cohorte.forker.basic"
	}, {
		"name" : "cohorte.forker.broker"
	}, {
		"name" : "cohorte.forker.state"
	}, {
		"name" : "cohorte.forker.state_updater"
	}, /*{
		"name" : "pelix.shell.console"
	}, */{
		"name" : "cohorte.shell.forker"
	}, {
		"name" : "cohorte.forker.watchers"
	},
	
	/*
	 * Isolate starters
	 */
	{
		"name" : "cohorte.forker.starters.cohorte_boot"
	}, {
		"name" : "cohorte.forker.starters.exe"
	},

	/*
	 * Monitor bundles
	 */
	{
		"name" : "cohorte.monitor.basic"
	}, {
		"name" : "cohorte.monitor.status"
	}, {
		// FIXME: Should be in the TopComposer/NodeManager only
		"name" : "cohorte.monitor.node_starter"
	},

	/*
	 * Repositories
	 */
	{
		"name" : "cohorte.repositories.java.bundles"
	}, {
		"name" : "cohorte.repositories.java.ipojo"
	}, {
		"name" : "cohorte.repositories.python.modules"
	}, {
		"name" : "cohorte.repositories.python.ipopo"
	},
		
	/*
	 * HTTP Service Proxy 
	 */
	{
		"name" : "proxy.proxy"	
	} 
	],

	/*
	 * Components
	 */
	"composition" : [
	/* Configuration of common components */
	{
		"name" : "pelix-http-service",
		"properties" : {
			// Standard forker HTTP port
			"pelix.http.port" : 8000
		}
	}, {
		"name" : "pelix-remote-shell",
		"properties" : {
			// Standard forker remote shell port
			"pelix.shell.port" : 8001
		}
	},

	/* Forker specific components */
	{
		"factory" : "cohorte-forker-basic-factory",
		"name" : "cohorte-forker-basic"
	}, {
		"factory" : "cohorte-forker-broker-factory",
		"name" : "cohorte-forker-broker"
	}, {
		"factory" : "cohorte-forker-state-factory",
		"name" : "cohorte-forker-state"
	}, {
		"factory" : "cohorte-forker-state-updater-factory",
		"name" : "cohorte-forker-state-updater"
	},

	/* Configuration of monitor components */
	{
		"factory" : "cohorte-monitor-basic-factory",
		"name" : "cohorte-monitor-basic"
	}, {
		"factory" : "cohorte-monitor-status-factory",
		"name" : "cohorte-monitor-status"
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
