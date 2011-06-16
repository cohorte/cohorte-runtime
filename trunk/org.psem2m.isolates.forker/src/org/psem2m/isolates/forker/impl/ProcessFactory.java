/**
 *
 */
package org.psem2m.isolates.forker.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.psem2m.isolates.forker.IProcess;

/**
 * Launches a process according to the current operating system
 */
public class ProcessFactory {

    /**
     * Utility method to run a process from the given process configuration
     * 
     * @param aProcessConfig
     *            The process configuration
     * @return The launched process information, null on error
     */
    public static IProcess startProcess(
	    final ProcessConfiguration aProcessConfig) {

	return startProcess(aProcessConfig.getExecutable(),
		aProcessConfig.getArguments(),
		aProcessConfig.getWorkingDirectory());
    }

    /**
     * Starts a process
     * 
     * @param aExecutable
     *            The executable path
     * @param aArguments
     *            The process arguments
     * @param aWorkingDirectory
     *            The process working directory
     * @return The launched process information, null on error
     */
    public static IProcess startProcess(final String aExecutable,
	    final String[] aArguments, final String aWorkingDirectory) {

	// Make the command array
	List<String> commandList = new ArrayList<String>();
	commandList.add(aExecutable);

	if (aArguments != null) {
	    for (String argument : aArguments) {
		commandList.add(argument);
	    }
	}

	// Compute the working directory
	File workingDirectory = new File(aWorkingDirectory);

	try {
	    workingDirectory = workingDirectory.getCanonicalFile();

	} catch (IOException e) {
	    e.printStackTrace();
	    System.err.println("Invalid working directory");
	    return null;
	}

	if (!workingDirectory.isDirectory()) {
	    System.err.println("Invalid working directory");
	}

	String[] cmdArray = commandList.toArray(new String[0]);

	Process process;
	try {
	    process = Runtime.getRuntime().exec(cmdArray, null,
		    workingDirectory);

	    if (process == null) {
		return null;
	    }

	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}

	return new UnixProcess(process);
    }
}