/**
 * File:   StandardForker.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.forker.impl;

import java.io.IOException;
import java.util.Map;

import org.psem2m.isolates.commons.PlatformConfiguration;
import org.psem2m.isolates.commons.forker.ProcessConfiguration;
import org.psem2m.isolates.forker.Activator;

/**
 * Starts standard processes
 * 
 * @author Thomas Calmant
 */
public class StandardForker extends AbstractForker {

    /**
     * Initiates the forker
     */
    public StandardForker() {
	// Do nothing (here for iPOJO)
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.impl.AbstractForker#doRunProcess(org.psem2m
     * .isolates.commons.IPlatformConfiguration,
     * org.psem2m.isolates.commons.forker.IProcessConfiguration)
     */
    @Override
    protected Process doRunProcess(
	    final PlatformConfiguration aPlatformConfiguration,
	    final ProcessConfiguration aProcessConfiguration)
	    throws IOException {

	Activator.getLogger().logDebug(this, "runProcess", "Trying to run : ",
		aPlatformConfiguration, aProcessConfiguration);

	// Prepare the builder
	ProcessBuilder processBuilder = new ProcessBuilder(
		aProcessConfiguration.getCommandArray());

	// Set the working directory
	final String workingDir = aProcessConfiguration.getWorkingDirectory();
	if (workingDir != null) {
	    processBuilder.directory(makeDirectory(workingDir));
	}

	// Set the environment
	final Map<String, String> environmentConfig = aProcessConfiguration
		.getEnvironment();
	if (environmentConfig != null) {
	    Map<String, String> environmentMap = processBuilder.environment();
	    environmentMap.putAll(environmentConfig);
	}

	return processBuilder.start();
    }
}
