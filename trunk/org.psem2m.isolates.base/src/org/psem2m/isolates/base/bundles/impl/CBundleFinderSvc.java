/**
 * File:   CBundleFinderSvc.java
 * Author: Thomas Calmant
 * Date:   28 juil. 2011
 */
package org.psem2m.isolates.base.bundles.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;

/**
 * Implements the bundle finder, using the platform directories service
 * 
 * @author Thomas Calmant
 */
public class CBundleFinderSvc implements IBundleFinderSvc {

    /** Bundle files cache : symbolic name -&gt; file */
    private final Map<String, File> pBundlesCache = new HashMap<String, File>();

    /** Platform directories service */
    private IPlatformDirsSvc pPlatformDirsSvc;

    /**
     * Default constructor
     */
    public CBundleFinderSvc() {
	super();
    }

    /**
     * Constructor for non-injected usage
     * 
     * @param aPlatformDirsSvc
     *            A platform service instance
     */
    public CBundleFinderSvc(final IPlatformDirsSvc aPlatformDirsSvc) {
	pPlatformDirsSvc = aPlatformDirsSvc;
    }

    /**
     * Finds all bundles in the known repositories and stores their path and
     * symbolic names
     */
    protected synchronized void findAllBundles() {

	// Clear existing cache
	pBundlesCache.clear();

	// Do the job
	for (File repository : pPlatformDirsSvc.getRepositories()) {
	    findAllBundles(repository);
	}
    }

    /**
     * Completes the given list with all files having the extension ".jar"
     * 
     * @param aRootDirectory
     *            Root search directory
     */
    protected void findAllBundles(final File aRootDirectory) {

	File[] repoFiles = aRootDirectory.listFiles();
	if (repoFiles != null) {

	    for (File file : repoFiles) {

		if (file.isFile()
			&& file.getName().toLowerCase().endsWith(".jar")) {
		    // Try to read it
		    String symbolicName = getBundleSymbolicName(file);
		    if (symbolicName != null) {
			// Store it
			pBundlesCache.put(symbolicName, file);
		    }

		} else {
		    // Recursive search
		    findAllBundles(file);
		}
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.bundles.IBundleFinderSvc#findBundle(java.lang
     * .String[])
     */
    @Override
    public BundleRef findBundle(final String... aBundlePossibleNames) {

	// Fill the cache, if needed
	if (pBundlesCache.isEmpty()) {
	    findAllBundles();
	}

	// Try with cache first
	for (String bundleName : aBundlePossibleNames) {
	    if (pBundlesCache.containsKey(bundleName)) {
		return new BundleRef(bundleName, pBundlesCache.get(bundleName));
	    }
	}

	// Look in each repository
	for (File repository : pPlatformDirsSvc.getRepositories()) {

	    if (!repository.exists() || !repository.isDirectory()) {
		continue;
	    }

	    // Look for each possible name
	    for (String bundleName : aBundlePossibleNames) {

		File bundleFile = new File(repository, bundleName);
		if (bundleFile.exists()) {
		    // Stop on first bundle found
		    return new BundleRef(bundleName, bundleFile);
		}
	    }
	}

	// Bundle not found in repositories, tries the names as full ones
	for (String bundleName : aBundlePossibleNames) {

	    // Try 'local' file
	    File bundleFile = new File(bundleName);
	    if (bundleFile.exists()) {
		// Stop on first bundle found
		return new BundleRef(bundleName, bundleFile);
	    }

	    // Try as a URI
	    try {
		URI bundleUri = new URI(bundleName);
		URL bundleUrl = bundleUri.toURL();

		if (bundleUrl.getProtocol().equals("file")) {

		    bundleFile = new File(bundleUri.getPath());
		    if (bundleFile.exists()) {
			return new BundleRef(bundleName, bundleFile);
		    }
		}

	    } catch (MalformedURLException e) {
		// Do nothing, we're determining the kind of element
	    } catch (URISyntaxException e) {
		// Do nothing, we're determining the kind of element
	    } catch (IllegalArgumentException e) {
		// Do nothing, the URI is not absolute
	    }
	}

	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.bundles.IBundleFinderSvc#getBootstrap()
     */
    @Override
    public File getBootstrap() {

	final BundleRef bootRef = findBundle(BOOTSTRAP_SYMBOLIC_NAME);
	if (bootRef == null) {
	    // Bootstrap not found
	    return null;
	}

	return bootRef.getFile();
    }

    /**
     * Retrieves the bundle symbolic name from the manifest of the given file,
     * null if unreadable
     * 
     * @param file
     *            A bundle Jar file
     * @return The bundle symbolic name, null if unreadable
     */
    public String getBundleSymbolicName(final File file) {

	if (!file.exists() || !file.isFile()) {
	    // Ignore invalid files
	    return null;
	}

	try {
	    JarFile jarFile = new JarFile(file);
	    Manifest jarManifest = jarFile.getManifest();

	    if (jarManifest == null) {
		// Ignore files without Manifest
		return null;
	    }

	    Attributes attributes = jarManifest.getMainAttributes();
	    if (attributes != null) {

		// Handle symbolic name format
		String symbolicName = attributes
			.getValue(Constants.BUNDLE_SYMBOLICNAME);
		if (symbolicName != null) {

		    // Test if there is an extra information (version,
		    // singleton, ...)
		    int endOfName = symbolicName.indexOf(';');
		    if (endOfName == -1) {
			return symbolicName;
		    }

		    // Only return the symbolic name part
		    return symbolicName.substring(0, endOfName);
		}
	    }

	} catch (IOException ex) {
	    // Ignore
	}

	return null;
    }
}
