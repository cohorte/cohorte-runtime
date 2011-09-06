/**
 * File:   ApplicationDescription.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.isolates.config.json.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.psem2m.isolates.config.json.IApplicationDescr;
import org.psem2m.isolates.config.json.IIsolateDescr;

/**
 * Description of an application
 * 
 * @author Thomas Calmant
 */
public class ApplicationDescription implements IApplicationDescr {

    /** Serializable version */
    private static final long serialVersionUID = 1L;

    /** The application ID */
    private String pApplicationId;

    /** Isolates ID -> Configuration map */
    private final Map<String, IIsolateDescr> pIsolates = new HashMap<String, IIsolateDescr>();

    /**
     * Sets up the description
     * 
     * @param aId
     *            The application ID
     */
    public ApplicationDescription(final String aId) {
	pApplicationId = aId;
    }

    /**
     * Adds the given isolate description to the application
     * 
     * @param aIsolateDescription
     *            An isolate
     */
    public void addIsolate(final IIsolateDescr aIsolateDescription) {
	pIsolates.put(aIsolateDescription.getId(), aIsolateDescription);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.json.IApplicationDescr#getApplicationId()
     */
    @Override
    public String getApplicationId() {
	return pApplicationId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.config.json.IApplicationDescr#getIsolate(java.lang
     * .String)
     */
    @Override
    public IIsolateDescr getIsolate(final String aId) {
	return pIsolates.get(aId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.json.IApplicationDescr#getIsolateIds()
     */
    @Override
    public Set<String> getIsolateIds() {
	return pIsolates.keySet();
    }
}
