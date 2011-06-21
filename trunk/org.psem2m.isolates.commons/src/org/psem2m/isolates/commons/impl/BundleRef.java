/**
 * File:   BundleRef.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.commons.impl;

import java.io.File;
import java.net.URI;

import org.psem2m.isolates.commons.IBundleRef;

/**
 * Describes a bundle reference
 * 
 * @author Thomas Calmant
 */
public class BundleRef implements IBundleRef {

    /** The bundle file */
    private File pBundleFile;

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
    public BundleRef(final String aBundleName, final File aBundleFile) {
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
    @Override
    public File getFile() {
	return pBundleFile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IBundleRef#getName()
     */
    @Override
    public String getName() {
	return pBundleName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IBundleRef#getUri()
     */
    @Override
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
