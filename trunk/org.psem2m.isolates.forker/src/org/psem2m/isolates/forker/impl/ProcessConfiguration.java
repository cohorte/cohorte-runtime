/**
 * File:   ProcessConfiguration.java
 * Author: Thomas Calmant
 * Date:   16 juin 2011
 */
package org.psem2m.isolates.forker.impl;

/**
 * Describes a process launch environment configuration
 * 
 * @author Thomas Calmant
 */
public class ProcessConfiguration {

    /** Process parameters */
    private String[] pArguments;

    /** Process launch environment */
    private String[] pEnvironment;

    /** Process executable full path */
    private String pExecutable;

    /** Process working directory */
    private String pWorkingDirectory;

    /**
     * Retrieves the process arguments
     * 
     * @return the process arguments
     */
    public String[] getArguments() {
	return pArguments;
    }

    /**
     * Retrieves the process launch environment
     * 
     * @return the process launch environment
     */
    public String[] getEnvironment() {
	return pEnvironment;
    }

    /**
     * Retrieves the executable file path
     * 
     * @return the executable file path
     */
    public String getExecutable() {
	return pExecutable;
    }

    /**
     * Retrieves the process working directory
     * 
     * @return the process working directory
     */
    public String getWorkingDirectory() {
	return pWorkingDirectory;
    }

    /**
     * Sets the process arguments
     * 
     * @param aArguments
     *            the process arguments
     */
    public void setArguments(final String[] aArguments) {
	pArguments = aArguments;
    }

    /**
     * Sets the process launch environment
     * 
     * @param aEnvironment
     *            the process launch environment
     */
    public void setEnvironment(final String[] aEnvironment) {
	pEnvironment = aEnvironment;
    }

    /**
     * Sets the executable file path
     * 
     * @param aExecutable
     *            the executable file path
     */
    public void setExecutable(final String aExecutable) {
	pExecutable = aExecutable;
    }

    /**
     * Sets the process working directory
     * 
     * @param aWorkingDirectory
     *            the process working directory
     */
    public void setWorkingDirectory(final String aWorkingDirectory) {
	pWorkingDirectory = aWorkingDirectory;
    }
}
