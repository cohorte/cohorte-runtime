/**
 * Start configuration for the monitor (Java configuration read by Python)
 */
{
	/*
	 * Java bundles
	 */
	"bundles" : [ {
		"name" : "osgi.cmpn"
	}, {
		"name" : "org.psem2m.isolates.constants"
	}, {
		"name" : "org.psem2m.isolates.utilities"
	}, {
		"name" : "org.psem2m.isolates.base"
	}, {
		"name" : "org.apache.felix.ipojo"
	}, {
		"name" : "org.apache.felix.ipojo.handler.jmx"
	}, {
		"name" : "org.psem2m.signals.api"
	}, {
		"name" : "org.psem2m.isolates.config"
	}, {
		"name" : "org.psem2m.signals.directory"
	}, {
		"name" : "org.psem2m.signals.base"
	}, {
		"name" : "org.psem2m.signals.serializer.java"
	}, {
		"name" : "org.psem2m.isolates.loggers"
	}, {
		"name" : "org.psem2m.status.storage"
	}, {
		"name" : "org.apache.felix.shell"
	}, {
		"name" : "org.apache.felix.shell.remote"
	}, {
		"name" : "org.apache.felix.ipojo.arch"
	}, {
		"name" : "org.psem2m.isolates.slave.agent"
	} ],

	/*
	 * Components
	 */
	"composition" : [
	/* Configuration of common components */
	{
		"factory" : "psem2m-slave-agent-core-factory",
		"name" : "psem2m-slave-agent-core"
	} ]
}
