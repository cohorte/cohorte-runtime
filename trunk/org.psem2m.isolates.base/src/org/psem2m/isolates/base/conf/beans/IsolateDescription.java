/**
 * File:   IsolateDescription.java
 * Author: Thomas Calmant
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.base.conf.beans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.psem2m.isolates.base.conf.IBundleDescr;
import org.psem2m.isolates.base.conf.IIsolateDescr;

/**
 * Description of an isolate
 * 
 * @author Thomas Calmant
 */
public class IsolateDescription implements IIsolateDescr {

    /** Serializable version */
    private static final long serialVersionUID = 1L;

    /** Isolate bundles (can't be null) */
    private final Set<IBundleDescr> pBundles = new HashSet<IBundleDescr>();

    /** Isolate ID */
    private final String pIsolateId;

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
     * @see org.psem2m.isolates.config.json.IIsolateDescr#getVMArgs()
     */
    @Override
    public List<String> getVMArgs() {
	return pVmArguments;
    }
}
