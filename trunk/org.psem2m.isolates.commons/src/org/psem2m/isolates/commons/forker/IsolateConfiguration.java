/**
 * File:   IIsolateConfiguration.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.commons.forker;

/**
 * Describes an isolate configuration
 * 
 * @author Thomas Calmant
 */
public class IsolateConfiguration {

    /** Needed bundles */
    private String[] pBundles;

    /** Isolate ID */
    private String pIsolateId;

    /** Maximum isolate timeout */
    private int pMaxTimeout;

    /**
     * Same as IsolateConfiguration(aIsolateId, new String[0], -1).
     * 
     * @param aIsolateId
     *            The isolate ID
     */
    public IsolateConfiguration(final String aIsolateId) {
	this(aIsolateId, new String[0], -1);
    }

    /**
     * Sets up the isolate configuration
     * 
     * @param aIsolateId
     *            The isolate ID
     * @param aBundles
     *            Isolate bundles
     * @param aMaxTimeout
     *            Maximum timeout of the isolate
     */
    public IsolateConfiguration(final String aIsolateId,
	    final String[] aBundles, final int aMaxTimeout) {

	pBundles = aBundles;
	pIsolateId = aIsolateId;
	pMaxTimeout = aMaxTimeout;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

	if (pIsolateId == null) {
	    return false;
	}

	if (aObj instanceof IsolateConfiguration) {
	    return pIsolateId.equals(((IsolateConfiguration) aObj).pIsolateId);

	} else if (aObj instanceof CharSequence) {
	    return pIsolateId.equals(aObj);
	}

	return false;
    }

    /**
     * Retrieves an array containing the IDs of bundles needed by the isolate,
     * if any.
     * 
     * Should return an empty array instead of null.
     * 
     * @return An array of bundle IDs
     */
    public String[] getBundles() {
	return pBundles;
    }

    /**
     * Retrieves the isolate ID.
     * 
     * Can't return null.
     * 
     * @return The isolate ID.
     */
    public String getIsolateId() {
	return pIsolateId;
    }

    /**
     * Retrieves the maximum timeout of the isolate before it is considered
     * stuck.
     * 
     * @return The maximum isolate timeout
     */
    public int getMaxTimeout() {
	return pMaxTimeout;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	return pIsolateId.hashCode();
    }
}
