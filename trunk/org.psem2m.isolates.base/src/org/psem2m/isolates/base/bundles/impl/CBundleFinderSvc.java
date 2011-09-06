/**
 * File:   CBundleFinderSvc.java
 * Author: Thomas Calmant
 * Date:   28 juil. 2011
 */
package org.psem2m.isolates.base.bundles.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.base.dirs.IPlatformDirsSvc;

/**
 * Implements the bundle finder, using the platform directories service
 * 
 * @author Thomas Calmant
 */
public class CBundleFinderSvc implements IBundleFinderSvc {

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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.bundles.IBundleFinderSvc#findBundle(java.lang
     * .String[])
     */
    @Override
    public BundleRef findBundle(final String... aBundlePossibleNames) {

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
}
