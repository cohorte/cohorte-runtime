/**
 * File:   CBundleFinderSvc.java
 * Author: Thomas Calmant
 * Date:   28 juil. 2011
 */
package org.psem2m.isolates.base.bundles.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.CPojoBase;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.base.bundles.IBundleFinderSvc;
import org.psem2m.isolates.base.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.files.CXFile;
import org.psem2m.utilities.files.CXFileDir;

/**
 * Implements the bundle finder, using the platform directories service
 * 
 * @author Thomas Calmant
 */
public class CBundleFinderSvc extends CPojoBase implements IBundleFinderSvc {

    /** Service reference managed by iPojo (see metadata.xml) **/
    private IIsolateLoggerSvc pIsolateLoggerSvc;

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
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {
	// ...
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
	for (CXFileDir repository : pPlatformDirsSvc.getRepositories()) {

	    if (!repository.exists() || !repository.isDirectory()) {
		continue;
	    }

	    // Look for each possible name
	    for (String bundleName : aBundlePossibleNames) {

		CXFile bundleFile = new CXFile(repository, bundleName);
		if (bundleFile.exists()) {
		    // Stop on first bundle found
		    return new BundleRef(bundleName, bundleFile);
		}
	    }
	}

	// Bundle not found in repositories, tries the names as full ones
	for (String bundleName : aBundlePossibleNames) {

	    // Try 'local' file
	    CXFile bundleFile = new CXFile(bundleName);
	    if (bundleFile.exists()) {
		// Stop on first bundle found
		return new BundleRef(bundleName, bundleFile);
	    }

	    // Try as a URI
	    try {
		URI bundleUri = new URI(bundleName);
		URL bundleUrl = bundleUri.toURL();

		if (bundleUrl.getProtocol().equals("file")) {

		    bundleFile = new CXFile(bundleUri.getPath());
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
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {

	// logs in the bundle output
	pIsolateLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
		toDescription());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

	// logs in the bundle output
	pIsolateLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
		toDescription());
    }
}
