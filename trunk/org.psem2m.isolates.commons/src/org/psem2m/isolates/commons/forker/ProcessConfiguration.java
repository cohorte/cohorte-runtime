/**
 * File:   IProcessConfiguration.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.commons.forker;

import java.util.Map;

/**
 * Description of a process configuration to parameter the Forker service
 * 
 * @author Thomas Calmant
 */
public class ProcessConfiguration {

    /** The command array */
    private String[] pCommandArray;

    /** The process environment */
    private Map<String, String> pEnvironment;

    /** The isolate configuration */
    private IsolateConfiguration pIsolateConfiguration;

    /** The process working directory */
    private String pWorkingDirectory;

    /**
     * Equivalent to ProcessConfiguration(aCommandArray, null, null,
     * aIsolateConfiguration).
     * 
     * @param aCommandArray
     *            The command array
     * @param aIsolateConfiguration
     *            The isolate configuration (with a valid isolate ID)
     */
    public ProcessConfiguration(final String[] aCommandArray,
	    final IsolateConfiguration aIsolateConfiguration) {

	this(aCommandArray, null, null, aIsolateConfiguration);
    }

    /**
     * Sets up the process configuration
     * 
     * @param aCommandArray
     *            The command array
     * @param aEnvironment
     *            The process environment
     * @param aWorkingDirectory
     *            The process working directory
     * @param aIsolateConfiguration
     *            The isolate configuration (with a valid isolate ID)
     */
    public ProcessConfiguration(final String[] aCommandArray,
	    final Map<String, String> aEnvironment,
	    final String aWorkingDirectory,
	    final IsolateConfiguration aIsolateConfiguration) {

	pCommandArray = aCommandArray;
	pEnvironment = aEnvironment;
	pWorkingDirectory = aWorkingDirectory;
	pIsolateConfiguration = aIsolateConfiguration;
    }

    /**
     * Retrieves the command array, directly usable with
     * {@link Runtime#exec(String[])}.
     * 
     * Must never return null. The array must always contain at least 1 element,
     * the executable full path.
     * 
     * @return The command array
     */
    public String[] getCommandArray() {
	return pCommandArray;
    }

    /**
     * Retrieves the environment of the future process.
     * 
     * Can be null.
     * 
     * @return the environment of the future process
     */
    public Map<String, String> getEnvironment() {
	return pEnvironment;
    }

    /**
     * Retrieves the future isolate configuration
     * 
     * @return The future isolate configuration
     */
    public IsolateConfiguration getIsolateConfiguration() {
	return pIsolateConfiguration;
    }

    /**
     * Retrieves the working directory of the future process.
     * 
     * Can be null. Can be inexistent.
     * 
     * @return The working directory of the future process
     */
    public String getWorkingDirectory() {
	return pWorkingDirectory;
    }
}
