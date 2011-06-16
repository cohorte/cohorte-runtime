/**
 *
 */
package org.psem2m.isolates.forker.impl;

import java.io.File;

import org.psem2m.isolates.forker.IForker;

/**
 * Loads and stores the forker configuration file
 */
public class ForkerConfiguration {

    /** Isolate directory pattern */
    private String pIsolateDirectoryPattern;

    /** Forker working directory */
    private String pWorkingDirectory;

    public String getFelixJar() {
	return System.getProperty("user.home") + "/felix.jar";
    }

    /**
     * Retrieves the isolate directory pattern
     * 
     * @return the isolate directory pattern
     */
    public String getIsolateDirectoryPattern() {
	return pIsolateDirectoryPattern;
    }

    /**
     * Retrieves the full path to the java executable
     * 
     * @return The full path to 'java'
     */
    public String getJava() {

	StringBuilder builder = new StringBuilder();
	builder.append(System.getProperty("java.home"));
	builder.append(File.separator);
	builder.append("/bin/java");

	if (isWindows()) {
	    builder.append(".exe");
	}

	return builder.toString();
    }

    /**
     * Retrieves the forker working directory
     * 
     * @return the forker working directory
     */
    public String getWorkingDirectory() {
	return pWorkingDirectory;
    }

    /**
     * Tests if we are running on Windows
     * 
     * @return True if we are running on Windows
     */
    public boolean isWindows() {
	return System.getProperty("os.name").toLowerCase().startsWith("win");
    }

    /**
     * Reads and stores the given configuration file
     * 
     * @param aConfigurationFile
     *            A configuration file
     */
    public void loadConfiguration(final String aConfigurationFile) {
	// TODO
	pWorkingDirectory = "./";
	pIsolateDirectoryPattern = "isolate-"
		+ IForker.ISOLATE_DIRECTORY_VARIABLE;
    }
}