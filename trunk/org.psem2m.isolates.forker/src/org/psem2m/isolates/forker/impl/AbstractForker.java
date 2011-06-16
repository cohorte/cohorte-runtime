/**
 * 
 */
package org.psem2m.isolates.forker.impl;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.psem2m.isolates.commons.IReconfigurable;
import org.psem2m.isolates.forker.IForker;
import org.psem2m.isolates.forker.IProcess;

/**
 * Abstract forker methods
 * 
 */
public abstract class AbstractForker implements IForker, IReconfigurable {

    /** Current forker configuration */
    private ForkerConfiguration pConfiguration = new ForkerConfiguration();

    /** Isolates ran from this forker */
    private Map<String, IProcess> pIsolatedProcesses = new TreeMap<String, IProcess>();

    /**
     * Retrieves the current configuration.
     * 
     * @return the configuration
     */
    protected ForkerConfiguration getConfiguration() {
	return pConfiguration;
    }

    /**
     * Retrieves the complete directory name for the given isolate ID
     * 
     * @param aIsolateId
     *            The isolate ID
     * @param aPattern
     *            The directory pattern
     * 
     * @return The directory name
     */
    public String getDirectoryName(final CharSequence aIsolateId,
	    final String aPattern) {

	// Build the complete directory name
	StringBuilder builder = new StringBuilder();
	builder.append(pConfiguration.getWorkingDirectory());
	builder.append(File.separator);

	// Replace the isolateId variable in the pattern
	builder.append(aPattern.replace(ISOLATE_DIRECTORY_VARIABLE, aIsolateId));

	return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.forker.IForker#getProcess(java.lang.String)
     */
    @Override
    public IProcess getProcess(final String aIsolateId) {
	return pIsolatedProcesses.get(aIsolateId);
    }

    /**
     * Creates the given directory and parent directories if necessary. If
     * aClear is true, removes existing directory content if any, else fails if
     * directory already exists.
     * 
     * @param aPath
     *            Directory path
     * @param aClear
     *            Clear existing content
     * 
     * @return True on success, False on failure
     */
    public boolean makeDirectory(final String aPath, final boolean aClear) {

	File dirPath = new File(aPath);

	if (!dirPath.exists()) {
	    // Create directory and parent directories if needed
	    if (!dirPath.mkdirs()) {
		return false;
	    }

	} else if (!dirPath.isDirectory()) {
	    // Can't touch a file with the same name
	    return false;

	} else {

	    // Test if the directory is empty
	    boolean emptyDir = (dirPath.list().length == 0);

	    if (!emptyDir) {

		if (!aClear) {
		    // We're not authorized to clear this directory
		    return false;

		} else {
		    // Erase it
		    removeDirectory(dirPath);
		}
	    }
	}

	return true;
    }

    /**
     * Asks child class to prepare the future isolate environment
     * 
     * @param aIsolateId
     *            The isolate ID
     * 
     * @return The isolate process configuration
     */
    protected abstract ProcessConfiguration prepareEnvironment(String aIsolateId);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.IReconfigurable#reloadConfiguration(java.
     * lang.String, boolean)
     */
    @Override
    public void reloadConfiguration(final String aPath, final boolean aForce) {

	pConfiguration.loadConfiguration(aPath);
	// TODO reload isolates
    }

    /**
     * Recursively deletes the given directory
     * 
     * @param aDirectory
     *            The directory to be deleted
     */
    public void removeDirectory(final File aDirectory) {

	if (aDirectory.isDirectory()) {

	    File[] dirContent = aDirectory.listFiles();
	    for (File node : dirContent) {

		if (node.isDirectory()) {
		    // Delete sub directory
		    removeDirectory(node);

		} else {
		    // Delete file
		    node.delete();
		}
	    }

	    aDirectory.delete();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.forker.IForker#startProcess(java.lang.String)
     */
    @Override
    public boolean startProcess(final String aIsolateId, final boolean aForce) {

	IProcess oldProcess = pIsolatedProcesses.get(aIsolateId);
	if (oldProcess != null) {
	    // The isolate is already running

	    if (aForce) {
		// Stop it
		oldProcess.stop();

	    } else {
		// We're not authorized to kill it
		return false;
	    }
	}

	// Prepare the isolate directory
	ProcessConfiguration processConfig = prepareEnvironment(aIsolateId);
	IProcess process = ProcessFactory.startProcess(processConfig);
	if (process != null) {
	    pIsolatedProcesses.put(aIsolateId, process);
	}

	return (process != null);
    }
}
