/**
 * File:   IPlatformConfig.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.commons;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes the platform configuration.
 * 
 * @author Thomas Calmant
 */
public class PlatformConfiguration {

    /**
     * Retrieves the java interpreter path, based on java.home property
     * 
     * @return The path to the java interpreter
     */
    public static String getJavaPath() {

	return System.getProperty("java.home") + File.separator + "bin"
		+ File.separator + "java";
    }

    /** Common bundles needed by all isolates */
    private Set<String> pCommonBundles = new HashSet<String>();

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
     * Adds the given bundle to the ones needed by all isolates
     * 
     * @param aBundle
     *            New common bundle
     */
    public void addCommonBundle(final String aBundle) {

	File bundleFile = new File(aBundle);

	if (!bundleFile.exists()) {
	    bundleFile = new File(pRepository, aBundle);
	}

	if (!bundleFile.exists()) {
	    return;
	}

	pCommonBundles.add(bundleFile.getAbsolutePath());
    }

    /**
     * Retrieves bundles needed by all isolates
     * 
     * @return bundles needed by all isolates
     */
    public String[] getCommonBundles() {
	return pCommonBundles.toArray(new String[0]);
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

    /**
     * Removes the given bundle from the ones needed by all isolates
     * 
     * @param aBundle
     *            Removed common bundle
     */
    public void removeCommonBundle(final String aBundle) {

	File bundleFile = new File(aBundle);
	if (!bundleFile.exists()) {
	    pCommonBundles.remove(aBundle);
	    return;
	}

	pCommonBundles.remove(bundleFile.getAbsolutePath());
    }
}
