/**
 * File:   ISignalBroadcaster.java
 * Author: Thomas Calmant
 * Date:   19 sept. 2011
 */
package org.psem2m.isolates.services.remote.signals;

import java.io.Serializable;

/**
 * Represents a signal emitter service
 * 
 * @author Thomas Calmant
 */
public interface ISignalBroadcaster {

    /**
     * Predefined signal targets
     * 
     * @author Thomas Calmant
     */
    enum EEmitterTargets {
        /** All isolates and monitors (except forker) */
        ALL,

        /** The forker (only value to access to it) */
        FORKER,

        /** All isolates (neither monitor nor forker) */
        ISOLATES,

        /** Local isolate only (doesn't use signal providers) */
        LOCAL,

        /** All monitors (not the forker) */
        MONITORS,
    }

    /**
     * Sends the given data to the given targets.
     * 
     * @param aTargets
     *            Signal targets
     * @param aSignalName
     *            Signal name (URI like string)
     * @param aData
     *            Signal content (can't be null)
     */
    void sendData(EEmitterTargets aTargets, String aSignalName,
            Serializable aData);

    /**
     * Sends the given data to the given isolate
     * 
     * @param aIsolateId
     *            The target isolate ID
     * @param aSignalName
     *            Signal name (URI like string)
     * @param aData
     *            Signal content (can't be null)
     * 
     * @return True if the isolate ID exists, else false
     */
    boolean sendData(String aIsolateId, String aSignalName, Serializable aData);
}
