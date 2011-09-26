/**
 * File:   StoredSignal.java
 * Author: Thomas Calmant
 * Date:   23 sept. 2011
 */
package org.psem2m.signals;

import java.io.Serializable;

import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;

/**
 * Stored signal description, when the signal sender waits for a provider
 * 
 * @author Thomas Calmant
 */
public class StoredSignal {

    /** Signal data */
    private final Serializable pSignalData;

    /** Signal name */
    private final String pSignalName;

    /** Target isolate ID */
    private final String pTargetId;

    /** Signal targets */
    private final ISignalBroadcaster.EEmitterTargets pTargets;

    /**
     * Sets up the stored signal
     * 
     * @param aTargets
     *            Signal targets
     * @param aSignalName
     *            Signal name
     * @param aSignalData
     *            Signal content
     */
    public StoredSignal(final ISignalBroadcaster.EEmitterTargets aTargets,
            final String aSignalName, final Serializable aSignalData) {

        pTargets = aTargets;
        pSignalName = aSignalName;
        pSignalData = aSignalData;

        // Targets specified
        pTargetId = null;
    }

    /**
     * Sets up the stored signal
     * 
     * @param aTargetIsolateId
     *            The targeted isolate ID
     * @param aSignalName
     *            The signal name
     * @param aSignalData
     *            The signal content
     */
    public StoredSignal(final String aTargetIsolateId,
            final String aSignalName, final Serializable aSignalData) {

        pTargetId = aTargetIsolateId;
        pSignalName = aSignalName;
        pSignalData = aSignalData;

        // Isolate ID specified
        pTargets = null;
    }

    /**
     * Retrieves the signal content
     * 
     * @return the signal content
     */
    public Serializable getSignalData() {

        return pSignalData;
    }

    /**
     * Retrieves the signal name
     * 
     * @return the signal name
     */
    public String getSignalName() {

        return pSignalName;
    }

    /**
     * Retrieves the target isolate ID, null if multiple targets are set.
     * 
     * @return the target isolate ID
     */
    public String getTargetId() {

        return pTargetId;
    }

    /**
     * Retrieves the signal targets, null if a specific target isolate ID is set
     * 
     * @return the signal targets
     */
    public ISignalBroadcaster.EEmitterTargets getTargets() {

        return pTargets;
    }
}
