/**
 * File:   IPlatformConfig.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.commons.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.psem2m.isolates.commons.IBundleRef;
import org.psem2m.isolates.commons.IPlatformConfiguration;
import org.psem2m.isolates.commons.Utilities;
import org.psem2m.utilities.CXOSUtils;

/**
 * Describes the platform configuration.
 * 
 * @author Thomas Calmant
 */
public class PlatformConfiguration implements IPlatformConfiguration {

    /** Common bundles needed by all isolates */
    private Set<String> pCommonBundles = new HashSet<String>();

    /** The platform base directory */
    private String pPlatformBase;

    /** The platform home directory */
    private String pPlatformHome;

    /** The full bundle repository path */
    private String pRepository;

    /**
     * Default constructor uses system properties
     */
    public PlatformConfiguration() {

	pPlatformHome = makeAbsolutePath(System.getProperty("psem2m.home"));
	pPlatformBase = makeAbsolutePath(System.getProperty("psem2m.base"),
		pPlatformHome);

	pRepository = makeAbsolutePath(pPlatformBase + "/repo");
    }

    /**
     * Sets up the platform configuration
     * 
     * @param aPlatformHomePath
     *            The platform home directory
     * @param aRepository
     *            The bundle repository path
     */
    public PlatformConfiguration(final String aPlatformHomePath,
	    final String aRepository) {

	this(aPlatformHomePath, null, aRepository);
    }

    /**
     * Sets up the platform configuration
     * 
     * @param aPlatformHomePath
     *            The platform home directory
     * @param aRepository
     *            The bundle repository path
     */
    public PlatformConfiguration(final String aPlatformHomePath,
	    final String aPlatformBasePath, final String aRepository) {

	pPlatformHome = makeAbsolutePath(aPlatformHomePath);
	pPlatformBase = makeAbsolutePath(aPlatformBasePath, pPlatformHome);
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
     * org.psem2m.isolates.commons.IPlatformConfiguration#getCommonBundlesRef()
     */
    @Override
    public IBundleRef[] getCommonBundlesRef() {

	List<IBundleRef> refs = new ArrayList<IBundleRef>();

	for (String bundleName : pCommonBundles) {
	    IBundleRef bundleRef = Utilities.findBundle(this, bundleName);
	    if (bundleRef != null) {
		refs.add(bundleRef);
	    }
	}

	return refs.toArray(new IBundleRef[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.IPlatformConfiguration#getForkerStartCommand
     * ()
     */
    @Override
    public List<String> getForkerStartCommand() {

	List<String> command = new ArrayList<String>();

	if (CXOSUtils.isOsUnixFamily()) {
	    // The interpreter
	    command.add("/bin/bash");

	    // The file
	    command.add(pPlatformHome + "/var/run_forker.sh");

	} else {
	    // TODO: the windows version
	    return null;
	}

	return command;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.IPlatformConfiguration#getIsolatesDirectory()
     */
    @Override
    public String getIsolatesDirectory() {
	return pPlatformHome + File.separator + "work";
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
     * org.psem2m.isolates.commons.IPlatformConfiguration#getPlatformBaseDirectory
     * ()
     */
    @Override
    public String getPlatformBaseDirectory() {
	return pPlatformBase;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.commons.IPlatformConfiguration#getPlatformDirectory()
     */
    @Override
    public String getPlatformHomeDirectory() {
	return pPlatformHome;
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
     *            Directory to transform
     * @return The absolute directory path or the user home
     */
    protected String makeAbsolutePath(final String aDirectory) {
	return makeAbsolutePath(aDirectory, System.getProperty("user.home"));
    }

    /**
     * Returns the absolute path for the given directory if it exists, else
     * returns the given default value
     * 
     * @param aDirectory
     *            Directory path to be transformed
     * @param aDefaultValue
     *            Default value to use if the given directory name is null or
     *            doesn't point to a directory
     * @return The absolute directory path or the user home
     */
    protected String makeAbsolutePath(final String aDirectory,
	    final String aDefaultValue) {

	if (aDirectory == null) {
	    return aDefaultValue;
	}

	File directory = new File(aDirectory);
	if (directory.isDirectory()) {
	    return directory.getAbsolutePath();

	} else {
	    return aDefaultValue;
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
