/**
 * File:   BundleRef.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.base.bundles;

import java.io.File;
import java.net.URI;

/**
 * Describes a bundle reference
 * 
 * @author Thomas Calmant
 */
public class BundleRef {

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
     *            The bundle file (can't be null)
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
     *            The bundle URI (can't be null)
     */
    public BundleRef(final String aBundleName, final URI aBundleUri) {

        pBundleName = aBundleName;
        pBundleUri = aBundleUri;
    }

    /**
     * Retrieves the bundle File (if any)
     * 
     * @return The bundle File, or null
     */
    public File getFile() {

        return pBundleFile;
    }

    /**
     * Retrieves the bundle name
     * 
     * @return The bundle name
     */
    public String getName() {

        return pBundleName;
    }

    /**
     * Retrieves the bundle URI
     * 
     * @return The bundle URI
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
