/**
 * File:   IProcessConfiguration.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.commons.forker;

import java.util.Map;
import java.util.TreeMap;

/**
 * Description of a process configuration to parameter the Forker service
 * 
 * @author Thomas Calmant
 */
public class ProcessConfiguration {

    /** The isolate ID environment variable */
    public static final String ISOLATE_ID = "psem2m.isolate.id";

    /** Isolate ID variable in environment values */
    public static final CharSequence ISOLATE_VARIABLE = "${isolate.id}";

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

	fillEnvironment();
    }

    /**
     * Sets up the common environment variables and replaces variables.
     */
    private void fillEnvironment() {

	if (pEnvironment == null) {
	    pEnvironment = new TreeMap<String, String>();
	}

	final String isolateId = pIsolateConfiguration.getIsolateId();

	for (String key : pEnvironment.keySet()) {

	    String value = pEnvironment.get(key);
	    value = value.replace(ISOLATE_VARIABLE, isolateId);
	    pEnvironment.put(key, value);
	}

	pEnvironment.put(ISOLATE_ID, isolateId);
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
