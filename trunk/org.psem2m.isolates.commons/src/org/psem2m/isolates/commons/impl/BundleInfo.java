/**
 * 
 */
package org.psem2m.isolates.commons.impl;

import org.osgi.framework.Bundle;
import org.psem2m.isolates.commons.IBundleInfo;

/**
 * Represents a serializable bundle information structure
 * 
 * @author Thomas Calmant
 */
public class BundleInfo implements IBundleInfo {

    /** Serialization version */
    private static final long serialVersionUID = 1L;

    /** Bundle UID */
    private long pId = -1;

    /** Bundle location */
    private String pLocation = null;

    /** Bundle symbolic name */
    private String pName = null;

    /** Bundle state */
    private int pState = -1;

    /** Bundle version */
    private String pVersion = null;

    /**
     * Sets up a bundle information structure
     * 
     * @param aBundle
     *            Bundle to read
     */
    public BundleInfo(final Bundle aBundle) {

        if (aBundle != null) {

            pId = aBundle.getBundleId();
            pName = aBundle.getSymbolicName();
            pState = aBundle.getState();
            pLocation = aBundle.getLocation();
            pVersion = aBundle.getVersion().toString();
        }
    }

    /**
     * Sets up a bundle information structure
     * 
     * @param aId
     *            Bundle's ID
     * @param aName
     *            Bundle's symbolic name
     * @param aState
     *            Bundle's state
     * @param aLocation
     *            Bundle's location
     * @param aVersion
     *            Bundle's version
     */
    public BundleInfo(final long aId, final String aName, final int aState,
            final String aLocation, final String aVersion) {

        pId = aId;
        pName = aName;
        pState = aState;
        pLocation = aLocation;
        pVersion = aVersion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IBundleInfo#getId()
     */
    @Override
    public long getId() {
        return pId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IBundleInfo#getLocation()
     */
    @Override
    public String getLocation() {
        return pLocation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IBundleInfo#getState()
     */
    @Override
    public int getState() {
        return pState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IBundleInfo#getSymbolicName()
     */
    @Override
    public String getSymbolicName() {
        return pName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IBundleInfo#getVersion()
     */
    @Override
    public String getVersion() {
        return pVersion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("BundleInfo(Id: ");
        builder.append(pId);
        builder.append(", Name: ");
        builder.append(pName);
        builder.append(", Version: ");
        builder.append(pVersion);
        builder.append(", State: ");
        builder.append(pState);
        builder.append(", Location: '");
        builder.append(pLocation);
        builder.append("')");

        return builder.toString();
    }
}
