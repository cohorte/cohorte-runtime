/**
 * File:   AbstractForker.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.forker.impl;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.TreeMap;

import org.psem2m.isolates.commons.PlatformConfiguration;
import org.psem2m.isolates.commons.forker.IForker;
import org.psem2m.isolates.commons.forker.ProcessConfiguration;
import org.psem2m.isolates.forker.Activator;

/**
 * Basic forker information behaviors
 * 
 * @author Thomas Calmant
 */
public abstract class AbstractForker implements IForker {

    /** Keeping trace of started processes */
    private final Map<String, Process> pStartedProcesses = new TreeMap<String, Process>();

    /**
     * Starts a process according to the given configuration. Called by
     * {@link AbstractForker#runProcess(PlatformConfiguration, ProcessConfiguration)}
     * , which stores the result of this method.
     * 
     * @param aPlatformConfiguration
     *            The platform configuration
     * @param aProcessConfiguration
     *            The configuration of the future process
     * @return The launched process, null on error
     * @throws IOException
     */
    protected abstract Process doRunProcess(
	    final PlatformConfiguration aPlatformConfiguration,
	    final ProcessConfiguration aProcessConfiguration)
	    throws IOException;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.forker.IForker#killProcess(java.lang.String)
     */
    @Override
    public void killProcess(final String aIsolateId) {

	Activator.getLogger().logDebug(this, "killProcess",
		"Trying to kill : ", aIsolateId);

	Process process = pStartedProcesses.get(aIsolateId);
	if (process != null) {
	    process.destroy();

	    try {
		process.waitFor();
		pStartedProcesses.remove(aIsolateId);

	    } catch (InterruptedException e) {
		Activator.getLogger().logSevere(this, "killProcess",
			"Error waiting for termination : ", e);
	    }
	}

	pStartedProcesses.remove(aIsolateId);
    }

    /**
     * Returns a valid directory from the given path (it may return the parent
     * directory).
     * 
     * @param aPath
     *            base directory path
     * @return A valid directory path
     */
    protected File makeDirectory(final String aPath) {

	File directory = new File(aPath);

	if (!directory.exists()) {
	    // Create directory if needed
	    directory.mkdirs();

	} else if (!directory.isDirectory()) {
	    // Use the parent directory if the path represents something else
	    directory = directory.getParentFile();
	}

	return directory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.forker.IForker#ping(java.lang.String)
     */
    @Override
    public EProcessState ping(final String aIsolateId) {

	Process process = pStartedProcesses.get(aIsolateId);
	if (process == null) {
	    return EProcessState.DEAD;
	}

	// TODO ping process

	return EProcessState.ALIVE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.forker.IForker#runProcess(org.psem2m.isolates
     * .commons.IPlatformConfiguration,
     * org.psem2m.isolates.commons.forker.IProcessConfiguration)
     */
    @Override
    public Process runProcess(
	    final PlatformConfiguration aPlatformConfiguration,
	    final ProcessConfiguration aProcessConfiguration)
	    throws IOException, InvalidParameterException {

	Activator.getLogger().logDebug(this, "runProcess",
		"Running process for isolate",
		aProcessConfiguration.getIsolateConfiguration().getIsolateId());

	final String isolateId = aProcessConfiguration
		.getIsolateConfiguration().getIsolateId();

	Process oldProcess = pStartedProcesses.get(isolateId);
	if (oldProcess != null) {
	    // throw new
	    // InvalidParameterException("Isolate is already running");
	    killProcess(isolateId);
	}

	Process newProcess = doRunProcess(aPlatformConfiguration,
		aProcessConfiguration);

	if (newProcess != null) {
	    pStartedProcesses.put(isolateId, newProcess);
	}

	return newProcess;
    }
}
