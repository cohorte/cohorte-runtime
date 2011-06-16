/**
 * 
 */
package org.psem2m.isolates.forker.impl;

/**
 * Specific forker to run a Felix framework
 */
public class FelixForker extends AbstractForker {

    @Override
    protected ProcessConfiguration prepareEnvironment(final String aIsolateId) {

	// Prepare the working directory
	final ForkerConfiguration forkerConfig = getConfiguration();
	final String isolateDir = getDirectoryName(aIsolateId,
		forkerConfig.getIsolateDirectoryPattern());

	makeDirectory(isolateDir, true);

	// Prepare the process configuration
	ProcessConfiguration runConfig = new ProcessConfiguration();
	runConfig.setExecutable(forkerConfig.getJava());
	runConfig.setArguments(new String[] { "-jar",
		forkerConfig.getFelixJar() });
	runConfig.setWorkingDirectory(isolateDir);

	return runConfig;
    }
}