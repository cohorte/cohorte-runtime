/**
 * File:   IsolateDescription.java
 * Author: Thomas Calmant
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.base.conf.beans;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.psem2m.isolates.services.conf.IBundleDescr;
import org.psem2m.isolates.services.conf.IIsolateDescr;

/**
 * Description of an isolate
 * 
 * @author Thomas Calmant
 */
public class IsolateDescription implements IIsolateDescr {

    /** Serializable version */
    private static final long serialVersionUID = 1L;

    /** Isolate bundles (can't be null) */
    private final Set<IBundleDescr> pBundles = new LinkedHashSet<IBundleDescr>();

    /** Isolate ID */
    private final String pIsolateId;

    /** Isolate kind, must never be null */
    private String pIsolateKind = "";

    /** Isolate Java VM arguments (can't be null, must be ordered) */
    private final List<String> pVmArguments = new ArrayList<String>();

    /**
     * Sets up the isolate description
     * 
     * @param aIsolateId
     */
    public IsolateDescription(final String aIsolateId) {
	pIsolateId = aIsolateId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.json.IIsolateDescr#getBundles()
     */
    @Override
    public Set<IBundleDescr> getBundles() {
	return pBundles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.json.IIsolateDescr#getId()
     */
    @Override
    public String getId() {
	return pIsolateId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.conf.IIsolateDescr#getKind()
     */
    @Override
    public String getKind() {
	return pIsolateKind;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.json.IIsolateDescr#getVMArgs()
     */
    @Override
    public List<String> getVMArgs() {
	return pVmArguments;
    }

    /**
     * Sets the kind of isolate
     * 
     * @param aKind
     *            The kind of the isolate
     * 
     * @see IIsolateDescr#getKind()
     */
    public void setKind(final String aKind) {

	if (aKind != null) {
	    pIsolateKind = aKind;

	} else {
	    pIsolateKind = "";
	}
    }
}
