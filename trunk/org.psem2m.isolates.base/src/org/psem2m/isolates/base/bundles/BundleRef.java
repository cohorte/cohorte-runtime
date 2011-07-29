/**
 * File:   BundleRef.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.base.bundles;

import java.net.URI;

import org.psem2m.utilities.files.CXFile;

/**
 * Describes a bundle reference
 * 
 * @author Thomas Calmant
 */
public class BundleRef {

    /** The bundle file */
    private CXFile pBundleFile;

    /** The bundle name */
    private String pBundleName;

    /** The bundle URI */
    private URI pBundleUri;

    /**
     * File constructor
     * 
     * @param aBundleName
     *            The bundle name
     * @param aBundleFile
     *            The bundle file
     */
    public BundleRef(final String aBundleName, final CXFile aBundleFile) {
	pBundleFile = aBundleFile;
	pBundleName = aBundleName;
	pBundleUri = aBundleFile.toURI();
    }

    /**
     * URI Constructor
     * 
     * @param aBundleName
     *            The bundle name
     * @param aBundleUri
     *            The bundle URI
     */
    public BundleRef(final String aBundleName, final URI aBundleUri) {
	pBundleName = aBundleName;
	pBundleUri = aBundleUri;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IBundleRef#getFile()
     */
    public CXFile getFile() {
	return pBundleFile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IBundleRef#getName()
     */
    public String getName() {
	return pBundleName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IBundleRef#getUri()
     */
    public URI getUri() {
	return pBundleUri;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return pBundleUri.toString();
    }
}
