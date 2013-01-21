/**
 * File:   ApplicationDescription.java
 * Author: Thomas Calmant
 * Date:   6 sept. 2011
 */
package org.psem2m.isolates.services.conf.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Description of an application
 * 
 * @author Thomas Calmant
 */
public class ApplicationDescription implements Serializable {

    /** Serializable version */
    private static final long serialVersionUID = 1L;

    /** The application ID */
    private String pApplicationId;

    /** Isolates ID -> Configuration map */
    private Map<String, IsolateDescription> pIsolates = new HashMap<String, IsolateDescription>();

    /** The multicast address */
    private String pMulticast;

    /** The application multicast port (default: 42000) */
    private int pMulticastPort = 42000;

    /**
     * Default constructor
     */
    public ApplicationDescription() {

        // Do nothing
    }

    /**
     * Configuration constructor
     * 
     * @param aApplicationId
     *            The application ID
     */
    public ApplicationDescription(final String aApplicationId) {

        pApplicationId = aApplicationId;
    }

    /**
     * Adds the given isolate description to the application
     * 
     * @param aIsolateDescription
     *            An isolate
     */
    public void addIsolate(final IsolateDescription aIsolateDescription) {

        pIsolates.put(aIsolateDescription.getUID(), aIsolateDescription);
    }

    /**
     * Retrieves the ID of this described application
     * 
     * @return The application ID
     */
    public String getApplicationId() {

        return pApplicationId;
    }

    /**
     * Retrieves the description of the given isolate, null if not unknown
     * 
     * @param aId
     *            An isolate ID
     * @return the description of the given isolate, or null
     */
    public IsolateDescription getIsolate(final String aId) {

        return pIsolates.get(aId);
    }

    /**
     * Retrieves all known isolates IDs
     * 
     * @return all known isolates IDs
     */
    public Set<String> getIsolateIds() {

        return pIsolates.keySet();
    }

    /**
     * Retrieves the application multicast address
     * 
     * @return the application multicast address
     */
    public String getMulticastGroup() {

        return pMulticast;
    }

    /**
     * Retrieves the application multicast port
     * 
     * @return the application multicast port
     */
    public int getMulticastPort() {

        return pMulticastPort;
    }

    /**
     * @param aApplicationId
     *            the applicationId to set
     */
    public void setApplicationId(final String aApplicationId) {

        pApplicationId = aApplicationId;
    }

    /**
     * @param aIsolates
     *            the isolates to set
     */
    public void setIsolates(final Map<String, IsolateDescription> aIsolates) {

        pIsolates = aIsolates;
    }

    /**
     * Sets up the application multicast address
     * 
     * @param aMulticast
     *            the application multicast address
     */
    public void setMulticastGroup(final String aMulticast) {

        pMulticast = aMulticast;
    }

    /**
     * Sets up the application multicast port
     * 
     * @param aMulticastPort
     *            the application multicast port
     */
    public void setMulticastPort(final int aMulticastPort) {

        if (aMulticastPort > 0) {
            pMulticastPort = aMulticastPort;
        }
    }
}
