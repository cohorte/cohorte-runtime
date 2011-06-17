/**
 * File:   IPlatformConfig.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.commons;

import java.io.File;

/**
 * Describes the platform configuration.
 * 
 * @author Thomas Calmant
 */
public class PlatformConfiguration {

    /** The platform home directory */
    private String pPlatformDirectory;

    /** The full bundle repository path */
    private String pRepository;

    /**
     * Sets up the platform configuration
     * 
     * @param aPlatformPath
     *            The platform home directory
     * @param aRepository
     *            The bundle repository path
     */
    public PlatformConfiguration(final String aPlatformPath,
	    final String aRepository) {

	pPlatformDirectory = makeAbsolutePath(aPlatformPath);
	pRepository = makeAbsolutePath(aRepository);
    }

    /**
     * Retrieves the platform home directory
     * 
     * @return The platform home directory
     */
    public String getPlatformDirectory() {
	return pPlatformDirectory;
    }

    /**
     * Retrieves the <b>full</b> path of the platform bundles repository.
     * 
     * @return The full path of the platform bundles repository.
     */
    public String getRepositoryDirectory() {
	return pRepository;
    }

    /**
     * Returns the absolute path for the given directory if it exists, else
     * returns the user home directory
     * 
     * @param aDirectory
     *            Directory path to be transformed
     * @return The absolute directory path or the user home
     */
    protected String makeAbsolutePath(final String aDirectory) {

	File directory = new File(aDirectory);
	if (directory.isDirectory()) {
	    return directory.getAbsolutePath();

	} else {
	    return System.getProperty("user.home");
	}
    }
}
