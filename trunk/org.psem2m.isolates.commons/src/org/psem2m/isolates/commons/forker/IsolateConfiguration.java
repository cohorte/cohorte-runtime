/**
 * File:   IIsolateConfiguration.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.commons.forker;

import java.util.Map;
import java.util.TreeMap;

import org.psem2m.isolates.base.bundles.BundleRef;
import org.psem2m.isolates.commons.IIsolateConfiguration;

/**
 * Describes an isolate configuration
 * 
 * @author Thomas Calmant
 */
public class IsolateConfiguration implements IIsolateConfiguration {

    /** Isolate process arguments */
    private String[] pArguments;

    /** Needed bundles */
    private BundleRef[] pBundles;

    /** Isolate process environment */
    private Map<String, String> pEnvironment;

    /** Isolate ID */
    private String pIsolateId;

    /** Kind of isolate */
    private IsolateKind pKind;

    /**
     * Sets up an isolate without extra bundles
     * 
     * @param aIsolateId
     *            The isolate ID
     * @param aKind
     *            The kind of isolate
     */
    public IsolateConfiguration(final String aIsolateId, final IsolateKind aKind) {
	this(aIsolateId, aKind, new BundleRef[0], null);
    }

    /**
     * Sets up the isolate configuration
     * 
     * @param aIsolateId
     *            The isolate ID
     * @param aKind
     *            The kind of isolate
     * @param aBundles
     *            Isolate bundles
     */
    public IsolateConfiguration(final String aIsolateId,
	    final IsolateKind aKind, final BundleRef[] aBundles) {
	this(aIsolateId, aKind, aBundles, null);
    }

    /**
     * Sets up the isolate configuration
     * 
     * @param aIsolateId
     *            The isolate ID
     * @param aKind
     *            The kind of isolate
     * @param aBundles
     *            Isolate bundles
     * @param aArguments
     *            Arguments for the isolate process
     */
    public IsolateConfiguration(final String aIsolateId,
	    final IsolateKind aKind, final BundleRef[] aBundles,
	    final String[] aArguments) {

	pArguments = aArguments;
	pBundles = aBundles;
	pIsolateId = aIsolateId;
	pKind = aKind;
	pEnvironment = new TreeMap<String, String>();
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

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IIsolateConfiguration#getArguments()
     */
    @Override
    public String[] getArguments() {
	return pArguments;
    }

    /**
     * Retrieves an array containing the IDs of bundles needed by the isolate,
     * if any.
     * 
     * Should return an empty array instead of null.
     * 
     * @return An array of bundle IDs
     */
    @Override
    public BundleRef[] getBundles() {
	return pBundles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IIsolateConfiguration#getEnvironment()
     */
    @Override
    public Map<String, String> getEnvironment() {
	return pEnvironment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IIsolateConfiguration#getId()
     */
    @Override
    public String getId() {
	return pIsolateId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.commons.IIsolateConfiguration#getKind()
     */
    @Override
    public IsolateKind getKind() {
	return pKind;
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
