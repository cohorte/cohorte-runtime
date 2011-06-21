/**
 * File:   AbstractRunner.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.forker.impl.runners;

import java.io.File;
import java.io.IOException;

import org.psem2m.isolates.commons.IPlatformConfiguration;
import org.psem2m.isolates.commons.Utilities;
import org.psem2m.isolates.forker.IIsolateRunner;
import org.psem2m.isolates.forker.IProcessRunner;
import org.psem2m.isolates.forker.impl.processes.ProcessBuilderRunner;

/**
 * @author Thomas Calmant
 * 
 */
public abstract class AbstractRunner implements IIsolateRunner {

    /** The platform configuration */
    private IPlatformConfiguration pPlatformConfiguration;

    /**
     * Retrieves a valid working directory based on the isolate id. Creates it
     * if needed
     * 
     * @param aIsolateId
     *            The isolate ID
     * @return The isolate working directory
     * @throws IOException
     *             The working directory can't be created
     */
    protected File createWorkingDirectory(final String aIsolateId)
	    throws IOException {

	// Compute the path
	StringBuilder workingPath = new StringBuilder(
		getPlatformConfiguration().getIsolatesDirectory());
	workingPath.append(File.separator);
	workingPath.append(aIsolateId);

	// Make the directory, if needed
	File workingDir = new File(workingPath.toString());
	if (!workingDir.exists()) {
	    Utilities.makeDirectory(workingPath);
	}

	return workingDir;
    }

    /**
     * Retrieves the associated platform configuration
     * 
     * @return the platform configuration
     */
    protected IPlatformConfiguration getPlatformConfiguration() {
	return pPlatformConfiguration;
    }

    /**
     * Retrieves a process runner instance, corresponding to the running
     * operating system.
     * 
     * @return A OS-dependent process runner instance
     */
    protected IProcessRunner getProcessRunner() {
	return new ProcessBuilderRunner();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.IIsolateRunner#setConfiguration(org.psem2m
     * .isolates.commons.IPlatformConfiguration)
     */
    @Override
    public void setConfiguration(
	    final IPlatformConfiguration aPlatformConfiguration) {
	pPlatformConfiguration = aPlatformConfiguration;
    }
}
