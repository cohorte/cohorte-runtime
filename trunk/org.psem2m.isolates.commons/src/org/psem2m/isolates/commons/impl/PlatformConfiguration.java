/**
 * File:   IPlatformConfig.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.commons.impl;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.psem2m.isolates.commons.IPlatformConfiguration;

/**
 * Describes the platform configuration.
 * 
 * @author Thomas Calmant
 */
public class PlatformConfiguration implements IPlatformConfiguration {

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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.IPlatformConfiguration#addCommonBundle(java
     * .lang.String)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.IPlatformConfiguration#getCommonBundles()
     */
    @Override
    public String[] getCommonBundles() {
	return pCommonBundles.toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.IPlatformConfiguration#getIsolatesDirectory()
     */
    @Override
    public String getIsolatesDirectory() {
	return pPlatformDirectory + File.separator + "work";
    }

    /**
     * Retrieves the java interpreter path, based on java.home property
     * 
     * @return The path to the java interpreter
     */
    @Override
    public String getJavaExecutable() {

	StringBuilder javaExecutablePath = new StringBuilder();
	javaExecutablePath.append(System.getProperty("java.home"));
	javaExecutablePath.append(File.separator);
	javaExecutablePath.append("bin");
	javaExecutablePath.append(File.separator);
	javaExecutablePath.append("java");

	if (System.getProperty("os.family", "").startsWith("win")) {
	    javaExecutablePath.append(".exe");
	}

	return javaExecutablePath.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.IPlatformConfiguration#getPlatformDirectory()
     */
    @Override
    public String getPlatformDirectory() {
	return pPlatformDirectory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.IPlatformConfiguration#getRepositoryDirectory
     * ()
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.IPlatformConfiguration#removeCommonBundle
     * (java.lang.String)
     */
    @Override
    public void removeCommonBundle(final String aBundle) {

	File bundleFile = new File(aBundle);
	if (!bundleFile.exists()) {
	    pCommonBundles.remove(aBundle);
	    return;
	}

	pCommonBundles.remove(bundleFile.getAbsolutePath());
    }
}
